package ceri.common.code;

class Prefix {
	public static final Prefix FROM_BUILDER = new Prefix("", "builder.");
	public static final Prefix THIS = new Prefix("this.", "");
	public final String field;
	public final String argument;

	private Prefix(String field, String argument) {
		this.field = field;
		this.argument = argument;
	}
}