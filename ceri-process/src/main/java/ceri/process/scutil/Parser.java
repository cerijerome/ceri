package ceri.process.scutil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.collection.Node;
import ceri.common.function.Functions;
import ceri.common.text.Regex;

public class Parser {
	private static final Pattern TEXT_GROUP = Pattern.compile("(.*)<(\\w+)>\\s*\\{");
	private static final Pattern TEXT_VALUE = Pattern.compile("(.*):(.*)");
	private static final Pattern TEXT_VALUE_GROUP = Pattern.compile("(.*):\\s*<(\\w+)>\\s*\\{");
	private static final Pattern CLOSE_GROUP = Pattern.compile("\\s*}");
	private final Node.Tree<?> tree;

	public static Node<Void> parse(String output) {
		Node.Tree<Void> tree = Node.tree();
		new Parser(tree).parseOutput(output);
		return tree.build();
	}

	private Parser(Node.Tree<?> tree) {
		this.tree = tree;
	}

	private Parser parseOutput(String output) {
		for (String line : Regex.Split.LINE.array(output)) {
			if (consume(line, TEXT_VALUE_GROUP, tree::startGroup)) continue;
			if (consume(line, TEXT_GROUP, tree::startGroup)) continue;
			if (consume(line, TEXT_VALUE, tree::value)) continue;
			if (CLOSE_GROUP.matcher(line).find()) tree.closeGroup();
			else {
				line = line.trim();
				if (!line.isEmpty()) tree.value(null, line);
			}
		}
		return this;
	}

	private boolean consume(String line, Pattern regex,
		Functions.BiConsumer<String, String> consumer) {
		Matcher m = regex.matcher(line);
		if (!m.find()) return false;
		consumer.accept(m.group(1).trim(), m.group(2).trim());
		return true;
	}
}
