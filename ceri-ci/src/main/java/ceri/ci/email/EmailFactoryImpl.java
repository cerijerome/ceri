package ceri.ci.email;

import ceri.ci.build.BuildEventProcessor;

public class EmailFactoryImpl implements EmailFactory {

	@Override
	public EmailService.Builder serviceBuilder(EmailRetriever retriever,
		BuildEventProcessor processor) {
		return EmailService.builder(retriever, processor);
	}

	@Override
	public EmailRetrieverImpl.Builder
		retrieverBuilder(String host, String account, String password) {
		return EmailRetrieverImpl.builder(host, account, password);
	}

}
