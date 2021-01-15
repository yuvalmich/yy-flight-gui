package model.interpreter;

import java.util.ArrayList;
import java.util.List;

import model.interpreter.commands.Command;
import model.interpreter.commands.CommandsMap;


public class Parser {
	private CommandsMap mapper;

	private static class ParserHolder {
		public static final Parser parser = new Parser();
	}

	private Parser() {
		mapper = CommandsMap.getInstance();
	}

	public static Parser getInstance() {
		return ParserHolder.parser;
	}

	public void parse(String[] tokens) {
	    
		List<Object> args = new ArrayList<Object>();
		Command c;
		for (int i = 0; i < tokens.length && !MyInterpreter.stop;i++) {
			args.clear();// re-using an empty list for each command.
			c = mapper.get(tokens[i]);// check whether the specific token is a command or not.
			if (c != null) {
				i += c.getArguments(tokens, i+1, args);// one line of calling the stringToArg parser in each cmd.
				c.doCommand(args);// the parser is the invoker
			}

		}

	}
}
