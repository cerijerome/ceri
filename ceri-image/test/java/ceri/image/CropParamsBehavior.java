package ceri.image;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.test.TestUtil;
import ceri.image.CropParams;
import ceri.image.Resolution;

public class CropParamsBehavior {

	@Test
	public void shouldCreateFromValidString() {
		CropParams params;
		params = CropParams.createFromString("123x456sd");
		assertThat(params.width, is(123));
		assertThat(params.height, is(456));
		assertThat(params.resolution, is(Resolution.SD));
		params = CropParams.createFromString("1x1hd");
		assertThat(params.width, is(1));
		assertThat(params.height, is(1));
		assertThat(params.resolution, is(Resolution.HD));
	}

	@Test
	public void shouldFailCreationForInvalidArguments() {
		TestUtil.assertException(new Runnable() {
			@Override
			public void run() {
				CropParams.create(1, 1, null);
			}
		});
		TestUtil.assertException(new Runnable() {
			@Override
			public void run() {
				CropParams.create(0, 1, Resolution.SD);
			}
		});
		TestUtil.assertException(new Runnable() {
			@Override
			public void run() {
				CropParams.create(1, -1, Resolution.HD);
			}
		});
		TestUtil.assertException(new Runnable() {
			@Override
			public void run() {
				CropParams.createFromString("0x0sd");
			}
		});
	}

}
