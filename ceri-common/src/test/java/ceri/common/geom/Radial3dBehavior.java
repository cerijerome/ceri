package ceri.common.geom;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;

public class Radial3dBehavior {

	@Test
	public void shouldConstrainVolume() {
		var r = new TestRadial3d();
		r.h.autoResponses(3.0);
		r.vFromH.autoResponse(h -> h * 33.0);
		Assert.approx(r.constrainVolume(-1), 0);
		Assert.approx(r.constrainVolume(0), 0);
		Assert.approx(r.constrainVolume(50), 50);
		Assert.approx(r.constrainVolume(99), 99);
		Assert.approx(r.constrainVolume(100), 99);
	}

	@Test
	public void shouldConstrainHeight() {
		var r = new TestRadial3d();
		r.h.autoResponses(3.0);
		Assert.approx(r.constrainH(-1), 0);
		Assert.approx(r.constrainH(0), 0);
		Assert.approx(r.constrainH(1), 1);
		Assert.approx(r.constrainH(3), 3);
		Assert.approx(r.constrainH(4), 3);
	}

	@Test
	public void shouldCalculateVolumeFromMaxHeight() {
		var r = new TestRadial3d();
		r.h.autoResponses(3.0);
		r.vFromH.autoResponse(h -> h * 33.0);
		Assert.approx(r.volume(), 99.0);
	}

	private static class TestRadial3d implements Radial3d {
		public CallSync.Function<Double, Double> gAtH = CallSync.function(null, 0.0);
		public CallSync.Supplier<Double> h = CallSync.supplier(0.0);
		public CallSync.Function<Double, Double> hFromV = CallSync.function(null, 0.0);
		public CallSync.Function<Double, Double> rFromH = CallSync.function(null, 0.0);
		public CallSync.Function<Double, Double> vFromH = CallSync.function(null, 0.0);
		
		@Override
		public double gradientAtH(double h) {
			return gAtH.apply(h);
		}

		@Override
		public double h() {
			return h.get();
		}

		@Override
		public double hFromVolume(double v) {
			return hFromV.apply(v);
		}

		@Override
		public double radiusFromH(double h) {
			return rFromH.apply(h);
		}

		@Override
		public double volumeFromH(double h) {
			return vFromH.apply(h);
		}
	}
}
