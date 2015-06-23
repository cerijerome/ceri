package ceri.image.spi;

import java.io.IOException;

public interface Downloader {

	byte[] download(String url) throws IOException;

}
