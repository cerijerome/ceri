package ceri.image.spi;

import java.io.IOException;

public interface CropperService {

	byte[] cropImage(String path) throws IOException;
	
}
