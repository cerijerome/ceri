package ceri.image.spi;


public interface CropperService {

	byte[] cropImage(String path) throws CropperServiceException;
	
}
