/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import raptor.Raptor;
import raptor.util.RaptorLogger;

/**
 * Creating your own threads should really be avoided in Raptor. Instead please
 * use this service. It contains lots of features, like showing errors to the
 * user and providing a thread dump when the pool runs out of threads.
 * 
 * This service provides exception handling, and pooling
 */
public class ThreadService {
	protected static final class RunnableExceptionDecorator implements Runnable {
		protected Runnable runnable;

		public RunnableExceptionDecorator(Runnable runnable) {
			this.runnable = runnable;
		}

		public void run() {
			try {
				runnable.run();
			} catch (Throwable t) {
				Raptor.getInstance().onError(
						"Error in ThreadService Runnable.", t);
			}
		}

	}

	private static final ThreadService instance = new ThreadService();
	private static final RaptorLogger LOG = RaptorLogger.getLog(ThreadService.class);

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
			for (long threadId : threadIds) {
				ThreadInfo threadInfo = threads.getThreadInfo(threadId, 10);
				printWriter.println("Thread " + threadInfo.getThreadName()
						+ " Block time:" + threadInfo.getBlockedTime()
						+ " Block count:" + threadInfo.getBlockedCount()
						+ " Lock name:" + threadInfo.getLockName()
						+ " Waited Count:" + threadInfo.getWaitedCount()
						+ " Waited Time:" + threadInfo.getWaitedTime()
						+ " Is Suspended:" + threadInfo.isSuspended());
				StackTraceElement[] stackTrace = threadInfo.getStackTrace();
				for (StackTraceElement element : stackTrace) {
					printWriter.println(element);
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

	ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(20);

	protected boolean isDisposed = false;

	private ThreadService() {
		executor.setCorePoolSize(25);
		executor.setMaximumPoolSize(50);
		executor.setKeepAliveTime(300, TimeUnit.SECONDS);
		executor.prestartAllCoreThreads();
	}

	public void dispose() {
		executor.shutdownNow();
		isDisposed = true;
	}

	public ScheduledThreadPoolExecutor getExecutor() {
		return executor;
	}

	/**
	 * Executes a runnable asynch in a controlled way. Exceptions are monitored
	 * and displayed if they occur.
	 */
	public void run(Runnable runnable) {
		if (!Raptor.getInstance().isDisposed() && !isDisposed) {
			try {
				executor.execute(new RunnableExceptionDecorator(runnable));
			} catch (RejectedExecutionException rej) {
				if (!Raptor.getInstance().isDisposed()) {
					LOG.error("Error executing runnable: ", rej);
					threadDump();
					Raptor.getInstance().onError(
							"ThreadServie has no more threads. A thread dump can be found at "
									+ THREAD_DUMP_FILE_PATH, rej);
				}
			}
		} else {
			LOG.info("Vetoing runnable in ThreadService, raptor is disposed. "
					+ runnable);
		}
	}

	/**
	 * Runs the runnable one time after a delay. Exceptions are monitored and
	 * displayed if they occur.
	 * 
	 * @param delay
	 *            Delay in milliseconds
	 * @param runnable
	 *            The runnable.
	 * @return The Future, may return null if there was an error scheduling the
	 *         Runnable or if execution was vetoed.
	 */
	@SuppressWarnings({ "rawtypes" })
	public Future scheduleOneShot(long delay, Runnable runnable) {
		if (!Raptor.getInstance().isDisposed() && !isDisposed) {
			try {
				return executor.schedule(new RunnableExceptionDecorator(
						runnable), delay, TimeUnit.MILLISECONDS);
			} catch (RejectedExecutionException rej) {
				if (!Raptor.getInstance().isDisposed()) {
					LOG.error("Error executing runnable in scheduleOneShot: ",
							rej);
					threadDump();
					Raptor.getInstance().onError(
							"ThreadServie has no more threads. A thread dump can be found at "
									+ THREAD_DUMP_FILE_PATH);
				}
				return null;
			}
		} else {
			LOG.info("Veoting runnable " + runnable + " raptor is disposed.");
			return null;
		}
	}
}
