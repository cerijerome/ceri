package ceri.common.array;

import org.junit.Test;
import ceri.common.data.IntProvider;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class DimensionsBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = Dimensions.of(3, 2);
		var eq0 = Dimensions.of(3, 2);
		var ne0 = Dimensions.of(3, 2, 1);
		var ne1 = Dimensions.of(3, 3);
		var ne2 = Dimensions.of(2, 2);
		Testing.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldCreateFromArrayInstance() {
		assertDims(Dimensions.from(null));
		assertDims(Dimensions.from(1));
		assertDims(Dimensions.from(new int[3][1][2]), 3, 1, 2);
		assertDims(Dimensions.from(new int[][][] { { {} }, {}, { { 0, 1 } } }), 3, 1, 2);
	}

	@Test
	public void shouldBuildDimensions() {
		assertDims(Dimensions.builder().add(Dimensions.of(3, 2)).add(0, 1).build(), 3, 2, 0, 1);
	}

	@Test
	public void shouldCreateArray() {
		Assert.equal(Dimensions.of(2, 1).create(null), null);
		Assert.array((int[][]) Dimensions.of(2, 1).create(int.class), new int[2][1]);
		Assert.array((Integer[][]) Dimensions.of(2, 1).create(Integer.class), new Integer[2][1]);
	}

	@Test
	public void shouldDetermineIfEmpty() {
		Assert.equal(Dimensions.NONE.isEmpty(), true);
		Assert.equal(Dimensions.of().isEmpty(), true);
		Assert.equal(Dimensions.of((int[]) null).isEmpty(), true);
		Assert.equal(Dimensions.of((IntProvider) null).isEmpty(), true);
		Assert.equal(Dimensions.of(0).isEmpty(), false);
	}

	@Test
	public void shouldCountTotalElements() {
		Assert.equal(Dimensions.NONE.total(), 0);
		Assert.equal(Dimensions.of(2).total(), 2);
		Assert.equal(Dimensions.of(2, 3, 0).total(), 0);
		Assert.equal(Dimensions.of(2, 1, 3).total(), 6);
	}

	@Test
	public void shouldProvideDimensionAtIndex() {
		var d = Dimensions.of(3, 1, 2);
		Assert.equal(d.dim(0), 3);
		Assert.equal(d.dim(1), 1);
		Assert.equal(d.dim(2), 2);
		Assert.equal(d.dim(3), 0);
		Assert.equal(d.dim(-1), 2);
		Assert.equal(d.dim(-2), 1);
		Assert.equal(d.dim(-3), 3);
		Assert.equal(d.dim(-4), 0);
	}

	@Test
	public void shouldProvideInnerDimensions() {
		var dims = assertCount(Dimensions.of(3, 2, 1, 2), 3, 12);
		dims = assertCount(dims.inner(), 2, 4);
		dims = assertCount(dims.inner(), 1, 2);
		dims = assertCount(dims.inner(), 2, 2);
		dims = assertCount(dims.inner(), 0, 0);
		dims = assertCount(dims.inner(), 0, 0);
	}

	@Test
	public void shouldProvideOuterDimensions() {
		var dims = assertCount(Dimensions.of(3, 2, 1, 2), 3, 12);
		dims = assertCount(dims.outer(), 3, 6);
		dims = assertCount(dims.outer(), 3, 6);
		dims = assertCount(dims.outer(), 3, 3);
		dims = assertCount(dims.outer(), 0, 0);
		dims = assertCount(dims.outer(), 0, 0);
	}

	private static void assertDims(Dimensions dims, int... values) {
		Assert.array(dims.dims, values);
	}

	private static Dimensions assertCount(Dimensions dims, int dim, int total) {
		Assert.equals(dims.dim(), dim);
		Assert.equals(dims.total(), total);
		return dims;
	}
}
