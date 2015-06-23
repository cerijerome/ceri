package ceri.image.spi;

public class DownloaderMock implements Downloader {
	public byte[] data;

	public DownloaderMock(byte[] data) {
		this.data = data;
	}

	@Override
	public byte[] download(String url) {
		return data;
	}

}
