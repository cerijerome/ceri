package ceri.serial.comm;

import java.util.Collection;
import java.util.Set;
import ceri.common.data.TypeTranscoder;
import ceri.serial.comm.jna.CSerial;

public enum FlowControl {
	rtsCtsIn(CSerial.FLOWCONTROL_RTSCTS_IN),
	rtsCtsOut(CSerial.FLOWCONTROL_RTSCTS_OUT),
	xonXoffIn(CSerial.FLOWCONTROL_XONXOFF_IN),
	xonXoffOut(CSerial.FLOWCONTROL_XONXOFF_OUT);

	private static final TypeTranscoder<FlowControl> xcoder =
		TypeTranscoder.of(t -> t.value, FlowControl.class);
	public static final Set<FlowControl> NONE = Set.of();
	public final int value;

	public static Set<FlowControl> allFrom(int value) {
		return xcoder.decodeAll(value);
	}

	public static int encode(Collection<FlowControl> flowControl) {
		return xcoder.encode(flowControl);
	}
	
	private FlowControl(int value) {
		this.value = value;
	}
}
