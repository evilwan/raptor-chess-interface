package raptor.service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThreadService {
	private static final Log LOG = LogFactory.getLog(ThreadService.class);
	private static final ThreadService instance = new ThreadService();

	public static ThreadService getInstance() {
		return instance;
	}

	ExecutorService executorService = Executors.newFixedThreadPool(20);

	ScheduledExecutorService scheduledExecutorService = Executors
			.newScheduledThreadPool(40);

	public ThreadService() {
	}

	public void dispose() {
		executorService.shutdownNow();
		scheduledExecutorService.shutdown();
	}

	/**
	 * Runs the runnable one time after a delay.
	 * 
	 * @param delay
	 *            Delay in millis
	 * @param runnable
	 *            The runnable.
	 */
	public void scheduleOneShot(long delay, Runnable runnable) {
		try {
			scheduledExecutorService.schedule(runnable, delay,
					TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException rej) {
			threadDump();
			System.exit(1);
		}
	}

	/**
	 * Dumps stack traces of all threads to threaddump.txt.
	 */
	public static void threadDump() {
		LOG
				.error("All threads are in use. Logging the thread stack trace to threaddump.txt and exiting.");
		final ThreadMXBean threads = ManagementFactory.getThreadMXBean();
		long[] threadIds = threads.getAllThreadIds();
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(new FileWriter("threaddump.txt",
					false));
			printWriter.println("Raptor ThreadService initiated dump "
					+ new Date());
			for (int i = 0; i < threadIds.length; i++) {
				ThreadInfo threadInfo = threads.getThreadInfo(threadIds[i], 10);
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
	}

	/**
	 * Executes a runnable asynchly.
	 */
	public void run(Runnable runnable) {
		try {
			executorService.execute(runnable);
		} catch (RejectedExecutionException rej) {
			threadDump();
			System.exit(1);
		}
	}
}
