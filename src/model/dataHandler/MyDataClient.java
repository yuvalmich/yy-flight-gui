package model.dataHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class MyDataClient implements DataClient {
	public static Socket connection;

	@Override
	public void connect(int port, String ip) {
		try {
			connection = new Socket(ip, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void set(String name, double value) {
		try {
			OutputStream out = connection.getOutputStream();
			PrintWriter UserOutput = new PrintWriter(out, true);
			UserOutput.println("set" + " " + name + " " + value);
			// after we send the value to the server it will make my local symbol-table change as well
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		// send the 'bye' ack to the simulator's server
		try {
			OutputStream out = connection.getOutputStream();
			PrintWriter UserOutput = new PrintWriter(out, true);
			UserOutput.println("bye");
			// client and simulator server are now closed.
		} catch (IOException e) {
			e.printStackTrace();
		}
		// closes itself
		try {
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
