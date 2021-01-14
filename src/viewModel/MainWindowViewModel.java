package viewModel;

import java.util.Observable;
import java.util.Observer;
import java.util.StringJoiner;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import model.MainWindowModel;

public class MainWindowViewModel extends Observable implements Observer {
	MainWindowModel model;
	
	public StringProperty solution;
	public DoubleProperty planeLatCord, planeLongCord;
	
	public MainWindowViewModel(MainWindowModel model) {
		this.model = model;
		
		planeLatCord = new SimpleDoubleProperty();
		planeLongCord = new SimpleDoubleProperty();
		solution = new SimpleStringProperty("");
	}
	
	public void connectToServer(){
		this.model.connectToServer();
	}
	
	public void connectToSolver(String ip, int port) {
		model.connectToSolver(ip, port);
	}
	
	public boolean isConnectedToSolver()
	{
		return model.isConnectedToSolver();
	}
	
	public void solveProblem(int[][] mapGrid, int currentX, int currentY, int xDest, int yDest)
	{
		model.solveProblem(mapGrid,currentX,currentY,xDest, yDest);	
	}

	@Override
	public void update(Observable o, Object arg) {
		
		String []data=arg.toString().split(" ");
		String action=data[0];
		StringJoiner sj=new StringJoiner(" ");
		for(int i=1;i<data.length;i++) sj.add(data[i]);	
		String value= sj.toString();
       switch(action) {
	       case("gotSolution"):
	    	   this.solution.set(value);  
	    	   break;
       }
	}
}