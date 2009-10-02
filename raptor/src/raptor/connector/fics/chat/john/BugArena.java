package raptor.connector.fics.chat.john;

import org.eclipse.jface.dialogs.Dialog;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import raptor.Raptor;

public class BugArena extends Dialog {
	
	protected BugArena(Shell parentShell) {
		super(Raptor.getInstance().getRaptorWindow().getShell());
	}
	
//	protected String title = "";
//	@Override
//	public Composite createContents(Composite parent) {
//		getShell().setText(title);
//		final Composite content = new Composite(parent, SWT.NONE);
//		
//		Label l = new Label(content,SWT.NONE);
//		l.setText("Available Partners:");
//		
//		BugWhoUParser p = new BugWhoUParser(title);
//		Button b = new Button(content, SWT.PUSH);
//		b.setText("Login");
//		
//		return content;
//	}

}
