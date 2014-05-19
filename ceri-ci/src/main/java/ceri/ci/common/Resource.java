package ceri.ci.common;

/**
 * Simple class to track a named binary data resource.
 */
public class Resource {
	public final String name;
	public final byte[] data;
	
	public Resource(String name, byte[] data) {
		this.name = name;
		this.data = data;
	}
	
}
