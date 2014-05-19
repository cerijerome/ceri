package ceri.ci.admin;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ResponseBehavior {

	@Test
	public void shouldCreateFailureResponseFromException() {
		Exception e = new RuntimeException();
		Response response = Response.fail(e);
		assertThat(response.exception, is(e));
		assertThat(response.success, is(false));
	}

	@Test
	public void shouldCreateFailureResponseFromMessage() {
		Response response = Response.fail("fail");
		assertThat(response.content, is("fail"));
		assertThat(response.exception, is((Exception) null));
		assertThat(response.success, is(false));
	}

	@Test
	public void shouldCreateSuccessResponseFromMessage() {
		Response response = Response.success("success");
		assertThat(response.content, is("success"));
		assertThat(response.exception, is((Exception) null));
		assertThat(response.success, is(true));
	}

	@Test
	public void shouldCreateDefaultSuccessResponse() {
		Response response = Response.success();
		assertThat(response.exception, is((Exception) null));
		assertThat(response.success, is(true));
	}

}
