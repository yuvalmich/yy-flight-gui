package model.dataConnector;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class FlightDataClient implements DataClient {
	public static Socket socketConnection;

	@Override
	public void connect(int port, String ip) {
		try {
			socketConnection = new Socket(ip, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void set(String propertyName, double value) {
		try {
			PrintWriter UserOutput = new PrintWriter(socketConnection.getOutputStream(), true);
			UserOutput.println("set " + propertyName + " " + value);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		try {
			PrintWriter UserOutput = new PrintWriter(socketConnection.getOutputStream(), true);
			UserOutput.println("bye");
			socketConnection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
