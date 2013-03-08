package ceri.image.spi;

public class CropperServiceException extends Exception {
	private static final long serialVersionUID = 6187141648976258836L;

	public CropperServiceException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public CropperServiceException(String message) {
		super(message);
	}
	
}
