package raptor.updater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class UpdateManager {

	public static final String APP_HOME_DIR = ".raptor/";
	public static final File USER_RAPTOR_PREF = new File(
			System.getProperty("user.home") + "/" + APP_HOME_DIR
					+ "/raptor.properties");

	private boolean isUpdateOn = false;
	private boolean isReadyToUpdate = false;
	private static String appVersionStr = ".99";

	protected static boolean isLikelyLinux = false;
	protected static boolean isLikelyOSX = false;
	protected static boolean isLikelyWindows = false;
	protected static boolean isLikelyWindowsVistaOrLater;
	
	/**
	 * Upgrade-file-list for Windows. We add all the files to move over to this
	 * list. See {@link #applyWindows()}.
	 */
	private static List<String> windowsUpgradeCommands = null;
	
	private static final String updActionsUrl = "http://dl.dropbox.com/u/46373738/updActions";

	static {
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS")) {
			isLikelyOSX = true;
		} else if (osName.startsWith("Windows")) {
			isLikelyWindows = true;
			isLikelyWindowsVistaOrLater = osName.contains("Vista")
					|| osName.contains("7") || osName.contains("8");
		} else {
			isLikelyLinux = true;
		}
	}

	private Label infoLabel1;
	private Label infoLabel2;

	Thread upgradeThread;
	private Shell shell;

	public static void center(Shell shell) {
		Rectangle bds = shell.getDisplay().getBounds();
		Point p = shell.getSize();

		int nLeft = (bds.width - p.x) / 2;
		int nTop = (bds.height - p.y) / 2;

		shell.setBounds(nLeft, nTop, p.x, p.y);
	}

	public static void invokeMain(String args[]) {

		File f = new File("Raptor.jar");

		try {
			URLClassLoader u = new URLClassLoader(
					new URL[] { f.toURI().toURL() });
			Class<?> c = u.loadClass("raptor.Raptor");
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
				upgradeThread.interrupt();
			}
		});
		createGuiContents(shell);
		shell.pack();
		center(shell);
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
		infoLabel1
				.setText("                                                                                        ");
		infoLabel2 = new Label(shell, SWT.NULL);
		infoLabel2
				.setText("                                                                                        ");

		Button bt = new Button(shell, SWT.PUSH);
		bt.setText("Cancel");
		bt.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		bt.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				upgradeThread.interrupt();
				shell.close();
			}
		});
	}
	
	private static void transferFile(String url, String toFilename) throws IOException {
		URL fileUrl = new URL(url);
		ReadableByteChannel rbc2 = Channels.newChannel(fileUrl
				.openStream());
		FileOutputStream fos2 = new FileOutputStream(toFilename);
		fos2.getChannel().transferFrom(rbc2, 0, 1 << 24);
		fos2.close();
	}

	public void upgrade() {
		if (!infoLabel1.isDisposed()) {
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					infoLabel1.setText("Initializing the remote connection...");
				}

			});
		}
		
		boolean upgradeMade = false;
		try {
			boolean forVersionSet = false;
			URL google = new URL(updActionsUrl);
			BufferedReader bin = new BufferedReader(new InputStreamReader(
					google.openStream()), 1024);
			String currentLine;
			while ((currentLine = bin.readLine()) != null) {
				if (currentLine.startsWith("for-version:") && forVersionSet)
					break;
				else if (currentLine.startsWith("for-version:")
						&& currentLine.substring(13).equals(appVersionStr)) {
					forVersionSet = true;
				} else if (forVersionSet && currentLine.startsWith("file:")) {
					if (!infoLabel2.isDisposed()) {
						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
								infoLabel2
										.setText("Downloading upgrade files...");
							}

						});
					}

					String data[] = currentLine.substring(6).split(" ");
					String tempFilename = System.getProperty("java.io.tmpdir")
							+ System.getProperty("file.separator")
							+ Math.abs(data[1].hashCode());
					transferFile(data[0], tempFilename);
					upgradeMade = true;
					if (isLikelyOSX)
						applyOSX(tempFilename, data[1]);
					else if (isLikelyWindows)
						addWindows(tempFilename, data[1]);
					else if (isLikelyLinux)
						applyLinux(tempFilename, data[1]);
				}
			}
			bin.close();
			
			// most likely some error. Upgrade Raptor.jar anyway. 
			if (!upgradeMade) {
				String tempFilename = System.getProperty("java.io.tmpdir")
						+ System.getProperty("file.separator")
						+ Math.abs("Raptor.jar".hashCode());
				transferFile("http://raptor-chess-interface.googlecode.com/files/Raptor.jar", tempFilename);
				if (isLikelyOSX)
					applyOSX(tempFilename, "Raptor.jar");
				else if (isLikelyWindows)
					addWindows(tempFilename, "Raptor.jar");
				else if (isLikelyLinux)
					applyLinux(tempFilename, "Raptor.jar");
			}
			
			if (isLikelyWindows) {
				applyWindows();
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		if (!shell.isDisposed()) {
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					shell.close();
				}

			});
		}
	}

	private static void applyLinux(String tempFilename, String dest) {
		Process p;
		try {
			p = Runtime.getRuntime().exec(
					new String[] {"gksudo", "mv", tempFilename, dest});
			if (p.waitFor() != 0) {
				p = Runtime.getRuntime().exec(
						new String[] { "kdesudo", "mv", tempFilename, dest });
				p.waitFor();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void addWindows(String tempFilename, String dest) {
		if (windowsUpgradeCommands == null) {
			windowsUpgradeCommands = new ArrayList<String>();
		}
		windowsUpgradeCommands.add("move /y \"" + tempFilename + "\" \"" + dest
				+ "\"");
	}
	
	/**
	 * Write the list of move commands to a batch file, then execute it with
	 * elevated privileges. This way, we only have to elevate once.
	 */
	private static void applyWindows() {
		if (windowsUpgradeCommands != null) {
			try {
				File tmp = File.createTempFile("rap-upd", ".bat");
				BufferedWriter fout = new BufferedWriter(new FileWriter(tmp));
				String cd = System.getProperty("user.dir");
				fout.write("@echo off\r\n");
				fout.write(cd.substring(0, 2) + "\r\ncd " + cd.substring(2)
						+ "\r\n");
				for (String cmd : windowsUpgradeCommands) {
					fout.write(cmd + "\r\n");
				}
				fout.close();
				
				Process p;
				if (isLikelyWindowsVistaOrLater) {
					p = Runtime.getRuntime().exec(
							new String[] { "elevate", "-wait", "cmd", "/c",
									tmp.getAbsolutePath() });
				} else {
					p = Runtime.getRuntime().exec(new String[] { "cmd", "/c",
							tmp.getAbsolutePath() });
				}
				p.waitFor();
				
				tmp.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void applyOSX(String tempFilename, String dest) {
		Process p;
		try {
			p = Runtime.getRuntime().exec(
					new String[] { "mv", tempFilename, dest });
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void checkPrefs() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					USER_RAPTOR_PREF));
			String currentLine;
			while ((currentLine = reader.readLine()) != null) {
				if (currentLine.startsWith("app-update")) {
					isUpdateOn = Boolean
							.parseBoolean(currentLine.substring(11));
					if (!isUpdateOn) {
						reader.close();
						return;
					}
				} else if (currentLine.startsWith("app-version")) {
					appVersionStr = currentLine.substring(12);
				} else if (currentLine.startsWith("ready-to-update")) {
					isReadyToUpdate = Boolean.parseBoolean(currentLine
							.substring(16));
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
//		if (!isLikelyOSX) {
//			final UpdateManager manager = new UpdateManager();
//
//			manager.checkPrefs();
//			if (manager.isUpdateOn && manager.isReadyToUpdate) {
//				manager.upgradeThread = new Thread(new Runnable() {
//
//					@Override
//					public void run() {
//						boolean threadDisposed = false;
//						try {
//							Thread.sleep(600);
//						} catch (InterruptedException e) {
//							threadDisposed = true;
//						}
//
//						if (!threadDisposed)
//							manager.upgrade();
//					}
//
//				});
//				manager.upgradeThread.start();
//				manager.open();
//			}
//		}
//		
//		invokeMain(args);
		System.out.println("Update manager is currently disabled until we can check it for security leaks.");
	}

}
