package ceri.image.ebay;

import static ceri.image.ebay.EpsImageType.*;
import java.util.Arrays;
import java.util.EnumSet;

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
	
}
