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

	MainWindowViewModel vm;

	@FXML
	Button LoadDataButton;
	@FXML
	Button ConnectButton;
	@FXML
	Button ExecuteButton;
	@FXML
	Button CalculatePathButton;
	@FXML
	RadioButton AutoPilotButton;
	@FXML
	RadioButton ManualButton;

	@FXML
	JoyStick JoyStickCanvas;
	@FXML
	MapGrid GridCanvas;

	@FXML
	TextArea CommandLineTextArea;
	@FXML
	TextArea PrintTextArea;
	@FXML
	Slider ThrottleSlider;
	@FXML
	Slider RudderSlider;

	public void setViewModel(MainWindowViewModel vm) {
		this.vm = vm;
		this.vm.rudderVal.bind(RudderSlider.valueProperty());
		this.vm.throttleVal.bind(ThrottleSlider.valueProperty());
		this.vm.commandLineText.bind(CommandLineTextArea.textProperty());
		PrintTextArea.textProperty().addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
				PrintTextArea.setScrollTop(Double.MAX_VALUE); // this will scroll to the bottom
				// use Double.MIN_VALUE to scroll to the top
			}
		});
		this.vm.printAreaText.addListener(new ChangeListener<String>() {
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				PrintTextArea.textProperty().set(newValue);
				PrintTextArea.appendText("");
			}
		});

		this.vm.aileronVal.bind(JoyStickCanvas.aileron);
		this.vm.elevatorVal.bind(JoyStickCanvas.elevator);
		this.GridCanvas.solution.bind(vm.solution);

		this.GridCanvas.heading.bind(this.vm.heading);
		this.GridCanvas.serverUp.bind(this.vm.serverUp);
		JoyStickCanvas.aileron.addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (ManualButton.isSelected()&& (GridCanvas.serverUp.get()))
					vm.aileronSend();			
			}
		});

		JoyStickCanvas.elevator.addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (ManualButton.isSelected()&& (GridCanvas.serverUp.get()))
					vm.elevatorSend();
				
			}
		});

	}

	public void onRudderSliderChanged() {
		if (ManualButton.isSelected()&& (GridCanvas.serverUp.get()))
			vm.RudderSend();
		
	}

	@FXML
	public void onThrottleSliderChanged() {
		if (ManualButton.isSelected()&& (GridCanvas.serverUp.get()))
			vm.throttleSend();	
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
			vm.connectToSimulator(serverInfo.getKey(), Integer.parseInt(serverInfo.getValue()));
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
			GridCanvas.setMapData(mapData, area, initialLat, initialLong);
			
			this.GridCanvas.planeYcord.bind(Bindings.createDoubleBinding(
					() -> ((110.54 * (GridCanvas.initialLat - vm.planeLatCord.get()) 
							/ Math.sqrt(GridCanvas.area)) * GridCanvas.recSizeHeight()),vm.planeLatCord));
			this.GridCanvas.planeXcord.bind(Bindings.createDoubleBinding(
					() -> ((111.320 *(vm.planeLongCord.get() - GridCanvas.initialLong) * Math.cos(Math.toRadians(GridCanvas.initialLat - vm.planeLatCord.get())))
							/ Math.sqrt(GridCanvas.area) * GridCanvas.recSizeWidth()),vm.planeLongCord));
			
			GridCanvas.setOnMouseClicked((e) -> {
				GridCanvas.destinationXcord.set(e.getX());
				GridCanvas.destinationYcord.set(e.getY());
				GridCanvas.redraw();
			});

			// whenever positions change, redraw the map.
			GridCanvas.planeXcord.addListener(new ChangeListener<Object>() {
				@Override
				public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
					GridCanvas.redraw();
				}});
			GridCanvas.planeYcord.addListener(new ChangeListener<Object>() {
				@Override
				public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
					GridCanvas.redraw();
				}});
			GridCanvas.heading.addListener(new ChangeListener<Object>() {
				@Override
				public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
					GridCanvas.redraw();
				}});
		}
	}

	@FXML
	public void calculatePathPressed() {
		if (!vm.isConnectedToSolver()) {
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
				vm.connectToSolver(serverInfo.getKey(), Integer.parseInt(serverInfo.getValue()));
			});
			
			if(!result.isPresent()) return;
		}

		// if already connected.
		// lat 63.9918
		// long -22.6054
//		this.GridCanvas.startXcord = (int) (GridCanvas.planeXcord.get()/GridCanvas.recSizeWidth());
//		this.GridCanvas.startYcord = (int) (GridCanvas.planeYcord.get()/GridCanvas.recSizeHeight());
		
		this.GridCanvas.startXcord = 50;
		this.GridCanvas.startYcord = 50;
		
		int destinationXcord =  (int) (GridCanvas.destinationXcord.get() / GridCanvas.recSizeWidth());
		int destinationYcord = (int) (GridCanvas.destinationYcord.get()/ GridCanvas.recSizeHeight());
		vm.solveProblem(GridCanvas.mapData,GridCanvas.startXcord, GridCanvas.startYcord,destinationXcord, destinationYcord);
		this.GridCanvas.redraw();
	}

	@FXML
	public void ExecutePressed() {
		if (!AutoPilotButton.isSelected())
			return;
		if (vm.interpreterBusy())
			vm.stop();
		// takes down the current thread and allows another new context of interpretation to run.
		vm.printAreaText.set("");
		vm.interpretText();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		PrintTextArea.setEditable(false);

		RudderSlider.setShowTickLabels(true);
		RudderSlider.setShowTickMarks(true);
		RudderSlider.setMajorTickUnit(0.5f);
		RudderSlider.setSnapToTicks(true);

		ThrottleSlider.setShowTickLabels(true);
		ThrottleSlider.setShowTickMarks(true);
		ThrottleSlider.setMajorTickUnit(0.25f);
		ThrottleSlider.setMinorTickCount(4);
		ThrottleSlider.setSnapToTicks(true);

		ManualButton.setOnAction((e) -> {
			vm.stop();
		});
		AutoPilotButton.setOnAction((e) -> {
			vm.updateInterpreter(true);
		});

		File planeImageFile = new File("resources/airplane-icon.png");
		Image planeImage = new Image("file:" + planeImageFile.toURI().getPath());
		File destinationImageFile = new File("resources/destination-icon.png");
		Image destinationImage = new Image("file:" + destinationImageFile.toURI().getPath());
		File arrowImageFile = new File("resources/arrow-icon.png");
		Image arrowImage = new Image("file:" + arrowImageFile.toURI().getPath());
		GridCanvas.setImages(planeImage, destinationImage, arrowImage);
		PrintTextArea.setEditable(false);
		ToggleGroup buttonGroup = new ToggleGroup();
		AutoPilotButton.setToggleGroup(buttonGroup);
		ManualButton.setToggleGroup(buttonGroup);
		JoyStickCanvas.setMouseEventHandlers();
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub

	}
}
