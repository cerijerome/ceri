package ceri.image.ebay;

public class EpsUtil {

	private EpsUtil() {}

	public static void print() {
		for (EpsSetId setId : EpsSetId.values()) {
			System.out.print(setId.id + "(" + setId.authorizedApps + ")");
			for (EpsImageType type : setId.types)
				System.out.print(" " + type.id);
			System.out.println();
		}
	}

	public static void main(String[] args) {
		print();
	}
}
