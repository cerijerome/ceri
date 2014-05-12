package ceri.ci.email;

import ceri.ci.build.BuildEventProcessor;

public interface EmailFactory {

	EmailService.Builder serviceBuilder(EmailRetriever retriever, BuildEventProcessor processor);
	EmailRetrieverImpl.Builder retrieverBuilder(String host, String account, String password);

}
