package model.dataHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import model.Model;

public class MyDataServer implements DataServer {

	private ConcurrentHashMap<String, Double> values;
	private volatile boolean open;
	public static Object lock;
	public static String[] paths = definePaths();

	private static class MyServerHolder {
		public static final MyDataServer ds = new MyDataServer();
	}

	public static String[] definePaths() {
		String[] paths = new String[25];
		paths[0] = "/instrumentation/airspeed-indicator/indicated-speed-kt";
		paths[1] = "/instrumentation/altimeter/indicated-altitude-ft";
		paths[2] = "/instrumentation/altimeter/pressure-alt-ft";
		paths[3] = "/instrumentation/attitude-indicator/indicated-pitch-deg";
		paths[4] = "/instrumentation/attitude-indicator/indicated-roll-deg";
		paths[5] = "/instrumentation/attitude-indicator/internal-pitch-deg";
		paths[6] = "/instrumentation/attitude-indicator/internal-roll-deg";
		paths[7] = "/instrumentation/encoder/indicated-altitude-ft";
		paths[8] = "/instrumentation/encoder/pressure-alt-ft";
		paths[9] = "/instrumentation/gps/indicated-altitude-ft";
		paths[10] = "/instrumentation/gps/indicated-ground-speed-kt";
		paths[11] = "/instrumentation/gps/indicated-vertical-speed";
		paths[12] = "/instrumentation/heading-indicator/indicated-heading-deg";
		paths[13] = "/instrumentation/magnetic-compass/indicated-heading-deg";
		paths[14] = "/instrumentation/slip-skid-ball/indicated-slip-skid";
		paths[15] = "/instrumentation/turn-indicator/indicated-turn-rate";
		paths[16] = "/instrumentation/vertical-speed-indicator/indicated-speed-fpm";
		paths[17] = "/controls/flight/aileron";
		paths[18] = "/controls/flight/elevator";
		paths[19] = "/controls/flight/rudder";
		paths[20] = "/controls/flight/flaps";
		paths[21] = "/controls/engines/engine/throttle";
		paths[22] = "/engines/engine/rpm";
		paths[23]= "position/longitude-deg";
		paths[24]="position/latitude-deg";
		return paths;
	}

	private MyDataServer() {
		values = new ConcurrentHashMap<String, Double>();
		open = false;
	}

	public static DataServer getServer() {
		return MyServerHolder.ds;
	}

	@Override
	public double get(String path) {	
		return values.get(path);
	}

	@Override
	public void open(int port, int freq, Object lock) {
		if (open)
			return;
		MyDataServer.lock = lock;
		open = true;
		Thread server_thread = new Thread(() -> {

			try {
				ServerSocket server = new ServerSocket(port);
				// server.setSoTimeout(3000);
				System.out.println("waiting for the client... (open the simulator)");
				System.out.println("trying to connect..!");
				Socket aClient = server.accept();
				System.out.println("client connected!");
				System.out.println("helllo!");
				InputStream in = aClient.getInputStream();
				BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(in));
				
				//makes sure we get data at least once before trying to access bounded data
				data_recieve(inputFromClient, freq);
				System.out.println("got the first set of data!");
			
				// makes sure that the main thread waits for the server to boot-up and function.
				// causes the main thread to wake up.
				DataSynchronizer.resume(lock);
				Model m = Model.getInstance();
				m.notifyDataServerAvailable();
				while (open) {
					this.data_recieve(inputFromClient, freq);
				}
				// since the client doesn't let us know the end of the relevant information we
				// need to keep reading a little more
				for (int x = 0; x < 10; x++) {
					this.data_recieve(inputFromClient, freq);
				}
				// causes the main thread to wait for all of the relevant information to arrive and then continue. 
				// tell the interpreter thread it's okay to keep going now.
				DataSynchronizer.resume(lock);
				aClient.close();
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		});
		server_thread.start();

	}

	public void data_recieve(BufferedReader inputFromClient, int freq) {
		try {
			String[] new_values;
			new_values = inputFromClient.readLine().split(",");
			for (int i = 0; i < new_values.length; i++) {
				double value = Double.parseDouble(new_values[i]);
				Double v = values.get(paths[i]);
				if ((v == null) || !(v.equals(value)))
					values.put(paths[i], value);
			}
			try {
				Thread.sleep(1000 / freq);
			} catch (InterruptedException e) {e.printStackTrace();}
		} catch (IOException e1) {e1.printStackTrace();}
	}

	@Override
	public void close() {
		if (!open)
			return;
		this.open = false;
	}

}
