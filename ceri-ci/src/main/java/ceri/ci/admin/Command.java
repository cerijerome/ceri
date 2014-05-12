package ceri.ci.admin;

import ceri.ci.alert.AlertService;

public interface Command {

	Response execute(AlertService service) throws Exception;

}
