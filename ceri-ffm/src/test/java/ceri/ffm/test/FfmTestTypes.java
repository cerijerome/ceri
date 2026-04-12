package ceri.ffm.test;

import ceri.ffm.reflect.Refine.Size;
import ceri.ffm.reflect.Refine.Unsigned;
import ceri.ffm.type.IntType;

public class FfmTestTypes {
	private FfmTestTypes() {}

	@Size(1)
	public static class s8 extends IntType<s8> {
		public static final s8 MIN = new s8(Byte.MIN_VALUE);
		public static final s8 MAX = new s8(Byte.MAX_VALUE);

		public s8(Number n) {
			super(n);
		}
	}

	@Unsigned
	@Size(1)
	public static class u8 extends IntType<u8> {
		public static final u8 MIN = new u8(0);
		public static final u8 MAX = new u8(-1);

		public u8(Number n) {
			super(n);
		}
	}

	@Size(2)
	public static class s16 extends IntType<s16> {
		public static final s16 MIN = new s16(Short.MIN_VALUE);
		public static final s16 MAX = new s16(Short.MAX_VALUE);
		public s16(Number n) {
			super(n);
		}
	}

	@Unsigned
	@Size(2)
	public static class u16 extends IntType<u16> {
		public static final u16 MIN = new u16(0);
		public static final u16 MAX = new u16(-1);

		public u16(Number n) {
			super(n);
		}
	}

	@Size(4)
	public static class s32 extends IntType<s32> {
		public static final s32 MIN = new s32(Integer.MIN_VALUE);
		public static final s32 MAX = new s32(Integer.MAX_VALUE);
		public s32(Number n) {
			super(n);
		}
	}

	@Unsigned
	@Size(4)
	public static class u32 extends IntType<u32> {
		public static final u32 MIN = new u32(0);
		public static final u32 MAX = new u32(-1);

		public u32(Number n) {
			super(n);
		}
	}

	@Size(8)
	public static class s64 extends IntType<s64> {
		public static final s64 MIN = new s64(Long.MIN_VALUE);
		public static final s64 MAX = new s64(Long.MAX_VALUE);
		public s64(Number n) {
			super(n);
		}
	}

	@Unsigned
	@Size(8)
	public static class u64 extends IntType<u64> {
		public static final u64 MIN = new u64(0);
		public static final u64 MAX = new u64(-1L);

		public u64(Number n) {
			super(n);
		}
	}
}
