package ceri.ci.alert;

/**
 * Test class to expose constructor to other packages.
 */
public class TestAlerters extends Alerters {

	public TestAlerters(Alerters.Builder builder) {
		super(builder);
	}
	
}
