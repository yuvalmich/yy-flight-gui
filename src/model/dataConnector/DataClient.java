package model.dataConnector;

public interface DataClient extends DataSetter {
	public void connect(int port, String ip);
	public void close();
}
