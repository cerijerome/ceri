package ceri.ci.admin;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;

public class ResponseBehavior {

	@Test
	public void shouldCreateFailureResponseFromException() {
		Exception e = new RuntimeException();
		Response response = Response.fail(e);
		assertEquals(response.exception, e);
		assertFalse(response.success);
	}

	@Test
	public void shouldCreateFailureResponseFromMessage() {
		Response response = Response.fail("fail");
		assertEquals(response.content, "fail");
		assertEquals(response.exception, (Exception) null);
		assertFalse(response.success);
	}

	@Test
	public void shouldCreateSuccessResponseFromMessage() {
		Response response = Response.success("success");
		assertEquals(response.content, "success");
		assertEquals(response.exception, (Exception) null);
		assertTrue(response.success);
	}

	@Test
	public void shouldCreateDefaultSuccessResponse() {
		Response response = Response.success();
		assertEquals(response.exception, (Exception) null);
		assertTrue(response.success);
	}

}
