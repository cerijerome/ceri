package ceri.x10.command;

import static ceri.common.validation.ValidationUtil.validateNotNull;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.text.RegexUtil;
import ceri.common.util.HashCoder;

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
		Matcher m = RegexUtil.matched(ADDRESS_REGEX, address);
		if (m == null) throw new IllegalArgumentException("Invalid address format: " + address);
		House house = House.from(m.group(1).charAt(0));
		Unit unit = Unit.from(Integer.parseInt(m.group(2)));
		return new Address(house, unit);
	}

	public static Address of(House house, Unit unit) {
		validateNotNull(house);
		validateNotNull(unit);
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
		return HashCoder.hash(house, unit);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Address)) return false;
		Address other = (Address) obj;
		if (house != other.house) return false;
		if (unit != other.unit) return false;
		return true;
	}

	@Override
	public String toString() {
		return house.name() + unit.value;
	}

}
