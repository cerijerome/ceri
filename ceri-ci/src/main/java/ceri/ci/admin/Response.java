package ceri.ci.admin;

public class Response {
	public final Exception exception;
	public final String content;
	public final boolean success;
	
	private Response(Exception exception, String content, boolean success) {
		this.exception = exception;
		this.content = content;
		this.success = success;
	}
	
	public static Response fail(Exception exception) {
		return new Response(exception, null, false);
	}
	
	public static Response fail(String message) {
		return new Response(null, message, false);
	}
	
	public static Response success(String content) {
		return new Response(null, content, true);
	}
	
	public static Response success() {
		return new Response(null, null, true);
	}
	
}
