package ceri.aws.glacier;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.amazonaws.services.glacier.AmazonGlacier;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadResult;
import com.amazonaws.services.glacier.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.glacier.model.InitiateMultipartUploadResult;
import com.amazonaws.services.glacier.model.UploadMultipartPartRequest;
import com.amazonaws.services.glacier.model.UploadMultipartPartResult;

public class GlacierMockHelper {
	@Mock public AmazonGlacier glacier;
	@Mock public InitiateMultipartUploadResult initResult;
	@Captor public ArgumentCaptor<InitiateMultipartUploadRequest> initCaptor;
	@Mock public UploadMultipartPartResult uploadResult;
	@Captor public ArgumentCaptor<UploadMultipartPartRequest> uploadCaptor;
	@Mock public CompleteMultipartUploadResult completeResult;
	@Captor public ArgumentCaptor<CompleteMultipartUploadRequest> completeCaptor;

	public GlacierMockHelper() {
		MockitoAnnotations.initMocks(this);
		when(glacier.initiateMultipartUpload(any(InitiateMultipartUploadRequest.class))).thenReturn(
			initResult);
		when(glacier.uploadMultipartPart(any(UploadMultipartPartRequest.class))).thenReturn(
			uploadResult);
		when(glacier.completeMultipartUpload(any(CompleteMultipartUploadRequest.class))).thenReturn(
			completeResult);
		when(uploadResult.getChecksum()).thenReturn("00");
	}
}
