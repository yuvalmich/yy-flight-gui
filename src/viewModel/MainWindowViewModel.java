package viewModel;

import java.util.Observable;
import java.util.Observer;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;

public class MainWindowViewModel extends Observable implements Observer {
	MainWindowModel model;

	public StringPropertyBase result;
	
	public MainWindowViewModel(MainWindowModel model) {
		this.model = model;
		result = new SimpleStringProperty();
	}
	
	public void plus(){
		m.plus(Double.parseDouble(x.get()), Double.parseDouble(y.get()));
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if(o == this.model) {
			result.set(this.model.getResult());
		}
		
		// usually here is where we call notifyObservers, but in this case we use data binding
	}
}