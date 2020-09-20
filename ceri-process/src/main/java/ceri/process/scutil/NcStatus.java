package ceri.process.scutil;

import java.util.Objects;
import ceri.common.collection.Node;
import ceri.common.text.StringUtil;

public class NcStatus {
	public final NcServiceState state;
	public final Node<Void> data;

	public static NcStatus from(String output) {
		String[] split = StringUtil.NEWLINE_REGEX.split(output, 2);
		NcServiceState state = NcServiceState.from(split[0]);
		if (split.length == 1) return new NcStatus(state, Node.of());
		return of(state, Parser.parse(split[1]));
	}

	public static NcStatus of(NcServiceState state, Node<Void> data) {
		return new NcStatus(state, data);
	}

	private NcStatus(NcServiceState state, Node<Void> data) {
		this.state = state;
		this.data = data;
	}

	@Override
	public int hashCode() {
		return Objects.hash(state, data);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof NcStatus)) return false;
		NcStatus other = (NcStatus) obj;
		if (!Objects.equals(state, other.state)) return false;
		if (!Objects.equals(data, other.data)) return false;
		return true;
	}

	@Override
	public String toString() {
		return state + ": " + data;
	}

}
