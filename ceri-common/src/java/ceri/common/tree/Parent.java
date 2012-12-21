package ceri.common.tree;

import java.util.Collection;

public interface Parent<T> {
	T parent();
	Collection<T> children();
}
