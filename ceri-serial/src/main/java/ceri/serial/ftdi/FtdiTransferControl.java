package ceri.serial.ftdi;

import java.util.List;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;

public class FtdiTransferControl extends Structure {
	private static final List<String> FIELDS = List.of( //
		"completed", "buf", "size", "offset", "ftdi", "transfer");

	public static class ByValue extends FtdiTransferControl //
		implements Structure.ByValue {}

	public static class ByReference extends FtdiTransferControl //
		implements Structure.ByReference {}

	public boolean completed;
	public Pointer buf;
	public int size;
	public int offset;
	public Pointer ftdi;
	public libusb_transfer.ByReference transfer;

	public FtdiTransferControl() {}

	public FtdiTransferControl(Pointer p) {
		super(p);
	}

	@Override
	protected List<String> getFieldOrder() {
		return FIELDS;
	}

}
