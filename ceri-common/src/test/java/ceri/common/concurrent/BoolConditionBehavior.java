package ceri.common.concurrent;

import org.junit.Test;
import ceri.common.test.Assert;

public class BoolConditionBehavior {

	@Test
	public void shouldReturnSignalState() {
		BoolCondition flag = BoolCondition.of();
		Assert.yes(flag.signal());
		Assert.no(flag.signal());
	}

	@Test
	public void shouldPeekWithoutResettingValue() throws InterruptedException {
		BoolCondition flag = BoolCondition.of();
		Assert.no(flag.awaitPeek(0));
		Assert.no(flag.awaitPeek(1));
		flag.signal();
		flag.awaitPeek();
		Assert.yes(flag.awaitPeek(0));
		Assert.yes(flag.awaitPeek(1));
	}

	@Test
	public void shouldSetWithoutSignal() {
		BoolCondition flag = BoolCondition.of();
		Assert.no(flag.isSet());
		Assert.yes(flag.set());
		Assert.no(flag.set());
		Assert.yes(flag.isSet());
	}

	@Test
	public void shouldSetAndClearValues() throws InterruptedException {
		BoolCondition flag = BoolCondition.of();
		Assert.no(flag.isSet());
		flag.signal();
		Assert.yes(flag.isSet());
		flag.await();
		Assert.no(flag.isSet());
		flag.signal();
		Assert.yes(flag.await(0));
		flag.clear();
		Assert.no(flag.await(0));
	}

}
