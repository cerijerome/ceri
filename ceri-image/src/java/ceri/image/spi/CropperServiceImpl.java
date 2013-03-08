package ceri.image.spi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.net.www.http.HttpClient;
import ceri.image.Cropper;
import ceri.image.Image;
import ceri.image.magick.MagickImage;

public class CropperServiceImpl implements CropperService {
	private static final String HTTP = "http://";
	private static final int BUFFER_SIZE = 64 * 1024;
	private static final Pattern SPLIT_PATTERN = Pattern.compile("^/?([^/]+)/(.*)$");
	private final Image.Factory imageFactory = MagickImage.FACTORY;
	private final Collection<Pattern> allowedImageUrlPaths;
	private final Map<String, Cropper> croppers;
	private final HttpClient httpClient;

	public static class Builder {
		final Collection<Pattern> allowedImagePaths = new HashSet<>();
		final Map<String, Cropper> croppers = new HashMap<>();
		final HttpClient httpClient;

		Builder(HttpClient httpClient) {
			this.httpClient = httpClient;
		}

		public Builder cropper(String key, Cropper cropper) {
			croppers.put(key, cropper);
			return this;
		}

		public Builder croppers(Map<String, Cropper> croppers) {
			croppers.putAll(croppers);
			return this;
		}

		public Builder allowImagePath(String... patterns) {
			for (String pattern : patterns)
				allowedImagePaths.add(Pattern.compile(pattern));
			return this;
		}

		public CropperServiceImpl build() {
			return new CropperServiceImpl(this);
		}
	}

	public static Builder builder(HttpClient httpClient) {
		return new Builder(httpClient);
	}

	CropperServiceImpl(Builder builder) {
		allowedImageUrlPaths = Collections.unmodifiableCollection(builder.allowedImagePaths);
		croppers = Collections.unmodifiableMap(new HashMap<>(builder.croppers));
		httpClient = builder.httpClient;
	}

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
		byte[] imageData = loadImageData(httpClient, imagePath);
		Image image = createImage(imageFactory, imageData);
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

	private byte[] loadImageData(HttpClient httpClient, String imageUrl)
		throws CropperServiceException {
		try {
			imageUrl = HTTP + imageUrl;
			byte[] buffer = new byte[BUFFER_SIZE];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			URL url = new URL(imageUrl);
			try (InputStream in = url.openStream()) {
				int count = 0;
				while ((count = in.read(buffer)) != -1) {
					out.write(buffer, 0, count);
				}
			}
			return out.toByteArray();
		} catch (IOException e) {
			throw new CropperServiceException("Failed to load " + imageUrl, e);
		}

	}

	private Image createImage(Image.Factory imageFactory, byte[] imageData)
		throws CropperServiceException {
		try {
			return imageFactory.create(imageData);
		} catch (IOException e) {
			throw new CropperServiceException("Failed to create image from byte array", e);
		}
	}

}
