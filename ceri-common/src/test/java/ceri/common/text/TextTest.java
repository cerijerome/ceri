package ceri.common.text;

import static ceri.common.test.AssertUtil.assertLines;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertString;
import java.util.List;
import org.junit.Test;
import ceri.common.collect.Immutable;

public class TextTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Text.class);
	}

	@Test
	public void testPrefixLines() {
		assertString(Text.prefixLines("xxx", "abc"), "xxxabc");
		assertString(Text.prefixLines("xxx", ""), "");
		assertString(Text.prefixLines("", ""), "");
		assertString(Text.prefixLines("", "abc"), "abc");
		assertString(Text.prefixLines("xxx", "a\r\nb"), "xxxa\r\nxxxb");
		assertString(Text.prefixLines("xxx", "a\r\n"), "xxxa\r\nxxx");
		assertString(Text.prefixLines("xxx", "\r\n"), "xxx\r\nxxx");
		assertString(Text.prefixLines("\t", "\n\r"), "\t\n\t\r\t");
		assertString(Text.prefixLines("\t", "\n\r\n\t"), "\t\n\t\r\n\t\t");
		assertString(Text.prefixLines("x", "a\nb\r\nc\rd"), "xa\nxb\r\nxc\rxd");
	}

	@Test
	public void testSpacesToTabs() {
		assertString(Text.lineSpacesToTabs(1, null), "");
		assertString(Text.spacesToTabs(0, "    "), "    ");
		assertString(Text.spacesToTabs(4, ""), "");
		assertString(Text.spacesToTabs(4, "a"), "a");
		assertString(Text.spacesToTabs(4, "ab"), "ab");
		assertString(Text.spacesToTabs(4, "abc"), "abc");
		assertString(Text.spacesToTabs(4, "abcd"), "abcd");
		assertString(Text.spacesToTabs(4, "a "), "a ");
		assertString(Text.spacesToTabs(4, "ab "), "ab ");
		assertString(Text.spacesToTabs(4, "abc "), "abc ");
		assertString(Text.spacesToTabs(4, "abcd "), "abcd ");
		assertString(Text.spacesToTabs(4, "a  "), "a  ");
		assertString(Text.spacesToTabs(4, "ab  "), "ab\t");
		assertString(Text.spacesToTabs(4, "abc  "), "abc  ");
		assertString(Text.spacesToTabs(4, "abcd  "), "abcd  ");
		assertString(Text.spacesToTabs(2, "a   "), "a \t");
		assertString(Text.spacesToTabs(2, "ab   "), "ab\t ");
		assertString(Text.spacesToTabs(2, "abc   "), "abc \t");
		assertString(Text.spacesToTabs(2, "abcd   "), "abcd\t ");
		assertString(Text.spacesToTabs(4, "       ab c   e "), "\t   ab c   e ");
		assertString(Text.spacesToTabs(4, "abcde   fgh      i"), "abcde\tfgh \t i");
		assertString(Text.spacesToTabs(4, "a\t b \t c"), "a\t b\t c");
	}

	@Test
	public void testTabsToSpaces() {
		assertString(Text.lineTabsToSpaces(1, null), "");
		assertString(Text.tabsToSpaces(0, "    "), "    ");
		assertString(Text.tabsToSpaces(4, ""), "");
		assertString(Text.tabsToSpaces(4, "a"), "a");
		assertString(Text.tabsToSpaces(4, "ab"), "ab");
		assertString(Text.tabsToSpaces(4, "abc"), "abc");
		assertString(Text.tabsToSpaces(4, "abcd"), "abcd");
		assertString(Text.tabsToSpaces(4, "\t"), "    ");
		assertString(Text.tabsToSpaces(4, "a\t"), "a   ");
		assertString(Text.tabsToSpaces(4, "ab\t"), "ab  ");
		assertString(Text.tabsToSpaces(4, "abc\t"), "abc ");
		assertString(Text.tabsToSpaces(4, "abcd\t"), "abcd    ");
		assertString(Text.tabsToSpaces(2, "\t\t"), "    ");
		assertString(Text.tabsToSpaces(2, "\t \t"), "    ");
		assertString(Text.tabsToSpaces(2, " \t \t"), "    ");
		assertString(Text.tabsToSpaces(2, "\t\t "), "     ");
		assertString(Text.tabsToSpaces(4, "abc\t\tdef  \tg"), "abc     def     g");
		assertString(Text.tabsToSpaces(4, "abcde\tfgh\t\ti"), "abcde   fgh     i");
	}

	@Test
	public void testAddLineNumbers() {
		assertLines(Text.addLineNumbers((String) null));
		assertLines(Text.addLineNumbers((String[]) null));
		assertLines(Text.addLineNumbers((List<String>) null));
		assertLines(Text.addLineNumbers((List<String>) null, 1));
		assertLines(Text.addLineNumbers("\n\nabc\n\n\n"), "1: ", "2: ", "3: abc");
		assertLines(Text.addLineNumbers(new String[] { null, "", "abc", "" }), "1: ", "2: ",
			"3: abc", "4: ");
		assertLines(Text.addLineNumbers(Immutable.listOf(null, "", "abc", ""), 3), "3: ", "4: ",
			"5: abc", "6: ");
	}

	@Test
	public void testMultilineJavadoc() {
		assertString(Text.multilineJavadoc(null), "");
		assertString(Text.multilineJavadoc(""), "");
		assertLines(Text.multilineJavadoc("a"), "/**", " * a", " */");
		assertLines(Text.multilineJavadoc("a" + Strings.EOL + "b"), "/**", " * a", " * b",
			" */");
	}

	@Test
	public void testMultilineComment() {
		assertString(Text.multilineComment(null), "");
		assertString(Text.multilineComment(""), "");
		assertLines(Text.multilineComment("a"), "/*", " * a", " */");
		assertLines(Text.multilineComment("a" + Strings.EOL + "b"), "/*", " * a", " * b",
			" */");
	}

	@Test
	public void testToWordsWithSpaces() {
		assertOrdered(Text.toWords(null));
		assertOrdered(Text.toWords(""));
		assertOrdered(Text.toWords("  "));
		assertOrdered(Text.toWords("a "), "a");
		assertOrdered(Text.toWords(" a"), "a");
		assertOrdered(Text.toWords("A "), "A");
		assertOrdered(Text.toWords(" A"), "A");
		assertOrdered(Text.toWords("a b"), "a", "b");
		assertOrdered(Text.toWords(" ab"), "ab");
		assertOrdered(Text.toWords("A b"), "A", "b");
		assertOrdered(Text.toWords(" Ab"), "Ab");
		assertOrdered(Text.toWords("a B"), "a", "B");
		assertOrdered(Text.toWords(" aB"), "a", "B");
		assertOrdered(Text.toWords("A B"), "A", "B");
		assertOrdered(Text.toWords(" AB"), "AB");
	}

	@Test
	public void testToWordsWithUnderscores() {
		assertOrdered(Text.toWords("_"));
		assertOrdered(Text.toWords("__"));
		assertOrdered(Text.toWords("__ _ "));
		assertOrdered(Text.toWords("a_"), "a");
		assertOrdered(Text.toWords("_a"), "a");
		assertOrdered(Text.toWords("A_"), "A");
		assertOrdered(Text.toWords("_A"), "A");
		assertOrdered(Text.toWords("a_b"), "a", "b");
		assertOrdered(Text.toWords("_ab"), "ab");
		assertOrdered(Text.toWords("A_b"), "A", "b");
		assertOrdered(Text.toWords("_Ab"), "Ab");
		assertOrdered(Text.toWords("a_B"), "a", "B");
		assertOrdered(Text.toWords("_aB"), "a", "B");
		assertOrdered(Text.toWords("A_B"), "A", "B");
		assertOrdered(Text.toWords("_AB"), "AB");
	}

	@Test
	public void testToWordsWithLettersAndNumbers() {
		assertOrdered(Text.toWords("a1"), "a", "1");
		assertOrdered(Text.toWords("1a"), "1a");
		assertOrdered(Text.toWords("A1"), "A1");
		assertOrdered(Text.toWords("1A"), "1", "A");
		assertOrdered(Text.toWords("a1b"), "a", "1b");
		assertOrdered(Text.toWords("1ab"), "1ab");
		assertOrdered(Text.toWords("A1b"), "A1b");
		assertOrdered(Text.toWords("1Ab"), "1", "Ab");
		assertOrdered(Text.toWords("a1B"), "a", "1", "B");
		assertOrdered(Text.toWords("1aB"), "1a", "B");
		assertOrdered(Text.toWords("A1B"), "A1", "B");
		assertOrdered(Text.toWords("1AB"), "1", "AB");
	}

	@Test
	public void testToWordsWithLetters() {
		assertOrdered(Text.toWords("a"), "a");
		assertOrdered(Text.toWords("A"), "A");
		assertOrdered(Text.toWords("ab"), "ab");
		assertOrdered(Text.toWords("Ab"), "Ab");
		assertOrdered(Text.toWords("aB"), "a", "B");
		assertOrdered(Text.toWords("AB"), "AB");
		assertOrdered(Text.toWords("abc"), "abc");
		assertOrdered(Text.toWords("Abc"), "Abc");
		assertOrdered(Text.toWords("aBc"), "a", "Bc");
		assertOrdered(Text.toWords("ABc"), "A", "Bc");
		assertOrdered(Text.toWords("abC"), "ab", "C");
		assertOrdered(Text.toWords("AbC"), "Ab", "C");
		assertOrdered(Text.toWords("aBC"), "a", "BC");
		assertOrdered(Text.toWords("ABC"), "ABC");
		assertOrdered(Text.toWords("abcd"), "abcd");
		assertOrdered(Text.toWords("Abcd"), "Abcd");
		assertOrdered(Text.toWords("aBcd"), "a", "Bcd");
		assertOrdered(Text.toWords("ABcd"), "A", "Bcd");
		assertOrdered(Text.toWords("abCd"), "ab", "Cd");
		assertOrdered(Text.toWords("AbCd"), "Ab", "Cd");
		assertOrdered(Text.toWords("aBCd"), "a", "B", "Cd");
		assertOrdered(Text.toWords("ABCd"), "AB", "Cd");
		assertOrdered(Text.toWords("abcD"), "abc", "D");
		assertOrdered(Text.toWords("AbcD"), "Abc", "D");
		assertOrdered(Text.toWords("aBcD"), "a", "Bc", "D");
		assertOrdered(Text.toWords("ABcD"), "A", "Bc", "D");
		assertOrdered(Text.toWords("abCD"), "ab", "CD");
		assertOrdered(Text.toWords("AbCD"), "Ab", "CD");
		assertOrdered(Text.toWords("aBCD"), "a", "BCD");
		assertOrdered(Text.toWords("ABCD"), "ABCD");
	}

	@Test
	public void testToPhrase() {
		assertString(Text.toPhrase(null), "");
		assertString(Text.toPhrase(""), "");
		assertString(Text.toPhrase("_"), "");

		assertString(Text.toPhrase("abcd"), "abcd");
		assertString(Text.toPhrase("Abcd"), "abcd");
		assertString(Text.toPhrase("aBcd"), "a bcd");
		assertString(Text.toPhrase("ABcd"), "a bcd");
		assertString(Text.toPhrase("abCd"), "ab cd");
		assertString(Text.toPhrase("AbCd"), "ab cd");
		assertString(Text.toPhrase("aBCd"), "a b cd");
		assertString(Text.toPhrase("ABCd"), "AB cd");

		assertString(Text.toPhrase("abcD"), "abc d");
		assertString(Text.toPhrase("AbcD"), "abc d");
		assertString(Text.toPhrase("aBcD"), "a bc d");
		assertString(Text.toPhrase("ABcD"), "a bc d");
		assertString(Text.toPhrase("abCD"), "ab CD");
		assertString(Text.toPhrase("AbCD"), "ab CD");
		assertString(Text.toPhrase("aBCD"), "a BCD");
		assertString(Text.toPhrase("ABCD"), "ABCD");

		assertString(Text.toPhrase("AbCDEf"), "ab CD ef");
		assertString(Text.toPhrase("_helloThere2ABC3_"), "hello there 2 ABC3");
		assertString(Text.toPhrase("testPart1a"), "test part 1a");
	}

	@Test
	public void testToCapitalizedPhrase() {
		assertString(Text.toCapitalizedPhrase(null), "");
		assertString(Text.toCapitalizedPhrase(""), "");
		assertString(Text.toCapitalizedPhrase("_"), "");

		assertString(Text.toCapitalizedPhrase("abcd"), "Abcd");
		assertString(Text.toCapitalizedPhrase("Abcd"), "Abcd");
		assertString(Text.toCapitalizedPhrase("aBcd"), "A Bcd");
		assertString(Text.toCapitalizedPhrase("ABcd"), "A Bcd");
		assertString(Text.toCapitalizedPhrase("abCd"), "Ab Cd");
		assertString(Text.toCapitalizedPhrase("AbCd"), "Ab Cd");
		assertString(Text.toCapitalizedPhrase("aBCd"), "A B Cd");
		assertString(Text.toCapitalizedPhrase("ABCd"), "AB Cd");

		assertString(Text.toCapitalizedPhrase("abcD"), "Abc D");
		assertString(Text.toCapitalizedPhrase("AbcD"), "Abc D");
		assertString(Text.toCapitalizedPhrase("aBcD"), "A Bc D");
		assertString(Text.toCapitalizedPhrase("ABcD"), "A Bc D");
		assertString(Text.toCapitalizedPhrase("abCD"), "Ab CD");
		assertString(Text.toCapitalizedPhrase("AbCD"), "Ab CD");
		assertString(Text.toCapitalizedPhrase("aBCD"), "A BCD");
		assertString(Text.toCapitalizedPhrase("ABCD"), "ABCD");

		assertString(Text.toCapitalizedPhrase("AbCDEf"), "Ab CD Ef");
		assertString(Text.toCapitalizedPhrase("_helloThere2ABC3_"), "Hello There 2 ABC3");
		assertString(Text.toCapitalizedPhrase("testPart1a"), "Test Part 1a");
	}

	@Test
	public void testCamelToHyphenated() {
		assertString(Text.camelToHyphenated(null), "");
		assertString(Text.camelToHyphenated(""), "");
		assertString(Text.camelToHyphenated("_helloThereABC_"), "_hello-there-abc_");
		assertString(Text.camelToHyphenated("hello1there2ABC3_"), "hello1there2-abc3_");
	}

	@Test
	public void testCamelToPascal() {
		assertString(Text.camelToPascal(null), "");
		assertString(Text.camelToPascal(""), "");
		assertString(Text.camelToPascal("_helloThereABC_"), "_HelloThereABC_");
		assertString(Text.camelToPascal("hello1there2ABC3_"), "Hello1There2ABC3_");
	}

	@Test
	public void testPascalToProperty() {
		assertString(Text.pascalToProperty(null), "");
		assertString(Text.pascalToProperty(""), "");
		assertString(Text.pascalToProperty("_HelloThereABC_"), "_hello.there.abc_");
		assertString(Text.pascalToProperty("hello1there2ABC3_"), "hello1there2.abc3_");
	}

	@Test
	public void testFirstToUpper() {
		assertString(Text.firstToUpper(null), "");
		assertString(Text.firstToUpper(""), "");
		assertString(Text.firstToUpper("hello"), "Hello");
		assertString(Text.firstToUpper("Hello"), "Hello");
		assertString(Text.firstToUpper("_"), "_");
	}

	@Test
	public void testFirstToLower() {
		assertString(Text.firstToLower(null), "");
		assertString(Text.firstToLower(""), "");
		assertString(Text.firstToLower("hello"), "hello");
		assertString(Text.firstToLower("Hello"), "hello");
		assertString(Text.firstToLower("_"), "_");
	}

	@Test
	public void testFirstLetterToUpper() {
		assertString(Text.firstLetterToUpper(null), "");
		assertString(Text.firstLetterToUpper(""), "");
		assertString(Text.firstLetterToUpper("abc"), "Abc");
		assertString(Text.firstLetterToUpper("  abc"), "  Abc");
		assertString(Text.firstLetterToUpper("_ABc"), "_ABc");
	}

	@Test
	public void testFirstLetterToLower() {
		assertString(Text.firstLetterToLower(null), "");
		assertString(Text.firstLetterToLower(""), "");
		assertString(Text.firstLetterToLower("ABC"), "aBC");
		assertString(Text.firstLetterToLower("  ABC"), "  aBC");
		assertString(Text.firstLetterToLower("_abC"), "_abC");
	}

	@Test
	public void testPascalToUnderscore() {
		assertString(Text.pascalToUnderscore(null), "");
		assertString(Text.pascalToUnderscore(""), "");
		assertString(Text.pascalToUnderscore("_HelloThereABC_"), "_HELLO_THERE_ABC_");
		assertString(Text.pascalToUnderscore("_Hello1There2ABC3_"), "_HELLO1_THERE2_ABC3_");
	}

	@Test
	public void testUpperToCapitalized() {
		assertString(Text.upperToCapitalized(null), "");
		assertString(Text.upperToCapitalized(""), "");
		assertString(Text.upperToCapitalized("_HELLO_THERE_ABC_"), "_Hello_There_Abc_");
		assertString(Text.upperToCapitalized("_HELLO1_THERE2_ABC3_"), "_Hello1_There2_Abc3_");
	}

	@Test
	public void testUnderscoreToPascal() {
		assertString(Text.underscoreToPascal(null), "");
		assertString(Text.underscoreToPascal(""), "");
		assertString(Text.underscoreToPascal("_HELLO_THERE_ABC_"), "_HelloThereAbc_");
		assertString(Text.underscoreToPascal("_HELLO1_THERE2_ABC3_"), "_Hello1There2Abc3_");
	}

	@Test
	public void testUnderscoreToCamel() {
		assertString(Text.underscoreToCamel(null), "");
		assertString(Text.underscoreToCamel(""), "");
		assertString(Text.underscoreToCamel("_HELLO_THERE_ABC_"), "_helloThereAbc_");
		assertString(Text.underscoreToCamel("_HELLO1_THERE2_ABC3_"), "_hello1There2Abc3_");
	}

	@Test
	public void testUnderscoreToProperty() {
		assertString(Text.underscoreToProperty(null), "");
		assertString(Text.underscoreToProperty(""), "");
		assertString(Text.underscoreToProperty("HELLO_THERE_ABC"), "hello.there.abc");
		assertString(Text.underscoreToProperty("HELLO1_THERE2_ABC3"), "hello1.there2.abc3");
	}

	@Test
	public void testPropertyToUnderscore() {
		assertString(Text.propertyToUnderscore(null), "");
		assertString(Text.propertyToUnderscore(""), "");
		assertString(Text.propertyToUnderscore("hello.there.abc"), "HELLO_THERE_ABC");
		assertString(Text.propertyToUnderscore("hello1.there2.abc3"), "HELLO1_THERE2_ABC3");
	}
}
