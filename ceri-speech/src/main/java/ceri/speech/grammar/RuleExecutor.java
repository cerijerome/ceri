/**
 * 
 */
package ceri.speech.grammar;

import java.util.List;

public interface RuleExecutor {
	void execute(String rule, List<String> tags);
}