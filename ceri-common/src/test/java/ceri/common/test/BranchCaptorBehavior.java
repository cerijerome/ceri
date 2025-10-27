package ceri.common.test;

import org.junit.Test;
import ceri.common.io.IoStream;
import ceri.common.io.SystemIo;

public class BranchCaptorBehavior {

	@Test
	public void shouldFindNoMissingBranches() {
		var bc = new BranchCaptor();
		Assert.equal(bc.missing().size(), 0);
		bc.add(true);
		Assert.equal(bc.missing().size(), 1);
		bc.add(false);
		Assert.equal(bc.missing().size(), 0);
	}

	@Test
	public void shouldIdentifyMissingBranches() {
		var bc = new BranchCaptor();
		bc.add(false, false, false);
		bc.add(false, false, true);
		bc.add(false, true, false);
		bc.add(true, false, true);
		bc.add(true, true, true);
		Assert.equal(bc.branches(), 5);
		Assert.unordered(bc.missing(), //
			BranchCaptor.string(false, true, true), //
			BranchCaptor.string(true, false, false), //
			BranchCaptor.string(true, true, false));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReport() {
		try (var stdio = SystemIo.of()) {
			stdio.out(IoStream.nullPrint());
			BranchCaptor bc = new BranchCaptor();
			bc.add(true, true);
			bc.add(true, false);
			bc.add(false, true);
			bc.report();
			bc.add(false, false);
			bc.report();
		}
	}
}
