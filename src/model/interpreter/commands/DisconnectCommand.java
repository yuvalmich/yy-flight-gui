package model.interpreter.commands;

import java.util.List;

import model.dataHandler.DataClient;
import model.dataHandler.DataServer;
import model.dataHandler.MyDataServer;

public class DisconnectCommand implements Command {
	@Override
	public int getArguments(String[] tokens, int idx, List<Object> emptyList) {
		return 0;
	}

	@Override
	public void doCommand(List<Object> args) {
		DataClient client = ConnectCommand.client;
		Object lock = MyDataServer.lock;
		DataServer ds = MyDataServer.getServer();
		ds.close();
		synchronized (lock)// we ensure the server has received all the relevant changes from the
		{// simulator client so we need to wait for it to finish reading before the
			// simulator
			// client closes.
			try {
				lock.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		client.close();

		// is the simulator server closing before the client?
		// assume it isn't

		// the simulator's server needs to send an ack too so we wont close before it
		// and result in timeout in the simulator's side.

	}

}