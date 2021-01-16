package model.solverConnector;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;

public class SolverServerHandler {
	public static Socket socketConnection;
	
	public void connect(String ip, int port) {
		try {
			socketConnection = new Socket(ip, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String solveProblem(int[][] mapGrid, int currentX, int currentY, int xDest, int yDest) {
		System.out.println("start calculate shortest path");
		String[] map = mapConverter(mapGrid);
		
		String currentLocation = locationConverter(currentX, currentY);
		String destination = locationConverter(xDest, yDest);
		
		String sol = "";
		
		try {
			PrintWriter UserOutput = new PrintWriter(socketConnection.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socketConnection.getInputStream()));
			System.out.println("Sending data to server");
			for (String line : map)
				UserOutput.println(line);
			
			UserOutput.println("end");
			
			UserOutput.println(currentLocation);
			UserOutput.println(destination);
			
			sol = in.readLine();
			System.out.println("got shortest path");
			
			return sol;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sol;
	}

	private String[] mapConverter(int[][] mapGrid) {
		String map[] = new String[mapGrid.length];

		int row, col;
		for (row = 0; row < mapGrid.length; row++) {
			map[row] = "";
			
			for (col = 0; col < mapGrid[0].length - 1; col++) {			
				map[row] += Integer.toString(mapGrid[row][col]) + ",";
			}
			
			map[row] += Integer.toString(mapGrid[row][col]);
		}
		
		return map;
	}
	
	private String locationConverter(int x, int y) {
		return x + "," + y;
	}
}
