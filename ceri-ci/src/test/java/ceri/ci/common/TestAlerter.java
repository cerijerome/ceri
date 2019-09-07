package ceri.ci.common;

import ceri.ci.build.Builds;

public class TestAlerter implements Alerter {

	@Override
	public void update(Builds builds) {
		common();
	}

	@Override
	public void clear() {
		common();
	}

	@Override
	public void remind() {
		common();
	}

	protected void common() {}

}
