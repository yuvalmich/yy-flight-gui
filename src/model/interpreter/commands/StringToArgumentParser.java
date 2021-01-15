package model.interpreter.commands;

import java.util.IllegalFormatConversionException;
import java.util.List;

import model.interpreter.expressions.ExpressionCalculate;


public class StringToArgumentParser {

	public static int parse(String[] tokens, int idx, int amount, List<Object> args, String... typeArguments) {
		for (int i = 0; i < amount; i++) {
			try {
				String type = typeArguments[i];
				if (type.equals("String"))
					args.add(tokens[idx+i]);
				else {
					double arg =ExpressionCalculate.invoke(tokens[idx+i]);
					switch (type) {
					case "Double":
						args.add(arg);
						break;
					case "Integer":
						args.add((int) arg);
						break;
					}
				}

			}

			catch (IllegalFormatConversionException e) {
				e.printStackTrace();
			}

		}
		return amount;
	}

}
