package model.interpreter.commands;

import java.util.List;
import model.interpreter.expressions.ExpressionCalculate;


public class PrintCommand implements Command {
	@Override
	public int getArguments(String[] tokens, int idx, List<Object> emptyList) {
		return StringToArgumentParser.parse(tokens, idx, 1, emptyList, "String");
	}

	@Override
	public void doCommand(List<Object> args) {
		String str = args.get(0).toString();
		if (str.charAt(0) != '"')// meaning, its a variable
		{
			str = Double.toString((ExpressionCalculate.invoke(str)));
		}
		else {
			str = str.substring(1, str.length()-1);
		}		
	}
}
