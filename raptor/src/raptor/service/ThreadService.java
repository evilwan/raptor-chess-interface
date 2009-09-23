package raptor.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThreadService {
	private static final Log LOG = LogFactory.getLog(ThreadService.class);
	private static final ThreadService instance = new ThreadService();
	ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 20, 60,
			TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20));

	public static ThreadService getInstance() {
		return instance;
	}

	public void dispose() {
		threadPoolExecutor.shutdownNow();
	}

	public void run(Runnable runnable) {
		threadPoolExecutor.execute(runnable);
		if (LOG.isDebugEnabled()) {
			LOG.info("Exected thread poolsize="
					+ threadPoolExecutor.getPoolSize() + " activeCount="
					+ threadPoolExecutor.getActiveCount() + " maxSize="
					+ threadPoolExecutor.getMaximumPoolSize());
		}
	}
}
