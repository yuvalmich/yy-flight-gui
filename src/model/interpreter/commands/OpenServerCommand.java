package model.interpreter.commands;
import java.util.*;
import model.dataHandler.*;
public class OpenServerCommand implements Command {
private DataServer server;
	@Override
	public int getArguments(String[] tokens, int idx, List<Object> emptyList) {
		return StringToArgumentParser.parse(tokens, idx, 2, emptyList, "Integer","Integer");
	}

	@Override
	public void doCommand(List<Object> args) {
		int port=(int)args.get(0);
		int freq=(int)args.get(1);
		
		this.server=MyDataServer.getServer();//object created for the first time 
		Object lock=new Object();//passed to the server to make this thread to wake up when needed 
		server.open(port,freq,lock);
		DataSynchronizer.waitForData(lock);//makes sure that our server first opens and recieves data before we might execute instructions
		//that rely on this data.
	 	
	}	
}