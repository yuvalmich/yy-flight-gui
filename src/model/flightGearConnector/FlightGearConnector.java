package model.flightGearConnector;

import java.util.Observable;
import model.dataConnector.*;

public class FlightGearConnector extends Observable {
	public DataServer dataServer;
	public DataClient dataClient;
	
	public FlightGearConnector() {		
		dataServer = FlightDataServer.getServer();
		dataClient = new FlightDataClient();
	}
	
	public void connect(String ip, int port) {
		Object lock = new Object();
		dataServer.open(5400, 10, lock);
		DataSynchronizer.waitForData(lock);	
		dataClient.connect(port, ip);
	}
}