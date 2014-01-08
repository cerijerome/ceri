package ceri.common.util;

public class ForceInitTestClass {
	public static final ForceInitTestClass INSTANCE = new ForceInitTestClass();
	
	public ForceInitTestClass() {
		ForceInitTestClassHelper.count++;
	}
	
}
