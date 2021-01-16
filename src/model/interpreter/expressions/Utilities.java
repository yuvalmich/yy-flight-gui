package model.interpreter.expressions;

import java.util.ArrayList;
import model.interpreter.MyInterpreter;
import model.interpreter.Variable.RegularVar;
import model.interpreter.Variable.Var;
import model.interpreter.commands.CommandsMap;

public class Utilities {
	public static boolean isDouble(String str) {
		try {
			@SuppressWarnings("unused")
			double d = Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	// when calculating all kind of variables are only used Regularly.
	public static Var getVar(String name) {

		if (MyInterpreter.SymbolTable.containsKey(name)) {
			double value = MyInterpreter.SymbolTable.get(name).calculate();
			//we create a new one since we don't want to harm the one in the symbol-table.
			return new RegularVar(value);
		}
		return null;
	}
	
	public static boolean IsVarOrCmd(String str)
	{
		try {
			double d = Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			ArrayList<String> operators=new ArrayList<String>();
			operators.add("+");
			operators.add("-");
			operators.add("/");
			operators.add("*");
			operators.add("(");
			operators.add(")");
			return !(operators.contains(str));			
		}
	}
	
	public static boolean IsCmd(String str)
	{
		return (CommandsMap.getInstance().get(str)!=null);
	}
}