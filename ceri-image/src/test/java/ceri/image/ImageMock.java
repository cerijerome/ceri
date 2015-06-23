package ceri.image;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.OutputStream;

public class ImageMock implements Image {
	public int x = 0;
	public int y = 0;
	public int w;
	public int h;
	public Interpolation interpolation;
	public Format format;
	public float quality;

	public static Image.Factory factory(final Image image) {
		return new Image.Factory() {
			@Override
			public Image create(byte[] data) {
				return image;
			}
		};
	}

	public ImageMock(int w, int h) {
		this.w = w;
		this.h = h;
	}

	@Override
	public int getWidth() {
		return w;
	}

	@Override
	public int getHeight() {
		return h;
	}

	@Override
	public Image resize(Dimension dimension, Interpolation interpolation) {
		w = dimension.width;
		h = dimension.height;
		this.interpolation = interpolation;
		return this;
	};

	@Override
	public Image crop(Rectangle rectangle) {
		x = rectangle.x;
		y = rectangle.y;
		w = rectangle.width;
		h = rectangle.height;
		return this;
	}

	@Override
	public void write(Format format, float quality, OutputStream out) {
		this.format = format;
		this.quality = quality;
	}

}
