package model.dataConnector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.net.ServerSocket;
import java.net.Socket;

import model.MainWindowModel;

public class FlightDataServer implements DataServer {
	private ConcurrentHashMap<String, Double> values;
	private volatile boolean open;
	public static Object lock;
	public static String[] paths = definePaths();

	private static class MyServerHolder {
		public static final FlightDataServer ds = new FlightDataServer();
	}
	
	// private ctor for the singleton.
	private FlightDataServer() {
		values = new ConcurrentHashMap<String, Double>();
		open = false;
	}

	public static DataServer getServer() {
		return MyServerHolder.ds;
	}
	
	public static String[] definePaths() {
		String[] propertiesPath = new String[25];
		propertiesPath[0] = "/instrumentation/airspeed-indicator/indicated-speed-kt";
		propertiesPath[1] = "/instrumentation/altimeter/indicated-altitude-ft";
		propertiesPath[2] = "/instrumentation/altimeter/pressure-alt-ft";
		propertiesPath[3] = "/instrumentation/attitude-indicator/indicated-pitch-deg";
		propertiesPath[4] = "/instrumentation/attitude-indicator/indicated-roll-deg";
		propertiesPath[5] = "/instrumentation/attitude-indicator/internal-pitch-deg";
		propertiesPath[6] = "/instrumentation/attitude-indicator/internal-roll-deg";
		propertiesPath[7] = "/instrumentation/encoder/indicated-altitude-ft";
		propertiesPath[8] = "/instrumentation/encoder/pressure-alt-ft";
		propertiesPath[9] = "/instrumentation/gps/indicated-altitude-ft";
		propertiesPath[10] = "/instrumentation/gps/indicated-ground-speed-kt";
		propertiesPath[11] = "/instrumentation/gps/indicated-vertical-speed";
		propertiesPath[12] = "/instrumentation/heading-indicator/indicated-heading-deg";
		propertiesPath[13] = "/instrumentation/magnetic-compass/indicated-heading-deg";
		propertiesPath[14] = "/instrumentation/slip-skid-ball/indicated-slip-skid";
		propertiesPath[15] = "/instrumentation/turn-indicator/indicated-turn-rate";
		propertiesPath[16] = "/instrumentation/vertical-speed-indicator/indicated-speed-fpm";
		propertiesPath[17] = "/controls/flight/aileron";
		propertiesPath[18] = "/controls/flight/elevator";
		propertiesPath[19] = "/controls/flight/rudder";
		propertiesPath[20] = "/controls/flight/flaps";
		propertiesPath[21] = "/controls/engines/engine/throttle";
		propertiesPath[22] = "/engines/engine/rpm";
		propertiesPath[23]= "/position/longitude-deg";
		propertiesPath[24]="/position/latitude-deg";
		return propertiesPath;
	}

	@Override
	public double get(String path) {	
		return values.get(path);
	}

	@Override
	public void open(int port, int freq, Object lock) {
		if (open)
			return;
		
		FlightDataServer.lock = lock;
		open = true;
		
		Thread serverThread = new Thread(() -> {
			try {
				ServerSocket server = new ServerSocket(port);
				
				System.out.println("waiting for the flight gear simulator...");
				Socket simulator = server.accept();
				System.out.println("flight gear connected!");
				
				BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(simulator.getInputStream()));
				
				// verify get data executed at least once before accessing bounded data
				recievedData(inputFromClient, freq);
				
				System.out.println("got data from simulator!");
			
				DataSynchronizer.resume(lock);
				
				MainWindowModel model = MainWindowModel.getInstance();
				model.notifyDataServerAvailable();
				
				while (open) {
					this.recievedData(inputFromClient, freq);
				}
				
				// prevent simulator get data bug.
				for (int x = 0; x < 10; x++) {
					this.recievedData(inputFromClient, freq);
				}
				
				DataSynchronizer.resume(lock);
				simulator.close();
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		});
		serverThread.start();
	}

	public void recievedData(BufferedReader inputFromClient, int freq) {
		try {
			String[] updatedValues = inputFromClient.readLine().split(",");
			
			for (int i = 0; i < updatedValues.length; i++) {
				double value = Double.parseDouble(updatedValues[i]);
				Double oldValue = values.get(paths[i]);
				
				if ((oldValue == null) || !(oldValue.equals(value))) {
					values.put(paths[i], value);					
				}
			}
			Thread.sleep(1000 / freq);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		this.open = false;
	}
}
