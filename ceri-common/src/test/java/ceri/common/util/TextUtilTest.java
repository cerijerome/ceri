package ceri.common.util;

import static ceri.common.test.TestUtil.assertElements;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TextUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(TextUtil.class);
	}

	@Test
	public void testToWordsWithSpaces() {
		assertElements(TextUtil.toWords(""));
		assertElements(TextUtil.toWords("  "));

		assertElements(TextUtil.toWords("a "), "a");
		assertElements(TextUtil.toWords(" a"), "a");
		assertElements(TextUtil.toWords("A "), "A");
		assertElements(TextUtil.toWords(" A"), "A");

		assertElements(TextUtil.toWords("a b"), "a", "b");
		assertElements(TextUtil.toWords(" ab"), "ab");
		assertElements(TextUtil.toWords("A b"), "A", "b");
		assertElements(TextUtil.toWords(" Ab"), "Ab");
		assertElements(TextUtil.toWords("a B"), "a", "B");
		assertElements(TextUtil.toWords(" aB"), "a", "B");
		assertElements(TextUtil.toWords("A B"), "A", "B");
		assertElements(TextUtil.toWords(" AB"), "AB");
	}

	@Test
	public void testToWordsWithUnderscores() {
		assertElements(TextUtil.toWords("_"));
		assertElements(TextUtil.toWords("__"));
		assertElements(TextUtil.toWords("__ _ "));

		assertElements(TextUtil.toWords("a_"), "a");
		assertElements(TextUtil.toWords("_a"), "a");
		assertElements(TextUtil.toWords("A_"), "A");
		assertElements(TextUtil.toWords("_A"), "A");

		assertElements(TextUtil.toWords("a_b"), "a", "b");
		assertElements(TextUtil.toWords("_ab"), "ab");
		assertElements(TextUtil.toWords("A_b"), "A", "b");
		assertElements(TextUtil.toWords("_Ab"), "Ab");
		assertElements(TextUtil.toWords("a_B"), "a", "B");
		assertElements(TextUtil.toWords("_aB"), "a", "B");
		assertElements(TextUtil.toWords("A_B"), "A", "B");
		assertElements(TextUtil.toWords("_AB"), "AB");
	}

	@Test
	public void testToWordsWithLettersAndNumbers() {
		assertElements(TextUtil.toWords("a1"), "a", "1");
		assertElements(TextUtil.toWords("1a"), "1a");
		assertElements(TextUtil.toWords("A1"), "A1");
		assertElements(TextUtil.toWords("1A"), "1", "A");
		assertElements(TextUtil.toWords("a1b"), "a", "1b");
		assertElements(TextUtil.toWords("1ab"), "1ab");
		assertElements(TextUtil.toWords("A1b"), "A1b");
		assertElements(TextUtil.toWords("1Ab"), "1", "Ab");
		assertElements(TextUtil.toWords("a1B"), "a", "1", "B");
		assertElements(TextUtil.toWords("1aB"), "1a", "B");
		assertElements(TextUtil.toWords("A1B"), "A1", "B");
		assertElements(TextUtil.toWords("1AB"), "1", "AB");
	}

	@Test
	public void testToWordsWithLetters() {
		assertElements(TextUtil.toWords("a"), "a");
		assertElements(TextUtil.toWords("A"), "A");

		assertElements(TextUtil.toWords("ab"), "ab");
		assertElements(TextUtil.toWords("Ab"), "Ab");
		assertElements(TextUtil.toWords("aB"), "a", "B");
		assertElements(TextUtil.toWords("AB"), "AB");

		assertElements(TextUtil.toWords("abc"), "abc");
		assertElements(TextUtil.toWords("Abc"), "Abc");
		assertElements(TextUtil.toWords("aBc"), "a", "Bc");
		assertElements(TextUtil.toWords("ABc"), "A", "Bc");
		assertElements(TextUtil.toWords("abC"), "ab", "C");
		assertElements(TextUtil.toWords("AbC"), "Ab", "C");
		assertElements(TextUtil.toWords("aBC"), "a", "BC");
		assertElements(TextUtil.toWords("ABC"), "ABC");

		assertElements(TextUtil.toWords("abcd"), "abcd");
		assertElements(TextUtil.toWords("Abcd"), "Abcd");
		assertElements(TextUtil.toWords("aBcd"), "a", "Bcd");
		assertElements(TextUtil.toWords("ABcd"), "A", "Bcd");
		assertElements(TextUtil.toWords("abCd"), "ab", "Cd");
		assertElements(TextUtil.toWords("AbCd"), "Ab", "Cd");
		assertElements(TextUtil.toWords("aBCd"), "a", "B", "Cd");
		assertElements(TextUtil.toWords("ABCd"), "AB", "Cd");
		assertElements(TextUtil.toWords("abcD"), "abc", "D");
		assertElements(TextUtil.toWords("AbcD"), "Abc", "D");
		assertElements(TextUtil.toWords("aBcD"), "a", "Bc", "D");
		assertElements(TextUtil.toWords("ABcD"), "A", "Bc", "D");
		assertElements(TextUtil.toWords("abCD"), "ab", "CD");
		assertElements(TextUtil.toWords("AbCD"), "Ab", "CD");
		assertElements(TextUtil.toWords("aBCD"), "a", "BCD");
		assertElements(TextUtil.toWords("ABCD"), "ABCD");
	}

	@Test
	public void testToPhrase() {
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
	public void testCamelToPascal() {
		assertThat(TextUtil.camelToPascal(""), is(""));
		assertThat(TextUtil.camelToPascal("_helloThereABC_"), is("_HelloThereABC_"));
		assertThat(TextUtil.camelToPascal("hello1there2ABC3_"), is("Hello1There2ABC3_"));
	}

	@Test
	public void testPascalToProperty() {
		assertThat(TextUtil.pascalToProperty(""), is(""));
		assertThat(TextUtil.pascalToProperty("_HelloThereABC_"), is("_hello.there.abc_"));
		assertThat(TextUtil.pascalToProperty("hello1there2ABC3_"), is("hello1there2.abc3_"));
	}

	@Test
	public void testFirstToUpper() {
		assertThat(TextUtil.firstToUpper(""), is(""));
		assertThat(TextUtil.firstToUpper("hello"), is("Hello"));
		assertThat(TextUtil.firstToUpper("Hello"), is("Hello"));
		assertThat(TextUtil.firstToUpper("_"), is("_"));
	}

	@Test
	public void testFirstToLower() {
		assertThat(TextUtil.firstToLower(""), is(""));
		assertThat(TextUtil.firstToLower("hello"), is("hello"));
		assertThat(TextUtil.firstToLower("Hello"), is("hello"));
		assertThat(TextUtil.firstToLower("_"), is("_"));
	}

	@Test
	public void testPascalToUpper() {
		assertThat(TextUtil.pascalToUpper(""), is(""));
		assertThat(TextUtil.pascalToUpper("_HelloThereABC_"), is("_HELLO_THERE_ABC_"));
		assertThat(TextUtil.pascalToUpper("_Hello1There2ABC3_"), is("_HELLO1_THERE2_ABC3_"));
	}

	@Test
	public void testUpperToCapitalized() {
		assertThat(TextUtil.upperToCapitalized(""), is(""));
		assertThat(TextUtil.upperToCapitalized("_HELLO_THERE_ABC_"), is("_Hello_There_Abc_"));
		assertThat(TextUtil.upperToCapitalized("_HELLO1_THERE2_ABC3_"), is("_Hello1_There2_Abc3_"));
	}

	@Test
	public void testUpperToPascal() {
		assertThat(TextUtil.upperToPascal(""), is(""));
		assertThat(TextUtil.upperToPascal("_HELLO_THERE_ABC_"), is("_HelloThereAbc_"));
		assertThat(TextUtil.upperToPascal("_HELLO1_THERE2_ABC3_"), is("_Hello1There2Abc3_"));
	}

	@Test
	public void testUpperToProperty() {
		assertThat(TextUtil.upperToProperty(""), is(""));
		assertThat(TextUtil.upperToProperty("HELLO_THERE_ABC"), is("hello.there.abc"));
		assertThat(TextUtil.upperToProperty("HELLO1_THERE2_ABC3"), is("hello1.there2.abc3"));
	}

	@Test
	public void testPropertyToUpper() {
		assertThat(TextUtil.propertyToUpper(""), is(""));
		assertThat(TextUtil.propertyToUpper("hello.there.abc"), is("HELLO_THERE_ABC"));
		assertThat(TextUtil.propertyToUpper("hello1.there2.abc3"), is("HELLO1_THERE2_ABC3"));
	}

}
