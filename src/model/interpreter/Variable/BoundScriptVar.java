package model.interpreter.Variable;

public class BoundScriptVar implements Var {
	private Var bound_to;

	public BoundScriptVar(Var bound_to) {
		this.bound_to = bound_to;
	}

	@Override
	public void set(double value) {
		bound_to.set(value);
	}

	@Override
	public double calculate() {
		return bound_to.calculate();
	}

}
