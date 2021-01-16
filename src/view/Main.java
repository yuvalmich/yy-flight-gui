package view;
	
import java.io.File;

import javafx.application.Application;
import javafx.stage.Stage;
import model.MainWindowModel;
import viewModel.MainWindowViewModel;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			
			MainWindowModel m = MainWindowModel.getInstance();
			MainWindowViewModel vm = new MainWindowViewModel(m);
			
			// Load xml
			FXMLLoader fxl =new FXMLLoader();
			AnchorPane root = fxl.load(getClass().getResource("MainWindow.fxml").openStream());
			MainWindowController v = fxl.getController();
			
			
			File windowBackgroundFile = new File("assets/background.png");
			Image windowBackground = new Image("file:" + windowBackgroundFile.toURI().getPath(),  1400, 550, false,true);
			BackgroundImage myBI = new BackgroundImage(windowBackground,
			        BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
			          BackgroundSize.DEFAULT);
			root.setBackground(new Background(myBI));
			
			// Observers
			v.setViewModel(vm);
			vm.addObserver(v);
			m.addObserver(vm);
			
			// Window size
			Scene scene = new Scene(root, 1400, 550);
			
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			System.out.println("startup failed.");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
