package ceri.serial.ftdi;

import ceri.common.data.TypeTranscoder;

public enum FtdiModemStatus {
	CTS(0x0010, "Clear to send"),
	DTS(0x0020, "Data set ready"),
	RI(0x0040, "Ring indicator"),
	RLSD(0x0080, "Receive line signal detect"),
	DR(0x0100, "Data ready"),
	OE(0x0200, "Overrun error"),
	PE(0x0400, "Parity error"),
	FE(0x0800, "Framing error"),
	BI(0x1000, "Break interrupt"),
	THRE(0x2000, "Transmitter holding register"),
	TEMT(0x4000, "Transmitter empty"),
	ERF(0x4000, "Error in RCVR FIFO");
	
	public static final TypeTranscoder.Flag<FtdiModemStatus> xcoder =
		TypeTranscoder.flag(t -> t.value, FtdiModemStatus.class);
	public final int value;
	public final String description;
	
	private FtdiModemStatus(int value, String description) {
		this.value = value;
		this.description = description;
	}

}
