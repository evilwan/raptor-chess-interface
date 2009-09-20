package raptor;

import java.util.Calendar;

import org.eclipse.swt.widgets.Display;

import raptor.pref.PreferenceKeys;

public class Test {
	public static final long TIMEZONE_OFFSET = -(Calendar.getInstance()
			.get(Calendar.ZONE_OFFSET));

	public static void main(String args[]) {

		Display display = new Display();
		App app = new App();
		app.getFicsConnector().setPreferences(app.getPreferences());
		app.getFicsConnector().getPreferences().setValue(
				PreferenceKeys.FICS_TIMESEAL_ENABLED, true);
		app.getFicsConnector().getPreferences().setValue(
				PreferenceKeys.FICS_IS_ANON_GUEST, true);
		//app.getFicsConnector().getPreferences().setValue(PreferenceKeys.FICS_USER_NAME, "raptorTestOne");
		app.getFicsConnector().connect();
		display.dispose();
	}
}
