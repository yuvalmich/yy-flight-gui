package viewModel;

import java.util.Observable;
import java.util.Observer;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;
import model.MainWindowModel;

public class MainWindowViewModel extends Observable implements Observer {
	MainWindowModel model;

	public StringPropertyBase result;
	
	public MainWindowViewModel(MainWindowModel model) {
		this.model = model;
		
	}
	
	public void connectToServer(){
		this.model.connectToServer();
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if(o == this.model) {
			
		}
		
		// usually here is where we call notifyObservers, but in this case we use data binding
	}
}