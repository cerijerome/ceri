/**
 * 
 */
package ceri.speech.grammar;

import java.util.List;

public interface RuleMatcher {
	boolean matches(String rule, List<String> tags);
}