package model.interpreter.commands;

import java.util.List;

import model.dataHandler.DataClient;
import model.dataHandler.MyDataClient;

public class ConnectCommand implements Command {
	public static DataClient client = new MyDataClient();// we only open one session towards the simulator.

	@Override
	public int getArguments(String[] tokens, int idx, List<Object> emptyList) {
		return StringToArgumentParser.parse(tokens, idx, 2, emptyList, "String", "Integer");
	}

	@Override
	public void doCommand(List<Object> args) {
		String ip = args.get(0).toString();
		int port = (int) args.get(1);
		client.connect(port, ip);
		try {
			
			Thread.sleep(55000);
		    } catch (InterruptedException | IllegalArgumentException e ){e.printStackTrace();}	
		System.out.println("done sleeping, start interpreting...!");
	}

}
