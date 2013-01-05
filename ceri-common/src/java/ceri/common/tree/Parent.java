package ceri.common.tree;

import java.util.Collection;

/**
 * Interface for a node in a hierarchy. Has children and its own parent.
 */
public interface Parent<T> {
	T parent();
	Collection<T> children();
}
