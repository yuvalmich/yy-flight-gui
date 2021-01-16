package view;

import java.util.*;
import java.io.File;
import javafx.scene.control.*;
import javafx.beans.value.ChangeListener;
import java.net.URL;
import javafx.beans.binding.Bindings;
import javafx.scene.image.Image;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.fxml.FXML;
import java.io.FileNotFoundException;
import javafx.stage.FileChooser;
import javafx.application.Platform;
import javafx.util.Pair;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.GridPane;
import javafx.fxml.Initializable;

import viewModel.MainWindowViewModel;

public class MainWindowController implements Initializable, Observer {

	MainWindowViewModel viewModel;

	// left
	@FXML
	Button loadDataButton;
	@FXML
	Button connectButton;
	@FXML
	Button calculatePathButton;
	@FXML
	MapGrid mapGridCanvas;
	
	// center
	@FXML
	Button executeCodeButton;
	@FXML
	RadioButton autoPilotModeButton;
	@FXML
	TextArea commandLineTextArea;
	
	// right
	@FXML
	RadioButton manualModeButton;
	@FXML
	JoyStick joyStickCanvas;
	@FXML
	Slider throttleSlider;
	@FXML
	Slider rudderSlider;

	public void setViewModel(MainWindowViewModel vm) {
		this.viewModel = vm;
		this.viewModel.rudder.bind(rudderSlider.valueProperty());
		this.viewModel.throttle.bind(throttleSlider.valueProperty());
		this.viewModel.cliText.bind(commandLineTextArea.textProperty());

		this.viewModel.aileron.bind(joyStickCanvas.aileron);
		this.viewModel.elevator.bind(joyStickCanvas.elevator);
		this.mapGridCanvas.solution.bind(vm.solution);

		this.mapGridCanvas.heading.bind(this.viewModel.heading);
		this.mapGridCanvas.serverUp.bind(this.viewModel.serverUp);
		joyStickCanvas.aileron.addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (manualModeButton.isSelected()&& (mapGridCanvas.serverUp.get()))
					vm.aileronSend();			
			}
		});

		joyStickCanvas.elevator.addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (manualModeButton.isSelected()&& (mapGridCanvas.serverUp.get()))
					vm.elevatorSend();
				
			}
		});

	}

	  ////////////////////
	 /// left methods ///
	////////////////////

	@FXML
	public void onConnectButtonClicked() {
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle("connect tp FlightGear");
		dialog.setHeaderText("Please enter the FlightGear ip and port");

		ButtonType loginButtonType = new ButtonType("connect", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);

		TextField ip = new TextField();
		ip.setPromptText("IP");
		TextField port = new TextField();
		port.setPromptText("port");

		grid.add(new Label("IP:"), 0, 0);
		grid.add(ip, 1, 0);
		grid.add(new Label("Port:"), 0, 1);
		grid.add(port, 1, 1);

		dialog.getDialogPane().setContent(grid);
		Platform.runLater(() -> ip.requestFocus());
		
		ip.setText("127.0.0.1");
		port.setText("5402");

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == loginButtonType) {
				return new Pair<>(ip.getText(), port.getText());
			}
			return null;
		});

		Optional<Pair<String, String>> result = dialog.showAndWait();
		
		result.ifPresent(serverInfo -> {
			viewModel.connectToSimulator(serverInfo.getKey(), Integer.parseInt(serverInfo.getValue()));
		});
	}

	@FXML
	public void onLoadDataButtonClicked() {
		FileChooser fc = new FileChooser();
		fc.setTitle("load map");
		fc.setInitialDirectory(new File("./assets/maps"));
		
		File chosen = fc.showOpenDialog(null);
		
		if (chosen == null) {
			return;
		}
		
		List<String> map = new LinkedList<String>();
		Scanner scanner = null;
		
		try {
			scanner = new Scanner(chosen);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
		while (scanner.hasNext()) {
			map.add(scanner.next());
		}
		scanner.close();

		String[] coordinates = map.get(0).split(",");
		double startLat = Double.parseDouble(coordinates[1]);
		double startLong = Double.parseDouble(coordinates[0]);
		double area = Double.parseDouble(map.get(1).split(",")[0]);

		int row = map.size() - 2;
		int col = map.get(2).split(",").length;
		int[][] mapData = new int[row][col];
		for (int i = 2; i < row + 2; i++) {
			String[] data = map.get(i).split(",");
			for (int j = 0; j < col; j++) {
				mapData[i - 2][j] = Integer.parseInt(data[j]);
				if (mapData[i - 2][j] < 1) {
					mapData[i - 2][j] = 1;
				}
			}
		}
		mapGridCanvas.setMapData(mapData, area, startLat, startLong);
		
		this.mapGridCanvas.planeYcord.bind(Bindings.createDoubleBinding(
				() -> ((110.54 * (mapGridCanvas.initialLat - viewModel.planeLat.get()) 
						/ Math.sqrt(mapGridCanvas.area)) * mapGridCanvas.recSizeHeight()),viewModel.planeLat));
		this.mapGridCanvas.planeXcord.bind(Bindings.createDoubleBinding(
				() -> ((111.320 *(viewModel.planeLong.get() - mapGridCanvas.initialLong) * Math.cos(Math.toRadians(mapGridCanvas.initialLat - viewModel.planeLat.get())))
						/ Math.sqrt(mapGridCanvas.area) * mapGridCanvas.recSizeWidth()),viewModel.planeLong));
		
		mapGridCanvas.setOnMouseClicked((e) -> {
			mapGridCanvas.destinationXcord.set(e.getX());
			mapGridCanvas.destinationYcord.set(e.getY());
			
			recalculateOrRedraw();
		});

		// listen to flight gear changes
		mapGridCanvas.heading.addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
				mapGridCanvas.redraw();
			}});
		mapGridCanvas.planeXcord.addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
				recalculateOrRedraw();
			}});
		mapGridCanvas.planeYcord.addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
				recalculateOrRedraw();
			}});
	}

	@FXML
	public void onCalculatePathButtonClicked() {
		if (!viewModel.isConnectedToSolver()) {
			Dialog<Pair<String, String>> dialog = new Dialog<>();
			dialog.setTitle("Connect to solver server");
			dialog.setHeaderText("Please enter the solver server ip and port");

			ButtonType loginButtonType = new ButtonType("Connect", ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);

			TextField ip = new TextField();
			ip.setPromptText("IP");
			TextField port = new TextField();
			port.setPromptText("port");

			grid.add(new Label("IP:"), 0, 0);
			grid.add(ip, 1, 0);
			grid.add(new Label("Port:"), 0, 1);
			grid.add(port, 1, 1);

			dialog.getDialogPane().setContent(grid);
			Platform.runLater(() -> ip.requestFocus());
			

			ip.setText("127.0.0.1");
			port.setText("9000");

			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == loginButtonType)
					return new Pair<>(ip.getText(), port.getText());
				return null;
			});
			Optional<Pair<String, String>> result = dialog.showAndWait();

			result.ifPresent(serverInfo -> {
				viewModel.connectToSolver(serverInfo.getKey(), Integer.parseInt(serverInfo.getValue()));
			});
			
			if(!result.isPresent()) return;
		}

		this.mapGridCanvas.startXcord = (int) (mapGridCanvas.planeXcord.get()/mapGridCanvas.recSizeWidth());
		this.mapGridCanvas.startYcord = (int) (mapGridCanvas.planeYcord.get()/mapGridCanvas.recSizeHeight());
		
		int destinationXcord =  (int) (mapGridCanvas.destinationXcord.get() / mapGridCanvas.recSizeWidth());
		int destinationYcord = (int) (mapGridCanvas.destinationYcord.get()/ mapGridCanvas.recSizeHeight());
		viewModel.solveProblem(mapGridCanvas.mapData,mapGridCanvas.startXcord, mapGridCanvas.startYcord,destinationXcord, destinationYcord);
		this.mapGridCanvas.redraw();
	}
	
	private void recalculateOrRedraw() {
		if (viewModel.isConnectedToSolver()) {
			int destinationXcord =  (int) (mapGridCanvas.destinationXcord.get() / mapGridCanvas.recSizeWidth());
			int destinationYcord = (int) (mapGridCanvas.destinationYcord.get()/ mapGridCanvas.recSizeHeight());
			viewModel.solveProblem(mapGridCanvas.mapData,mapGridCanvas.startXcord, mapGridCanvas.startYcord,destinationXcord, destinationYcord);	
		}
		mapGridCanvas.redraw();
	}

	  //////////////////////
	 /// center methods ///
	//////////////////////
	
	@FXML
	public void onExecuteButtonClicked() {
		if (!autoPilotModeButton.isSelected()) {
			return;			
		}
		
		if (viewModel.isInterpreterBusy()) {
			viewModel.stop();			
		}
		
		viewModel.interpretCode();
	}
	
	  /////////////////////
	 /// right methods ///
	/////////////////////
	
	public void onRudderSliderChanged() {
		if (manualModeButton.isSelected() && (mapGridCanvas.serverUp.get()))
			viewModel.RudderSend();
	}

	@FXML
	public void onThrottleSliderChanged() {
		if (manualModeButton.isSelected() && (mapGridCanvas.serverUp.get()))
			viewModel.throttleSend();	
	}

	  ////////////////////////
	 /// Override methods ///
	////////////////////////
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		throttleSlider.setShowTickLabels(true);
		throttleSlider.setMinorTickCount(4);
		throttleSlider.setShowTickMarks(true);
		throttleSlider.setSnapToTicks(true);
		throttleSlider.setMajorTickUnit(0.25f);

		rudderSlider.setMajorTickUnit(0.5f);
		rudderSlider.setShowTickLabels(true);
		rudderSlider.setShowTickMarks(true);
		rudderSlider.setSnapToTicks(true);
		
		manualModeButton.setOnAction((e) -> {
			viewModel.stop();
		});
		autoPilotModeButton.setOnAction((e) -> {
			viewModel.updateInterpreter(true);
		});

		File planeImageFile = new File("assets/airplane-icon.png");
		Image planeImage = new Image("file:" + planeImageFile.toURI().getPath());
		File destinationImageFile = new File("assets/destination-icon.png");
		Image destinationImage = new Image("file:" + destinationImageFile.toURI().getPath());
		File arrowImageFile = new File("assets/arrow-icon.png");
		Image arrowImage = new Image("file:" + arrowImageFile.toURI().getPath());
		
		mapGridCanvas.setImages(planeImage, destinationImage, arrowImage);
 		
		ToggleGroup buttonGroup = new ToggleGroup();
		autoPilotModeButton.setToggleGroup(buttonGroup);
		manualModeButton.setToggleGroup(buttonGroup);
		
		joyStickCanvas.setMouseEventHandlers();
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// Note: this function has no implementation because of data bindings.
	}
}
