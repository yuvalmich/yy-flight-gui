package model.dataHandler;



public class DataSynchronizer {
	public static void waitForData(Object lock) {
		try {
			synchronized (lock) {
				lock.wait();
			}
		} catch (InterruptedException e) {
		}
	}
	
	public static void resume(Object lock) {
		synchronized (lock) {
			lock.notify();
		}
	}
}
