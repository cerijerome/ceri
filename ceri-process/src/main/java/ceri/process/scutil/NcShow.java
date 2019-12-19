package ceri.process.scutil;

import ceri.common.collection.Node;
import ceri.common.text.StringUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class NcShow {
	public final NcListItem item;
	public final Node<Void> data;

	public static NcShow from(String output) {
		String[] split = StringUtil.NEWLINE_REGEX.split(output, 2);
		NcListItem item = NcListItem.from(split[0]);
		if (split.length == 1) return new NcShow(item, Node.of());
		return of(item, Parser.parse(split[1]));
	}

	public static NcShow of(NcListItem item, Node<Void> data) {
		return new NcShow(item, data);
	}

	private NcShow(NcListItem item, Node<Void> data) {
		this.item = item;
		this.data = data;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(item, data);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof NcShow)) return false;
		NcShow other = (NcShow) obj;
		if (!EqualsUtil.equals(item, other.item)) return false;
		if (!EqualsUtil.equals(data, other.data)) return false;
		return true;
	}

	@Override
	public String toString() {
		return item + System.lineSeparator() + data;
	}

}
