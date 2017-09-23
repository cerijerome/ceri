package ceri.ent.web;

import java.util.Set;
import ceri.common.collection.ImmutableUtil;

/**
 *  class for accessing common web resources.
 */
public class EntWeb {
	public static final String CSS = "css/ent-web.css";
	public static final Set<String> FONTS = ImmutableUtil.asSet("Montserrat", "Nunito", "Roboto"); 
	
	private EntWeb() {}
	
}
