package model.interpreter.Variable;

import model.interpreter.expressions.Expression;

public interface Var extends Expression {
	public void set(double value);
}