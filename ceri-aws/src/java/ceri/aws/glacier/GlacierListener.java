package ceri.aws.glacier;

import ceri.common.test.Debugger;
import com.amazonaws.services.glacier.AmazonGlacier;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadResult;
import com.amazonaws.services.glacier.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.glacier.model.InitiateMultipartUploadResult;
import com.amazonaws.services.glacier.model.UploadArchiveRequest;
import com.amazonaws.services.glacier.model.UploadArchiveResult;
import com.amazonaws.services.glacier.model.UploadMultipartPartRequest;
import com.amazonaws.services.glacier.model.UploadMultipartPartResult;

public class GlacierListener extends GlacierDelegator {
	private static final Debugger DBG = new Debugger();

	public GlacierListener(AmazonGlacier glacier) {
		super(glacier);
	}

	@Override
	public InitiateMultipartUploadResult initiateMultipartUpload(
		InitiateMultipartUploadRequest initiateMultipartUploadRequest) {
		DBG.method();
		return super.initiateMultipartUpload(initiateMultipartUploadRequest);
	}

	@Override
	public UploadArchiveResult uploadArchive(UploadArchiveRequest uploadArchiveRequest) {
		DBG.method();
		return super.uploadArchive(uploadArchiveRequest);
	}

	@Override
	public CompleteMultipartUploadResult completeMultipartUpload(
		CompleteMultipartUploadRequest completeMultipartUploadRequest) {
		DBG.method();
		return super.completeMultipartUpload(completeMultipartUploadRequest);
	}

	@Override
	public UploadMultipartPartResult uploadMultipartPart(
		UploadMultipartPartRequest uploadMultipartPartRequest) {
		DBG.method();
		return super.uploadMultipartPart(uploadMultipartPartRequest);
	}

}
