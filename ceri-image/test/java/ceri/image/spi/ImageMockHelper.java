package ceri.image.spi;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import java.awt.image.BufferedImage;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.image.Cropper;

public class ImageMockHelper {
	@Mock public BufferedImage bufferedImage;
	@Mock public Cropper cropper1;
	@Mock public Cropper cropper2;
	@Mock public byte[] cropResult;
	@Captor public ArgumentCaptor<BufferedImage> cropCaptor;

	public ImageMockHelper() {
		MockitoAnnotations.initMocks(this);
		when(cropper.crop(any(BufferedImage.class))).thenReturn(cropResult);
	}
}
