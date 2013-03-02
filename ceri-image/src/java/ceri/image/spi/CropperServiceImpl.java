package ceri.image.spi;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.image.Cropper;
import ceri.image.ImageUtil;

public class CropperServiceImpl implements CropperService {
	private static final String HTTP = "http://";
	private static final Pattern SPLIT_PATTERN = Pattern.compile("^/?([^/]+)/(.*)$");
	private final Collection<Pattern> allowedImagePaths;
	private final Map<String, Cropper> croppers;

	public static class Builder {
		final Collection<Pattern> allowedImagePaths = new HashSet<>();
		final Map<String, Cropper> croppers = new HashMap<>();

		Builder() {}

		public Builder register(String name, Cropper cropper) {
			croppers.put(name, cropper);
			return this;
		}
		
		public Builder register(Map<String, Cropper> croppers) {
			croppers.putAll(croppers);
			return this;
		}
		
		public Builder imagePath(String pattern) {
			allowedImagePaths.add(Pattern.compile(pattern));
			return this;
		}

		public CropperServiceImpl build() {
			return new CropperServiceImpl(this);
		}
	}

	public static Builder builder() {
		return new Builder();	
	}
	
	CropperServiceImpl(Builder builder) {
		allowedImagePaths =
			Collections.unmodifiableCollection(new HashSet<>(builder.allowedImagePaths));
		croppers = Collections.unmodifiableMap(new HashMap<>(builder.croppers));
	}

	@Override
	public byte[] cropImage(String path) throws IOException {
		Matcher m = SPLIT_PATTERN.matcher(path);
		if (!m.find()) throw new IllegalArgumentException("path did not match pattern " +
			SPLIT_PATTERN.pattern() + ": " + path);
		int group = 1;
		String name = m.group(group++);
		String imagePath = m.group(group++);
		
		Cropper cropper = croppers.get(name);
		if (cropper == null) throw new IllegalArgumentException("No matching cropper: " + name);
		if (!isAllowedImagePath(imagePath)) throw new IllegalArgumentException(
			"Image path not allowed: " + imagePath);
		BufferedImage image = loadImage(imagePath);
		return cropper.crop(image);
	}
	
	private boolean isAllowedImagePath(String imagePath) {
		for (Pattern pattern : allowedImagePaths)
			if (pattern.matcher(imagePath).matches()) return true;
		return false;
	}
	
	private BufferedImage loadImage(String imagePath) throws IOException {
		// Use HttpClient with timeout
		String url = HTTP + imagePath;
		System.out.println("Loading " + url);
		return ImageUtil.readFromUrl(url);
	}
	
}
