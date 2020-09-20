package ceri.zwave.command;

import java.io.IOException;

public interface Executor {

	String execute(String url) throws IOException;

}
