package model.dataConnector;

public interface DataServer extends DataGetter {
	void open(int port, int freq, Object lock);
    void close();
}
