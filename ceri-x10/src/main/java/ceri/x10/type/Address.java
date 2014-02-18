package ceri.x10.type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.util.HashCoder;

/**
 * Device address, made up of house and unit codes.
 */
public class Address {
	private static final Pattern FROM_STRING_REGEX = Pattern.compile("(\\w)(\\d\\d?)");
	public final House house;
	public final Unit unit;
	private final int hashCode;
	
	public Address(House house, Unit unit) {
		this.house = house;
		this.unit = unit;
		hashCode = HashCoder.hash(house, unit);
	}
	
	/**
	 * Creates an address object from the string address such as "P13"
	 */
	public static Address fromString(String address) {
		Matcher m = FROM_STRING_REGEX.matcher(address);
		if (!m.find()) throw new IllegalArgumentException("Invalid address format: " + address);
		House house = House.valueOf(m.group(1));
		Unit unit = Unit.valueOf("_" + m.group(2));
		return new Address(house, unit);
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Address)) return false;
		Address other = (Address)obj;
		return house == other.house && unit == other.unit;
	}
	
	@Override
	public String toString() {
		return house.name() + unit.index;
	}
	
}
