package model.interpreter.commands;

import java.util.List;

import model.interpreter.MyInterpreter;

public class ReturnCommand implements Command {

	@Override
	public int getArguments(String[] tokens, int idx, List<Object> emptyList) {
		return StringToArgumentParser.parse(tokens, idx, 1, emptyList, "Double");
	}
	@Override
	public void doCommand(List<Object> args) {
		MyInterpreter.returnValue = (double) args.get(0);
		
	}

}
