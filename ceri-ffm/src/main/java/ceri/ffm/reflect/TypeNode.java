package ceri.ffm.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;
import ceri.common.reflect.Annotations;
import ceri.common.reflect.Generics;
import ceri.ffm.core.Native;

/**
 * A generic type node to facilitate traversal and refinement resolution.
 */
public class TypeNode {
	public static final TypeNode NULL = new TypeNode(Annotations.Node.NULL, null);
	public static final TypeNode VOID = new TypeNode(Annotations.node(Generics.Typed.VOID), null);
	private final Annotations.Node node;
	private final Refine.Context context;
	private volatile Native.Spec spec = null; // cached

	public static TypeNode of(Generics.Token<?> token) {
		return token == null ? NULL : of(token.node());
	}

	public static TypeNode of(Field field) {
		return of(Annotations.node(field));
	}

	public static TypeNode ofReturn(Method method) {
		return of(Annotations.nodeReturn(method));
	}

	public static TypeNode of(Parameter param) {
		return of(Annotations.node(param));
	}

	public static TypeNode of(Class<?> cls, Refine.Context context) {
		if (cls == null) return NULL;
		return new TypeNode(Annotations.node(cls), context);
	}

	private static TypeNode of(Annotations.Node node) {
		if (Annotations.Node.isNull(node)) return NULL;
		return new TypeNode(node, null);
	}

	private TypeNode(Annotations.Node node, Refine.Context context) {
		this.node = node;
		this.context = (context != null) ? context : Refine.context(node.element());
	}

	/**
	 * Returns the node with context replaced.
	 */
	public TypeNode with(Refine.Context context) {
		if (context == null || context().equals(context)) return this;
		return new TypeNode(node, context);
	}

	/**
	 * Returns true if this type is supported.
	 */
	public boolean isValid() {
		return spec().isValid();
	}

	/**
	 * Returns true if the generic type can be treated as void.
	 */
	public boolean isVoid() {
		return Native.isVoid(typed());
	}

	/**
	 * Returns true if the node represents an array type.
	 */
	public boolean isArray() {
		return typed().isArray();
	}

	/**
	 * Returns the wrapped annotated node.
	 */
	public Annotations.Node node() {
		return node;
	}

	/**
	 * Returns the generic type.
	 */
	public Generics.Typed typed() {
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
	public TypeNode components() {
		return create(node.components());
	}

	/**
	 * Navigate to the array component if available.
	 */
	public TypeNode component() {
		return create(node.component());
	}

	/**
	 * Add a class to the path.
	 */
	public TypeNode sub(Class<?> cls) {
		return create(node.sub(cls));
	}

	/**
	 * Returns the refinement context for the current type and its parents.
	 */
	public Refine.Context context() {
		return context;
	}

	/**
	 * Returns the type breakdown for the current type.
	 */
	public Native.Spec spec() {
		var spec = this.spec;
		if (spec == null) {
			spec = Native.Spec.of(typed());
			this.spec = spec;
		}
		return spec;
	}

	@Override
	public int hashCode() {
		return Objects.hash(node(), context());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return (obj instanceof TypeNode t) && Objects.equals(node(), t.node())
			&& Objects.equals(context(), t.context());
	}

	@Override
	public String toString() {
		return node.toString();
	}

	// support

	private TypeNode create(Annotations.Node node) {
		return Annotations.Node.isNull(node) || this.node == node ? this : of(node);
	}
}
