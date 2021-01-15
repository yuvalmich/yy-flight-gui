package model.interpreter;

import java.util.*;
import model.interpreter.Variable.Var;

public class MyInterpreter {
	public static HashMap<String, Var> SymbolTable = new HashMap<String, Var>();
	public static double returnValue = 0;
	public static Thread interpretation_thread = null;
	public static boolean is_busy=false;
	public static boolean stop=true; //used for the current activation of the interpreting session.
    public static boolean enabled=false; //tells us whether interpretation mode is enabled or not.
    //the status of the interpretation sessions and the enabled tells us if its even possible to interpret.
    
    //invoked when enabled is true
    //from now on we manage our sessions with stop.
	public void interpret(String text) {
		if(text.equals("")) return;
		interpretation_thread = new Thread(() -> {
			
			if(interpreterBusy())//to make sure the previous existing 
				try { //thread is done running.
					interpretation_thread.join();
				    } catch (InterruptedException e) {}
			if(!enabled) return;
			is_busy=true;
			   stop=false;
			   
			SymbolTable.clear();// clears the variable context
			String[] lines = text.split("\n");
			Lexer lexer = Lexer.getInstance();
			Parser parser = Parser.getInstance();
			StringJoiner sj = new StringJoiner("\n");
			for (String line : lines)
				sj.add(line);
			String code = sj.toString();
			String[] tokens = lexer.lexer(code);
			parser.parse(tokens);
			is_busy=false;
			stop=true;
		});
		interpretation_thread.start();
	}

	public boolean interpreterBusy() {
		return is_busy;
	}
	
	public void stop()
	{
		stop=true;
	}
	

}