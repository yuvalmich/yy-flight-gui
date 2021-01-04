package view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import viewModel.MainWindowViewModel;


public class MainWindowView {
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
	}
}
