package model.interpreter.expressions;

import java.util.Arrays;
import java.util.List;
//I saw those two lines repeatedly in the code so I decided to make it neater.
public class ExpressionCalculate {
	public static double invoke(String sticked_expression)
	{
		List<String> expression=Arrays.asList(sticked_expression.split(" "));	
	String fixed_exp = ExpressionConvertor.infixToPostfix(expression);
	return ExpressionConvertor.calculatePostfix(fixed_exp);
	}
}
