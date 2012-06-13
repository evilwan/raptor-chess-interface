package raptor.service;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import raptor.Raptor;
import raptor.international.L10n;
import raptor.pref.RaptorPreferenceStore;

public class CheckUpdates {
	private static final L10n local = L10n.getInstance();

	private static final int appVersion[] = RaptorPreferenceStore.APP_VERSION;

	private static final String updUrl = "http://dl.dropbox.com/u/46373738/upd";
	
	private static int[] parseVersion(String version) {
		int t[] = null;
		Pattern p = Pattern.compile("([0-9]*)\\.([0-9]+)u?([0-9]*)f?([0-9]*)");
		Matcher m = p.matcher(version);
		if (m.matches()) {
			t = new int[4];
			t[0] = StringUtils.isBlank(m.group(1)) ? 0 : Integer.parseInt(m.group(1));
			t[1] = StringUtils.isBlank(m.group(2)) ? 0 : Integer.parseInt(m.group(2));
			t[2] = StringUtils.isBlank(m.group(3)) ? 0 : Integer.parseInt(m.group(3));
			t[3] = StringUtils.isBlank(m.group(4)) ? 0 : Integer.parseInt(m.group(4));
		}
		return t;
	}

	public static void checkUpdates() {
		if (Raptor.getInstance().getPreferences().getBoolean("ready-to-update")) {
			Raptor.getInstance().getPreferences().setValue("ready-to-update", "false");
			return;
		}
		
		if (!Raptor.getInstance().getPreferences().getString("app-version")
				.equals(RaptorPreferenceStore.getVersion()))
			Raptor.getInstance().getPreferences().setValue("app-version", RaptorPreferenceStore.getVersion());
		
		URL updateUrl;
		try {
			updateUrl = new URL(updUrl);
			BufferedReader bin = new BufferedReader(new InputStreamReader(
					updateUrl.openStream()), 1024);
			final String lastVersionLine = bin.readLine();
			int[] newVersionData = parseVersion(lastVersionLine.substring(9));
			boolean isNewerVersion = false;
	        for (int i = 0; i < 4; i++) {
	            if (appVersion[i] < newVersionData[i]) {
	                isNewerVersion = true;
	                break;
	            }
	        }
			bin.close();
			if (isNewerVersion) {
				Raptor.getInstance().getPreferences().setValue("ready-to-update", "true");
				Raptor.getInstance().getDisplay().asyncExec (new Runnable () {

					@Override
					public void run() {
						MessageDialog.openInformation(
								Raptor.getInstance().getDisplay()
										.getActiveShell(),
								local.getString("newVersion"),
								local.getString("newVersAvail",
										lastVersionLine.substring(9)));
					}
					
				});				
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
