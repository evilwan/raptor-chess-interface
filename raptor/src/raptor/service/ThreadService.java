package raptor.service;

public class ThreadService {
	private static final ThreadService instance = new ThreadService();

	public static ThreadService getInstance() {
		return instance;
	}

	public void run(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
	}

}
