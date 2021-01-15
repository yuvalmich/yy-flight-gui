package model.interpreter.Variable;

import model.dataHandler.DataClient;
import model.dataHandler.DataGetter;
import model.interpreter.commands.ConnectCommand;

public class BoundRemoteVar implements Var {

	String path;
	DataGetter getter;

	public BoundRemoteVar(String path, DataGetter getter) {
		this.path = path;
		this.getter = getter;
	}

	@Override
	public void set(double value) {
		DataClient client = ConnectCommand.client;
		client.set(path, value);
	}

	@Override
	public double calculate() {
		return getter.get(path);
	}

}
