package ceri.ci.x10;

/**
 * Test class to expose constructor to other packages.
 */
public class TestX10Alerter extends X10Alerter {

	public TestX10Alerter(X10Alerter.Builder builder) {
		super(builder);
	}
	
}
