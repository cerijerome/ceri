package ceri.aws.glacier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import ceri.aws.util.AwsUtil;
import ceri.aws.util.ByteRange;
import ceri.aws.util.StreamReconstructor;
import ceri.common.io.IoUtil;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadResult;
import com.amazonaws.services.glacier.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.glacier.model.InitiateMultipartUploadResult;
import com.amazonaws.services.glacier.model.UploadMultipartPartRequest;
import com.amazonaws.services.glacier.model.UploadMultipartPartResult;
import com.amazonaws.util.BinaryUtils;

public class GlacierFiler extends GlacierAdapter {
	private final File file;
	private final String uploadId;
	private final String archiveId;
	private final int maxNonSequentialParts;
	private final Map<Long, byte[]> checksums = new TreeMap<>();
	private String vaultName = null;
	private long partSize = 0;
	private StreamReconstructor reconstructor = null;

	public GlacierFiler(File file, String uploadId, String archiveId, int maxNonSequentialParts) {
		this.file = file;
		this.uploadId = uploadId;
		this.archiveId = archiveId;
		this.maxNonSequentialParts = maxNonSequentialParts;
	}

	@Override
	public InitiateMultipartUploadResult initiateMultipartUpload(
		InitiateMultipartUploadRequest request) {
		if (reconstructor != null) throw new AmazonClientException("Upload already initiated");

		partSize = Long.valueOf(request.getPartSize());
		vaultName = request.getVaultName();
		reconstructor = createReconstructor(file, maxNonSequentialParts);
		
		InitiateMultipartUploadResult result = new InitiateMultipartUploadResult();
		result.setUploadId(uploadId);
		return result;
	}

	@Override
	public UploadMultipartPartResult uploadMultipartPart(UploadMultipartPartRequest request) {
		if (!vaultName.equals(request.getVaultName())) throw new AmazonClientException(
			"Expected vault name " + vaultName + ": " + request.getVaultName());
		if (!uploadId.equals(request.getUploadId())) throw new AmazonClientException(
			"Expected upload id " + uploadId + ": " + request.getUploadId());

		ByteRange range = ByteRange.fromString(request.getRange());
		long position = range.start / partSize;
		if (position * partSize != range.start) throw new AmazonClientException(
			"Range does not start on a multiple of " + partSize + ": " + range.start);
		
		String requestChecksum = request.getChecksum();
		byte[] data = getData(request.getBody());
		String checksum = AwsUtil.checksumOfData(data);
		if (!checksum.equals(requestChecksum)) throw new AmazonClientException(
			"Expected checksum " + requestChecksum + ": " + checksum);
		checksums.put(position, BinaryUtils.fromHex(checksum));
		writeData(position, data);
		
		UploadMultipartPartResult result = new UploadMultipartPartResult();
		result.setChecksum(checksum);
		return result;
	}

	@Override
	public CompleteMultipartUploadResult completeMultipartUpload(
		CompleteMultipartUploadRequest request) {
		if (reconstructor == null) throw new AmazonClientException("Upload has not started");
		if (reconstructor.closed()) throw new AmazonClientException("Upload already completed");
		if (!vaultName.equals(request.getVaultName())) throw new AmazonClientException(
			"Expected vault name " + vaultName + ": " + request.getVaultName());
		if (!uploadId.equals(request.getUploadId())) throw new AmazonClientException(
			"Expected upload id " + uploadId + ": " + request.getUploadId());

		closeReconstructor(reconstructor);
		long size = Long.valueOf(request.getArchiveSize());
		if (reconstructor.bytesWritten() != size) throw new AmazonClientException(
			"Expected archive size of " + size + ": " + reconstructor.bytesWritten());
		String requestTotalChecksum = request.getChecksum();
		String totalChecksum = AwsUtil.checksumOfChecksums(checksums.values());
		if (!totalChecksum.equals(requestTotalChecksum)) throw new AmazonClientException(
			"Expected checksum " + requestTotalChecksum + ": " + totalChecksum);
		
		CompleteMultipartUploadResult result = new CompleteMultipartUploadResult();
		result.setArchiveId(archiveId);
		result.setChecksum(totalChecksum);
		return result;
	}

	private void writeData(long position, byte[] data) {
		try {
			reconstructor.write(position, data);
		} catch (IOException e) {
			throw new AmazonClientException("Failed due to nested exception", e);
		}
	}
	
	private byte[] getData(InputStream in) {
		try {
			return IoUtil.getContent(in, 0);
		} catch (IOException e) {
			throw new AmazonClientException("Failed due to nested exception", e);
		}
	}

	private void closeReconstructor(StreamReconstructor reconstructor) {
		try {
			reconstructor.close();
		} catch (IOException e) {
			throw new AmazonClientException("Failed due to nested exception", e);
		}
	}

	private StreamReconstructor createReconstructor(File file, int maxNonSequentialParts) {
		try {
			return StreamReconstructor.forFile(file, maxNonSequentialParts);
		} catch (IOException e) {
			throw new AmazonClientException("Failed due to nested exception", e);
		}
	}

}
