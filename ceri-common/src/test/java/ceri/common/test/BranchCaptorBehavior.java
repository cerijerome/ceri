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
		bc.commit(true);
		assertThat(bc.missing().size(), is(1));
		bc.commit(false);
		assertThat(bc.missing().size(), is(0));
	}
	
	@Test
	public void shouldIdentifyMissingBranches() {
		BranchCaptor bc = new BranchCaptor();
		bc.commit(false, false, false);
		bc.commit(false, false, true);
		bc.commit(false, true, false);
		bc.commit(true, false, true);
		bc.commit(true, true, true);
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
			bc.commit(true, true);
			bc.commit(true, false);
			bc.commit(false, true);
			bc.report();
			bc.commit(false, false);
			bc.report();
		}
	}

}
