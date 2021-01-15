package model.interpreter.commands;

import java.util.List;

import model.interpreter.Parser;

public class IfCommand extends ConditionParser {

	@Override
	public void doCommand(List<Object> args) {
		Parser parser=Parser.getInstance();
		if(state()) 
			parser.parse(block);	
	}

}
