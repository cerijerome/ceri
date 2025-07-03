package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertLines;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;

public class TextUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(TextUtil.class);
	}

	@Test
	public void testMultilineJavadoc() {
		assertEquals(TextUtil.multilineJavadoc(null), null);
		assertEquals(TextUtil.multilineJavadoc(""), "");
		assertLines(TextUtil.multilineJavadoc("a"), "/**", " * a", " */");
		assertLines(TextUtil.multilineJavadoc("a" + StringUtil.EOL + "b"), "/**", " * a", " * b",
			" */");
	}

	@Test
	public void testMultilineComment() {
		assertEquals(TextUtil.multilineComment(null), null);
		assertEquals(TextUtil.multilineComment(""), "");
		assertLines(TextUtil.multilineComment("a"), "/*", " * a", " */");
		assertLines(TextUtil.multilineComment("a" + StringUtil.EOL + "b"), "/*", " * a", " * b",
			" */");
	}

	@Test
	public void testToWordsWithSpaces() {
		assertTrue(TextUtil.toWords(null).isEmpty());
		assertIterable(TextUtil.toWords(""));
		assertIterable(TextUtil.toWords("  "));

		assertIterable(TextUtil.toWords("a "), "a");
		assertIterable(TextUtil.toWords(" a"), "a");
		assertIterable(TextUtil.toWords("A "), "A");
		assertIterable(TextUtil.toWords(" A"), "A");

		assertIterable(TextUtil.toWords("a b"), "a", "b");
		assertIterable(TextUtil.toWords(" ab"), "ab");
		assertIterable(TextUtil.toWords("A b"), "A", "b");
		assertIterable(TextUtil.toWords(" Ab"), "Ab");
		assertIterable(TextUtil.toWords("a B"), "a", "B");
		assertIterable(TextUtil.toWords(" aB"), "a", "B");
		assertIterable(TextUtil.toWords("A B"), "A", "B");
		assertIterable(TextUtil.toWords(" AB"), "AB");
	}

	@Test
	public void testToWordsWithUnderscores() {
		assertIterable(TextUtil.toWords("_"));
		assertIterable(TextUtil.toWords("__"));
		assertIterable(TextUtil.toWords("__ _ "));

		assertIterable(TextUtil.toWords("a_"), "a");
		assertIterable(TextUtil.toWords("_a"), "a");
		assertIterable(TextUtil.toWords("A_"), "A");
		assertIterable(TextUtil.toWords("_A"), "A");

		assertIterable(TextUtil.toWords("a_b"), "a", "b");
		assertIterable(TextUtil.toWords("_ab"), "ab");
		assertIterable(TextUtil.toWords("A_b"), "A", "b");
		assertIterable(TextUtil.toWords("_Ab"), "Ab");
		assertIterable(TextUtil.toWords("a_B"), "a", "B");
		assertIterable(TextUtil.toWords("_aB"), "a", "B");
		assertIterable(TextUtil.toWords("A_B"), "A", "B");
		assertIterable(TextUtil.toWords("_AB"), "AB");
	}

	@Test
	public void testToWordsWithLettersAndNumbers() {
		assertIterable(TextUtil.toWords("a1"), "a", "1");
		assertIterable(TextUtil.toWords("1a"), "1a");
		assertIterable(TextUtil.toWords("A1"), "A1");
		assertIterable(TextUtil.toWords("1A"), "1", "A");
		assertIterable(TextUtil.toWords("a1b"), "a", "1b");
		assertIterable(TextUtil.toWords("1ab"), "1ab");
		assertIterable(TextUtil.toWords("A1b"), "A1b");
		assertIterable(TextUtil.toWords("1Ab"), "1", "Ab");
		assertIterable(TextUtil.toWords("a1B"), "a", "1", "B");
		assertIterable(TextUtil.toWords("1aB"), "1a", "B");
		assertIterable(TextUtil.toWords("A1B"), "A1", "B");
		assertIterable(TextUtil.toWords("1AB"), "1", "AB");
	}

	@Test
	public void testToWordsWithLetters() {
		assertIterable(TextUtil.toWords("a"), "a");
		assertIterable(TextUtil.toWords("A"), "A");

		assertIterable(TextUtil.toWords("ab"), "ab");
		assertIterable(TextUtil.toWords("Ab"), "Ab");
		assertIterable(TextUtil.toWords("aB"), "a", "B");
		assertIterable(TextUtil.toWords("AB"), "AB");

		assertIterable(TextUtil.toWords("abc"), "abc");
		assertIterable(TextUtil.toWords("Abc"), "Abc");
		assertIterable(TextUtil.toWords("aBc"), "a", "Bc");
		assertIterable(TextUtil.toWords("ABc"), "A", "Bc");
		assertIterable(TextUtil.toWords("abC"), "ab", "C");
		assertIterable(TextUtil.toWords("AbC"), "Ab", "C");
		assertIterable(TextUtil.toWords("aBC"), "a", "BC");
		assertIterable(TextUtil.toWords("ABC"), "ABC");

		assertIterable(TextUtil.toWords("abcd"), "abcd");
		assertIterable(TextUtil.toWords("Abcd"), "Abcd");
		assertIterable(TextUtil.toWords("aBcd"), "a", "Bcd");
		assertIterable(TextUtil.toWords("ABcd"), "A", "Bcd");
		assertIterable(TextUtil.toWords("abCd"), "ab", "Cd");
		assertIterable(TextUtil.toWords("AbCd"), "Ab", "Cd");
		assertIterable(TextUtil.toWords("aBCd"), "a", "B", "Cd");
		assertIterable(TextUtil.toWords("ABCd"), "AB", "Cd");
		assertIterable(TextUtil.toWords("abcD"), "abc", "D");
		assertIterable(TextUtil.toWords("AbcD"), "Abc", "D");
		assertIterable(TextUtil.toWords("aBcD"), "a", "Bc", "D");
		assertIterable(TextUtil.toWords("ABcD"), "A", "Bc", "D");
		assertIterable(TextUtil.toWords("abCD"), "ab", "CD");
		assertIterable(TextUtil.toWords("AbCD"), "Ab", "CD");
		assertIterable(TextUtil.toWords("aBCD"), "a", "BCD");
		assertIterable(TextUtil.toWords("ABCD"), "ABCD");
	}

	@Test
	public void testToPhrase() {
		assertNull(TextUtil.toPhrase(null));
		assertEquals(TextUtil.toPhrase(""), "");
		assertEquals(TextUtil.toPhrase("_"), "");

		assertEquals(TextUtil.toPhrase("abcd"), "abcd");
		assertEquals(TextUtil.toPhrase("Abcd"), "abcd");
		assertEquals(TextUtil.toPhrase("aBcd"), "a bcd");
		assertEquals(TextUtil.toPhrase("ABcd"), "a bcd");
		assertEquals(TextUtil.toPhrase("abCd"), "ab cd");
		assertEquals(TextUtil.toPhrase("AbCd"), "ab cd");
		assertEquals(TextUtil.toPhrase("aBCd"), "a b cd");
		assertEquals(TextUtil.toPhrase("ABCd"), "AB cd");

		assertEquals(TextUtil.toPhrase("abcD"), "abc d");
		assertEquals(TextUtil.toPhrase("AbcD"), "abc d");
		assertEquals(TextUtil.toPhrase("aBcD"), "a bc d");
		assertEquals(TextUtil.toPhrase("ABcD"), "a bc d");
		assertEquals(TextUtil.toPhrase("abCD"), "ab CD");
		assertEquals(TextUtil.toPhrase("AbCD"), "ab CD");
		assertEquals(TextUtil.toPhrase("aBCD"), "a BCD");
		assertEquals(TextUtil.toPhrase("ABCD"), "ABCD");

		assertEquals(TextUtil.toPhrase("AbCDEf"), "ab CD ef");
		assertEquals(TextUtil.toPhrase("_helloThere2ABC3_"), "hello there 2 ABC3");
		assertEquals(TextUtil.toPhrase("testPart1a"), "test part 1a");
	}

	@Test
	public void testToCapitalizedPhrase() {
		assertNull(TextUtil.toCapitalizedPhrase(null));
		assertEquals(TextUtil.toCapitalizedPhrase(""), "");
		assertEquals(TextUtil.toCapitalizedPhrase("_"), "");

		assertEquals(TextUtil.toCapitalizedPhrase("abcd"), "Abcd");
		assertEquals(TextUtil.toCapitalizedPhrase("Abcd"), "Abcd");
		assertEquals(TextUtil.toCapitalizedPhrase("aBcd"), "A Bcd");
		assertEquals(TextUtil.toCapitalizedPhrase("ABcd"), "A Bcd");
		assertEquals(TextUtil.toCapitalizedPhrase("abCd"), "Ab Cd");
		assertEquals(TextUtil.toCapitalizedPhrase("AbCd"), "Ab Cd");
		assertEquals(TextUtil.toCapitalizedPhrase("aBCd"), "A B Cd");
		assertEquals(TextUtil.toCapitalizedPhrase("ABCd"), "AB Cd");

		assertEquals(TextUtil.toCapitalizedPhrase("abcD"), "Abc D");
		assertEquals(TextUtil.toCapitalizedPhrase("AbcD"), "Abc D");
		assertEquals(TextUtil.toCapitalizedPhrase("aBcD"), "A Bc D");
		assertEquals(TextUtil.toCapitalizedPhrase("ABcD"), "A Bc D");
		assertEquals(TextUtil.toCapitalizedPhrase("abCD"), "Ab CD");
		assertEquals(TextUtil.toCapitalizedPhrase("AbCD"), "Ab CD");
		assertEquals(TextUtil.toCapitalizedPhrase("aBCD"), "A BCD");
		assertEquals(TextUtil.toCapitalizedPhrase("ABCD"), "ABCD");

		assertEquals(TextUtil.toCapitalizedPhrase("AbCDEf"), "Ab CD Ef");
		assertEquals(TextUtil.toCapitalizedPhrase("_helloThere2ABC3_"), "Hello There 2 ABC3");
		assertEquals(TextUtil.toCapitalizedPhrase("testPart1a"), "Test Part 1a");
	}

	@Test
	public void testCamelToHyphenated() {
		assertNull(TextUtil.camelToHyphenated(null));
		assertEquals(TextUtil.camelToHyphenated(""), "");
		assertEquals(TextUtil.camelToHyphenated("_helloThereABC_"), "_hello-there-abc_");
		assertEquals(TextUtil.camelToHyphenated("hello1there2ABC3_"), "hello1there2-abc3_");
	}

	@Test
	public void testCamelToPascal() {
		assertNull(TextUtil.camelToPascal(null));
		assertEquals(TextUtil.camelToPascal(""), "");
		assertEquals(TextUtil.camelToPascal("_helloThereABC_"), "_HelloThereABC_");
		assertEquals(TextUtil.camelToPascal("hello1there2ABC3_"), "Hello1There2ABC3_");
	}

	@Test
	public void testPascalToProperty() {
		assertNull(TextUtil.pascalToProperty(null));
		assertEquals(TextUtil.pascalToProperty(""), "");
		assertEquals(TextUtil.pascalToProperty("_HelloThereABC_"), "_hello.there.abc_");
		assertEquals(TextUtil.pascalToProperty("hello1there2ABC3_"), "hello1there2.abc3_");
	}

	@Test
	public void testFirstToUpper() {
		assertNull(TextUtil.firstToUpper(null));
		assertEquals(TextUtil.firstToUpper(""), "");
		assertEquals(TextUtil.firstToUpper("hello"), "Hello");
		assertEquals(TextUtil.firstToUpper("Hello"), "Hello");
		assertEquals(TextUtil.firstToUpper("_"), "_");
	}

	@Test
	public void testFirstToLower() {
		assertNull(TextUtil.firstToLower(null));
		assertEquals(TextUtil.firstToLower(""), "");
		assertEquals(TextUtil.firstToLower("hello"), "hello");
		assertEquals(TextUtil.firstToLower("Hello"), "hello");
		assertEquals(TextUtil.firstToLower("_"), "_");
	}

	@Test
	public void testFirstLetterToUpper() {
		assertNull(TextUtil.firstLetterToUpper(null));
		assertEquals(TextUtil.firstLetterToUpper(""), "");
		assertEquals(TextUtil.firstLetterToUpper("abc"), "Abc");
		assertEquals(TextUtil.firstLetterToUpper("  abc"), "  Abc");
		assertEquals(TextUtil.firstLetterToUpper("_ABc"), "_ABc");
	}

	@Test
	public void testFirstLetterToLower() {
		assertNull(TextUtil.firstLetterToLower(null));
		assertEquals(TextUtil.firstLetterToLower(""), "");
		assertEquals(TextUtil.firstLetterToLower("ABC"), "aBC");
		assertEquals(TextUtil.firstLetterToLower("  ABC"), "  aBC");
		assertEquals(TextUtil.firstLetterToLower("_abC"), "_abC");
	}

	@Test
	public void testPascalToUnderscore() {
		assertNull(TextUtil.pascalToUnderscore(null));
		assertEquals(TextUtil.pascalToUnderscore(""), "");
		assertEquals(TextUtil.pascalToUnderscore("_HelloThereABC_"), "_HELLO_THERE_ABC_");
		assertEquals(TextUtil.pascalToUnderscore("_Hello1There2ABC3_"), "_HELLO1_THERE2_ABC3_");
	}

	@Test
	public void testUpperToCapitalized() {
		assertNull(TextUtil.upperToCapitalized(null));
		assertEquals(TextUtil.upperToCapitalized(""), "");
		assertEquals(TextUtil.upperToCapitalized("_HELLO_THERE_ABC_"), "_Hello_There_Abc_");
		assertEquals(TextUtil.upperToCapitalized("_HELLO1_THERE2_ABC3_"), "_Hello1_There2_Abc3_");
	}

	@Test
	public void testUnderscoreToPascal() {
		assertNull(TextUtil.underscoreToPascal(null));
		assertEquals(TextUtil.underscoreToPascal(""), "");
		assertEquals(TextUtil.underscoreToPascal("_HELLO_THERE_ABC_"), "_HelloThereAbc_");
		assertEquals(TextUtil.underscoreToPascal("_HELLO1_THERE2_ABC3_"), "_Hello1There2Abc3_");
	}

	@Test
	public void testUnderscoreToCamel() {
		assertNull(TextUtil.underscoreToCamel(null));
		assertEquals(TextUtil.underscoreToCamel(""), "");
		assertEquals(TextUtil.underscoreToCamel("_HELLO_THERE_ABC_"), "_helloThereAbc_");
		assertEquals(TextUtil.underscoreToCamel("_HELLO1_THERE2_ABC3_"), "_hello1There2Abc3_");
	}

	@Test
	public void testUnderscoreToProperty() {
		assertNull(TextUtil.underscoreToProperty(null));
		assertEquals(TextUtil.underscoreToProperty(""), "");
		assertEquals(TextUtil.underscoreToProperty("HELLO_THERE_ABC"), "hello.there.abc");
		assertEquals(TextUtil.underscoreToProperty("HELLO1_THERE2_ABC3"), "hello1.there2.abc3");
	}

	@Test
	public void testPropertyToUnderscore() {
		assertNull(TextUtil.propertyToUnderscore(null));
		assertEquals(TextUtil.propertyToUnderscore(""), "");
		assertEquals(TextUtil.propertyToUnderscore("hello.there.abc"), "HELLO_THERE_ABC");
		assertEquals(TextUtil.propertyToUnderscore("hello1.there2.abc3"), "HELLO1_THERE2_ABC3");
	}
}
