package raptor.service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThreadService {
	private static final Log LOG = LogFactory.getLog(ThreadService.class);
	private static final ThreadService instance = new ThreadService();

	public static ThreadService getInstance() {
		return instance;
	}

	ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 20, 60,
			TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20));

	public ThreadService() {
	}

	public void dispose() {
		threadPoolExecutor.shutdownNow();
	}

	public void run(Runnable runnable) {
		try {
			threadPoolExecutor.execute(runnable);
			if (LOG.isDebugEnabled()) {
				LOG.info("Exected thread poolsize="
						+ threadPoolExecutor.getPoolSize() + " activeCount="
						+ threadPoolExecutor.getActiveCount() + " maxSize="
						+ threadPoolExecutor.getMaximumPoolSize());
			}
		} catch (RejectedExecutionException rej) {
			LOG
					.error("All threads are in use. Logging the thread stack trace to threaddumnp.txt and exiting.");
			final ThreadMXBean threads = ManagementFactory.getThreadMXBean();
			long[] threadIds = threads.getAllThreadIds();
			PrintWriter printWriter = null;
			try {
				printWriter = new PrintWriter(new FileWriter("threaddump.txt",
						false));
				printWriter.println("Raptor ThreadService initiated dump "
						+ new Date());
				for (int i = 0; i < threadIds.length; i++) {
					ThreadInfo threadInfo = threads.getThreadInfo(threadIds[i],
							10);
					printWriter.println("Thread " + threadInfo.getThreadName()
							+ " Block time:" + threadInfo.getBlockedTime()
							+ " Block count:" + threadInfo.getBlockedCount()
							+ " Lock name:" + threadInfo.getLockName()
							+ " Waited Count:" + threadInfo.getWaitedCount()
							+ " Waited Time:" + threadInfo.getWaitedTime()
							+ " Is Suspended:" + threadInfo.isSuspended());
					StackTraceElement[] stackTrace = threadInfo.getStackTrace();
					for (int j = 0; j < stackTrace.length; j++) {
						printWriter.println(stackTrace[j]);
					}

				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				if (printWriter != null) {
					try {
						printWriter.flush();
						printWriter.close();
					} catch (Exception e2) {
					}
				}
			}
			System.exit(1);
		}
	}
}
