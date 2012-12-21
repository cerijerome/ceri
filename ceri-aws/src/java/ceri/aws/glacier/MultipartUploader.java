package ceri.aws.glacier;

import static ceri.common.test.Debugger.DBG;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import ceri.aws.util.AwsUtil;
import ceri.aws.util.ByteRange;
import ceri.common.io.IoUtil;
import com.amazonaws.services.glacier.AmazonGlacier;
import com.amazonaws.services.glacier.TreeHashGenerator;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadResult;
import com.amazonaws.services.glacier.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.glacier.model.InitiateMultipartUploadResult;
import com.amazonaws.services.glacier.model.UploadMultipartPartRequest;
import com.amazonaws.services.glacier.model.UploadMultipartPartResult;
import com.amazonaws.util.BinaryUtils;

/**
 * Uploads data from an input stream to a new archive in a Glacier vault.
 * Multipart allows for data up to 1TB (1000 parts with 1GB part size).
 */
public class MultipartUploader {
	private static final int MB = 1024 * 1024;
	private static final int GB = 1024 * MB;
	private static final int PART_SIZE_DEF = MB; // 1MB default
	private final AmazonGlacier glacier;
	private final String vaultName;
	public final int partSize;
	private final String archiveDescription;
	private final InputStream in;

	public static class Builder {
		final AmazonGlacier glacier;
		final InputStream in;
		final String vaultName;
		int partSize = 0;
		String archiveDescription = null;

		Builder(AmazonGlacier glacier, InputStream in, String vaultName) {
			this.glacier = glacier;
			this.in = in;
			this.vaultName = vaultName;
		}

		public Builder archiveDescription(String archiveDescription) {
			this.archiveDescription = archiveDescription;
			return this;
		}

		public Builder partSize(int partSize) {
			if (partSize > GB) throw new IllegalArgumentException(
				"Part size cannot be larger than 1GB: " + partSize);
			this.partSize = partSize;
			return this;
		}

		public MultipartUploader build() {
			return new MultipartUploader(this);
		}

	}

	MultipartUploader(Builder builder) {
		glacier = builder.glacier;
		in = builder.in;
		vaultName = builder.vaultName;
		partSize = builder.partSize == 0 ? PART_SIZE_DEF : builder.partSize;
		archiveDescription = builder.archiveDescription;
	}

	/**
	 * Creates the builder with required fields.
	 */
	public static Builder builder(AmazonGlacier glacier, InputStream in, String vaultName) {
		return new Builder(glacier, in, vaultName);
	}

	/**
	 * Uploads content from the input stream and returns the archive id.
	 */
	public String execute() throws IOException {
		String uploadId = init();
		List<byte[]> checksums = new LinkedList<>();
		long size = upload(uploadId, checksums);
		String archiveId = complete(uploadId, checksums, size);
		return archiveId;
	}

	private String init() {
		InitiateMultipartUploadRequest request =
			new InitiateMultipartUploadRequest().withVaultName(vaultName).withPartSize(
				String.valueOf(partSize));
		if (archiveDescription != null) request.setArchiveDescription(archiveDescription);
		InitiateMultipartUploadResult result = glacier.initiateMultipartUpload(request);
		return result.getUploadId();
	}

	private long upload(String uploadId, List<byte[]> checksums) throws IOException {
		long position = 0;
		while (true) {
			byte[] buffer = new byte[partSize];
			int count = IoUtil.fillBuffer(in, buffer);
			if (count == 0) break;
			UploadMultipartPartResult result = uploadPart(uploadId, buffer, 0, count, position);
			checksums.add(BinaryUtils.fromHex(result.getChecksum()));
			position += count;
		}
		return position;
	}

	private UploadMultipartPartResult uploadPart(String uploadId, byte[] data, int offset, int len,
		long position) {
		DBG.log(uploadId, data, offset, len, position);
		String range = ByteRange.asString(position, len);
		String checksum = AwsUtil.checksumOfData(data, offset, len);
		UploadMultipartPartRequest request =
			new UploadMultipartPartRequest().withVaultName(vaultName).withBody(
				new ByteArrayInputStream(data, offset, len)).withChecksum(checksum)
				.withRange(range).withUploadId(uploadId);
		return glacier.uploadMultipartPart(request);
	}

	private String complete(String uploadId, List<byte[]> checksums, long size) {
		String checksum = TreeHashGenerator.calculateTreeHash(checksums);
		CompleteMultipartUploadRequest request =
			new CompleteMultipartUploadRequest().withVaultName(vaultName).withUploadId(uploadId)
				.withChecksum(checksum).withArchiveSize(String.valueOf(size));
		CompleteMultipartUploadResult result = glacier.completeMultipartUpload(request);
		return result.getArchiveId();
	}

}
