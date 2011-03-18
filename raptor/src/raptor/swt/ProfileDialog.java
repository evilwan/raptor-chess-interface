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
package raptor.swt;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import raptor.Raptor;
import raptor.international.L10n;
import raptor.service.ThreadService;
import raptor.util.RaptorStringUtils;

/**
 * The profile window class. This is just a poor mans profiler to be able to
 * quickly check how Raptor is doing in terms of thread usage and memory usage.
 */
public class ProfileDialog extends Dialog {

	private Label heapm, heap1, heap2, heap3, heap4, stackm, stack1, stack2,
			stack3, stack4, threadsm, threads1, threads2, threads3, threads4,
			threads5, image1;
	protected static L10n local = L10n.getInstance();

	public ProfileDialog() {
		super(Raptor.getInstance().getWindow().getShell(), SWT.DIALOG_TRIM);
		setText(local.getString("profileDialog1"));
	}

	/**
	 * Opens the dialog and returns the input
	 * 
	 * @return String
	 */
	public void open() {
		// Create the dialog window
		Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		createContents(shell);
		shell.pack();
		SWTUtils.center(shell);
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	protected void createContents(final Shell parent) {
		parent.setLayout(new FillLayout());
		final MemoryUsage heap = ManagementFactory.getMemoryMXBean()
				.getHeapMemoryUsage();
		final MemoryUsage stack = ManagementFactory.getMemoryMXBean()
				.getNonHeapMemoryUsage();
		final ThreadMXBean threads = ManagementFactory.getThreadMXBean();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		heapm = new Label(composite, SWT.NONE);
		heapm.setText(local.getString("profileDialog2"));
		heap1 = new Label(composite, SWT.NONE);
		heap2 = new Label(composite, SWT.NONE);
		heap3 = new Label(composite, SWT.NONE);
		heap4 = new Label(composite, SWT.NONE);

		stackm = new Label(composite, SWT.NONE);
		stackm.setText(local.getString("profileDialog3"));
		stack1 = new Label(composite, SWT.NONE);
		stack2 = new Label(composite, SWT.NONE);
		stack3 = new Label(composite, SWT.NONE);
		stack4 = new Label(composite, SWT.NONE);

		heap1.setText(local.getString("profileDialog4")
				+ RaptorStringUtils.getMegs(heap.getInit()));
		heap2.setText(local.getString("profileDialog5") + RaptorStringUtils.getMegs(heap.getUsed()));
		heap3.setText(local.getString("profileDialog6")
				+ RaptorStringUtils.getMegs(heap.getMax()));
		heap4.setText(local.getString("profileDialog7") + RaptorStringUtils.getMegs(heap.getMax()));
		stack1.setText(local.getString("profileDialog4")
				+ RaptorStringUtils.getMegs(stack.getInit()));
		stack2
				.setText(local.getString("profileDialog5")
						+ RaptorStringUtils.getMegs(stack.getUsed()));
		stack3.setText(local.getString("profileDialog6")
				+ RaptorStringUtils.getMegs(stack.getMax()));
		stack4.setText(local.getString("profileDialog7") + RaptorStringUtils.getMegs(stack.getMax()));

		threadsm = new Label(composite, SWT.NONE);
		threadsm.setText(local.getString("profileDialog8"));
		threads1 = new Label(composite, SWT.NONE);
		threads1.setText(local.getString("profileDialog9") + threads.getThreadCount());
		threads2 = new Label(composite, SWT.NONE);
		threads2.setText(local.getString("profileDialog10") + threads.getPeakThreadCount());
		threads3 = new Label(composite, SWT.NONE);
		threads3.setText(local.getString("profileDialog11")
				+ threads.getTotalStartedThreadCount());

		Label label = new Label(composite, SWT.NONE);
		label.setText(local.getString("profileDialog12"));
		threads4 = new Label(composite, SWT.NONE);
		threads4.setText(local.getString("profileDialog13")
				+ ThreadService.getInstance().getExecutor().getPoolSize()
				+ "/"
				+ ThreadService.getInstance().getExecutor().getCorePoolSize()
				+ "/"
				+ ThreadService.getInstance().getExecutor()
						.getLargestPoolSize()
				+ "/"
				+ ThreadService.getInstance().getExecutor()
						.getMaximumPoolSize());
		threads5 = new Label(composite, SWT.NONE);
		threads5.setText(local.getString("profileDialog14")
				+ ThreadService.getInstance().getExecutor().getTaskCount()
				+ "/"
				+ ThreadService.getInstance().getExecutor()
						.getCompletedTaskCount());

		image1 = new Label(composite, SWT.NONE);
		image1.setText(local.getString("profileDialog15")
				+ Raptor.getInstance().getImageRegistry().getSize() + "/"
				+ Raptor.getInstance().getFontRegistry().getKeySet().size()
				+ "/"
				+ Raptor.getInstance().getColorRegistry().getKeySet().size()
				+ "/" + Raptor.getInstance().getCursorRegistry().getSize());

		Button button = new Button(composite, SWT.PUSH);
		button.setText(local.getString("profileDialog16"));
		button.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				System.gc();
			}

		});

		Display.getCurrent().timerExec(2000, new Runnable() {
			public void run() {
				try {
					if (!Display.getCurrent().isDisposed()) {
						MemoryUsage curHeap = ManagementFactory
								.getMemoryMXBean().getHeapMemoryUsage();
						MemoryUsage curStack = ManagementFactory
								.getMemoryMXBean().getNonHeapMemoryUsage();
						ThreadMXBean curThreads = ManagementFactory
								.getThreadMXBean();

						heap1.setText(local.getString("profileDialog4")
								+ RaptorStringUtils.getMegs(curHeap.getInit()));
						heap2.setText(local.getString("profileDialog5")
								+ RaptorStringUtils.getMegs(curHeap.getUsed()));
						heap3.setText(local.getString("profileDialog6")
								+ RaptorStringUtils.getMegs(curHeap.getMax()));
						heap4.setText(local.getString("profileDialog7")
								+ RaptorStringUtils.getMegs(curHeap.getMax()));
						stack1
								.setText(local.getString("profileDialog4")
										+ RaptorStringUtils.getMegs(curStack
												.getInit()));
						stack2
								.setText(local.getString("profileDialog5")
										+ RaptorStringUtils.getMegs(curStack
												.getUsed()));
						stack3.setText(local.getString("profileDialog6")
								+ RaptorStringUtils.getMegs(curStack.getMax()));
						stack4.setText(local.getString("profileDialog7")
								+ RaptorStringUtils.getMegs(curStack.getMax()));

						threads1.setText(local.getString("profileDialog9")
								+ curThreads.getThreadCount());
						threads2.setText(local.getString("profileDialog10")
								+ curThreads.getPeakThreadCount());
						threads3.setText(local.getString("profileDialog11")
								+ curThreads.getTotalStartedThreadCount());

						threads4.setText(local.getString("profileDialog13")
								+ ThreadService.getInstance().getExecutor()
										.getPoolSize()
								+ "/"
								+ ThreadService.getInstance().getExecutor()
										.getCorePoolSize()
								+ "/"
								+ ThreadService.getInstance().getExecutor()
										.getLargestPoolSize()
								+ "/"
								+ ThreadService.getInstance().getExecutor()
										.getMaximumPoolSize());
						threads5.setText(local.getString("profileDialog14")
								+ ThreadService.getInstance().getExecutor()
										.getTaskCount()
								+ "/"
								+ ThreadService.getInstance().getExecutor()
										.getCompletedTaskCount());
						image1.setText(local.getString("profileDialog15")
								+ Raptor.getInstance().getImageRegistry()
										.getSize()
								+ "/"
								+ Raptor.getInstance().getFontRegistry()
										.getKeySet().size()
								+ "/"
								+ Raptor.getInstance().getColorRegistry()
										.getKeySet().size()
								+ "/"
								+ Raptor.getInstance().getCursorRegistry()
										.getSize());

						Display.getCurrent().timerExec(2000, this);
					}
				} catch (SWTException e) // eat it its prob a widget disposed.
				{
				}
			}

		});
	}
}