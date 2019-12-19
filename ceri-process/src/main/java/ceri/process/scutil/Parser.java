package ceri.process.scutil;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.collection.Node;
import ceri.common.collection.NodeBuilder;
import ceri.common.text.StringUtil;

public class Parser {
	private static final Pattern TEXT_GROUP = Pattern.compile("(.*)<(\\w+)>\\s*\\{");
	private static final Pattern TEXT_VALUE = Pattern.compile("(.*):(.*)");
	private static final Pattern TEXT_VALUE_GROUP = Pattern.compile("(.*):\\s*<(\\w+)>\\s*\\{");
	private static final Pattern CLOSE_GROUP = Pattern.compile("\\s*}");
	private final NodeBuilder<?> builder;

	public static Node<Void> parse(String output) {
		NodeBuilder<Void> builder = NodeBuilder.of();
		new Parser(builder).parseOutput(output);
		return builder.build();
	}

	private Parser(NodeBuilder<?> builder) {
		this.builder = builder;
	}

	private Parser parseOutput(String output) {
		for (String line : StringUtil.NEWLINE_REGEX.split(output)) {
			if (consume(line, TEXT_VALUE_GROUP, builder::startGroup)) continue;
			if (consume(line, TEXT_GROUP, builder::startGroup)) continue;
			if (consume(line, TEXT_VALUE, builder::value)) continue;
			if (CLOSE_GROUP.matcher(line).find()) builder.closeGroup();
			else {
				line = line.trim();
				if (!line.isEmpty()) builder.value(null, line);
			}
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
