package raptor;

import java.util.Calendar;

import raptor.connector.fics.FicsUtils;

public class Test {
	public static final long TIMEZONE_OFFSET = -(Calendar.getInstance()
			.get(Calendar.ZONE_OFFSET));

	public static void main(String args[]) throws Exception {

		// System.out.println(FicsUtils.replaceUnicode("&#x3b1;&#x3b2;&#x3b3;&#x3b4;&#x3b5;&#x3b6;"));
		StringBuilder builder = new StringBuilder(""
				+ (char) (Integer.valueOf("2654", 16).intValue()));
		// FicsUtils.filterOutbound(builder);
		System.out.println(builder.toString());
		// Display display = new Display();
		// App app = new App();
		// app.getFicsConnector().setPreferences(app.getPreferences());
		// app.getFicsConnector().getPreferences().setValue(
		// PreferenceKeys.FICS_TIMESEAL_ENABLED, true);
		// app.getFicsConnector().getPreferences().setValue(
		// PreferenceKeys.FICS_IS_ANON_GUEST, true);
		// //app.getFicsConnector().getPreferences().setValue(PreferenceKeys.FICS_USER_NAME,
		// "raptorTestOne");
		// app.getFicsConnector().connect();
		// display.dispose();
	}
}
