package model.interpreter.commands;

import java.util.LinkedList;
import java.util.List;

import model.interpreter.expressions.ExpressionCalculate;

public abstract class ConditionParser implements Command {

	protected String[] block;
	private String[] condition;

	// re-calculate the condition
	protected boolean state() {
		String operator = condition[1];
		double x = ExpressionCalculate.invoke(condition[0]);
		double y = ExpressionCalculate.invoke(condition[2]);
		switch (operator) {
		case "==":
			return (x == y);
		case "!=":
			return (x != y);
		case "<":
			return (x < y);
		case "<=":
			return (x <= y);
		case ">":
			return (x > y);
		case ">=":
			return (x >= y);

		}
		return false;
	}

	// they're considered arguments because theyre relevant for the while to run
	@Override
	public int getArguments(String[] tokens, int idx, List<Object> emptyList) {
		// while(we are here)...{
		//saving the condition
		//TODO: invoke the lexer function on the condition as well?
		List<String> condition_list = new LinkedList<String>();
		int i;
		for (i=idx;;i++){
			String token=tokens[i];
			if (token.equals("{")) break;
			condition_list.add(token);
		}
		condition=new String[condition_list.size()];
		condition=condition_list.toArray(condition);

		int open_curly = 1;
		int close_curly = 0;
		int block_end =i+1;
		List<String> container = new LinkedList<String>();
		// a way to detect our block boundaries
		while (close_curly < open_curly) {
			String token = tokens[block_end];
			if (token.equals("{"))
				open_curly++;
			else if (token.equals("}"))
				close_curly++;
			container.add(token);
			block_end++;
			
		}
		container = container.subList(0, container.size() - 1);// since we don't need the { } of the current block.
		block=new String[container.size()];
		container.toArray(block);
		return block_end - idx;// tells the first parser to run to advance in the total block size.
	}

}
