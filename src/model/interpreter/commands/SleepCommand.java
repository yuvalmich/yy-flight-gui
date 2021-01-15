package model.interpreter.commands;

import java.util.List;

public class SleepCommand implements Command {

	@Override
	public int getArguments(String[] tokens, int idx, List<Object> emptyList) {
		return StringToArgumentParser.parse(tokens, idx,1, emptyList, "Integer");
	}
	
	@Override
	public void doCommand(List<Object> args) {		
		try {
			long millis=((Number)args.get(0)).longValue();
			Thread.sleep(millis);
		    } catch (InterruptedException | IllegalArgumentException e ) {e.printStackTrace();}				
	}
}