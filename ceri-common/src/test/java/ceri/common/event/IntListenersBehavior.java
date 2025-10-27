package ceri.common.event;

import org.junit.Test;
import ceri.common.function.Functions;
import ceri.common.test.Assert;

public class IntListenersBehavior {

	@Test
	public void shouldAddAndRemoveIntListeners() {
		int[] count = new int[1];
		Functions.IntConsumer l0 = i -> count[0] += i;
		Functions.IntConsumer l1 = i -> count[0] += (i * 100);
		IntListeners ls = IntListeners.of();
		Assert.yes(ls.isEmpty());
		ls.listen(l0);
		ls.listen(l0);
		ls.listen(l1);
		Assert.equal(ls.size(), 3);
		ls.accept(1);
		Assert.equal(count[0], 102);
		ls.unlisten(l0);
		ls.accept(2);
		Assert.equal(count[0], 102 + 202);
		ls.unlisten(l1);
		ls.accept(3);
		Assert.equal(count[0], 102 + 202 + 3);
		ls.unlisten(l0);
		ls.accept(4);
		Assert.equal(count[0], 102 + 202 + 3);
	}

	@Test
	public void shouldDuplicateIntListeners() {
		IntListeners ls = IntListeners.of();
		Functions.IntConsumer l0 = _ -> {};
		Functions.IntConsumer l1 = _ -> {};
		Assert.yes(ls.listen(l0));
		Assert.yes(ls.listen(l0));
		Assert.yes(ls.listen(l1));
		Assert.yes(ls.listen(l1));
		Assert.yes(ls.listen(l0));
		Assert.yes(ls.unlisten(l0));
		Assert.yes(ls.unlisten(l0));
		Assert.yes(ls.unlisten(l0));
		Assert.no(ls.unlisten(l0));
		Assert.yes(ls.unlisten(l1));
		Assert.yes(ls.unlisten(l1));
		Assert.no(ls.unlisten(l1));
	}
}
