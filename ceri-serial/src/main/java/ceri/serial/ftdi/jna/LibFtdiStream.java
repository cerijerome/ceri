package ceri.serial.ftdi.jna;

import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_IO;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.*;
import java.util.List;
import java.util.function.IntConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.data.FieldTranscoder;
import ceri.common.data.IntAccessor;
import ceri.common.function.ExceptionRunnable;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_progress_info;
import ceri.serial.jna.Struct;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_error;
import ceri.serial.libusb.jna.LibUsb.libusb_iso_packet_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_status;
import ceri.serial.libusb.jna.LibUsb.timeval;
import ceri.serial.libusb.jna.LibUsbException;

public class LibFtdiStream {
	private static final Logger logger = LogManager.getLogger();

	// typedef int (FTDIStreamCallback)(uint8_t *buffer, int length, FTDIProgressInfo *progress,
	// void *userdata);
	public static interface ftdi_stream_callback extends Callback {
		public int invoke(Pointer buffer, int length, ftdi_progress_info progress, Pointer userdata);
	}

	static class FTDIStreamState extends Struct {
		private static final List<String> FIELDS = List.of( //
			"callback", "userdata", "packetsize", "activity", "result", "progress");
		private static final IntAccessor.Typed<FTDIStreamState> resultAccessor =
			IntAccessor.typed(t -> t.result, (t, i) -> t.result = i);

		public static class ByValue extends FTDIStreamState //
			implements Structure.ByValue {}

		public static class ByReference extends FTDIStreamState //
			implements Structure.ByReference {}

		public static FTDIStreamState of(ftdi_stream_callback callback, Pointer userdata,
            int packetsize, int activity) {
			FTDIStreamState state = new FTDIStreamState();
			state.callback = callback;
			state.userdata = userdata;
			state.packetsize = packetsize;
			state.activity = activity;
			state.progress = new ftdi_progress_info();
			return state;
		}
		
		public ftdi_stream_callback callback;
		public Pointer userdata;
		public int packetsize;
		public int activity;
		public int result;
		public ftdi_progress_info progress;

		public FTDIStreamState() {}

		public FTDIStreamState(Pointer p) {
			super(p);
		}

		public FieldTranscoder.Single<libusb_error> result() {
			return libusb_error.xcoder.field(resultAccessor.from(this));
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}

	}

	private static final int READ_STATUS_BYTES = 2;

	/*
	 * Handle callbacks With Exit request, free memory and release the transfer state->result is
	 * only set when some error happens
	 */

	public static void ftdi_readstream_cb(libusb_transfer transfer) {
		FTDIStreamState state = new FTDIStreamState(transfer.user_data);
		int packet_size = state.packetsize;
		state.activity++;

		if (transfer.status().get() != LIBUSB_TRANSFER_COMPLETED) {
			logger.warn("Unknown status: {} ({})", transfer.status().get(), transfer.status);
			state.result().set(LIBUSB_ERROR_IO);
			return;
		}

		try {
			Pointer ptr = transfer.buffer;
			int length = transfer.actual_length;
			int numPackets = (length + packet_size - 1) / packet_size;

			for (int i = 0; i < numPackets; i++) {
				int packetLen = Math.min(length, packet_size);
				int payloadLen = packetLen - READ_STATUS_BYTES;
				state.progress.current.totalBytes += payloadLen;
				LibUsbException.verify(state.callback.invoke(ptr.share(READ_STATUS_BYTES),
					payloadLen, null, state.userdata), "callback");
				ptr = ptr.share(packetLen);
				length -= packetLen;
			}
			transfer.status = -1;
			state.result = 0;
			LibUsb.libusb_submit_transfer(transfer);
		} catch (LibUsbException e) {
			logger.catching(e);
			LibUsb.libusb_free_transfer(transfer);
		}
	}

	private static double TimevalDiff(timeval a, timeval b) {
	    return a.minus(b);
	}
	
	/**
    Streaming reading of data from the device

    Use asynchronous transfers in libusb-1.0 for high-performance
    streaming of data from a device interface back to the PC. This
    function continuously transfers data until either an error occurs
    or the callback returns a nonzero value. This function returns
    a libusb error code or the callback's return value.

    For every contiguous block of received data, the callback will
    be invoked.
	 */
	public static int ftdi_readstream(ftdi_context ftdi, FTDIStreamCallback callback, Pointer userdata,
	    int packetsPerTransfer, int numTransfers) throws FtdiException {
	    libusb_transfer.ByReference transfers;
	    FTDIStreamState state = FTDIStreamState.of(callback, userdata, ftdi.max_packet_size, 1);
	    int bufferSize = packetsPerTransfer * ftdi.max_packet_size;
	    int xferIndex;
	    int err = 0;
	
	    /* Only FT2232H and FT232H know about the synchronous FIFO Mode*/
	    if (!ftdi.type().get().isSyncFifoType()) throw new FtdiException(1,
	    	"Device does not support synchronous FIFO mode: " + ftdi.type().get());
	
	    /* We don't know in what state we are, switch to reset*/
	    if (ftdi_set_bitmode(ftdi,  0xff, BITMODE_RESET) < 0)
	    {
	        fprintf(stderr,"Can't reset mode\n");
	        return 1;
	    }
	
	    /* Purge anything remaining in the buffers*/
	    if (ftdi_usb_purge_buffers(ftdi) < 0)
	    {
	        fprintf(stderr,"Can't Purge\n");
	        return 1;
	    }
	
	    /*
	     * Set up all transfers
	     */
	
	    transfers = calloc(numTransfers, sizeof *transfers);
	    if (!transfers)
	    {
	        err = LIBUSB_ERROR_NO_MEM;
	        goto cleanup;
	    }
	
	    for (xferIndex = 0; xferIndex < numTransfers; xferIndex++)
	    {
	        struct libusb_transfer *transfer;
	
	        transfer = libusb_alloc_transfer(0);
	        transfers[xferIndex] = transfer;
	        if (!transfer)
	        {
	            err = LIBUSB_ERROR_NO_MEM;
	            goto cleanup;
	        }
	
	        libusb_fill_bulk_transfer(transfer, ftdi->usb_dev, ftdi->out_ep,
	                                  malloc(bufferSize), bufferSize,
	                                  ftdi_readstream_cb,
	                                  &state, 0);
	
	        if (!transfer->buffer)
	        {
	            err = LIBUSB_ERROR_NO_MEM;
	            goto cleanup;
	        }
	
	        transfer->status = -1;
	        err = libusb_submit_transfer(transfer);
	        if (err)
	            goto cleanup;
	    }
	
	    /* Start the transfers only when everything has been set up.
	     * Otherwise the transfers start stuttering and the PC not
	     * fetching data for several to several ten milliseconds
	     * and we skip blocks
	     */
	    if (ftdi_set_bitmode(ftdi,  0xff, BITMODE_SYNCFF) < 0)
	    {
	        fprintf(stderr,"Can't set synchronous fifo mode: %s\n",
	                ftdi_get_error_string(ftdi));
	        goto cleanup;
	    }
	
	    /*
	     * Run the transfers, and periodically assess progress.
	     */
	
	    gettimeofday(&state.progress.first.time, NULL);
	
	    do
	    {
	        FTDIProgressInfo  *progress = &state.progress;
	        const double progressInterval = 1.0;
	        struct timeval timeout = { 0, ftdi->usb_read_timeout * 1000};
	        struct timeval now;
	
	        int err = libusb_handle_events_timeout(ftdi->usb_ctx, &timeout);
	        if (err ==  LIBUSB_ERROR_INTERRUPTED)
	            /* restart interrupted events */
	            err = libusb_handle_events_timeout(ftdi->usb_ctx, &timeout);
	        if (!state.result)
	        {
	            state.result = err;
	        }
	        if (state.activity == 0)
	            state.result = 1;
	        else
	            state.activity = 0;
	
	        // If enough time has elapsed, update the progress
	        gettimeofday(&now, NULL);
	        if (TimevalDiff(&now, &progress->current.time) >= progressInterval)
	        {
	            progress->current.time = now;
	            progress->totalTime = TimevalDiff(&progress->current.time,
	                                              &progress->first.time);
	
	            if (progress->prev.totalBytes)
	            {
	                // We have enough information to calculate rates
	
	                double currentTime;
	
	                currentTime = TimevalDiff(&progress->current.time,
	                                          &progress->prev.time);
	
	                progress->totalRate =
	                    progress->current.totalBytes /progress->totalTime;
	                progress->currentRate =
	                    (progress->current.totalBytes -
	                     progress->prev.totalBytes) / currentTime;
	            }
	
	            state.callback(NULL, 0, progress, state.userdata);
	            progress->prev = progress->current;
	
	        }
	    } while (!state.result);
	
	    /*
	     * Cancel any outstanding transfers, and free memory.
	     */
	
	cleanup:
	    fprintf(stderr, "cleanup\n");
	    if (transfers)
	        free(transfers);
	    if (err)
	        return err;
	    else
	        return state.result;
	}
}
