package view;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.Scanner;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
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
	}
	
	@FXML
	public void onConnectServerButtonClicked() {
		this.viewModel.connectToServer();
	}
	
	@FXML
	public void onCalculatePathButtonClicked() {
		System.out.println("Load data button clicked");
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
		
		// TODO: continue refactor from here
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
