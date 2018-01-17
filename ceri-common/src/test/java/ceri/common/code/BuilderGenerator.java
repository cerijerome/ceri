package ceri.common.code;

/**
 * Generates code text for an immutable class with a builder. Class contains methods for hashCode(),
 * equals(), and toString().
 * 
 * Run main then enter class name followed by fields on subsequent lines. Fields can contain
 * public/protected/private/final/; as these will be removed. Finish with an empty line and the
 * class will be generated then copied to the clipboard. Example:
 * 
 * <pre>
 * MyClass
 * int id
 * String name
 * Date date
 * </pre>
 */
public class BuilderGenerator {

	public static void main(String[] args) {
		ClassGenerator.createToClipBoardFromSystemIn(true);
	}

	public static String from(String input) {
		return ClassGenerator.from(input, true);
	}

}
