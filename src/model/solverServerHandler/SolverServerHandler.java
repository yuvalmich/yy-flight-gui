package model.solverServerHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import model.MainWindowModel;

public class SolverServerHandler {
	public static Socket connection;
	MainWindowModel m=MainWindowModel.getInstance();

	public void connect(String ip, int port) {
		try {
			connection = new Socket(ip, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String solveProblem(int[][] mapGrid, int currentX, int currentY, int xDest, int yDest) {
		String map[] = formatMap(mapGrid);
		String currentPos = formatLocation(currentX, currentY);
		String destPos = formatLocation(xDest, yDest);
		/*
		for(String line:map) System.out.println(line);
		*/
		System.out.println("CURRENT: "+currentPos);
		System.out.println("DEST: "+destPos);
		String sol = "";
		try {
			OutputStream out = connection.getOutputStream();
			PrintWriter UserOutput = new PrintWriter(out, true);
			BufferedReader in=new BufferedReader(new InputStreamReader(connection.getInputStream()));
			for (String line : map)
				UserOutput.println(line);
			UserOutput.println("end");
			UserOutput.println(currentPos);
			UserOutput.println(destPos);
			sol = in.readLine();
			//System.out.println(sol);
			return sol;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sol;
	}

	private String[] formatMap(int[][] mapGrid) {
		int j;
		String map[] = new String[mapGrid.length];
		
		for (int i = 0; i < mapGrid.length; i++) {
			map[i]="";
			for (j = 0; j < mapGrid[0].length - 1; j++) {			
				map[i] += Integer.toString(mapGrid[i][j]) + ",";
			}
			map[i] += Integer.toString(mapGrid[i][j]);
		}
		return map;
	}
	// converts the actual position on the canvas onto the cell dimensions
	private String formatLocation(int x, int y) {
		return x + "," + y;
	}

	public void close() {

	}

}
