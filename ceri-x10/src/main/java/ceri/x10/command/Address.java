package ceri.x10.command;

import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Pattern;
import ceri.common.text.Regex;
import ceri.common.validation.ValidationUtil;

/**
 * Device address, made up of house and unit codes.
 */
public class Address implements Comparable<Address> {
	private static final Comparator<Address> COMPARATOR =
		Comparator.<Address, House>comparing(a -> a.house).thenComparing(a -> a.unit);
	private static final Pattern ADDRESS_REGEX = Pattern.compile("(\\w)(\\d\\d?)");
	public final House house;
	public final Unit unit;

	/**
	 * Creates an address object from the string address such as "P13"
	 */
	public static Address from(String address) {
		var m = Regex.matchValid(ADDRESS_REGEX, address, "address");
		var house = House.from(m.group(1).charAt(0));
		var unit = Unit.from(Integer.parseInt(m.group(2)));
		return new Address(house, unit);
	}

	public static Address of(House house, Unit unit) {
		ValidationUtil.validateAllNotNull(house, unit);
		return new Address(house, unit);
	}

	private Address(House house, Unit unit) {
		this.house = house;
		this.unit = unit;
	}

	@Override
	public int compareTo(Address other) {
		return COMPARATOR.compare(this, other);
	}

	@Override
	public int hashCode() {
		return Objects.hash(house, unit);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Address other)) return false;
		if (house != other.house) return false;
		if (unit != other.unit) return false;
		return true;
	}

	@Override
	public String toString() {
		return house.name() + unit.value;
	}
}
