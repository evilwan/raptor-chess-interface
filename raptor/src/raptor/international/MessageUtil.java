package raptor.international;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A utility class used to retrieve messages from the mesages resource bundle.
 */
public class MessageUtil {
	private static Locale locale = Locale.getDefault();
	private static ResourceBundle bundle = ResourceBundle.getBundle(
			"raptor.international.messages", locale);

	/**
	 * Sets the Locale used for internationalization.
	 */
	public static void setLocale(Locale newLocale) {
		synchronized (MessageUtil.class) {
			locale = newLocale;
			bundle = ResourceBundle.getBundle("raptor.international.messages",
					locale);
		}
	}

	/**
	 * Returns the internationalized message with the specified key.
	 * 
	 * @param key
	 *            The property key.
	 * @return The value.
	 */
	public static String getMessage(String key) {
		synchronized (MessageUtil.class) {
			return bundle.getString(key);
		}
	}

	/**
	 * Returns an internationalized message with the specified key and
	 * parameters.
	 * 
	 * @param key
	 *            The key.
	 * @param params
	 *            The parameters.
	 * @return The value.
	 */
	public static String getMessage(String key, Object[] params) {
		synchronized (MessageUtil.class) {
			return new MessageFormat(getMessage(key)).format(params);
		}
	}

	/**
	 * Returns the internationalized message for the specified key and integer
	 * parameter.
	 * 
	 * @param key
	 *            The key.
	 * @param param0
	 *            The integer.
	 * @return The internationalized value.
	 */
	public static String getMessage(String key, int param0) {
		synchronized (MessageUtil.class) {
			return new MessageFormat(getMessage(key))
					.format(new Object[] { param0 });
		}
	}

	/**
	 * Returns the internationalized message for the specified key and integer
	 * parameter.
	 * 
	 * @param key
	 *            The key.
	 * @param param0
	 *            The string parameter.
	 * @return The internationalized value.
	 */
	public static String getMessage(String key, String param0) {
		synchronized (MessageUtil.class) {
			return new MessageFormat(getMessage(key))
					.format(new Object[] { param0 });
		}
	}
}
