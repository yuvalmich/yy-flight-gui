package view;

import javafx.application.Application;
import javafx.stage.Stage;
import model.MainWindowModel;
import viewModel.MainWindowViewModel;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			// Create main-window model and viewModel
			MainWindowModel mainWindowModel = new MainWindowModel(); // Model
			MainWindowViewModel mainWindowViewModel = new MainWindowViewModel(mainWindowModel); // View-Model
			mainWindowModel.addObserver(mainWindowViewModel);
			
			FXMLLoader fxl = new FXMLLoader();
			
			GridPane root = (GridPane)fxl.load(getClass().getResource("MainWindowView.fxml").openStream());
			
			MainWindowController mainWindowView = fxl.getController();
			mainWindowView.setViewModel(mainWindowViewModel);
			mainWindowViewModel.addObserver(mainWindowView);
			
			Scene scene = new Scene(root,800,800);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
