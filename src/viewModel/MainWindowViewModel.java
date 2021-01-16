package viewModel;


import java.util.StringJoiner;
import java.util.Observer;
import java.util.Observable;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

import model.MainWindowModel;

public class MainWindowViewModel extends Observable implements Observer {
	MainWindowModel model;
	
	public StringProperty cliText, printAreaText, solution;
	public DoubleProperty throttle, rudder, planeLat, planeLong, aileron, elevator, heading;
	public BooleanProperty serverUp;
	
	volatile boolean dataServAvailable;

	public MainWindowViewModel(MainWindowModel model) {
		this.model = model;
		
		cliText = new SimpleStringProperty();
		printAreaText = new SimpleStringProperty();
		solution = new SimpleStringProperty();
		
		throttle = new SimpleDoubleProperty();
		rudder = new SimpleDoubleProperty();
		planeLat = new SimpleDoubleProperty();
		planeLong = new SimpleDoubleProperty();
		aileron = new SimpleDoubleProperty();
		elevator = new SimpleDoubleProperty();
		heading = new SimpleDoubleProperty();
		
		serverUp = new SimpleBooleanProperty(false);
		dataServAvailable = false;
	}

	  /////////////////
	 /// flightGear //
	/////////////////
	
	public void RudderSend() {
		model.setflightVar("/controls/flight/rudder", rudder.get());
	}

	public void throttleSend() {
		model.setflightVar("/controls/engines/current-engine/throttle", throttle.get());
	}

	public void aileronSend() {
		model.setflightVar("/controls/flight/aileron", aileron.get());
	}

	public void elevatorSend() {
		model.setflightVar("/controls/flight/elevator", elevator.get());
	}

	public void connectToSimulator(String ip, int port) {
		model.connectToSimulator(ip, port);
	}

	  //////////////////
	 /// interpreter //
	//////////////////
	
	public void interpretCode() {
		model.interpretCode(this.cliText.get());
	}
	
	public boolean isInterpreterBusy()
	{
		return model.isInterpreterBusy();
	}
	
	public void updateInterpreter(boolean state) {
		model.updateIntepreterStatus(state);
	}
	
	  //////////////
	 /// solver ///
	//////////////

	public void connectToSolver(String ip, int port) {
		model.connectToSolver(ip, port);
	}
	
	public boolean isConnectedToSolver() {
		return (model.isConnectedToSolver());
	}
	
	public void solveProblem(int[][] mapGrid, int currentX, int currentY, int xDest, int yDest) {
		model.solveProblem(mapGrid,currentX,currentY,xDest, yDest);	
	}
	
	/////////////////////
	/// data binding ///
	///////////////////
	
	@Override
	public void update(Observable o, Object arg) {
		String[] data = arg.toString().split(" ");
		
		String action = data[0];
		
		StringJoiner sj = new StringJoiner(" ");
		for(int i=1; i < data.length; i++) {
			sj.add(data[i]);	
		}
		String value= sj.toString();
		
		switch(action) {
   			case("print"):
   				handlePrint(value);
   				break;
   			case("DataServerAvailable"):
   				handleServerAvailable();
   				break;
	       case("gotSolution"):
	    	   this.solution.set(value);  
	    	   break;  
       }
	}

	private void handleServerAvailable() {
		serverUp.setValue(true);
		   dataServAvailable = true;
		   
		  new Thread(() -> {
			   while(dataServAvailable) {
				   planeLong.set(model.getPlaneLongCord());
				   planeLat.set(model.getPlaneLatCord());
				   heading.set(model.getHeading());
				   
				   try {
					Thread.sleep(500);
				   } catch (InterruptedException e) {
					   e.printStackTrace();
				   }
			   }
		   }).start();
	}

	private void handlePrint(String value) {
		String curr_text = this.printAreaText.get();
		   if(curr_text == null) {
			   curr_text = "";
		   }
		   this.printAreaText.set(curr_text + value + "\n");
	}
	
	public void stop() {
		model.stop();
	}
}
