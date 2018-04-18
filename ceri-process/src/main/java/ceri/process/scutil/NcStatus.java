package ceri.process.scutil;

import ceri.common.text.StringUtil;
import ceri.process.scutil.parser.Group;

public class NcStatus {
	public final NcServiceState state;
	public final Group data;

	public static NcStatus from(String output) {
		String[] split = StringUtil.NEWLINE_REGEX.split(output, 2);
		NcServiceState state = NcServiceState.from(split[0]);
		if (split.length == 1) return new NcStatus(state, Group.NULL);
		Group data = Group.from(split[1]);
		return new NcStatus(state, data);
	}

	public NcStatus(NcServiceState state, Group data) {
		this.state = state;
		this.data = data;
	}

}
