package ceri.serial.comm;

import java.util.Set;
import ceri.common.data.TypeTranscoder;
import ceri.serial.comm.jna.CSerial;

public enum FlowControl {
	rtsCtsIn(CSerial.FLOWCONTROL_RTSCTS_IN),
	rtsCtsOut(CSerial.FLOWCONTROL_RTSCTS_OUT),
	xonXoffIn(CSerial.FLOWCONTROL_XONXOFF_IN),
	xonXoffOut(CSerial.FLOWCONTROL_XONXOFF_OUT);

	public static final TypeTranscoder<FlowControl> xcoder =
		TypeTranscoder.of(t -> t.value, FlowControl.class);
	public static final Set<FlowControl> NONE = Set.of();
	public final int value;

	private FlowControl(int value) {
		this.value = value;
	}
}
