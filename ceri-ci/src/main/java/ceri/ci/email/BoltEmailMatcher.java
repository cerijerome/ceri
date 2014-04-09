package ceri.ci.email;

import ceri.ci.build.BuildEvent;

public class BoltEmailMatcher implements EmailEventMatcher {

	@Override
	public BuildEvent getEvent(Email email) {
		return null;
	}
	
}
