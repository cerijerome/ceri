package ceri.zwave.command;


public class CommandFactory {
	private static final String ID = "id";
	private static final String SERVICE_ID = "serviceId";
	private final String host;
	private final Executor executor;
	
	public CommandFactory(String host, Executor executor) {
		this.host = host;
		this.executor = executor;
	}
	
	public Command userData() {
		Command command = new Command(host, executor);
		command.param(ID, Id.user_data);
		return command;
	}
	
	public Command action(Action action, String sid) {
		Command command = new Command(host, executor);
		command.param(ID, Id.action.name());
		command.param(Id.action.name(), action.name());
		command.param(SERVICE_ID, sid);
		return command;
	}
	
}
