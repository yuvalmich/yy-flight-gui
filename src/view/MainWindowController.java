package view;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import viewModel.MainWindowViewModel;


public class MainWindowController implements Observer, Initializable {
	MainWindowViewModel viewModel;
	
	@FXML
	Button connectServerButton;
	@FXML
	Button loadDataButton;
	@FXML
	Button calculatePathButton;
	@FXML
	MapGrid GridCanvas;
	
	public void setViewModel(MainWindowViewModel vm) {
		this.viewModel = vm;
		
		this.GridCanvas.solution.bind(vm.solution);
	}
	
	@FXML
	public void onConnectServerButtonClicked() {
		this.viewModel.connectToServer();
	}
	
	@FXML
	public void onCalculatePathButtonClicked() {
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

		// if already connected.
//		this.GridCanvas.startXcord = (int) (GridCanvas.planeXcord.get()/GridCanvas.recSizeWidth());
//		this.GridCanvas.startYcord = (int) (GridCanvas.planeYcord.get()/GridCanvas.recSizeHeight());
		
		// TODO: after connect to server change it to real position
		this.GridCanvas.startXcord = 100;
		this.GridCanvas.startYcord = 100;
		
		int destinationXcord =  (int) (GridCanvas.destinationXcord.get() / GridCanvas.recSizeWidth());
		int destinationYcord = (int) (GridCanvas.destinationYcord.get()/ GridCanvas.recSizeHeight());
		viewModel.solveProblem(GridCanvas.mapData, GridCanvas.startXcord, GridCanvas.startYcord, destinationXcord, destinationYcord);
		this.GridCanvas.redraw();
	}
	
	@FXML
	public void onLoadDataButtonClicked() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Load plane map");
		fc.setInitialDirectory(new File("./assets/maps"));
		
		File chosen = fc.showOpenDialog(null);
		
		if (chosen == null) {
			return;
		}
		
		// TODO: continue refactor from here (separate to functions)
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
				() -> ((110.54 * (GridCanvas.initialLat - viewModel.planeLatCord.get()) 
						/ Math.sqrt(GridCanvas.area)) * GridCanvas.recSizeHeight()), viewModel.planeLatCord));
		this.GridCanvas.planeXcord.bind(Bindings.createDoubleBinding(
				() -> ((111.320 *(viewModel.planeLongCord.get() - GridCanvas.initialLong) * Math.cos(Math.toRadians(GridCanvas.initialLat - viewModel.planeLatCord.get())))
						/ Math.sqrt(GridCanvas.area) * GridCanvas.recSizeWidth()), viewModel.planeLongCord));
		
		GridCanvas.setOnMouseClicked((e) -> {
			GridCanvas.destinationXcord.set(e.getX());
			GridCanvas.destinationYcord.set(e.getY());
			recalculateOrUpdate();
		});

		// whenever positions change, redraw the map.
		GridCanvas.planeXcord.addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
				recalculateOrUpdate();
			}});
		GridCanvas.planeYcord.addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
				recalculateOrUpdate();
			}});
		GridCanvas.heading.addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
				GridCanvas.redraw();
			}});
	}
	
	private void recalculateOrUpdate() {
		if (viewModel.isConnectedToSolver()) {
			this.onCalculatePathButtonClicked();
		} else {
			GridCanvas.redraw();
		}
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
		File planeImageFile = new File("assets/airplane-icon.png");
		Image planeImage = new Image("file:" + planeImageFile.toURI().getPath());
		File destinationImageFile = new File("assets/destination-icon.png");
		Image destinationImage = new Image("file:" + destinationImageFile.toURI().getPath());
		File arrowImageFile = new File("assets/arrow-icon.png");
		Image arrowImage = new Image("file:" + arrowImageFile.toURI().getPath());
		
		GridCanvas.setImages(planeImage, destinationImage, arrowImage);
	}
}
