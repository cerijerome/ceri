package ceri.common.text;

import java.util.List;
import org.junit.Test;
import ceri.common.collect.Immutable;
import ceri.common.test.Assert;

public class TextTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Text.class);
	}

	@Test
	public void testPrefixLines() {
		Assert.string(Text.prefixLines("xxx", "abc"), "xxxabc");
		Assert.string(Text.prefixLines("xxx", ""), "");
		Assert.string(Text.prefixLines("", ""), "");
		Assert.string(Text.prefixLines("", "abc"), "abc");
		Assert.string(Text.prefixLines("xxx", "a\r\nb"), "xxxa\r\nxxxb");
		Assert.string(Text.prefixLines("xxx", "a\r\n"), "xxxa\r\nxxx");
		Assert.string(Text.prefixLines("xxx", "\r\n"), "xxx\r\nxxx");
		Assert.string(Text.prefixLines("\t", "\n\r"), "\t\n\t\r\t");
		Assert.string(Text.prefixLines("\t", "\n\r\n\t"), "\t\n\t\r\n\t\t");
		Assert.string(Text.prefixLines("x", "a\nb\r\nc\rd"), "xa\nxb\r\nxc\rxd");
	}

	@Test
	public void testSpacesToTabs() {
		Assert.string(Text.lineSpacesToTabs(1, null), "");
		Assert.string(Text.spacesToTabs(0, "    "), "    ");
		Assert.string(Text.spacesToTabs(4, ""), "");
		Assert.string(Text.spacesToTabs(4, "a"), "a");
		Assert.string(Text.spacesToTabs(4, "ab"), "ab");
		Assert.string(Text.spacesToTabs(4, "abc"), "abc");
		Assert.string(Text.spacesToTabs(4, "abcd"), "abcd");
		Assert.string(Text.spacesToTabs(4, "a "), "a ");
		Assert.string(Text.spacesToTabs(4, "ab "), "ab ");
		Assert.string(Text.spacesToTabs(4, "abc "), "abc ");
		Assert.string(Text.spacesToTabs(4, "abcd "), "abcd ");
		Assert.string(Text.spacesToTabs(4, "a  "), "a  ");
		Assert.string(Text.spacesToTabs(4, "ab  "), "ab\t");
		Assert.string(Text.spacesToTabs(4, "abc  "), "abc  ");
		Assert.string(Text.spacesToTabs(4, "abcd  "), "abcd  ");
		Assert.string(Text.spacesToTabs(2, "a   "), "a \t");
		Assert.string(Text.spacesToTabs(2, "ab   "), "ab\t ");
		Assert.string(Text.spacesToTabs(2, "abc   "), "abc \t");
		Assert.string(Text.spacesToTabs(2, "abcd   "), "abcd\t ");
		Assert.string(Text.spacesToTabs(4, "       ab c   e "), "\t   ab c   e ");
		Assert.string(Text.spacesToTabs(4, "abcde   fgh      i"), "abcde\tfgh \t i");
		Assert.string(Text.spacesToTabs(4, "a\t b \t c"), "a\t b\t c");
	}

	@Test
	public void testTabsToSpaces() {
		Assert.string(Text.lineTabsToSpaces(1, null), "");
		Assert.string(Text.tabsToSpaces(0, "    "), "    ");
		Assert.string(Text.tabsToSpaces(4, ""), "");
		Assert.string(Text.tabsToSpaces(4, "a"), "a");
		Assert.string(Text.tabsToSpaces(4, "ab"), "ab");
		Assert.string(Text.tabsToSpaces(4, "abc"), "abc");
		Assert.string(Text.tabsToSpaces(4, "abcd"), "abcd");
		Assert.string(Text.tabsToSpaces(4, "\t"), "    ");
		Assert.string(Text.tabsToSpaces(4, "a\t"), "a   ");
		Assert.string(Text.tabsToSpaces(4, "ab\t"), "ab  ");
		Assert.string(Text.tabsToSpaces(4, "abc\t"), "abc ");
		Assert.string(Text.tabsToSpaces(4, "abcd\t"), "abcd    ");
		Assert.string(Text.tabsToSpaces(2, "\t\t"), "    ");
		Assert.string(Text.tabsToSpaces(2, "\t \t"), "    ");
		Assert.string(Text.tabsToSpaces(2, " \t \t"), "    ");
		Assert.string(Text.tabsToSpaces(2, "\t\t "), "     ");
		Assert.string(Text.tabsToSpaces(4, "abc\t\tdef  \tg"), "abc     def     g");
		Assert.string(Text.tabsToSpaces(4, "abcde\tfgh\t\ti"), "abcde   fgh     i");
	}

	@Test
	public void testAddLineNumbers() {
		Assert.lines(Text.addLineNumbers((String) null));
		Assert.lines(Text.addLineNumbers((String[]) null));
		Assert.lines(Text.addLineNumbers((List<String>) null));
		Assert.lines(Text.addLineNumbers((List<String>) null, 1));
		Assert.lines(Text.addLineNumbers("\n\nabc\n\n\n"), "1: ", "2: ", "3: abc");
		Assert.lines(Text.addLineNumbers(new String[] { null, "", "abc", "" }), "1: ", "2: ",
			"3: abc", "4: ");
		Assert.lines(Text.addLineNumbers(Immutable.listOf(null, "", "abc", ""), 3), "3: ", "4: ",
			"5: abc", "6: ");
	}

	@Test
	public void testMultilineJavadoc() {
		Assert.string(Text.multilineJavadoc(null), "");
		Assert.string(Text.multilineJavadoc(""), "");
		Assert.lines(Text.multilineJavadoc("a"), "/**", " * a", " */");
		Assert.lines(Text.multilineJavadoc("a" + Strings.EOL + "b"), "/**", " * a", " * b", " */");
	}

	@Test
	public void testMultilineComment() {
		Assert.string(Text.multilineComment(null), "");
		Assert.string(Text.multilineComment(""), "");
		Assert.lines(Text.multilineComment("a"), "/*", " * a", " */");
		Assert.lines(Text.multilineComment("a" + Strings.EOL + "b"), "/*", " * a", " * b", " */");
	}

	@Test
	public void testToWordsWithSpaces() {
		Assert.ordered(Text.toWords(null));
		Assert.ordered(Text.toWords(""));
		Assert.ordered(Text.toWords("  "));
		Assert.ordered(Text.toWords("a "), "a");
		Assert.ordered(Text.toWords(" a"), "a");
		Assert.ordered(Text.toWords("A "), "A");
		Assert.ordered(Text.toWords(" A"), "A");
		Assert.ordered(Text.toWords("a b"), "a", "b");
		Assert.ordered(Text.toWords(" ab"), "ab");
		Assert.ordered(Text.toWords("A b"), "A", "b");
		Assert.ordered(Text.toWords(" Ab"), "Ab");
		Assert.ordered(Text.toWords("a B"), "a", "B");
		Assert.ordered(Text.toWords(" aB"), "a", "B");
		Assert.ordered(Text.toWords("A B"), "A", "B");
		Assert.ordered(Text.toWords(" AB"), "AB");
	}

	@Test
	public void testToWordsWithUnderscores() {
		Assert.ordered(Text.toWords("_"));
		Assert.ordered(Text.toWords("__"));
		Assert.ordered(Text.toWords("__ _ "));
		Assert.ordered(Text.toWords("a_"), "a");
		Assert.ordered(Text.toWords("_a"), "a");
		Assert.ordered(Text.toWords("A_"), "A");
		Assert.ordered(Text.toWords("_A"), "A");
		Assert.ordered(Text.toWords("a_b"), "a", "b");
		Assert.ordered(Text.toWords("_ab"), "ab");
		Assert.ordered(Text.toWords("A_b"), "A", "b");
		Assert.ordered(Text.toWords("_Ab"), "Ab");
		Assert.ordered(Text.toWords("a_B"), "a", "B");
		Assert.ordered(Text.toWords("_aB"), "a", "B");
		Assert.ordered(Text.toWords("A_B"), "A", "B");
		Assert.ordered(Text.toWords("_AB"), "AB");
	}

	@Test
	public void testToWordsWithLettersAndNumbers() {
		Assert.ordered(Text.toWords("a1"), "a", "1");
		Assert.ordered(Text.toWords("1a"), "1a");
		Assert.ordered(Text.toWords("A1"), "A1");
		Assert.ordered(Text.toWords("1A"), "1", "A");
		Assert.ordered(Text.toWords("a1b"), "a", "1b");
		Assert.ordered(Text.toWords("1ab"), "1ab");
		Assert.ordered(Text.toWords("A1b"), "A1b");
		Assert.ordered(Text.toWords("1Ab"), "1", "Ab");
		Assert.ordered(Text.toWords("a1B"), "a", "1", "B");
		Assert.ordered(Text.toWords("1aB"), "1a", "B");
		Assert.ordered(Text.toWords("A1B"), "A1", "B");
		Assert.ordered(Text.toWords("1AB"), "1", "AB");
	}

	@Test
	public void testToWordsWithLetters() {
		Assert.ordered(Text.toWords("a"), "a");
		Assert.ordered(Text.toWords("A"), "A");
		Assert.ordered(Text.toWords("ab"), "ab");
		Assert.ordered(Text.toWords("Ab"), "Ab");
		Assert.ordered(Text.toWords("aB"), "a", "B");
		Assert.ordered(Text.toWords("AB"), "AB");
		Assert.ordered(Text.toWords("abc"), "abc");
		Assert.ordered(Text.toWords("Abc"), "Abc");
		Assert.ordered(Text.toWords("aBc"), "a", "Bc");
		Assert.ordered(Text.toWords("ABc"), "A", "Bc");
		Assert.ordered(Text.toWords("abC"), "ab", "C");
		Assert.ordered(Text.toWords("AbC"), "Ab", "C");
		Assert.ordered(Text.toWords("aBC"), "a", "BC");
		Assert.ordered(Text.toWords("ABC"), "ABC");
		Assert.ordered(Text.toWords("abcd"), "abcd");
		Assert.ordered(Text.toWords("Abcd"), "Abcd");
		Assert.ordered(Text.toWords("aBcd"), "a", "Bcd");
		Assert.ordered(Text.toWords("ABcd"), "A", "Bcd");
		Assert.ordered(Text.toWords("abCd"), "ab", "Cd");
		Assert.ordered(Text.toWords("AbCd"), "Ab", "Cd");
		Assert.ordered(Text.toWords("aBCd"), "a", "B", "Cd");
		Assert.ordered(Text.toWords("ABCd"), "AB", "Cd");
		Assert.ordered(Text.toWords("abcD"), "abc", "D");
		Assert.ordered(Text.toWords("AbcD"), "Abc", "D");
		Assert.ordered(Text.toWords("aBcD"), "a", "Bc", "D");
		Assert.ordered(Text.toWords("ABcD"), "A", "Bc", "D");
		Assert.ordered(Text.toWords("abCD"), "ab", "CD");
		Assert.ordered(Text.toWords("AbCD"), "Ab", "CD");
		Assert.ordered(Text.toWords("aBCD"), "a", "BCD");
		Assert.ordered(Text.toWords("ABCD"), "ABCD");
	}

	@Test
	public void testToPhrase() {
		Assert.string(Text.toPhrase(null), "");
		Assert.string(Text.toPhrase(""), "");
		Assert.string(Text.toPhrase("_"), "");

		Assert.string(Text.toPhrase("abcd"), "abcd");
		Assert.string(Text.toPhrase("Abcd"), "abcd");
		Assert.string(Text.toPhrase("aBcd"), "a bcd");
		Assert.string(Text.toPhrase("ABcd"), "a bcd");
		Assert.string(Text.toPhrase("abCd"), "ab cd");
		Assert.string(Text.toPhrase("AbCd"), "ab cd");
		Assert.string(Text.toPhrase("aBCd"), "a b cd");
		Assert.string(Text.toPhrase("ABCd"), "AB cd");

		Assert.string(Text.toPhrase("abcD"), "abc d");
		Assert.string(Text.toPhrase("AbcD"), "abc d");
		Assert.string(Text.toPhrase("aBcD"), "a bc d");
		Assert.string(Text.toPhrase("ABcD"), "a bc d");
		Assert.string(Text.toPhrase("abCD"), "ab CD");
		Assert.string(Text.toPhrase("AbCD"), "ab CD");
		Assert.string(Text.toPhrase("aBCD"), "a BCD");
		Assert.string(Text.toPhrase("ABCD"), "ABCD");

		Assert.string(Text.toPhrase("AbCDEf"), "ab CD ef");
		Assert.string(Text.toPhrase("_helloThere2ABC3_"), "hello there 2 ABC3");
		Assert.string(Text.toPhrase("testPart1a"), "test part 1a");
	}

	@Test
	public void testToCapitalizedPhrase() {
		Assert.string(Text.toCapitalizedPhrase(null), "");
		Assert.string(Text.toCapitalizedPhrase(""), "");
		Assert.string(Text.toCapitalizedPhrase("_"), "");

		Assert.string(Text.toCapitalizedPhrase("abcd"), "Abcd");
		Assert.string(Text.toCapitalizedPhrase("Abcd"), "Abcd");
		Assert.string(Text.toCapitalizedPhrase("aBcd"), "A Bcd");
		Assert.string(Text.toCapitalizedPhrase("ABcd"), "A Bcd");
		Assert.string(Text.toCapitalizedPhrase("abCd"), "Ab Cd");
		Assert.string(Text.toCapitalizedPhrase("AbCd"), "Ab Cd");
		Assert.string(Text.toCapitalizedPhrase("aBCd"), "A B Cd");
		Assert.string(Text.toCapitalizedPhrase("ABCd"), "AB Cd");

		Assert.string(Text.toCapitalizedPhrase("abcD"), "Abc D");
		Assert.string(Text.toCapitalizedPhrase("AbcD"), "Abc D");
		Assert.string(Text.toCapitalizedPhrase("aBcD"), "A Bc D");
		Assert.string(Text.toCapitalizedPhrase("ABcD"), "A Bc D");
		Assert.string(Text.toCapitalizedPhrase("abCD"), "Ab CD");
		Assert.string(Text.toCapitalizedPhrase("AbCD"), "Ab CD");
		Assert.string(Text.toCapitalizedPhrase("aBCD"), "A BCD");
		Assert.string(Text.toCapitalizedPhrase("ABCD"), "ABCD");

		Assert.string(Text.toCapitalizedPhrase("AbCDEf"), "Ab CD Ef");
		Assert.string(Text.toCapitalizedPhrase("_helloThere2ABC3_"), "Hello There 2 ABC3");
		Assert.string(Text.toCapitalizedPhrase("testPart1a"), "Test Part 1a");
	}

	@Test
	public void testCamelToHyphenated() {
		Assert.string(Text.camelToHyphenated(null), "");
		Assert.string(Text.camelToHyphenated(""), "");
		Assert.string(Text.camelToHyphenated("_helloThereABC_"), "_hello-there-abc_");
		Assert.string(Text.camelToHyphenated("hello1there2ABC3_"), "hello1there2-abc3_");
	}

	@Test
	public void testCamelToPascal() {
		Assert.string(Text.camelToPascal(null), "");
		Assert.string(Text.camelToPascal(""), "");
		Assert.string(Text.camelToPascal("_helloThereABC_"), "_HelloThereABC_");
		Assert.string(Text.camelToPascal("hello1there2ABC3_"), "Hello1There2ABC3_");
	}

	@Test
	public void testPascalToProperty() {
		Assert.string(Text.pascalToProperty(null), "");
		Assert.string(Text.pascalToProperty(""), "");
		Assert.string(Text.pascalToProperty("_HelloThereABC_"), "_hello.there.abc_");
		Assert.string(Text.pascalToProperty("hello1there2ABC3_"), "hello1there2.abc3_");
	}

	@Test
	public void testFirstToUpper() {
		Assert.string(Text.firstToUpper(null), "");
		Assert.string(Text.firstToUpper(""), "");
		Assert.string(Text.firstToUpper("hello"), "Hello");
		Assert.string(Text.firstToUpper("Hello"), "Hello");
		Assert.string(Text.firstToUpper("_"), "_");
	}

	@Test
	public void testFirstToLower() {
		Assert.string(Text.firstToLower(null), "");
		Assert.string(Text.firstToLower(""), "");
		Assert.string(Text.firstToLower("hello"), "hello");
		Assert.string(Text.firstToLower("Hello"), "hello");
		Assert.string(Text.firstToLower("_"), "_");
	}

	@Test
	public void testFirstLetterToUpper() {
		Assert.string(Text.firstLetterToUpper(null), "");
		Assert.string(Text.firstLetterToUpper(""), "");
		Assert.string(Text.firstLetterToUpper("abc"), "Abc");
		Assert.string(Text.firstLetterToUpper("  abc"), "  Abc");
		Assert.string(Text.firstLetterToUpper("_ABc"), "_ABc");
	}

	@Test
	public void testFirstLetterToLower() {
		Assert.string(Text.firstLetterToLower(null), "");
		Assert.string(Text.firstLetterToLower(""), "");
		Assert.string(Text.firstLetterToLower("ABC"), "aBC");
		Assert.string(Text.firstLetterToLower("  ABC"), "  aBC");
		Assert.string(Text.firstLetterToLower("_abC"), "_abC");
	}

	@Test
	public void testPascalToUnderscore() {
		Assert.string(Text.pascalToUnderscore(null), "");
		Assert.string(Text.pascalToUnderscore(""), "");
		Assert.string(Text.pascalToUnderscore("_HelloThereABC_"), "_HELLO_THERE_ABC_");
		Assert.string(Text.pascalToUnderscore("_Hello1There2ABC3_"), "_HELLO1_THERE2_ABC3_");
	}

	@Test
	public void testUpperToCapitalized() {
		Assert.string(Text.upperToCapitalized(null), "");
		Assert.string(Text.upperToCapitalized(""), "");
		Assert.string(Text.upperToCapitalized("_HELLO_THERE_ABC_"), "_Hello_There_Abc_");
		Assert.string(Text.upperToCapitalized("_HELLO1_THERE2_ABC3_"), "_Hello1_There2_Abc3_");
	}

	@Test
	public void testUnderscoreToPascal() {
		Assert.string(Text.underscoreToPascal(null), "");
		Assert.string(Text.underscoreToPascal(""), "");
		Assert.string(Text.underscoreToPascal("_HELLO_THERE_ABC_"), "_HelloThereAbc_");
		Assert.string(Text.underscoreToPascal("_HELLO1_THERE2_ABC3_"), "_Hello1There2Abc3_");
	}

	@Test
	public void testUnderscoreToCamel() {
		Assert.string(Text.underscoreToCamel(null), "");
		Assert.string(Text.underscoreToCamel(""), "");
		Assert.string(Text.underscoreToCamel("_HELLO_THERE_ABC_"), "_helloThereAbc_");
		Assert.string(Text.underscoreToCamel("_HELLO1_THERE2_ABC3_"), "_hello1There2Abc3_");
	}

	@Test
	public void testUnderscoreToProperty() {
		Assert.string(Text.underscoreToProperty(null), "");
		Assert.string(Text.underscoreToProperty(""), "");
		Assert.string(Text.underscoreToProperty("HELLO_THERE_ABC"), "hello.there.abc");
		Assert.string(Text.underscoreToProperty("HELLO1_THERE2_ABC3"), "hello1.there2.abc3");
	}

	@Test
	public void testPropertyToUnderscore() {
		Assert.string(Text.propertyToUnderscore(null), "");
		Assert.string(Text.propertyToUnderscore(""), "");
		Assert.string(Text.propertyToUnderscore("hello.there.abc"), "HELLO_THERE_ABC");
		Assert.string(Text.propertyToUnderscore("hello1.there2.abc3"), "HELLO1_THERE2_ABC3");
	}
}
