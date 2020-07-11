package ceri.common.event;

import static ceri.common.test.TestUtil.assertIterable;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ListenableBehavior {

	@Test
	public void shouldProvideIndirectAccess() {
		List<String> list = new ArrayList<>();
		Listeners<String> listeners = new Listeners<>();
		listeners.indirect().listeners().listen(list::add);
		listeners.accept("test");
		assertIterable(list, "test");
	}

}
