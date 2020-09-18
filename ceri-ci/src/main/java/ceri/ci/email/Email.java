package ceri.ci.email;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.TreeSet;
import javax.mail.Message;
import javax.mail.MessagingException;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * Keeps track of the main fields of an email.
 */
public class Email {
	public final Collection<String> recipients;
	public final String from;
	public final long sentDateMs;
	public final String subject;
	public final String content;

	public static class Builder {
		final Collection<String> recipients = new HashSet<>();
		String from = null;
		long sentDateMs = 0;
		String subject = null;
		String content = null;

		Builder() {}

		public Builder recipients(String... recipients) {
			return recipients(Arrays.asList(recipients));
		}

		public Builder recipients(Collection<String> recipients) {
			this.recipients.addAll(recipients);
			return this;
		}

		public Builder from(String from) {
			this.from = from;
			return this;
		}

		public Builder sentDateMs(long sentDateMs) {
			this.sentDateMs = sentDateMs;
			return this;
		}

		public Builder subject(String subject) {
			this.subject = subject;
			return this;
		}

		public Builder content(String content) {
			this.content = content;
			return this;
		}

		public Email build() {
			return new Email(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	Email(Builder builder) {
		recipients = Collections.unmodifiableSet(new TreeSet<>(builder.recipients));
		from = builder.from;
		sentDateMs = builder.sentDateMs;
		subject = builder.subject;
		content = builder.content;
	}

	public static Email createFrom(Message message) throws MessagingException, IOException {
		Builder b = builder();
		b.recipients(EmailUtil.addressesFrom(message.getAllRecipients()));
		b.from(EmailUtil.firstAddress(message.getFrom()));
		b.subject(message.getSubject());
		b.sentDateMs(message.getSentDate().getTime());
		b.content(EmailUtil.content(message));
		return b.build();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Email)) return false;
		Email other = (Email) obj;
		if (!EqualsUtil.equals(other.recipients, recipients)) return false;
		if (!EqualsUtil.equals(other.from, from)) return false;
		if (other.sentDateMs != sentDateMs) return false;
		if (!EqualsUtil.equals(other.subject, subject)) return false;
		return EqualsUtil.equals(other.content, content);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(recipients, from, sentDateMs, subject, content);
	}

	@Override
	public String toString() {
		return ToStringHelper
			.createByClass(this, recipients, from, sentDateMs, new Date(sentDateMs), subject)
			.toString();
	}

}
