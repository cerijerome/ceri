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

public class GlacierDelegator implements AmazonGlacier {
	private final AmazonGlacier glacier;

	public GlacierDelegator(AmazonGlacier glacier) {
		this.glacier = glacier;
	}
	
	@Override
	public void setEndpoint(String endpoint) throws IllegalArgumentException {
		glacier.setEndpoint(endpoint);
	}

	@Override
	public ListVaultsResult listVaults(ListVaultsRequest listVaultsRequest)
		throws AmazonServiceException, AmazonClientException {
		return glacier.listVaults(listVaultsRequest);
	}

	@Override
	public DescribeJobResult describeJob(DescribeJobRequest describeJobRequest)
		throws AmazonServiceException, AmazonClientException {
		return glacier.describeJob(describeJobRequest);
	}

	@Override
	public ListPartsResult listParts(ListPartsRequest listPartsRequest)
		throws AmazonServiceException, AmazonClientException {
		return glacier.listParts(listPartsRequest);
	}

	@Override
	public GetVaultNotificationsResult getVaultNotifications(
		GetVaultNotificationsRequest getVaultNotificationsRequest) throws AmazonServiceException,
		AmazonClientException {
		return glacier.getVaultNotifications(getVaultNotificationsRequest);
	}

	@Override
	public ListJobsResult listJobs(ListJobsRequest listJobsRequest) throws AmazonServiceException,
		AmazonClientException {
		return glacier.listJobs(listJobsRequest);
	}

	@Override
	public CreateVaultResult createVault(CreateVaultRequest createVaultRequest)
		throws AmazonServiceException, AmazonClientException {
		return glacier.createVault(createVaultRequest);
	}

	@Override
	public InitiateMultipartUploadResult initiateMultipartUpload(
		InitiateMultipartUploadRequest initiateMultipartUploadRequest)
		throws AmazonServiceException, AmazonClientException {
		return glacier.initiateMultipartUpload(initiateMultipartUploadRequest);
	}

	@Override
	public void abortMultipartUpload(AbortMultipartUploadRequest abortMultipartUploadRequest)
		throws AmazonServiceException, AmazonClientException {
		glacier.abortMultipartUpload(abortMultipartUploadRequest);
	}

	@Override
	public void deleteArchive(DeleteArchiveRequest deleteArchiveRequest)
		throws AmazonServiceException, AmazonClientException {
		glacier.deleteArchive(deleteArchiveRequest);
	}

	@Override
	public GetJobOutputResult getJobOutput(GetJobOutputRequest getJobOutputRequest)
		throws AmazonServiceException, AmazonClientException {
		return glacier.getJobOutput(getJobOutputRequest);
	}

	@Override
	public InitiateJobResult initiateJob(InitiateJobRequest initiateJobRequest)
		throws AmazonServiceException, AmazonClientException {
		return glacier.initiateJob(initiateJobRequest);
	}

	@Override
	public UploadArchiveResult uploadArchive(UploadArchiveRequest uploadArchiveRequest)
		throws AmazonServiceException, AmazonClientException {
		return glacier.uploadArchive(uploadArchiveRequest);
	}

	@Override
	public void setVaultNotifications(SetVaultNotificationsRequest setVaultNotificationsRequest)
		throws AmazonServiceException, AmazonClientException {
		glacier.setVaultNotifications(setVaultNotificationsRequest);
	}

	@Override
	public CompleteMultipartUploadResult completeMultipartUpload(
		CompleteMultipartUploadRequest completeMultipartUploadRequest)
		throws AmazonServiceException, AmazonClientException {
		return glacier.completeMultipartUpload(completeMultipartUploadRequest);
	}

	@Override
	public UploadMultipartPartResult uploadMultipartPart(
		UploadMultipartPartRequest uploadMultipartPartRequest) throws AmazonServiceException,
		AmazonClientException {
		return glacier.uploadMultipartPart(uploadMultipartPartRequest);
	}

	@Override
	public DescribeVaultResult describeVault(DescribeVaultRequest describeVaultRequest)
		throws AmazonServiceException, AmazonClientException {
		return glacier.describeVault(describeVaultRequest);
	}

	@Override
	public void deleteVaultNotifications(
		DeleteVaultNotificationsRequest deleteVaultNotificationsRequest)
		throws AmazonServiceException, AmazonClientException {
		glacier.deleteVaultNotifications(deleteVaultNotificationsRequest);
	}

	@Override
	public ListMultipartUploadsResult listMultipartUploads(
		ListMultipartUploadsRequest listMultipartUploadsRequest) throws AmazonServiceException,
		AmazonClientException {
		return glacier.listMultipartUploads(listMultipartUploadsRequest);
	}

	@Override
	public void deleteVault(DeleteVaultRequest deleteVaultRequest) throws AmazonServiceException,
		AmazonClientException {
		glacier.deleteVault(deleteVaultRequest);
	}

	@Override
	public void shutdown() {
		glacier.shutdown();
	}

	@Override
	public ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest request) {
		return glacier.getCachedResponseMetadata(request);
	}

}
