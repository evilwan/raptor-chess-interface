package raptor.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import raptor.Raptor;
import raptor.international.L10n;

public class UserInfoDialog extends Dialog {
	
	private Label timeOn, iface, pingTime;
	private StyledText fingerText;

	public UserInfoDialog() {
		super(Raptor.getInstance().getWindow().getShell(), SWT.DIALOG_TRIM);
		setText(L10n.getInstance().getString("userProf"));
	}
	
	public void open() {
		// Create the dialog window
		Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		createContents(shell);
		shell.setSize(600, 500);
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
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		parent.setLayout(layout);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		timeOn = new Label(composite, SWT.NONE);
		iface = new Label(composite, SWT.NONE);
		pingTime = new Label(composite, SWT.NONE);
		timeOn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
				1));
		pingTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		iface.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
				1));
		timeOn.setText(L10n.getInstance().getString("plsWait"));
		fingerText = new StyledText(parent, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		fingerText.setEditable(false);
		fingerText.setFont(Raptor.getInstance().getPreferences().getFont("chat-input-font"));
		//fingerText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
		//		true, 3, 1));

		/*Display.getCurrent().timerExec(2000, new Runnable() {

			public void run() {
				updateData("On for: 1 day, 22 hrs, 20 mins",
						"Interface: Raptor v97", "Ping: not available");
			}
		});*/

	}
	
	public void updateInterface(final String t) {
		getParent().getDisplay().syncExec(new Runnable() {
			public void run() {
				iface.setText("   " + t);
			}
		});
	}
	
	public void updateTime(final String t) {
		getParent().getDisplay().syncExec(new Runnable() {
			public void run() {
				pingTime.setText("   " + t);
			}
		});
	}
	
	public void updateOn(final String t) {
		getParent().getDisplay().syncExec(new Runnable() {
			public void run() {
				timeOn.setText("   " + t);
			}
		});
	}

	public void updateData(final String time, final String itface,
			final String ping, final String notes) {
		getParent().getDisplay().syncExec(new Runnable() {
			public void run() {
				String indent = "   ";
				timeOn.setText(indent + time);
				iface.setText(indent + itface);
				pingTime.setText(indent + ping);
				fingerText.setText(notes);				
			}
		});
	}

}
