package ceri.serial.javax;

import java.util.Enumeration;
import ceri.common.util.BasicUtil;
import purejavacomm.CommPortIdentifier;

public class PortLister {

	public static void main(String[] args) {
		for (CommPortIdentifier id : identifiers()) {
			String owner = id.getCurrentOwner();
			if (owner == null) owner = "none";
			System.out.printf("%s %d (%s)%n", id.getName(), id.getPortType(), owner);
		}
	}

	private static Iterable<CommPortIdentifier> identifiers() {
		Enumeration<?> enumeration = CommPortIdentifier.getPortIdentifiers();
		Enumeration<CommPortIdentifier> identifiers = BasicUtil.uncheckedCast(enumeration);
		return identifiers::asIterator;
	}

}
