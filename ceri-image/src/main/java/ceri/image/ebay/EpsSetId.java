package ceri.image.ebay;

import static ceri.image.ebay.EpsImageType._0;
import static ceri.image.ebay.EpsImageType._1;
import static ceri.image.ebay.EpsImageType._12;
import static ceri.image.ebay.EpsImageType._14;
import static ceri.image.ebay.EpsImageType._21;
import static ceri.image.ebay.EpsImageType._23;
import static ceri.image.ebay.EpsImageType._26;
import static ceri.image.ebay.EpsImageType._28;
import static ceri.image.ebay.EpsImageType._33;
import static ceri.image.ebay.EpsImageType._34;
import static ceri.image.ebay.EpsImageType._35;
import static ceri.image.ebay.EpsImageType._37;
import static ceri.image.ebay.EpsImageType._39;
import static ceri.image.ebay.EpsImageType._40;
import static ceri.image.ebay.EpsImageType._41;
import static ceri.image.ebay.EpsImageType._42;
import static ceri.image.ebay.EpsImageType._43;
import static ceri.image.ebay.EpsImageType._44;
import static ceri.image.ebay.EpsImageType._49;
import static ceri.image.ebay.EpsImageType._50;
import static ceri.image.ebay.EpsImageType._51;
import static ceri.image.ebay.EpsImageType._52;
import static ceri.image.ebay.EpsImageType._53;
import static ceri.image.ebay.EpsImageType._54;
import static ceri.image.ebay.EpsImageType._55;
import static ceri.image.ebay.EpsImageType._7;
import java.util.Arrays;
import java.util.EnumSet;

/**
 * The documented Set Ids for eBay Picture Services.
 */
public enum EpsSetId {
	Standard("All", _0, _1, EpsImageType._2, _12, _14, _35, _39),
	Supersize("All", _0, _1, EpsImageType._2, EpsImageType._3, _12, _14, _35, _39),
	_2("New Apps (DIP Enabled)", _1),
	_3("Tagz app", EpsImageType._3),
	_4002("Marktplaats", _1, _14),
	_4000008("Motors Mobile App", EpsImageType._3, _26),
	_A80000500F("Daily Deals", _0, _1, EpsImageType._2, EpsImageType._3, _12, _14, _35, _37, _39),
	_20000000001("CSA Flash Hero Image", _0, _41),
	_40010000000("CSA Flash Brand Image", _28, _42),
	_80000000000("CSA Flash Gallery Image", _43),
	_100000000000("CSA Flash Annoucement Image", _44),
	_3E000404004000("EU Deals", _14, _26, _34, _49, _50, _51, _52, _53),
	_C0010200A00080("FR Local Classified", _7, _21, _23, _33, _40, _54, _55),
	_C0018A00A05087("New Fr Classified Set Id For Migration", _0, _1, EpsImageType._2, _7, _12,
		_14, _21, _23, _33, _35, _39, _40, _54, _55);

	public final String id;
	public final EnumSet<EpsImageType> types;
	public final String authorizedApps;

	private EpsSetId(String authorizedApps, EpsImageType... types) {
		id = name().startsWith("_") ? name().substring(1) : name();
		this.authorizedApps = authorizedApps;
		this.types =
			types.length == 0 ? EnumSet.noneOf(EpsImageType.class) : EnumSet.copyOf(Arrays
				.asList(types));
	}

	/**
	 * Returns the image type with the largest area for this set id.
	 */
	public EpsImageType largestType() {
		return EpsImageType.largestType(types);
	}

}
