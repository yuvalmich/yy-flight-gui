package view;
	
import javafx.application.Application;
import javafx.stage.Stage;
import model.MainWindowModel;
import viewModel.MainWindowViewModel;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			MainWindowModel m = MainWindowModel.getInstance();
			MainWindowViewModel vm = new MainWindowViewModel(m);
			FXMLLoader fxl=new FXMLLoader();
			AnchorPane root= fxl.load(getClass().getResource("MainWindow.fxml").openStream());
			MainWindowController v=fxl.getController(); // View
			v.setViewModel(vm);
			vm.addObserver(v);
			m.addObserver(vm);
			Scene scene = new Scene(root,1400,650);
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
