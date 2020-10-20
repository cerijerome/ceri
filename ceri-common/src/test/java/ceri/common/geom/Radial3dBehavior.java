package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertApprox;
import org.junit.Test;

public class Radial3dBehavior {

	@Test
	public void shouldConstrainVolume() {
		Radial3d r = new TestRadial3d() {
			@Override
			public double volume() {
				return 99;
			}
		};
		assertApprox(r.constrainVolume(-1), 0);
		assertApprox(r.constrainVolume(0), 0);
		assertApprox(r.constrainVolume(50), 50);
		assertApprox(r.constrainVolume(99), 99);
		assertApprox(r.constrainVolume(100), 99);
	}

	@Test
	public void shouldConstrainHeight() {
		Radial3d r = new TestRadial3d() {
			@Override
			public double height() {
				return 3;
			}
		};
		assertApprox(r.constrainHeight(-1), 0);
		assertApprox(r.constrainHeight(0), 0);
		assertApprox(r.constrainHeight(1), 1);
		assertApprox(r.constrainHeight(3), 3);
		assertApprox(r.constrainHeight(4), 3);
	}

	@Test
	public void shouldCalculateVolumeFromMaxHeight() {
		Radial3d r = new TestRadial3d() {
			@Override
			public double height() {
				return 3.0;
			}

			@Override
			public double volumeFromHeight(double h) {
				return 33.0 * h;
			}
		};
		assertApprox(r.volume(), 99.0);
	}

	static class TestRadial3d implements Radial3d {
		@Override
		public double gradientAtHeight(double h) {
			throw new UnsupportedOperationException();
		}

		@Override
		public double height() {
			throw new UnsupportedOperationException();
		}

		@Override
		public double heightFromVolume(double v) {
			throw new UnsupportedOperationException();
		}

		@Override
		public double radiusFromHeight(double h) {
			throw new UnsupportedOperationException();
		}

		@Override
		public double volumeFromHeight(double h) {
			throw new UnsupportedOperationException();
		}
	}

}
