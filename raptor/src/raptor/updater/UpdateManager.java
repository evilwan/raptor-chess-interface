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

public class UpdateManager {

	public static final String APP_HOME_DIR = ".raptor/";
	public static final File USER_RAPTOR_PREF = new File(
			System.getProperty("user.home") + "/" + APP_HOME_DIR
					+ "/raptor.properties");

	private boolean isUpdateOn;
	private boolean isReadyToUpdate;
	private static String appVersionStr = ".98u3";

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

	boolean upgrade() {
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

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		UpdateManager manager = new UpdateManager();
		manager.checkPrefs();
		if (manager.isUpdateOn && manager.isReadyToUpdate) {
			manager.upgrade();
		}

		manager.invokeMain(args);
	}

}
