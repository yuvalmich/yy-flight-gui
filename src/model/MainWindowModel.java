package model;

import java.util.Observable;

import model.pathSolver.PathSolver;

public class MainWindowModel extends Observable {
	public PathSolver pathSolver;
	
	public MainWindowModel( ) {
		pathSolver = new PathSolver();
	}

	  ////////////////////////
	 /// Public Methods ////
	//////////////////////
	
	public void connectToServer() {
		// TODO: implement.
	}
	
	public void connectToSolver(String ip, int port) {
		System.out.println("ip: "+ip+" port: "+port);
		pathSolver.connect(ip,port);	
	}
	
	public boolean isConnectedToSolver()
	{
		return  (PathSolver.connection!=null);
	}
	
	public void solveProblem(int[][] mapGrid, int currentX, int currentY, int xDest, int yDest)
	{
		String sol = pathSolver.solveProblem(mapGrid,currentX,currentY,xDest, yDest);
		if(sol == "") {System.out.println("ERROR ON READING LINE");}
		passSolution(sol);
	}
	
	  ///////////////////////
	 /// Private Methods ///
	///////////////////////
	
	private void passSolution(String solution)
	{
		String data="gotSolution "+solution;
		setChanged();
		notifyObservers(data);
	}
}
