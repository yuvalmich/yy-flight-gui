package model.interpreter.commands;

import java.util.List;

import model.dataHandler.MyDataServer;
import model.interpreter.MyInterpreter;
import model.interpreter.Variable.*;

public class BindAssignmentCommand implements Command {
	@Override
	public int getArguments(String[] tokens, int idx, List<Object> emptyList) {
		emptyList.add(tokens[idx-2]);//add the name first
		return StringToArgumentParser.parse(tokens, idx, 1, emptyList, "String");
	}

	@Override
	public void doCommand(List<Object> args) {
		String name=args.get(0).toString();
		String identifier=args.get(1).toString();//either a path or a local var name.
		//variable has been created before command invoking.
		
		if( MyInterpreter.SymbolTable.containsKey(identifier))  //binding to a script variable
		{
		  Var to_bound=MyInterpreter.SymbolTable.get(identifier);
		  MyInterpreter.SymbolTable.put(name,new BoundScriptVar(to_bound));
		}
		else //binding to a remote variable
		{			
			  MyInterpreter.SymbolTable.put(name,new BoundRemoteVar(identifier,MyDataServer.getServer()));						
		}
		

	}

}