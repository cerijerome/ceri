package ceri.common.test;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.io.IoUtil;
import ceri.common.io.SystemIo;

public class BranchCaptorBehavior {

	@Test
	public void shouldFindNoMissingBranches() {
		BranchCaptor bc = new BranchCaptor();
		assertEquals(bc.missing().size(), 0);
		bc.add(true);
		assertEquals(bc.missing().size(), 1);
		bc.add(false);
		assertEquals(bc.missing().size(), 0);
	}

	@Test
	public void shouldIdentifyMissingBranches() {
		BranchCaptor bc = new BranchCaptor();
		bc.add(false, false, false);
		bc.add(false, false, true);
		bc.add(false, true, false);
		bc.add(true, false, true);
		bc.add(true, true, true);
		assertEquals(bc.branches(), 5);
		assertCollection(bc.missing(), //
			BranchCaptor.string(false, true, true), //
			BranchCaptor.string(true, false, false), //
			BranchCaptor.string(true, true, false));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReport() {
		try (SystemIo stdio = SystemIo.of()) {
			stdio.out(IoUtil.nullPrintStream());
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
