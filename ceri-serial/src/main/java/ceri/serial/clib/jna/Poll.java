package ceri.serial.clib.jna;

import java.util.List;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.data.FieldTranscoder;
import ceri.common.data.IntAccessor;
import ceri.common.data.TypeTranscoder;
import ceri.serial.jna.Struct;

public class Poll {

	private Poll() {}

	public enum poll_event {
		POLLIN(0x0001),
		POLLPRI(0x0002),
		POLLOUT(0x0004),
		POLLERR(0x0008),
		POLLHUP(0x0010),
		POLLNVAL(0x0020);

		public static final TypeTranscoder<poll_event> xcoder =
			TypeTranscoder.of(t -> t.value, poll_event.class);
		public final int value;

		poll_event(int value) {
			this.value = value;
		}
	}

	/**
	 * File descriptor for polling
	 */
	public static class pollfd extends Struct {
		private static final List<String> FIELDS = List.of("fd", "events");
		private static final IntAccessor.Typed<pollfd> eventsAccessor =
			IntAccessor.typedUshort(t -> t.events, (t, s) -> t.events = s);
		private static final IntAccessor.Typed<pollfd> reventsAccessor =
			IntAccessor.typedUshort(t -> t.revents, (t, s) -> t.revents = s);

		public static class ByValue extends pollfd //
			implements Structure.ByValue {}

		public static class ByReference extends pollfd //
			implements Structure.ByReference {}

		public int fd;
		public short events;
		public short revents;

		public pollfd() {}

		public pollfd(Pointer p) {
			super(p);
		}

		public FieldTranscoder<poll_event> events() {
			return poll_event.xcoder.field(eventsAccessor.from(this));
		}

		public FieldTranscoder<poll_event> revents() {
			return poll_event.xcoder.field(reventsAccessor.from(this));
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

}
