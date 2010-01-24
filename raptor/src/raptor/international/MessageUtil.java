/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
