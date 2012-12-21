package ceri.aws.util;

import static org.junit.Assert.assertTrue;
import java.io.PrintStream;
import org.junit.Test;
import ceri.common.util.StringUtil;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonServiceException.ErrorType;

public class AwsUtilTest {

	@Test
	public void testPrintErrorClient() {
		AmazonClientException e = new AmazonClientException("client", new Exception("exception"));
		StringBuilder b = new StringBuilder();
		PrintStream p = StringUtil.asPrintStream(b);
		AwsUtil.printError(e, p);
		String s = b.toString().trim();
		assertTrue(s.contains("client"));
	}

	@Test
	public void testPrintErrorService() {
		AmazonServiceException es =
			new AmazonServiceException("service", new Exception("exception"));
		es.setErrorCode("errorCode");
		es.setErrorType(ErrorType.Service);
		es.setRequestId("requestId");
		es.setServiceName("serviceName");
		es.setStatusCode(100);
		AmazonClientException e = es;

		StringBuilder b = new StringBuilder();
		PrintStream p = StringUtil.asPrintStream(b);
		AwsUtil.printError(e, p);
		String s = b.toString().trim();

		assertTrue(s.contains("service"));
		assertTrue(s.contains("errorCode"));
		assertTrue(s.contains("Service"));
		assertTrue(s.contains("requestId"));
		assertTrue(s.contains("100"));
	}

}
