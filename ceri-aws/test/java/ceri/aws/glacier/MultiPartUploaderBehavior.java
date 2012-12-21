package ceri.aws.glacier;

import static ceri.common.test.TestUtil.assertArray;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.amazonaws.services.glacier.model.UploadMultipartPartRequest;

public class MultiPartUploaderBehavior {
	private GlacierMockHelper helper;
	private MultipartUploader uploader;
	private byte[] data;

	@Before
	public void init() {
		helper = new GlacierMockHelper();
		data = new byte[256];
		for (int i = 0; i < data.length; i++) data[i] = (byte)i; 
		uploader =
			MultipartUploader.builder(helper.glacier, new ByteArrayInputStream(data), "test")
				.partSize(100).build();
	}

	@Test
	public void shouldSplitDataIntoEqualChunksExceptTheLastOne() throws IOException {
		uploader.execute();
		verify(helper.glacier, times(3)).uploadMultipartPart(helper.uploadCaptor.capture());
		List<UploadMultipartPartRequest> requests = helper.uploadCaptor.getAllValues();
		assertThat(requests.get(0).getBody().available(), is(100));
		assertThat(requests.get(1).getBody().available(), is(100));
		assertThat(requests.get(2).getBody().available(), is(56));
	}

	@Test
	public void shouldSpecifyTheIncrementalByteRange() throws IOException {
		uploader.execute();
		verify(helper.glacier, times(3)).uploadMultipartPart(helper.uploadCaptor.capture());
		List<UploadMultipartPartRequest> requests = helper.uploadCaptor.getAllValues();
		assertThat(requests.get(0).getRange(), is("bytes 0-99/*"));
		assertThat(requests.get(1).getRange(), is("bytes 100-199/*"));
		assertThat(requests.get(2).getRange(), is("bytes 200-255/*"));
	}

	@Test
	public void shouldUploadDataInOrder() throws IOException {
		uploader.execute();
		verify(helper.glacier, times(3)).uploadMultipartPart(helper.uploadCaptor.capture());
		List<UploadMultipartPartRequest> requests = helper.uploadCaptor.getAllValues();
		byte[] buffer = new byte[256];
		requests.get(0).getBody().read(buffer, 0, 100);
		requests.get(1).getBody().read(buffer, 100, 100);
		requests.get(2).getBody().read(buffer, 200, 56);
		assertArray(buffer, data);
	}

}
