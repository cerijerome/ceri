package ceri.common.text;

import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertNull;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class TextUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(TextUtil.class);
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
		assertThat(TextUtil.toPhrase(""), is(""));
		assertThat(TextUtil.toPhrase("_"), is(""));

		assertThat(TextUtil.toPhrase("abcd"), is("abcd"));
		assertThat(TextUtil.toPhrase("Abcd"), is("abcd"));
		assertThat(TextUtil.toPhrase("aBcd"), is("a bcd"));
		assertThat(TextUtil.toPhrase("ABcd"), is("a bcd"));
		assertThat(TextUtil.toPhrase("abCd"), is("ab cd"));
		assertThat(TextUtil.toPhrase("AbCd"), is("ab cd"));
		assertThat(TextUtil.toPhrase("aBCd"), is("a b cd"));
		assertThat(TextUtil.toPhrase("ABCd"), is("AB cd"));

		assertThat(TextUtil.toPhrase("abcD"), is("abc d"));
		assertThat(TextUtil.toPhrase("AbcD"), is("abc d"));
		assertThat(TextUtil.toPhrase("aBcD"), is("a bc d"));
		assertThat(TextUtil.toPhrase("ABcD"), is("a bc d"));
		assertThat(TextUtil.toPhrase("abCD"), is("ab CD"));
		assertThat(TextUtil.toPhrase("AbCD"), is("ab CD"));
		assertThat(TextUtil.toPhrase("aBCD"), is("a BCD"));
		assertThat(TextUtil.toPhrase("ABCD"), is("ABCD"));

		assertThat(TextUtil.toPhrase("AbCDEf"), is("ab CD ef"));
		assertThat(TextUtil.toPhrase("_helloThere2ABC3_"), is("hello there 2 ABC3"));
		assertThat(TextUtil.toPhrase("testPart1a"), is("test part 1a"));
	}

	@Test
	public void testToCapitalizedPhrase() {
		assertNull(TextUtil.toCapitalizedPhrase(null));
		assertThat(TextUtil.toCapitalizedPhrase(""), is(""));
		assertThat(TextUtil.toCapitalizedPhrase("_"), is(""));

		assertThat(TextUtil.toCapitalizedPhrase("abcd"), is("Abcd"));
		assertThat(TextUtil.toCapitalizedPhrase("Abcd"), is("Abcd"));
		assertThat(TextUtil.toCapitalizedPhrase("aBcd"), is("A Bcd"));
		assertThat(TextUtil.toCapitalizedPhrase("ABcd"), is("A Bcd"));
		assertThat(TextUtil.toCapitalizedPhrase("abCd"), is("Ab Cd"));
		assertThat(TextUtil.toCapitalizedPhrase("AbCd"), is("Ab Cd"));
		assertThat(TextUtil.toCapitalizedPhrase("aBCd"), is("A B Cd"));
		assertThat(TextUtil.toCapitalizedPhrase("ABCd"), is("AB Cd"));

		assertThat(TextUtil.toCapitalizedPhrase("abcD"), is("Abc D"));
		assertThat(TextUtil.toCapitalizedPhrase("AbcD"), is("Abc D"));
		assertThat(TextUtil.toCapitalizedPhrase("aBcD"), is("A Bc D"));
		assertThat(TextUtil.toCapitalizedPhrase("ABcD"), is("A Bc D"));
		assertThat(TextUtil.toCapitalizedPhrase("abCD"), is("Ab CD"));
		assertThat(TextUtil.toCapitalizedPhrase("AbCD"), is("Ab CD"));
		assertThat(TextUtil.toCapitalizedPhrase("aBCD"), is("A BCD"));
		assertThat(TextUtil.toCapitalizedPhrase("ABCD"), is("ABCD"));

		assertThat(TextUtil.toCapitalizedPhrase("AbCDEf"), is("Ab CD Ef"));
		assertThat(TextUtil.toCapitalizedPhrase("_helloThere2ABC3_"), is("Hello There 2 ABC3"));
		assertThat(TextUtil.toCapitalizedPhrase("testPart1a"), is("Test Part 1a"));
	}

	@Test
	public void testCamelToHyphenated() {
		assertNull(TextUtil.camelToHyphenated(null));
		assertThat(TextUtil.camelToHyphenated(""), is(""));
		assertThat(TextUtil.camelToHyphenated("_helloThereABC_"), is("_hello-there-abc_"));
		assertThat(TextUtil.camelToHyphenated("hello1there2ABC3_"), is("hello1there2-abc3_"));
	}

	@Test
	public void testCamelToPascal() {
		assertNull(TextUtil.camelToPascal(null));
		assertThat(TextUtil.camelToPascal(""), is(""));
		assertThat(TextUtil.camelToPascal("_helloThereABC_"), is("_HelloThereABC_"));
		assertThat(TextUtil.camelToPascal("hello1there2ABC3_"), is("Hello1There2ABC3_"));
	}

	@Test
	public void testPascalToProperty() {
		assertNull(TextUtil.pascalToProperty(null));
		assertThat(TextUtil.pascalToProperty(""), is(""));
		assertThat(TextUtil.pascalToProperty("_HelloThereABC_"), is("_hello.there.abc_"));
		assertThat(TextUtil.pascalToProperty("hello1there2ABC3_"), is("hello1there2.abc3_"));
	}

	@Test
	public void testFirstToUpper() {
		assertNull(TextUtil.firstToUpper(null));
		assertThat(TextUtil.firstToUpper(""), is(""));
		assertThat(TextUtil.firstToUpper("hello"), is("Hello"));
		assertThat(TextUtil.firstToUpper("Hello"), is("Hello"));
		assertThat(TextUtil.firstToUpper("_"), is("_"));
	}

	@Test
	public void testFirstToLower() {
		assertNull(TextUtil.firstToLower(null));
		assertThat(TextUtil.firstToLower(""), is(""));
		assertThat(TextUtil.firstToLower("hello"), is("hello"));
		assertThat(TextUtil.firstToLower("Hello"), is("hello"));
		assertThat(TextUtil.firstToLower("_"), is("_"));
	}

	@Test
	public void testFirstLetterToUpper() {
		assertNull(TextUtil.firstLetterToUpper(null));
		assertThat(TextUtil.firstLetterToUpper(""), is(""));
		assertThat(TextUtil.firstLetterToUpper("abc"), is("Abc"));
		assertThat(TextUtil.firstLetterToUpper("  abc"), is("  Abc"));
		assertThat(TextUtil.firstLetterToUpper("_ABc"), is("_ABc"));
	}

	@Test
	public void testFirstLetterToLower() {
		assertNull(TextUtil.firstLetterToLower(null));
		assertThat(TextUtil.firstLetterToLower(""), is(""));
		assertThat(TextUtil.firstLetterToLower("ABC"), is("aBC"));
		assertThat(TextUtil.firstLetterToLower("  ABC"), is("  aBC"));
		assertThat(TextUtil.firstLetterToLower("_abC"), is("_abC"));
	}

	@Test
	public void testPascalToUnderscore() {
		assertNull(TextUtil.pascalToUnderscore(null));
		assertThat(TextUtil.pascalToUnderscore(""), is(""));
		assertThat(TextUtil.pascalToUnderscore("_HelloThereABC_"), is("_HELLO_THERE_ABC_"));
		assertThat(TextUtil.pascalToUnderscore("_Hello1There2ABC3_"), is("_HELLO1_THERE2_ABC3_"));
	}

	@Test
	public void testUpperToCapitalized() {
		assertNull(TextUtil.upperToCapitalized(null));
		assertThat(TextUtil.upperToCapitalized(""), is(""));
		assertThat(TextUtil.upperToCapitalized("_HELLO_THERE_ABC_"), is("_Hello_There_Abc_"));
		assertThat(TextUtil.upperToCapitalized("_HELLO1_THERE2_ABC3_"), is("_Hello1_There2_Abc3_"));
	}

	@Test
	public void testUnderscoreToPascal() {
		assertNull(TextUtil.underscoreToPascal(null));
		assertThat(TextUtil.underscoreToPascal(""), is(""));
		assertThat(TextUtil.underscoreToPascal("_HELLO_THERE_ABC_"), is("_HelloThereAbc_"));
		assertThat(TextUtil.underscoreToPascal("_HELLO1_THERE2_ABC3_"), is("_Hello1There2Abc3_"));
	}

	@Test
	public void testUnderscoreToCamel() {
		assertNull(TextUtil.underscoreToCamel(null));
		assertThat(TextUtil.underscoreToCamel(""), is(""));
		assertThat(TextUtil.underscoreToCamel("_HELLO_THERE_ABC_"), is("_helloThereAbc_"));
		assertThat(TextUtil.underscoreToCamel("_HELLO1_THERE2_ABC3_"), is("_hello1There2Abc3_"));
	}

	@Test
	public void testUnderscoreToProperty() {
		assertNull(TextUtil.underscoreToProperty(null));
		assertThat(TextUtil.underscoreToProperty(""), is(""));
		assertThat(TextUtil.underscoreToProperty("HELLO_THERE_ABC"), is("hello.there.abc"));
		assertThat(TextUtil.underscoreToProperty("HELLO1_THERE2_ABC3"), is("hello1.there2.abc3"));
	}

	@Test
	public void testPropertyToUnderscore() {
		assertNull(TextUtil.propertyToUnderscore(null));
		assertThat(TextUtil.propertyToUnderscore(""), is(""));
		assertThat(TextUtil.propertyToUnderscore("hello.there.abc"), is("HELLO_THERE_ABC"));
		assertThat(TextUtil.propertyToUnderscore("hello1.there2.abc3"), is("HELLO1_THERE2_ABC3"));
	}

}
