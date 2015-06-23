package ceri.image.spi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Downloads data from a given URL using Apache HttpClient. maxSize specifies the maximum data size
 * allowed, or 0 for unlimited.
 */
public class HttpClientDownloader implements Downloader {
	private static final int BUFFER_SIZE = 32 * 1024;
	private final ClientConnectionManager connectionManager;
	private final int maxSize;

	public HttpClientDownloader(ClientConnectionManager connectionManager, int maxSize) {
		this.connectionManager = connectionManager;
		this.maxSize = maxSize;
	}

	@Override
	public byte[] download(String url) throws IOException {
		HttpClient client = new DefaultHttpClient(connectionManager);
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);
		byte[] buffer = new byte[BUFFER_SIZE];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (InputStream in = response.getEntity().getContent()) {
			int n = 0;
			int count = 0;
			while ((count = in.read(buffer)) != -1) {
				n += count;
				if (maxSize > 0 && n > maxSize) throw new IOException(
					"Maxiumum data size exceeded: " + n + " bytes");
				out.write(buffer, 0, count);
			}
		}
		return out.toByteArray();
	}

}
