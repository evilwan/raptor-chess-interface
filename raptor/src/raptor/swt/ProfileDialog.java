package raptor.swt;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;

import org.eclipse.swt.SWT;
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
import raptor.service.ThreadService;

/**
 * The profile window class. This is just a poor mans profiler to be able to
 * quickly check how Raptor is doing in terms of thread usage and memory usage.
 */
public class ProfileDialog extends Dialog {

	private Label heapm, heap1, heap2, heap3, heap4, stackm, stack1, stack2,
			stack3, stack4, threadsm, threads1, threads2, threads3, threads4,
			threads5, image1;

	public ProfileDialog() {
		super(Raptor.getInstance().getRaptorWindow().getShell(),
				SWT.DIALOG_TRIM);
		setText("Mini Profiler");
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
		heapm.setText("Heap: ");
		heap1 = new Label(composite, SWT.NONE);
		heap1.setText("   Initial: " + heap.getInit() / 1024 + "K");
		heap2 = new Label(composite, SWT.NONE);
		heap2.setText("   Used: " + heap.getUsed() / 1024 + "K");
		heap3 = new Label(composite, SWT.NONE);
		heap3.setText("   Committed: " + heap.getMax() / 1024 + "K");
		heap4 = new Label(composite, SWT.NONE);
		heap4.setText("   Max: " + heap.getMax() / 1024 + "K");

		stackm = new Label(composite, SWT.NONE);
		stackm.setText("Stack:");
		stack1 = new Label(composite, SWT.NONE);
		stack1.setText("   Initial: " + stack.getInit() / 1024 + "K");
		stack2 = new Label(composite, SWT.NONE);
		stack2.setText("   Used: " + stack.getUsed() / 1024 + "K");
		stack3 = new Label(composite, SWT.NONE);
		stack3.setText("   Committed: " + stack.getMax() / 1024 + "K");
		stack4 = new Label(composite, SWT.NONE);
		stack4.setText("   Max: " + stack.getMax() / 1024 + "K");

		threadsm = new Label(composite, SWT.NONE);
		threadsm.setText("Threads:");
		threads1 = new Label(composite, SWT.NONE);
		threads1.setText("   Threads: " + threads.getThreadCount());
		threads2 = new Label(composite, SWT.NONE);
		threads2.setText("   Peak Threads: " + threads.getPeakThreadCount());
		threads3 = new Label(composite, SWT.NONE);
		threads3.setText("   Total Started Threads: "
				+ threads.getTotalStartedThreadCount());

		Label label = new Label(composite, SWT.NONE);
		label.setText("ThreadService:");
		threads4 = new Label(composite, SWT.NONE);
		threads4.setText("   Size/Core/Largest/Max: "
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
		threads5.setText("   Task Scheduled/Completed: "
				+ ThreadService.getInstance().getExecutor().getTaskCount()
				+ "/"
				+ ThreadService.getInstance().getExecutor()
						.getCompletedTaskCount());

		image1 = new Label(composite, SWT.NONE);
		image1.setText("Cached Images/Fonts/Colors/Cursors: "
				+ Raptor.getInstance().getImageRegistry().getSize() + "/"
				+ Raptor.getInstance().getFontRegistry().getKeySet().size()
				+ "/"
				+ Raptor.getInstance().getColorRegistry().getKeySet().size()
				+ "/" + Raptor.getInstance().getCursorRegistry().getSize());

		Button button = new Button(composite, SWT.PUSH);
		button.setText("Suggest Garbage Collection");
		button.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				System.gc();
			}

		});

		Display.getCurrent().timerExec(2000, new Runnable() {
			public void run() {
				if (!Display.getCurrent().isDisposed()) {
					MemoryUsage curHeap = ManagementFactory.getMemoryMXBean()
							.getHeapMemoryUsage();
					MemoryUsage curStack = ManagementFactory.getMemoryMXBean()
							.getNonHeapMemoryUsage();
					ThreadMXBean curThreads = ManagementFactory
							.getThreadMXBean();

					heap1.setText("   Initial: " + curHeap.getInit() / 1024
							+ "K");
					heap2.setText("   Used: " + curHeap.getUsed() / 1024 + "K");
					heap3.setText("   Committed: " + curHeap.getMax() / 1024
							+ "K");
					heap4.setText("   Max: " + curHeap.getMax() / 1024 + "K");

					stack1.setText("   Initial: " + curStack.getInit() / 1024
							+ "K");
					stack2.setText("   Used: " + curStack.getUsed() / 1024
							+ "K");
					stack3.setText("   Committed: " + curStack.getMax() / 1024
							+ "K");
					stack4.setText("   Max: " + curStack.getMax() / 1024 + "K");

					threads1.setText("   Threads: "
							+ curThreads.getThreadCount());
					threads2.setText("   Peak Threads: "
							+ curThreads.getPeakThreadCount());
					threads3.setText("   Total Started Threads: "
							+ curThreads.getTotalStartedThreadCount());

					threads4.setText("   Size/Core/Largest/Max: "
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
					threads5.setText("   Task Scheduled/Completed: "
							+ ThreadService.getInstance().getExecutor()
									.getTaskCount()
							+ "/"
							+ ThreadService.getInstance().getExecutor()
									.getCompletedTaskCount());
					image1.setText("Cached Images/Fonts/Colors: "
							+ Raptor.getInstance().getImageRegistry().getSize()
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
			}

		});
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
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
}
