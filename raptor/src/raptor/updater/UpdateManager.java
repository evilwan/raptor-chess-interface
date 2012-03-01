package raptor.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import raptor.international.L10n;
import raptor.swt.SWTUtils;

public class UpdateManager {

	public static final String APP_HOME_DIR = ".raptor/";
	public static final File USER_RAPTOR_PREF = new File(
			System.getProperty("user.home") + "/" + APP_HOME_DIR
					+ "/raptor.properties");

	private boolean isUpdateOn;
	private boolean isReadyToUpdate;
	private static String appVersionStr = ".98u3";
	
	private Label infoLabel1;
	private Label infoLabel2;
	
	private Thread upgradeThread;
	private Shell shell;

	public void invokeMain(String args[]) {

		File f = new File("/home/bodia/Raptor_v98/Raptor.jar");

		try {
			URLClassLoader u = new URLClassLoader(
					new URL[] { f.toURI().toURL() });
			Class c = u.loadClass("raptor.Raptor");
			Method m = c.getMethod("main", new Class[] { args.getClass() });
			m.setAccessible(true);
			m.invoke(null, new Object[] { args });
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void open() {
        Display display = new Display();
        shell = new Shell(display, SWT.DIALOG_TRIM | SWT.MIN);        
        shell.setText("Raptor upgrade");
        shell.addListener(SWT.Close, new Listener() {

            public void handleEvent(Event event) {
                return;
            }
        });
        createGuiContents(shell);
        shell.pack();
        SWTUtils.center(shell);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.close();
    }
	
	void createGuiContents(final Shell shell) {
		GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;        
        gridLayout.marginWidth = 9;
        shell.setLayout(gridLayout);

        Label label = new Label(shell, SWT.NULL);
        label.setText("Update initiated...               ");
        
        infoLabel1 = new Label(shell, SWT.NULL);
        infoLabel1.setText("                                                                                        ");
        infoLabel2 = new Label(shell, SWT.NULL);
        infoLabel2.setText("                                                                                        ");
        
        Button bt = new Button(shell, SWT.PUSH);
        bt.setText("Cancel");
        bt.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
                false, 1, 1));
        bt.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {   
            	upgradeThread.interrupt();
                shell.close();
            }
        });
	}
	
	public void upgrade() {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				infoLabel1.setText("Initializing the remote connection...");							
			}
			
		});
		try {
			boolean forVersionSet = false;
			URL google = new URL("file:///home/bodia/test");
			BufferedReader bin = new BufferedReader(new InputStreamReader(
					google.openStream()),1024);
			String currentLine = null;
			while ((currentLine = bin.readLine()) != null) {
				if (currentLine.startsWith("for-version:") && forVersionSet)
					break;
				else if (currentLine.startsWith("for-version:")
						&& currentLine.substring(13).equals(appVersionStr)) {
					forVersionSet = true;
				}
				else if (forVersionSet && currentLine.startsWith("file:")) {
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							infoLabel2.setText("Downloading upgrade files...");							
						}
						
					});
					
					String data[] = currentLine.substring(6).split(" ");
					String tempFilename = System.getProperty("java.io.tmpdir")+
							System.getProperty("file.separator") + Math.abs(data[1].hashCode());
					System.out.println(data[0]);
					URL fileUrl = new URL(data[0]);
				    ReadableByteChannel rbc2 = Channels.newChannel(fileUrl.openStream());
				    FileOutputStream fos2 = new FileOutputStream(tempFilename);
				    fos2.getChannel().transferFrom(rbc2, 0, 1 << 24);
				    fos2.close();					
					System.out.println(data[1]);
				}
			}
			bin.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				//shell.close();							
			}
			
		});
	}

	void checkPrefs() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					USER_RAPTOR_PREF));
			String currentLine = null;
			while ((currentLine = reader.readLine()) != null) {
				if (currentLine.startsWith("app-update")) {
					isUpdateOn = Boolean
							.parseBoolean(currentLine.substring(11));
					if (!isUpdateOn) {
						reader.close();
						return;
					}
				}
				else if (currentLine.startsWith("app-version")) {
					appVersionStr = currentLine.substring(12);
				}
				else if (currentLine.startsWith("ready-to-update")) {
					isReadyToUpdate = Boolean.parseBoolean(currentLine.substring(16));
				}				
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final UpdateManager manager = new UpdateManager();
		Thread th = new Thread(new Runnable() {

			@Override
			public void run() {
				manager.open();
			}
			
		});
		th.start();
		
		try {
			Thread.sleep(600);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		manager.checkPrefs();
		if (manager.isUpdateOn && manager.isReadyToUpdate) {
			manager.upgrade();			
		}

		//manager.invokeMain(args);
	}

}
