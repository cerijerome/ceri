package ceri.image;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import java.awt.image.BufferedImage;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.image.Cropper;

public class CropperMockHelper {
	@Mock public BufferedImage bufferedImage;
	@Mock public Cropper cropper;
	@Mock public byte[] cropResult;
	@Captor public ArgumentCaptor<BufferedImage> cropCaptor;

	public CropperMockHelper() {
		MockitoAnnotations.initMocks(this);
		when(cropper.crop(any(BufferedImage.class))).thenReturn(cropResult);
	}
}
