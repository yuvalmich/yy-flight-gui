package model;

import java.util.Observable;

public class MainWindowModel extends Observable {
	
	public void connectToServer() {
		setChanged();
		notifyObservers();
	}
}
