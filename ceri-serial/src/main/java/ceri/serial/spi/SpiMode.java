package ceri.serial.spi;

import static ceri.serial.spi.jna.SpiDev.SPI_3WIRE;
import static ceri.serial.spi.jna.SpiDev.SPI_CPHA;
import static ceri.serial.spi.jna.SpiDev.SPI_CPOL;
import static ceri.serial.spi.jna.SpiDev.SPI_CS_HIGH;
import static ceri.serial.spi.jna.SpiDev.SPI_LOOP;
import static ceri.serial.spi.jna.SpiDev.SPI_LSB_FIRST;
import static ceri.serial.spi.jna.SpiDev.SPI_NO_CS;
import static ceri.serial.spi.jna.SpiDev.SPI_READY;
import static ceri.serial.spi.jna.SpiDev.SPI_RX_DUAL;
import static ceri.serial.spi.jna.SpiDev.SPI_RX_QUAD;
import static ceri.serial.spi.jna.SpiDev.SPI_TX_DUAL;
import static ceri.serial.spi.jna.SpiDev.SPI_TX_QUAD;

/**
 * Encapsulates 8-bit and 32-bit modes.
 */
public record SpiMode(int value) {
	public static final SpiMode MODE_0 = new SpiMode(0);
	public static final SpiMode MODE_1 = builder().clockPhaseHigh().build();
	public static final SpiMode MODE_2 = builder().clockPolarityHigh().build();
	public static final SpiMode MODE_3 = builder().clockPhaseHigh().clockPolarityHigh().build();

	public static class Builder {
		int value = 0;

		Builder() {}

		public Builder clockPhaseHigh() {
			return addFlag(SPI_CPHA);
		}

		public Builder clockPolarityHigh() {
			return addFlag(SPI_CPOL);
		}

		public Builder chipSelectHigh() {
			return addFlag(SPI_CS_HIGH);
		}

		public Builder lsbFirst() {
			return addFlag(SPI_LSB_FIRST);
		}

		public Builder spi3Wire() {
			return addFlag(SPI_3WIRE);
		}

		public Builder loop() {
			return addFlag(SPI_LOOP);
		}

		public Builder noChipSelect() {
			return addFlag(SPI_NO_CS);
		}

		public Builder ready() {
			return addFlag(SPI_READY);
		}

		public Builder txDual() {
			return addFlag(SPI_TX_DUAL);
		}

		public Builder txQuad() {
			return addFlag(SPI_TX_QUAD);
		}

		public Builder rxDual() {
			return addFlag(SPI_RX_DUAL);
		}

		public Builder rxQuad() {
			return addFlag(SPI_RX_QUAD);
		}

		private Builder addFlag(int mask) {
			value |= mask;
			return this;
		}

		public SpiMode build() {
			return new SpiMode(value);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public boolean is32Bit() {
		return (value & 0xff) != value;
	}

	public boolean clockPhaseHigh() {
		return hasFlag(SPI_CPHA);
	}

	public boolean clockPolarityHigh() {
		return hasFlag(SPI_CPOL);
	}

	public boolean chipSelectHigh() {
		return hasFlag(SPI_CS_HIGH);
	}

	public boolean lsbFirst() {
		return hasFlag(SPI_LSB_FIRST);
	}

	public boolean spi3Wire() {
		return hasFlag(SPI_3WIRE);
	}

	public boolean loop() {
		return hasFlag(SPI_LOOP);
	}

	public boolean noChipSelect() {
		return hasFlag(SPI_NO_CS);
	}

	public boolean ready() {
		return hasFlag(SPI_READY);
	}

	public boolean txDual() {
		return hasFlag(SPI_TX_DUAL);
	}

	public boolean txQuad() {
		return hasFlag(SPI_TX_QUAD);
	}

	public boolean rxDual() {
		return hasFlag(SPI_RX_DUAL);
	}

	public boolean rxQuad() {
		return hasFlag(SPI_RX_QUAD);
	}

	private boolean hasFlag(int mask) {
		return (value & mask) != 0;
	}

	@Override
	public String toString() {
		return is32Bit() ? String.format("0x%08x", value) : String.format("0x%02x", value);
	}
}
