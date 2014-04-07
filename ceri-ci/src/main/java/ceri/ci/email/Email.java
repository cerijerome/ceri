package ceri.ci.email;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.TreeSet;
import javax.mail.Message;
import javax.mail.MessagingException;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.common.util.ToStringHelper;

/**
 * Keeps track of the main
 */
public class Email {
	public final Collection<String> recipients;
	public final String from;
	public final long sentDate;
	public final String subject;
	public final String content;
	private final int hashCode;

	public static class Builder {
		Collection<String> recipients = Collections.emptySet();
		String from = null;
		long sentDate = 0;
		String subject = null;
		String content = null;

		Builder() {}

		public Builder recipients(Collection<String> recipients) {
			this.recipients = recipients;
			return this;
		}

		public Builder from(String from) {
			this.from = from;
			return this;
		}

		public Builder sentDate(long sentDate) {
			this.sentDate = sentDate;
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
		recipients = Collections.unmodifiableCollection(new TreeSet<>(builder.recipients));
		from = builder.from;
		sentDate = builder.sentDate;
		subject = builder.subject;
		content = builder.content;
		hashCode = HashCoder.hash(recipients, from, sentDate, subject, content);
	}

	public static Email createFrom(Message message) throws MessagingException, IOException {
		Builder b = builder();
		b.recipients(EmailUtil.addressesFrom(message.getAllRecipients()));
		b.from(EmailUtil.from(message.getFrom()));
		b.subject(message.getSubject());
		b.sentDate(message.getSentDate().getTime());
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
		if (other.sentDate != sentDate) return false;
		if (!EqualsUtil.equals(other.subject, subject)) return false;
		return !EqualsUtil.equals(other.content, content);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, recipients, from, new Date(sentDate), subject)
			.toString();
	}

}
