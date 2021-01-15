package model.interpreter.Variable;

public class RegularVar implements Var {
	private double value;

	public RegularVar(double value) {
		this.value = value;
	}

	@Override
	public double calculate() {
		return this.value;
	}

	@Override
	public void set(double value) {
		this.value = value;
	}

}
