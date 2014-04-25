package ceri.ci.admin;

import ceri.ci.alert.AlertService;

public interface Command {

	String execute(AlertService service);

}
