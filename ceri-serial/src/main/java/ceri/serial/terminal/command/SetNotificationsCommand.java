package ceri.serial.terminal.command;

import static ceri.common.validation.ValidationUtil.validateEqual;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import ceri.common.collection.ImmutableUtil;
import ceri.common.data.DataDecoder;
import ceri.common.data.DataEncoder;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class SetNotificationsCommand extends Command {
	static final int SIZE = CommandType.BYTES + Integer.BYTES + Integer.BYTES;
	public final Set<NotificationType> on;
	public final Set<NotificationType> off;

	public static SetNotificationsCommand decode(DataDecoder decoder) {
		Commands.validateType(decoder, CommandType.setNotifications);
		return decodeBody(decoder);
	}

	static SetNotificationsCommand decodeBody(DataDecoder decoder) {
		validateEqual(decoder.total(), SIZE);
		int on = decoder.decodeIntMsb();
		int off = decoder.decodeIntMsb();
		return builder().on(on).off(off).build();
	}

	public static SetNotificationsCommand on(NotificationType... notifications) {
		return builder().on(notifications).build();
	}

	public static SetNotificationsCommand off(NotificationType... notifications) {
		return builder().off(notifications).build();
	}

	public static class Builder {
		final Collection<NotificationType> on = new LinkedHashSet<>();
		final Collection<NotificationType> off = new LinkedHashSet<>();

		Builder() {}

		public Builder on(int onValue) {
			return on(NotificationType.xcoder.decode(onValue));
		}

		public Builder on(NotificationType... notifications) {
			return on(Arrays.asList(notifications));
		}

		public Builder on(Collection<NotificationType> notifications) {
			this.on.addAll(notifications);
			return this;
		}

		public Builder off(int offValue) {
			return off(NotificationType.xcoder.decode(offValue));
		}

		public Builder off(NotificationType... notifications) {
			return off(Arrays.asList(notifications));
		}

		public Builder off(Collection<NotificationType> notifications) {
			this.off.addAll(notifications);
			return this;
		}

		public SetNotificationsCommand build() {
			return new SetNotificationsCommand(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	SetNotificationsCommand(Builder builder) {
		super(CommandType.setNotifications);
		on = ImmutableUtil.copyAsSet(builder.on);
		off = ImmutableUtil.copyAsSet(builder.off);
	}

	public int onValue() {
		return NotificationType.xcoder.encode(on);
	}

	public int offValue() {
		return NotificationType.xcoder.encode(off);
	}

	@Override
	public void encode(DataEncoder encoder) {
		super.encode(encoder);
		encoder.encodeIntMsb(onValue());
		encoder.encodeIntMsb(offValue());
	}

	@Override
	public int size() {
		return SIZE;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(on, off);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SetNotificationsCommand)) return false;
		SetNotificationsCommand other = (SetNotificationsCommand) obj;
		if (!EqualsUtil.equals(on, other.on)) return false;
		if (!EqualsUtil.equals(off, other.off)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, on, off).toString();
	}

}
