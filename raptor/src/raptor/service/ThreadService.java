package raptor.service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;

public class ThreadService {
	private static final Log LOG = LogFactory.getLog(ThreadService.class);
	private static final ThreadService instance = new ThreadService();
	public static final String THREAD_DUMP_FILE_PATH = Raptor.USER_RAPTOR_HOME_PATH
			+ "/logs/threaddump_" + System.currentTimeMillis() + ".txt";

	public static ThreadService getInstance() {
		return instance;
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
			printWriter = new PrintWriter(new FileWriter(THREAD_DUMP_FILE_PATH,
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

	ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(50) {
		@Override
		protected void afterExecute(Runnable arg0, Throwable arg1) {
			if (arg1 != null) {
				LOG.error("Error executing runnable: ", arg1);
				Raptor.getInstance().onError(
						"Error in ThreadService Runnable.", arg1);
			}
			super.afterExecute(arg0, arg1);
		}
	};

	public ThreadService() {
	}

	public void dispose() {
		executor.shutdownNow();
	}

	/**
	 * Executes a runnable asynch in a controlled way. Exceptions are monitored
	 * and displayed if they occur.
	 */
	public void run(Runnable runnable) {
		try {
			executor.execute(runnable);
		} catch (RejectedExecutionException rej) {
			LOG.error("Error executing runnable: ", rej);
			threadDump();
			Raptor.getInstance().onError(
					"ThreadServie has no more threads. A thread dump can be found at "
							+ THREAD_DUMP_FILE_PATH, rej);
		}
	}

	/**
	 * Runs the runnable one time after a delay. Exceptions are monitored and
	 * displayed if they occur.
	 * 
	 * @param delay
	 *            Delay in millis
	 * @param runnable
	 *            The runnable.
	 */
	public void scheduleOneShot(long delay, Runnable runnable) {
		try {
			executor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException rej) {
			LOG.error("Error executing runnable in scheduleOneShot: ", rej);
			threadDump();
			Raptor.getInstance().onError(
					"ThreadServie has no more threads. A thread dump can be found at "
							+ THREAD_DUMP_FILE_PATH);
		}
	}
}
