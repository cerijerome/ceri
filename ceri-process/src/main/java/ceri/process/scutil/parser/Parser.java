package ceri.process.scutil.parser;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.text.StringUtil;

public class Parser {
	private static final Pattern TEXT_GROUP = Pattern.compile("(.*)<(\\w+)>\\s*\\{");
	private static final Pattern TEXT_VALUE = Pattern.compile("(.*):(.*)");
	private static final Pattern TEXT_VALUE_GROUP = Pattern.compile("(.*):\\s*<(\\w+)>\\s*\\{");
	private static final Pattern CLOSE_GROUP = Pattern.compile("\\s*}");
	private final ParserListener listener;
	
	public Parser(ParserListener listener) {
		this.listener = listener;
	}
	
	public Parser parse(String output) {
		for (String line : StringUtil.NEWLINE_REGEX.split(output)) {
			if (consume(line, TEXT_VALUE_GROUP, listener::textValueGroup)) continue;
			if (consume(line, TEXT_GROUP, listener::textGroup)) continue;
			if (consume(line, TEXT_VALUE, listener::textValue)) continue;
			if (CLOSE_GROUP.matcher(line).find()) listener.closeGroup();
			listener.text(line.trim());
		}
		return this;
	}
	
	private boolean consume(String line, Pattern regex, BiConsumer<String, String> consumer) {
		Matcher m = regex.matcher(line);
		if (!m.find()) return false;
		consumer.accept(m.group(1).trim(), m.group(2).trim());
		return true;
	}
	
}
