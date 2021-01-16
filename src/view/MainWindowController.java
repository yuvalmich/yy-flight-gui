package view;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.*;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.Pair;
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
	@FXML
	TextArea printTextArea;
	
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
		this.viewModel.rudderVal.bind(rudderSlider.valueProperty());
		this.viewModel.throttleVal.bind(throttleSlider.valueProperty());
		this.viewModel.commandLineText.bind(commandLineTextArea.textProperty());
		printTextArea.textProperty().addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
				printTextArea.setScrollTop(Double.MAX_VALUE); // this will scroll to the bottom
				// use Double.MIN_VALUE to scroll to the top
			}
		});
		this.viewModel.printAreaText.addListener(new ChangeListener<String>() {
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				printTextArea.textProperty().set(newValue);
				printTextArea.appendText("");
			}
		});

		this.viewModel.aileronVal.bind(joyStickCanvas.aileron);
		this.viewModel.elevatorVal.bind(joyStickCanvas.elevator);
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

	public void onRudderSliderChanged() {
		if (manualModeButton.isSelected()&& (mapGridCanvas.serverUp.get()))
			viewModel.RudderSend();
		
	}

	@FXML
	public void onThrottleSliderChanged() {
		if (manualModeButton.isSelected()&& (mapGridCanvas.serverUp.get()))
			viewModel.throttleSend();	
	}

	@FXML
	public void ConnectPressed() {
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle("FlightGear Server connection");
		dialog.setHeaderText("Please insert the ip and port of the FlightGear server");

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
	//this method not used but can be use to rescale the size of the grid to have less rectangles
	public int[][] RescaleMapData(int[][] mapData, int scaleDiv) {
		int[][] scaledmapData = new int[mapData.length / scaleDiv][mapData[0].length / scaleDiv];
		for (int i = 0; i < mapData.length; i += scaleDiv) {
			for (int j = 0; j < mapData[0].length; j += scaleDiv) {
				for (int k = 0; k < scaleDiv; k++) {
					for (int l = 0; l < scaleDiv; l++) {
						scaledmapData[i / scaleDiv][j / scaleDiv] += mapData[i + k][j + l];
					}
				}
				scaledmapData[i / scaleDiv][j / scaleDiv] /= (scaleDiv * scaleDiv);
			}
		}
		return scaledmapData;
	}

	@FXML
	public void LoadDataPressed() {

		FileChooser fc = new FileChooser();
		fc.setTitle("load csv File");
		fc.setInitialDirectory(new File("./resources/maps"));
		File chosen = fc.showOpenDialog(null);
		if (chosen != null) {

			List<String> list = new LinkedList<String>();
			Scanner scanner = null;
			try {
				scanner = new Scanner(chosen);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			while (scanner.hasNext()) {
				list.add(scanner.next());
			}
			scanner.close();

			// scanning initial coordinates and the area of each cell (km^2)
			String[] coordinates = list.get(0).split(",");
			double initialLat = Double.parseDouble(coordinates[1]);
			double initialLong = Double.parseDouble(coordinates[0]);
			double area = Double.parseDouble(list.get(1).split(",")[0]);

			// Scanning the heights matrix. Each cell is measured by meters.
			int row = list.size() - 2;
			int col = list.get(2).split(",").length;
			int[][] mapData = new int[row][col];
			for (int i = 2; i < row + 2; i++) {
				String[] data = list.get(i).split(",");
				for (int j = 0; j < col; j++) {
					mapData[i - 2][j] = Integer.parseInt(data[j]);
					if (mapData[i - 2][j] < 1) {
						mapData[i - 2][j] = 1;
					}
				}
			}
			mapGridCanvas.setMapData(mapData, area, initialLat, initialLong);
			
			this.mapGridCanvas.planeYcord.bind(Bindings.createDoubleBinding(
					() -> ((110.54 * (mapGridCanvas.initialLat - viewModel.planeLatCord.get()) 
							/ Math.sqrt(mapGridCanvas.area)) * mapGridCanvas.recSizeHeight()),viewModel.planeLatCord));
			this.mapGridCanvas.planeXcord.bind(Bindings.createDoubleBinding(
					() -> ((111.320 *(viewModel.planeLongCord.get() - mapGridCanvas.initialLong) * Math.cos(Math.toRadians(mapGridCanvas.initialLat - viewModel.planeLatCord.get())))
							/ Math.sqrt(mapGridCanvas.area) * mapGridCanvas.recSizeWidth()),viewModel.planeLongCord));
			
			mapGridCanvas.setOnMouseClicked((e) -> {
				mapGridCanvas.destinationXcord.set(e.getX());
				mapGridCanvas.destinationYcord.set(e.getY());
				mapGridCanvas.redraw();
			});

			// whenever positions change, redraw the map.
			mapGridCanvas.planeXcord.addListener(new ChangeListener<Object>() {
				@Override
				public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
					mapGridCanvas.redraw();
				}});
			mapGridCanvas.planeYcord.addListener(new ChangeListener<Object>() {
				@Override
				public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
					mapGridCanvas.redraw();
				}});
			mapGridCanvas.heading.addListener(new ChangeListener<Object>() {
				@Override
				public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
					mapGridCanvas.redraw();
				}});
		}
	}

	@FXML
	public void calculatePathPressed() {
		if (!viewModel.isConnectedToSolver()) {
			Dialog<Pair<String, String>> dialog = new Dialog<>();
			dialog.setTitle("Solver Server connection");
			dialog.setHeaderText("Please insert the ip and port of Solver server");

			ButtonType loginButtonType = new ButtonType("Connect and solve", ButtonData.OK_DONE);
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

		// if already connected.
		// lat 63.9918
		// long -22.6054
//		this.GridCanvas.startXcord = (int) (GridCanvas.planeXcord.get()/GridCanvas.recSizeWidth());
//		this.GridCanvas.startYcord = (int) (GridCanvas.planeYcord.get()/GridCanvas.recSizeHeight());
		
		this.mapGridCanvas.startXcord = 50;
		this.mapGridCanvas.startYcord = 50;
		
		int destinationXcord =  (int) (mapGridCanvas.destinationXcord.get() / mapGridCanvas.recSizeWidth());
		int destinationYcord = (int) (mapGridCanvas.destinationYcord.get()/ mapGridCanvas.recSizeHeight());
		viewModel.solveProblem(mapGridCanvas.mapData,mapGridCanvas.startXcord, mapGridCanvas.startYcord,destinationXcord, destinationYcord);
		this.mapGridCanvas.redraw();
	}

	@FXML
	public void ExecutePressed() {
		if (!autoPilotModeButton.isSelected())
			return;
		if (viewModel.interpreterBusy())
			viewModel.stop();
		// takes down the current thread and allows another new context of interpretation to run.
		viewModel.printAreaText.set("");
		viewModel.interpretText();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		printTextArea.setEditable(false);

		rudderSlider.setShowTickLabels(true);
		rudderSlider.setShowTickMarks(true);
		rudderSlider.setMajorTickUnit(0.5f);
		rudderSlider.setSnapToTicks(true);

		throttleSlider.setShowTickLabels(true);
		throttleSlider.setShowTickMarks(true);
		throttleSlider.setMajorTickUnit(0.25f);
		throttleSlider.setMinorTickCount(4);
		throttleSlider.setSnapToTicks(true);

		manualModeButton.setOnAction((e) -> {
			viewModel.stop();
		});
		autoPilotModeButton.setOnAction((e) -> {
			viewModel.updateInterpreter(true);
		});

		File planeImageFile = new File("resources/airplane-icon.png");
		Image planeImage = new Image("file:" + planeImageFile.toURI().getPath());
		File destinationImageFile = new File("resources/destination-icon.png");
		Image destinationImage = new Image("file:" + destinationImageFile.toURI().getPath());
		File arrowImageFile = new File("resources/arrow-icon.png");
		Image arrowImage = new Image("file:" + arrowImageFile.toURI().getPath());
		mapGridCanvas.setImages(planeImage, destinationImage, arrowImage);
		printTextArea.setEditable(false);
		ToggleGroup buttonGroup = new ToggleGroup();
		autoPilotModeButton.setToggleGroup(buttonGroup);
		manualModeButton.setToggleGroup(buttonGroup);
		joyStickCanvas.setMouseEventHandlers();
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub

	}
}
