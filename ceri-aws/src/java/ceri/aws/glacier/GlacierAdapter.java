package ceri.aws.glacier;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.services.glacier.AmazonGlacier;
import com.amazonaws.services.glacier.model.AbortMultipartUploadRequest;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadResult;
import com.amazonaws.services.glacier.model.CreateVaultRequest;
import com.amazonaws.services.glacier.model.CreateVaultResult;
import com.amazonaws.services.glacier.model.DeleteArchiveRequest;
import com.amazonaws.services.glacier.model.DeleteVaultNotificationsRequest;
import com.amazonaws.services.glacier.model.DeleteVaultRequest;
import com.amazonaws.services.glacier.model.DescribeJobRequest;
import com.amazonaws.services.glacier.model.DescribeJobResult;
import com.amazonaws.services.glacier.model.DescribeVaultRequest;
import com.amazonaws.services.glacier.model.DescribeVaultResult;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.GetVaultNotificationsRequest;
import com.amazonaws.services.glacier.model.GetVaultNotificationsResult;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.glacier.model.InitiateMultipartUploadResult;
import com.amazonaws.services.glacier.model.ListJobsRequest;
import com.amazonaws.services.glacier.model.ListJobsResult;
import com.amazonaws.services.glacier.model.ListMultipartUploadsRequest;
import com.amazonaws.services.glacier.model.ListMultipartUploadsResult;
import com.amazonaws.services.glacier.model.ListPartsRequest;
import com.amazonaws.services.glacier.model.ListPartsResult;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;
import com.amazonaws.services.glacier.model.SetVaultNotificationsRequest;
import com.amazonaws.services.glacier.model.UploadArchiveRequest;
import com.amazonaws.services.glacier.model.UploadArchiveResult;
import com.amazonaws.services.glacier.model.UploadMultipartPartRequest;
import com.amazonaws.services.glacier.model.UploadMultipartPartResult;

/**
 * Convenience class implementing the glacier interface, throwing unsupported exceptions
 * for all methods.
 */
public class GlacierAdapter implements AmazonGlacier {
	
	@Override
	public InitiateMultipartUploadResult initiateMultipartUpload(
		InitiateMultipartUploadRequest initiateMultipartUploadRequest)
		throws AmazonServiceException, AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void abortMultipartUpload(AbortMultipartUploadRequest abortMultipartUploadRequest)
		throws AmazonServiceException, AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public CompleteMultipartUploadResult completeMultipartUpload(
		CompleteMultipartUploadRequest completeMultipartUploadRequest)
		throws AmazonServiceException, AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public UploadMultipartPartResult uploadMultipartPart(
		UploadMultipartPartRequest uploadMultipartPartRequest) throws AmazonServiceException,
		AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListMultipartUploadsResult listMultipartUploads(
		ListMultipartUploadsRequest listMultipartUploadsRequest) throws AmazonServiceException,
		AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setEndpoint(String endpoint) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public UploadArchiveResult uploadArchive(UploadArchiveRequest uploadArchiveRequest)
		throws AmazonServiceException, AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteArchive(DeleteArchiveRequest deleteArchiveRequest)
		throws AmazonServiceException, AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public InitiateJobResult initiateJob(InitiateJobRequest initiateJobRequest)
		throws AmazonServiceException, AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DescribeJobResult describeJob(DescribeJobRequest describeJobRequest)
		throws AmazonServiceException, AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListJobsResult listJobs(ListJobsRequest listJobsRequest) throws AmazonServiceException,
		AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public GetJobOutputResult getJobOutput(GetJobOutputRequest getJobOutputRequest)
		throws AmazonServiceException, AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListPartsResult listParts(ListPartsRequest listPartsRequest)
		throws AmazonServiceException, AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListVaultsResult listVaults(ListVaultsRequest listVaultsRequest)
		throws AmazonServiceException, AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public GetVaultNotificationsResult getVaultNotifications(
		GetVaultNotificationsRequest getVaultNotificationsRequest) throws AmazonServiceException,
		AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public CreateVaultResult createVault(CreateVaultRequest createVaultRequest)
		throws AmazonServiceException, AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setVaultNotifications(SetVaultNotificationsRequest setVaultNotificationsRequest)
		throws AmazonServiceException, AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DescribeVaultResult describeVault(DescribeVaultRequest describeVaultRequest)
		throws AmazonServiceException, AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteVaultNotifications(
		DeleteVaultNotificationsRequest deleteVaultNotificationsRequest)
		throws AmazonServiceException, AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteVault(DeleteVaultRequest deleteVaultRequest) throws AmazonServiceException,
		AmazonClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void shutdown() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest request) {
		throw new UnsupportedOperationException();
	}
	
}
