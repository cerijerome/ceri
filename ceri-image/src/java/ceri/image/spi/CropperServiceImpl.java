package ceri.image.spi;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.image.Cropper;
import ceri.image.Image;

/**
 * Implementation of the CropperService that takes a path, downloads the image,
 * extracts the key to lookup a Cropper instance, processes the image, and
 * returns the image data as a byte array. The path should take the form
 * (/)key/imageurl
 */
public class CropperServiceImpl implements CropperService {
	private static final String HTTP = "http://";
	private static final Pattern SPLIT_PATTERN = Pattern.compile("^/?([^/]+)/(.*)$");
	private final Collection<Pattern> allowedImageUrlPaths;
	private final Map<String, Cropper> croppers;
	private final Downloader downloader;
	private final Image.Factory imageFactory;

	public static class Builder {
		final Collection<Pattern> allowedImagePaths = new HashSet<>();
		final Map<String, Cropper> croppers = new HashMap<>();
		final Downloader downloader;
		final Image.Factory imageFactory;

		Builder(Downloader downloader, Image.Factory imageFactory) {
			this.downloader = downloader;
			this.imageFactory = imageFactory;
		}

		/**
		 * Registers a Cropper instance and key for lookup.
		 */
		public Builder cropper(String key, Cropper cropper) {
			croppers.put(key, cropper);
			return this;
		}

		/**
		 * Registers Cropper instances and keys for lookup.
		 */
		public Builder croppers(Map<String, Cropper> croppers) {
			croppers.putAll(croppers);
			return this;
		}

		/**
		 * Specify allowed image url regex patterns.
		 * Pattern matches whole path.
		 */
		public Builder allowImagePath(String... patterns) {
			for (String pattern : patterns)
				allowedImagePaths.add(Pattern.compile(pattern));
			return this;
		}

		/**
		 * Builds the service.
		 */
		public CropperServiceImpl build() {
			return new CropperServiceImpl(this);
		}
	}

	/**
	 * Constructs a builder with given downloader and image factory.
	 */
	public static Builder builder(Downloader downloader, Image.Factory imageFactory) {
		return new Builder(downloader, imageFactory);
	}

	CropperServiceImpl(Builder builder) {
		allowedImageUrlPaths = Collections.unmodifiableCollection(builder.allowedImagePaths);
		croppers = Collections.unmodifiableMap(new HashMap<>(builder.croppers));
		downloader = builder.downloader;
		imageFactory = builder.imageFactory;
	}

	/**
	 * The collection of keys that map to Cropper instances.
	 */
	@Override
	public Collection<String> keys() {
		return croppers.keySet();
	}
	
	/**
	 * Downloads and crops an image. The path should be in the format
	 * (/)key/imageurl and is typically taken as the path after the domain in
	 * the request URL. Key is used to get the registered Cropper instance.
	 * Image urls must match a pattern added during construction or an
	 * exception will be thrown.
	 */
	@Override
	public byte[] cropImage(String path) throws CropperServiceException {
		Matcher m = SPLIT_PATTERN.matcher(path);
		if (!m.find()) throw new IllegalArgumentException("path did not match pattern " +
			SPLIT_PATTERN.pattern() + ": " + path);
		int group = 1;
		String key = m.group(group++);
		String imagePath = m.group(group++);

		Cropper cropper = getCropper(key);
		validateImagePath(imagePath);
		byte[] imageData = loadImageData(imagePath);
		Image image = createImage(imageData);
		return cropper.crop(image);
	}

	private Cropper getCropper(String key) throws CropperServiceException {
		Cropper cropper = croppers.get(key);
		if (cropper == null) throw new CropperServiceException("No matching Cropper for key " + key);
		return cropper;
	}

	private void validateImagePath(String imagePath) throws CropperServiceException {
		for (Pattern pattern : allowedImageUrlPaths)
			if (pattern.matcher(imagePath).matches()) return;
		throw new CropperServiceException("Image path not allowed: " + imagePath);
	}

	private byte[] loadImageData(String imageUrl)
		throws CropperServiceException {
		try {
			if (!imageUrl.toLowerCase().startsWith(HTTP)) imageUrl = HTTP + imageUrl;
			return downloader.download(imageUrl);
		} catch (IOException e) {
			throw new CropperServiceException("Failed to load " + imageUrl, e);
		}
	}

	private Image createImage(byte[] imageData)
		throws CropperServiceException {
		try {
			return imageFactory.create(imageData);
		} catch (IOException e) {
			throw new CropperServiceException("Failed to create image from byte array", e);
		}
	}

}
