package ceri.process.scutil.parser;

import java.util.Deque;
import java.util.LinkedList;

class GroupParser implements ParserListener {
	private final Group.Builder root;
	private final Deque<Group.Builder> stack = new LinkedList<>();
	
	GroupParser() {
		root = Group.builder(null, null);
		stack.add(root);
	}

	public Group build() {
		return root.build();
	}
	
	@Override
	public void textGroup(String name, String type) {
		Group.Builder builder = Group.builder(name, type);
		stack.getLast().add(builder::build);
		stack.add(builder);
	}

	@Override
	public void textValueGroup(String name, String type) {
		textGroup(name, type);
	}

	@Override
	public void textValue(String name, String value) {
		stack.getLast().add(name, value);
	}
	
	@Override
	public void text(String line) {
		stack.getLast().add(line);
	}
	
	@Override
	public void closeGroup() {
		stack.removeLast();
	}
	
}
