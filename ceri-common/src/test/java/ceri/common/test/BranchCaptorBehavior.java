package ceri.common.test;

import static ceri.common.test.TestUtil.assertCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.io.IoUtil;
import ceri.common.io.SystemIo;

public class BranchCaptorBehavior {

	@Test
	public void shouldFindNoMissingBranches() {
		BranchCaptor bc = new BranchCaptor();
		assertThat(bc.missing().size(), is(0));
		bc.add(true);
		assertThat(bc.missing().size(), is(1));
		bc.add(false);
		assertThat(bc.missing().size(), is(0));
	}

	@Test
	public void shouldIdentifyMissingBranches() {
		BranchCaptor bc = new BranchCaptor();
		bc.add(false, false, false);
		bc.add(false, false, true);
		bc.add(false, true, false);
		bc.add(true, false, true);
		bc.add(true, true, true);
		assertThat(bc.branches(), is(5));
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
