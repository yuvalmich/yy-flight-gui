package model.dataConnector;

public class DataSynchronizer {
	public static void waitForData(Object lock) {
		try {
			synchronized (lock) {
				lock.wait();
			}
		} catch (InterruptedException e) {
			System.out.println("Something went worng.");
		}
	}
	
	public static void resume(Object lock) {
		synchronized (lock) {
			lock.notify();
		}
	}
}
