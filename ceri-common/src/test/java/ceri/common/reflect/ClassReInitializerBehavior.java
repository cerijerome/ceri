package ceri.common.reflect;

import org.junit.Before;
import org.junit.Test;
import ceri.common.test.Captor;
import ceri.common.text.StringUtil;

public class ClassReInitializerBehavior {
	public static Captor<String> captor = null;

	public static class I {
		public static int i = 1;
	}

	public static class S {
		static {
			ClassReInitializerBehavior.captor.accept(StringUtil.repeat('s', I.i));
		}
	}

	@Before
	public void before() {
		captor = Captor.of();
	}

	@Test
	public void shouldReinitClass() {
		I.i = 3;
		ClassReInitializer.of(S.class).reinit();
		captor.verify("sss");
	}

	@Test
	public void shouldReinitClassAndSupporters() {
		I.i = 3;
		ClassReInitializer.builder().init(S.class).reload(I.class).build().reinit();
		captor.verify("s");
	}
}
