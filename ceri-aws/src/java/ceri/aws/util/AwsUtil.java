package ceri.aws.util;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.glacier.TreeHashGenerator;

public class AwsUtil {

	private AwsUtil() {}

	/**
	 * Friendly output of AWS exception.
	 */
	public static void printError(AmazonClientException e, PrintStream p) {
		if (e instanceof AmazonServiceException) {
			AmazonServiceException es = (AmazonServiceException)e;
			p.println("Error response from Amazon: " + e.getMessage());
			p.println("HTTP Status Code: " + es.getStatusCode());
			p.println("AWS Error Code:   " + es.getErrorCode());
			p.println("Error Type:       " + es.getErrorType());
			p.println("Request ID:       " + es.getRequestId());
		} else {
			p.println("Error trying to reach Amazon: " + e.getMessage());
		}
	}

	public static String checksumOfChecksums(Collection<byte[]> checksums) {
		List<byte[]> castChecksums;
		if (checksums instanceof List<?>) castChecksums = (List<byte[]>)checksums;
		else {
			castChecksums = new ArrayList<>();
			castChecksums.addAll(checksums);
		}
		return TreeHashGenerator.calculateTreeHash(castChecksums);
	}
	
	public static String checksumOfData(byte[] data) {
		return TreeHashGenerator.calculateTreeHash(new ByteArrayInputStream(data));
	}

	public static String checksumOfData(byte[] data, int offset, int len) {
		return TreeHashGenerator.calculateTreeHash(new ByteArrayInputStream(data, offset, len));
	}

}
