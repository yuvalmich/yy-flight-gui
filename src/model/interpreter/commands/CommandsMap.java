package model.interpreter.commands;

import java.util.HashMap;

public class CommandsMap {
	private HashMap<String, Command> map;

	public static class CommandsMapHolder {
		private static final CommandsMap commands_mapper = new CommandsMap();
	}

	public static CommandsMap getInstance() {
		return CommandsMapHolder.commands_mapper;
	}

	private CommandsMap() {

		map = new HashMap<String, Command>();
		map.put("connect", new ConnectCommand());
		map.put("openDataServer", new OpenServerCommand());
		map.put("var", new DefineVarCommand());
		map.put("sleep", new SleepCommand());
		map.put("print", new PrintCommand());
		map.put("return", new ReturnCommand());
		map.put("=", new AssignmentCommand());
		map.put("=bind", new BindAssignmentCommand());
		map.put("while", new LoopCommand());
		map.put("if", new IfCommand());
		map.put("disconnect", new DisconnectCommand());

	}

	public Command get(String key) {
		return map.get(key);
	}
	public boolean contains(String key)
	{
		return map.containsKey(key);
	}

}
