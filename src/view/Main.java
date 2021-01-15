package view;
	
import javafx.application.Application;
import javafx.stage.Stage;
import model.Model;
import viewModel.ViewModel;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Model m = Model.getInstance();
			ViewModel vm = new ViewModel(m);
			FXMLLoader fxl=new FXMLLoader();
			AnchorPane root= fxl.load(getClass().getResource("View.fxml").openStream());
			View v=fxl.getController(); // View
			v.setViewModel(vm);
			vm.addObserver(v);
			m.addObserver(vm);
			Scene scene = new Scene(root,1400,650);
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
