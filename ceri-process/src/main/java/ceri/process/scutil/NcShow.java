package ceri.process.scutil;

import ceri.common.text.StringUtil;
import ceri.process.scutil.parser.Group;

public class NcShow {
	public final NcListItem item;
	public final Group data;

	public static NcShow from(String output) {
		String[] split = StringUtil.NEWLINE_REGEX.split(output, 2);
		NcListItem item = NcListItem.from(split[0]);
		if (split.length == 1) return new NcShow(item, Group.NULL);
		Group data = Group.from(split[1]);
		return new NcShow(item, data);
	}

	public NcShow(NcListItem item, Group data) {
		this.item = item;
		this.data = data;
	}

}
