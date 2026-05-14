package ceri.ffm.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import ceri.common.reflect.Annotations;
import ceri.common.reflect.Generics;
import ceri.ffm.core.Native;

/**
 * A generic type node to facilitate traversal and annotation resolution.
 */
public class TypeNode {
	public static final TypeNode NULL = new TypeNode(Annotations.Node.NULL);
	private final Annotations.Node node;
	private volatile Native.Kind.Spec spec = null;

	public static TypeNode of(Field field) {
		return of(Annotations.node(field));
	}
	
	public static TypeNode ofReturn(Method method) {
		return of(Annotations.nodeReturn(method));
	}
	
	public static TypeNode of(Parameter param) {
		return of(Annotations.node(param));
	}
	
	private static TypeNode of(Annotations.Node node) {
		if (Annotations.Node.isNull(node)) return NULL;
		return new TypeNode(node);
	}

	private TypeNode(Annotations.Node node) {
		this.node = node;
	}

	/**
	 * Returns true if this type is supported.
	 */
	public boolean isValid() {
		return spec().isValid();
	}
	
	/**
	 * Returns the generic type.
	 */
	public Generics.Typed types() {
		return node.typed();
	}

	/**
	 * Navigate to the first generic type if available.
	 */
	public TypeNode type() {
		return create(node.type(0));
	}

	/**
	 * Navigate to the (multi-)array component if available.
	 */
	public TypeNode component() {
		return create(node.components());
	}

	/**
	 * Returns the refinement context for the current type and its parents.
	 */
	public Refine.Context context() {
		return Refine.context(node.element());
	}

	/**
	 * Returns the type breakdown for the current type.
	 */
	public Native.Kind.Spec spec() {
		var spec = this.spec;
		if (spec == null) {
			spec = Native.Kind.spec(node.typed());
			this.spec = spec;
		}
		return spec;
	}

	// support

	private TypeNode create(Annotations.Node node) {
		return Annotations.Node.isNull(node) || this.node == node ? this : of(node);
	}
}
