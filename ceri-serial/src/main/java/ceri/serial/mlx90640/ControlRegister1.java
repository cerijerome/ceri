package ceri.serial.mlx90640;

import ceri.common.data.ByteUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class ControlRegister1 {
	public static final ControlRegister1 DEFAULT = builder().build();
	private static final int READING_PATTERN_BIT = 12;
	private static final int RESOLUTION_BIT = 10;
	private static final int REFRESH_RATE_BIT = 7;
	private static final int SUB_PAGE_BIT = 4;
	private static final int SUB_PAGES_REPEAT_BIT = 3;
	private static final int DATA_HOLD_BIT = 2;
	private static final int SUB_PAGES_MODE_BIT = 0;
	private static final int RESERVED_MASK = 0xe000;
	public final ReadingPattern readingPattern;
	public final Resolution resolution;
	public final RefreshRate refreshRate;
	public final SubPage selectSubPage;
	public final boolean enableSubPagesRepeat;
	public final boolean enableDataHold;
	public final boolean enableSubPagesMode;
	public final int reserved; // preserve reserved bits

	/**
	 * Decode settings from integer value.
	 */
	public static ControlRegister1 decode(int value) {
		return builder().readingPattern(ReadingPattern.decode(value >>> READING_PATTERN_BIT))
			.resolution(Resolution.decode(value >>> RESOLUTION_BIT))
			.refreshRate(RefreshRate.decode(value >>> REFRESH_RATE_BIT))
			.selectSubPage(SubPage.decode(value >>> SUB_PAGE_BIT))
			.enableSubPagesRepeat(ByteUtil.bit(value, SUB_PAGES_REPEAT_BIT))
			.enableDataHold(ByteUtil.bit(value, DATA_HOLD_BIT))
			.enableSubPagesMode(ByteUtil.bit(value, SUB_PAGES_MODE_BIT)).reserved(value).build();
	}

	public static class Builder {
		ReadingPattern readingPattern = ReadingPattern.chess;
		Resolution resolution = Resolution._18bit;
		RefreshRate refreshRate = RefreshRate._2Hz;
		SubPage selectSubPage = SubPage._0;
		boolean enableSubPagesRepeat = false;
		boolean enableDataHold = false;
		boolean enableSubPagesMode = true;
		int reserved = 0;

		Builder() {}

		public Builder readingPattern(ReadingPattern readingPattern) {
			this.readingPattern = readingPattern;
			return this;
		}

		public Builder resolution(Resolution resolution) {
			this.resolution = resolution;
			return this;
		}

		public Builder refreshRate(RefreshRate refreshRate) {
			this.refreshRate = refreshRate;
			return this;
		}

		public Builder selectSubPage(SubPage selectSubPage) {
			this.selectSubPage = selectSubPage;
			return this;
		}

		public Builder enableSubPagesRepeat(boolean enableSubPagesRepeat) {
			this.enableSubPagesRepeat = enableSubPagesRepeat;
			return this;
		}

		public Builder enableDataHold(boolean enableDataHold) {
			this.enableDataHold = enableDataHold;
			return this;
		}

		public Builder enableSubPagesMode(boolean enableSubPagesMode) {
			this.enableSubPagesMode = enableSubPagesMode;
			return this;
		}

		public Builder reserved(int reserved) {
			this.reserved = reserved & RESERVED_MASK;
			return this;
		}

		public ControlRegister1 build() {
			return new ControlRegister1(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	ControlRegister1(Builder builder) {
		readingPattern = builder.readingPattern;
		resolution = builder.resolution;
		refreshRate = builder.refreshRate;
		selectSubPage = builder.selectSubPage;
		enableSubPagesRepeat = builder.enableSubPagesRepeat;
		enableDataHold = builder.enableDataHold;
		enableSubPagesMode = builder.enableSubPagesMode;
		reserved = builder.reserved;
	}

	/**
	 * Encodes settings to an integer value.
	 */
	public int encode() {
		return (readingPattern.encode() << READING_PATTERN_BIT) |
			(resolution.encode() << RESOLUTION_BIT) | (refreshRate.encode() << REFRESH_RATE_BIT) |
			(selectSubPage.encode() << SUB_PAGE_BIT) |
			ByteUtil.maskOfBitInt(enableSubPagesRepeat, SUB_PAGES_REPEAT_BIT) |
			ByteUtil.maskOfBitInt(enableDataHold, DATA_HOLD_BIT) |
			ByteUtil.maskOfBitInt(enableSubPagesMode, SUB_PAGES_MODE_BIT);
	}

	/**
	 * Returns a builder to modify settings.
	 */
	public Builder modify() {
		return builder().readingPattern(readingPattern).resolution(resolution)
			.refreshRate(refreshRate).selectSubPage(selectSubPage)
			.enableSubPagesRepeat(enableSubPagesRepeat).enableDataHold(enableDataHold)
			.enableSubPagesMode(enableSubPagesMode).reserved(reserved);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(readingPattern, resolution, refreshRate, selectSubPage,
			enableSubPagesRepeat, enableDataHold, enableSubPagesMode, reserved);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ControlRegister1)) return false;
		ControlRegister1 other = (ControlRegister1) obj;
		if (!EqualsUtil.equals(readingPattern, other.readingPattern)) return false;
		if (!EqualsUtil.equals(resolution, other.resolution)) return false;
		if (!EqualsUtil.equals(refreshRate, other.refreshRate)) return false;
		if (!EqualsUtil.equals(selectSubPage, other.selectSubPage)) return false;
		if (enableSubPagesRepeat != other.enableSubPagesRepeat) return false;
		if (enableDataHold != other.enableDataHold) return false;
		if (enableSubPagesMode != other.enableSubPagesMode) return false;
		if (reserved != other.reserved) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, readingPattern, resolution, refreshRate,
			selectSubPage, enableSubPagesRepeat, enableDataHold, enableSubPagesMode,
			"0x" + Integer.toHexString(reserved)).toString();
	}

}
