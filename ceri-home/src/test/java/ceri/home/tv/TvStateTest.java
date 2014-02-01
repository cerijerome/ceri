package ceri.home.tv;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.security.SecureRandom;
import org.junit.Test;
import ceri.home.device.tv.TvState;

public class TvStateTest {

	@Test
	public void test() {
		SecureRandom rnd = new SecureRandom();
		for (int i = 0; i < 1000; i++) {
			TvState state = new TvState();
			state.setOn(rnd.nextBoolean());
			state.setChannel(rnd.nextInt(999));
			state.setLastChannel(rnd.nextInt(999));
			state.setInput(rnd.nextInt(9));
			state.setVolume(rnd.nextInt(99));
			state.setMuted(rnd.nextBoolean());
			state.setUseChannelAsLast(rnd.nextBoolean());
			String s1 = state.toString();
			String s2 = TvState.createFromString(s1).toString();
			assertThat(s1, is(s2));
		}
	}

}
