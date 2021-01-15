package model.interpreter.commands;

import java.util.List;

import model.interpreter.Parser;

public class LoopCommand extends ConditionParser {

	@Override
	public void doCommand(List<Object> args) {
		Parser parser=Parser.getInstance();
		while(state())
			parser.parse(block);				
	}

}
