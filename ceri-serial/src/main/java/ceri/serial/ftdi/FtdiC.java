package ceri.serial.ftdi;

import static ceri.common.validation.ValidationUtil.*;
import static ceri.serial.ftdi.FtdiChipType.*;
import static ceri.serial.ftdi.FtdiModuleDetachMode.*;
import static ceri.serial.jna.JnaUtil.ushort;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import ceri.serial.jna.JnaUtil;
import ceri.serial.libusb.LibUsbConfig;
import ceri.serial.libusb.LibUsbContext;
import ceri.serial.libusb.LibUsbDevice;
import ceri.serial.libusb.LibUsbDeviceHandle;
import ceri.serial.libusb.LibUsbDeviceList;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_interface;
import ceri.serial.libusb.jna.LibUsb.libusb_interface_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;
import ceri.serial.libusb.jna.LibUsb.timeval;
import ceri.serial.libusb.jna.LibUsbException;

public class FtdiC {
	private static final int VENDOR_ID = 0x0403;
	private static final Set<Integer> COMPATIBLE_PRODUCT_IDS =
		Set.of(0x6001, 0x6010, 0x6011, 0x6014, 0x6015);

	public int ftdi_error_return(FtdiContext ftdi, int code, String str) {
		if (ftdi != null) ftdi.errorStr = str;
		else System.err.println(str);
		return code;
	}

	public int ftdi_error_return_free_device_list(FtdiContext ftdi, int code, String str,
		libusb_device.ByReference devs) {
		LibUsb.libusb_free_device_list(devs, 1);
		ftdi.errorStr = str;
		return code;
	}

	public FtdiContext ftdi_new() throws LibUsbException {
		// (init FtdiContext - done in builder)
		FtdiContext ftdi = FtdiContext.init();
		ftdi.setInterface(FtdiInterface.INTERFACE_A);
		ftdi.eeprom = new FtdiEeprom();
		/* All fine. Now allocate the readbuffer */
		ftdi_read_data_set_chunksize(ftdi, 4096);
		return ftdi;
	}

	void ftdi_free(FtdiContext ftdi) {
		if (ftdi != null) ftdi.close();
	}

	public List<LibUsbDevice> ftdi_usb_find_all(FtdiContext ftdi, int vendorId, int productId)
		throws LibUsbException {
		List<LibUsbDevice> devices = new ArrayList<>();
		try (LibUsbDeviceList list = ftdi.usbCtx.deviceList()) {
			for (LibUsbDevice dev : list.devices) {
				libusb_device_descriptor desc = dev.descriptor();
				if (vendorId != 0 && vendorId != ushort(desc.idVendor)) continue;
				if (VENDOR_ID != ushort(desc.idVendor)) continue;
				if (productId != 0 && productId != ushort(desc.idVendor)) continue;
				if (!COMPATIBLE_PRODUCT_IDS.contains(ushort(desc.idProduct))) continue;
				devices.add(dev);
				dev.ref();
			}
		}
		return devices;
	}

	public void ftdi_list_free(List<LibUsbDevice> devices) {
		devices.forEach(LibUsbDevice::unref);
	}

	public void ftdi_usb_get_strings(FtdiContext ftdi, LibUsbDevice dev) throws LibUsbException {
		if (ftdi == null || dev == null) return;
		if (ftdi.usbDev != null) ftdi_usb_get_strings2(dev, ftdi.usbDev);
		else try (LibUsbDeviceHandle handle = dev.open()) {
			ftdi_usb_get_strings2(dev, handle);
		}
	}

	public void ftdi_usb_get_strings2(LibUsbDevice dev, LibUsbDeviceHandle handle)
		throws LibUsbException {
		libusb_device_descriptor desc = dev.descriptor();
		String manufacturer = handle.stringDescriptorAscii(desc.iManufacturer);
		String description = handle.stringDescriptorAscii(desc.iProduct);
		String serial = handle.stringDescriptorAscii(desc.iSerialNumber);
	}

	///**
	// * Internal function to determine the maximum packet size.
	// */
	public static int _ftdi_determine_max_packet_size(FtdiContext ftdi, LibUsbDevice dev)
	 throws LibUsbException {
	    int packet_size = 64;
	
	    // Sanity check
	    if (ftdi == null || dev == null) return packet_size;
	    if (ftdi.type.isHType()) packet_size = 512;
	
	    libusb_device_descriptor desc = dev.descriptor();
	    try (LibUsbConfig config0 = dev.configDescriptor()) {
		    if (desc.bNumConfigurations > 0) {
		        if (ftdi.iface.iface < config0.config.bNumInterfaces) {
		            libusb_interface iface = config0.config.interfaces()[ftdi.iface.iface];
		            if (iface.num_altsetting > 0) {
		                libusb_interface_descriptor descriptor = iface.altsettings()[0];
		                if (descriptor.bNumEndpoints > 0) {
		                    packet_size = descriptor.endpoints()[0].wMaxPacketSize;
		                }
		            }
		        }
		    }
	    }
	    return packet_size;
	}
	
	///**
	//    Opens a ftdi device given by an usbDevice.
	//
	//    \param ftdi pointer to ftdi_context
	//    \param dev libusb usbDev to use
	//*/
	public void ftdi_usb_open_dev(FtdiContext ftdi, LibUsbDevice dev) throws LibUsbException {
	    int cfg0, detach_errno = 0;
	
	    if (ftdi == null) throw new IllegalArgumentException("ftdi context invalid");
	    ftdi.usbDev = dev.open();
	    libusb_device_descriptor desc = dev.descriptor();
	    try (LibUsbConfig config0 = dev.configDescriptor()) {
	    	cfg0 = config0.config.bConfigurationValue;
	    }
	    if (ftdi.moduleDetachMode == AUTO_DETACH_SIO_MODULE)
	    	ftdi.usbDev.detachKernelDriver(ftdi.iface.iface);
	
	    int cfg = ftdi.usbDev.configuration();
	    // set configuration (needed especially for windows)
	    // tolerate EBUSY: one device with one configuration, but two interfaces
	    //    and libftdi sessions to both interfaces (e.g. FT2232)
	    if (desc.bNumConfigurations > 0 && cfg != cfg0)
	    	ftdi.usbDev.setConfiguration(cfg0);
	
	    ftdi.usbDev.claimInterface(ftdi.iface.iface);
	    //if (detach_errno == EPERM) -8, "inappropriate permissions on device!");
	    //else -5, "unable to claim usb device. Make sure the default FTDI driver is not in use");
	
	    ftdi_usb_reset(ftdi);
	    ftdi.type = FtdiChipType.guess(ushort(desc.bcdDevice), ushort(desc.iSerialNumber));
	    // Determine maximum packet size
	    ftdi.maxPacketSize = _ftdi_determine_max_packet_size(ftdi, dev);
	    FtdiBaudRate.set(ftdi, 9600);
	}
	
	int ftdi_usb_open(FtdiContext ftdi, int vendor, int product) {
	    return ftdi_usb_open_desc(ftdi, vendor, product, null, null);
	}
	
	int ftdi_usb_open_desc(FtdiContext ftdi, int vendor, int product,
	                       String description, String serial) {
	    return ftdi_usb_open_desc_index(ftdi,vendor,product,description,serial,0);
	}
	
	void ftdi_usb_open_desc_index(FtdiContext ftdi, int vendor, int product,
	                             String description, String serial, int index) {
	    validateNotNull(ftdi);
	    LibUsbDeviceList devs = ftdi.usbCtx.deviceList();
	    for (int i = 0; i < devs.devices.size(); i++) {
	    	LibUsbDevice dev = devs.devices.get(i);
	        libusb_device_descriptor desc = dev.descriptor();
	        int res;
	
	        if (desc.idVendor == vendor && desc.idProduct == product) {
	        	ftdi.usbDev = dev.open();
	
	        	// TODO: concat strings to length
	            if (description != null) {
	            	String s = ftdi.usbDev.stringDescriptorAscii(desc.iProduct);
	            	if (!description.equals(s)) continue;
	            }
	            if (serial != null) {
	            	String s = ftdi.usbDev.stringDescriptorAscii(desc.iSerialNumber);
	            	if (!serial.equals(s)) continue;
	            }
	            
	            ftdi.usbDev.close();
	            ftdi.usbDev = null;
	
	            if (index > 0) {
	                index--;
	                continue;
	            }
	
	            ftdi_usb_open_dev(ftdi, dev);
	            return;
	        }
	    }
	    // TODO: throw exception?
	    //ftdi_error_return_free_device_list(-3, "device not found", devs);
	}

	public static void ftdi_read_data_cb(libusb_transfer transfer) {
		FtdiTransferControl tc = new FtdiTransferControl(transfer.user_data);
		FtdiContext ftdi = tc.ftdi;
		int packet_size, actual_length, num_of_chunks, chunk_remains, i, ret;

		packet_size = ftdi.maxPacketSize;
		actual_length = transfer.actual_length;

	    if (actual_length > 2) {
	        // skip FTDI status bytes.
	        // Maybe stored in the future to enable modem use
	        num_of_chunks = actual_length / packet_size;
	        chunk_remains = actual_length % packet_size;
	        //printf("actual_length = %X, num_of_chunks = %X, chunk_remains = %X, readbuffer_offset = %X\n", actual_length, num_of_chunks, chunk_remains, ftdi.readbuffer_offset);
	
	        ftdi.readbuffer_offset += 2;
	        actual_length -= 2;
	
	        if (actual_length > packet_size - 2) {
	            for (i = 1; i < num_of_chunks; i++)
	                memmove (ftdi.readbuffer+ftdi.readbuffer_offset+(packet_size - 2)*i,
	                         ftdi.readbuffer+ftdi.readbuffer_offset+packet_size*i,
	                         packet_size - 2);
	            if (chunk_remains > 2) {
	                memmove (ftdi.readbuffer+ftdi.readbuffer_offset+(packet_size - 2)*i,
	                         ftdi.readbuffer+ftdi.readbuffer_offset+packet_size*i,
	                         chunk_remains-2);
	                actual_length -= 2*num_of_chunks;
	            }
	            else
	                actual_length -= 2*(num_of_chunks-1)+chunk_remains;
	        }
	
	        if (actual_length > 0) {
	            // data still fits in buf?
	            if (tc.offset + actual_length <= tc.size) {
	                memcpy (tc.buf + tc.offset, ftdi.readbuffer + ftdi.readbuffer_offset, actual_length);
	                //printf("buf[0] = %X, buf[1] = %X\n", buf[0], buf[1]);
	                tc.offset += actual_length;
	
	                ftdi.readbuffer_offset = 0;
	                ftdi.readbuffer_remaining = 0;
	
	                /* Did we read exactly the right amount of bytes? */
	                if (tc.offset == tc.size) {
	                    //printf("read_data exact rem %d offset %d\n",
	                    //ftdi.readbuffer_remaining, offset);
	                    tc.completed = 1;
	                    return;
	                }
	            }
	            else {
	                // only copy part of the data or size <= readbuffer_chunksize
	                int part_size = tc.size - tc.offset;
	                memcpy (tc.buf + tc.offset, ftdi.readbuffer + ftdi.readbuffer_offset, part_size);
	                tc.offset += part_size;
	
	                ftdi.readbuffer_offset += part_size;
	                ftdi.readbuffer_remaining = actual_length - part_size;
	
	                /* printf("Returning part: %d - size: %d - offset: %d - actual_length: %d - remaining: %d\n",
	                part_size, size, offset, actual_length, ftdi.readbuffer_remaining); */
	                tc.completed = 1;
	                return;
	            }
	        }
	    }
	
	    if (transfer.status == LIBUSB_TRANSFER_CANCELLED)
	        tc.completed = LIBUSB_TRANSFER_CANCELLED;
	    else {
	        ret = LibUsb.libusb_submit_transfer(transfer);
	        if (ret < 0)
	            tc.completed = 1;
	    }
	}


	String ftdi_get_error_string(FtdiContext ftdi) {
	    return ftdi == null ? "" : ftdi.errorStr;
	}
	
}
