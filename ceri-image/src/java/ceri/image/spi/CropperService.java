package ceri.image.spi;

/**
 * Service for loading and processing images.
 */
public interface CropperService {

	/**
	 * Loads and crops an image. The path should be in the format (/)key/imageurl
	 * and is typically taken as the path after the domain in the request URL.
	 */
	byte[] cropImage(String path) throws CropperServiceException;
	
}
