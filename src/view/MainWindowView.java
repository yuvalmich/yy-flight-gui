package view;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import viewModel.MainWindowViewModel;


public class MainWindowView implements Observer, Initializable {
	MainWindowViewModel viewModel;
	
	@FXML
	Button connectServerButton;
	
	@FXML
	Label resultLabel;
	
	public void setViewModel(MainWindowViewModel vm) {
		this.viewModel = vm;
		resultLabel.textProperty().bind(vm.result);
	}
	
	@FXML
	public void onConnectServerButtonClicked() {
		this.viewModel.connectToServer();
	}
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		/* Note:
		 * this has no implementation yet.
		 * it is here before its implementation because it is part of MVVM architecture.
		 */
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
}
