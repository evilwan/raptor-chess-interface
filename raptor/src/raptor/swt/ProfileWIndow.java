package raptor.swt;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class ProfileWIndow extends ApplicationWindow {

	private Label heapm, heap1, heap2, heap3, heap4, stackm, stack1, stack2,
			stack3, stack4, threadsm, threads1, threads2, threads3;

	public ProfileWIndow() {
		super(null);
	}

	protected Control createContents(Composite parent) {
		parent.setLayout(new FillLayout());
		getShell().setText("Raptor Mini-Profiler");
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
				MemoryUsage curHeap = ManagementFactory.getMemoryMXBean()
						.getHeapMemoryUsage();
				MemoryUsage curStack = ManagementFactory.getMemoryMXBean()
						.getNonHeapMemoryUsage();
				ThreadMXBean curThreads = ManagementFactory.getThreadMXBean();

				heap1.setText("   Initial: " + curHeap.getInit() / 1024 + "K");
				heap2.setText("   Used: " + curHeap.getUsed() / 1024 + "K");
				heap3.setText("   Committed: " + curHeap.getMax() / 1024 + "K");
				heap4.setText("   Max: " + curHeap.getMax() / 1024 + "K");

				stack1
						.setText("   Initial: " + curStack.getInit() / 1024
								+ "K");
				stack2.setText("   Used: " + curStack.getUsed() / 1024 + "K");
				stack3.setText("   Committed: " + curStack.getMax() / 1024
						+ "K");
				stack4.setText("   Max: " + curStack.getMax() / 1024 + "K");

				threads1.setText("   Threads: " + curThreads.getThreadCount());
				threads2.setText("   Peak Threads: "
						+ curThreads.getPeakThreadCount());
				threads3.setText("   Total Started Threads: "
						+ curThreads.getTotalStartedThreadCount());

				Display.getCurrent().timerExec(2000, this);
			}

		});
		composite.pack();
		return composite;
	}

	@Override
	protected void initializeBounds() {
		getShell().setSize(250, 350);
		getShell().setLocation(0, 0);
	}
}
