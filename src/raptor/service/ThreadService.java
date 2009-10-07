/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;

/**
 * Multi-threading should be usually avoided in Raptor. Instead to launch
 * something asynchronously you should use this service.
 * 
 * This service provides exception handling, and pooling
 */
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

	ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(20) {
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

	private ThreadService() {
		executor.setCorePoolSize(20);
		executor.setMaximumPoolSize(50);
		executor.setKeepAliveTime(180, TimeUnit.SECONDS);
		executor.prestartAllCoreThreads();
	}

	public void dispose() {
		executor.shutdownNow();
	}

	public ScheduledThreadPoolExecutor getExecutor() {
		return executor;
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
