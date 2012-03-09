package raptor.service;

import org.eclipse.jface.dialogs.MessageDialog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import raptor.Raptor;

public class CheckUpdates {
	
	private static int appVersion[] = {0,98,3,0};
	
	private static String updUrl = "http://raptor-chess-interface.googlecode.com/files/upd";
	
	private static int[] parseVersion(String version) {
		int t[] = null;
		Pattern p = Pattern.compile("([0-9]*)\\.([0-9]+)u?([0-9]*)f?([0-9]*)");
		Matcher m = p.matcher(version);
		if (m.matches()) {
			t = new int[4];
			t[0] = Integer.parseInt(m.group(1).isEmpty() ? "0" : m.group(1));
			t[1] = Integer.parseInt(m.group(2).isEmpty() ? "0" : m.group(2));
			t[2] = Integer.parseInt(m.group(3).isEmpty() ? "0" : m.group(3));
			t[3] = Integer.parseInt(m.group(4).isEmpty() ? "0" : m.group(4));
		}
		return t;
	}

	public static void checkUpdates() {
		if (Raptor.getInstance().getPreferences().getBoolean("ready-to-update")) {
			Raptor.getInstance().getPreferences().setValue("ready-to-update", "false");
			return;
		}
		
		URL updateUrl;
		try {
			updateUrl = new URL(updUrl);
			BufferedReader bin = new BufferedReader(new InputStreamReader(
					updateUrl.openStream()), 1024);
			final String lastVersionLine = bin.readLine();
			int[] newVersionData = parseVersion(lastVersionLine.substring(9));
			boolean isNewerVersion = newVersionData[0] > appVersion[0] 
					|| newVersionData[1] > appVersion[1]
							|| newVersionData[2] > appVersion[2]
									|| newVersionData[3] > appVersion[3];
			for (int i = 0; i < 4; i++) {
				if (appVersion[i] != 0 && newVersionData[i] != 0 
						&& newVersionData[i] < appVersion[i])
					isNewerVersion = false;
			}
			bin.close();
			if (isNewerVersion) {
				Raptor.getInstance().getPreferences().setValue("ready-to-update", "true");
				Raptor.getInstance().getDisplay().asyncExec (new Runnable () {

					@Override
					public void run() {
						MessageDialog.openInformation(Raptor.getInstance().getDisplay().getActiveShell(), "New version",
								"Raptor " + lastVersionLine.substring(9) + " is available. Restart to upgrade to it. You " +
										"can turn off updates on Preferences->General page.");
						
					}
					
				});				
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
