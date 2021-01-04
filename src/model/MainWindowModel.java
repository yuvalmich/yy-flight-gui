package model;

import java.util.Observable;

public class MainWindowModel extends Observable {
	String result;
	
	public void connectToServer() {
		result = "server connected";
		setChanged();
		notifyObservers();
	}
	
	public String getResult() {
		return result;
	}
}
