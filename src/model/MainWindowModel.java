package model;

import java.util.Observable;

import model.flightGearConnector.FlightGearConnector;
import model.interpreter.MyInterpreter;
import model.solverConnector.SolverServerHandler;


public class MainWindowModel extends Observable {
	public SolverServerHandler solverHandler;
	public FlightGearConnector flightGearHandler;
	public MyInterpreter interpreter;	
	
	private static class ModelHolder {
		public static final MainWindowModel model = new MainWindowModel();
	}
	
	private MainWindowModel() {
		this.solverHandler=new SolverServerHandler();
		this.flightGearHandler = new FlightGearConnector();
		this.interpreter = new MyInterpreter();
	}

	public static MainWindowModel getInstance() {
		return ModelHolder.model;
	}
	
	  /////////////////
	 /// flightGear //
	/////////////////
	
	public void connectToSimulator(String ip, int port) {
		flightGearHandler.connect(ip, port);
	}

	public void setflightVar(String path, double value) {
		flightGearHandler.dataClient.set(path, value);
	}
	
	public double getHeading() {
		return flightGearHandler.dataServer.get("/instrumentation/magnetic-compass/indicated-heading-deg");
	}
	
	public double getPlaneLatCord() {
		return flightGearHandler.dataServer.get("/position/latitude-deg");
	}
	
	public double getPlaneLongCord() {
		return flightGearHandler.dataServer.get("/position/longitude-deg");
	}

	  //////////////////
	 /// interpreter //
	//////////////////
	
	public void interpretCode(String code) {
		interpreter.interpret(code);
	}
	
	public boolean isInterpreterBusy() {
		return interpreter.interpreterBusy();
	}
	
	public void stop() {
		interpreter.stop();
	}
	
	public void updateIntepreterStatus(boolean state) {
		MyInterpreter.enabled = state;
	}
	
	  //////////////
	 /// solver ///
	//////////////
	
	public void connectToSolver(String ip, int port) {
		System.out.println("connecting to solver");
		solverHandler.connect(ip,port);	
		System.out.println("solver connected");
	}
	
	public boolean isConnectedToSolver() {
		return  (SolverServerHandler.socketConnection!=null);
	}
	
	public void solveProblem(int[][] mapGrid, int currentX, int currentY, int xDest, int yDest) {
		String sol = solverHandler.solveProblem(mapGrid,currentX,currentY,xDest, yDest);
		
		if (sol == "") {
			System.out.println("Something went wrong.");
		}
		
		passSolution(sol);
	}


	  /////////////////
	 /// notifiers ///
	/////////////////
	
	public void notifyDataServerAvailable() {
		String data="DataServerAvailable";
		setChanged();
		notifyObservers(data);
	}
	
	public void passSolution(String solution) {
		String data="gotSolution " + solution;
		setChanged();
		notifyObservers(data);
	}

}
