package viewModel;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.StringJoiner;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.MainWindowModel;

public class MainWindowViewModel extends Observable implements Observer {
	public StringProperty commandLineText, printAreaText, solution; // these are observable values
	public DoubleProperty throttleVal, rudderVal, planeLatCord, planeLongCord, aileronVal, elevatorVal;
	public DoubleProperty heading;
	volatile boolean dataServAvailable;
	public BooleanProperty serverUp;
	MainWindowModel model;

	public MainWindowViewModel(MainWindowModel model) {
		this.model = model;
		commandLineText = new SimpleStringProperty();
		printAreaText = new SimpleStringProperty();
		solution = new SimpleStringProperty("");
		throttleVal = new SimpleDoubleProperty();
		rudderVal = new SimpleDoubleProperty();
		planeLatCord = new SimpleDoubleProperty();
		planeLongCord = new SimpleDoubleProperty();
		aileronVal = new SimpleDoubleProperty();
		elevatorVal = new SimpleDoubleProperty();
		heading = new SimpleDoubleProperty();
		serverUp = new SimpleBooleanProperty(false);
		dataServAvailable = false;
	}

	public void RudderSend() {
		model.setVar("/controls/flight/rudder", rudderVal.get());
	}

	public void throttleSend() {
		model.setVar("/controls/engines/current-engine/throttle", throttleVal.get());
	}

	public void aileronSend() {
		model.setVar("/controls/flight/aileron", aileronVal.get());
	}

	public void elevatorSend() {
		model.setVar("/controls/flight/elevator", elevatorVal.get());
	}

	public void connectToSimulator(String ip, int port) {
		model.connectToSimulator(ip, port);
	}

	public void interpretText() {
		model.interpretText(this.commandLineText.get());
	}

	public boolean interpreterBusy() {
		return model.interpreterBusy();
	}

	public void stop() {
		model.stop();
	}

	public void updateInterpreter(boolean state) {
		model.updateIntepreter(state);
	}

	@Override
	public void update(Observable o, Object arg) {

		String[] data = arg.toString().split(" ");
		String action = data[0];
		StringJoiner sj = new StringJoiner(" ");
		for (int i = 1; i < data.length; i++)
			sj.add(data[i]);
		String value = sj.toString();
		switch (action) {
		case ("print"):
			String existing_print = this.printAreaText.get();
			if (existing_print == null)
				existing_print = "";
			this.printAreaText.set(existing_print + value + "\n");
			break;
		case ("DataServerAvailable"):
			serverUp.setValue(true);
			dataServAvailable = true;
			// inform Eli we added those 2 variables to generic_small
			new Thread(() -> {
				while (dataServAvailable) {
					double tmp = model.getPlaneLongCord();
					planeLongCord.set(tmp);
					heading.set(model.getHeading());
					planeLatCord.set(model.getPlaneLatCord());
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
			break;
		case ("gotSolution"):
			this.solution.set(value);
			break;

		}
	}

	public void connectToSolver(String ip, int port) {
		model.connectToSolver(ip, port);
	}

	public boolean isConnectedToSolver() {
		return (model.isConnectedToSolver());
	}

	public void solveProblem(int[][] mapGrid, int currentX, int currentY, int xDest, int yDest) {
		model.solveProblem(mapGrid, currentX, currentY, xDest, yDest);
	}
}
