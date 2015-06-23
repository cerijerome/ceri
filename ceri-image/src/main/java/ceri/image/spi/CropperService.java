package ceri.image.spi;

import java.util.Collection;

/**
 * Service for loading and processing images.
 */
public interface CropperService {

	/**
	 * Returns the list of keys that map to Cropper instances.
	 */
	Collection<String> keys();

	/**
	 * Loads and crops an image. The path should be in the format (/)key/imageurl and is typically
	 * taken as the path after the domain in the request URL.
	 */
	byte[] cropImage(String path) throws CropperServiceException;

}
