package ceri.image;

public class CropperMock extends Cropper {
	public byte[] data;

	public CropperMock(byte[] data) {
		super(builder(0, 0));
		this.data = data;
	}

	@Override
	public byte[] crop(Image image) {
		return data;
	}

}
