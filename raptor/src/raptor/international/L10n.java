/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2011, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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

import java.io.*;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.swt.LanguageDialog;

public class L10n {
	public static Locale currentLocale;
        public static boolean noSavedLocaleFile = false;
	private static L10n singletonInstance;
	private static ResourceBundle captions;
	public static Locale[] availableLocales = { Locale.ENGLISH, Locale.ITALIAN };
	private static Locale suitLocCache;
	
	public static L10n getInstance() {		
		if (singletonInstance == null)
			singletonInstance = new L10n();
		
		return singletonInstance;
	}
	
	private L10n() {
		init();		
	}

	private void init() {		
		Locale locale = getSuitableLocale();
		currentLocale = locale;
		captions = ResourceBundle.getBundle("raptor.international.Messages",
				locale);
	}
	
	public String getString(String key) {
		return captions.getString(key);
	}
	
	/**
	 * Reloads messages for new locale set in the preference store
	 */
	public static void updateLanguage() {
		Locale locale = new Locale(Raptor.getInstance().getPreferences()
				.getString(PreferenceKeys.APP_LOCALE));
		currentLocale = locale;
		captions = ResourceBundle.getBundle("raptor.international.Messages",
				locale);
		try {
			File localeFile = new File(Raptor.USER_RAPTOR_DIR + File.separator
					+ "locale");
			if (localeFile.exists()) {
				localeFile.delete();
			}
			FileOutputStream fstream = new FileOutputStream(localeFile);
			fstream.write(locale.getLanguage().getBytes());
			fstream.close();
		} catch (IOException e) {
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
	public String getString(String key, int param0) {
		synchronized (L10n.class) {
			return new MessageFormat(getString(key))
					.format(new Object[] { param0 });
		}
	}
	
	public String getString(String key, String param0) {
		synchronized (L10n.class) {
			return new MessageFormat(getString(key))
					.format(new Object[] { param0 });
		}
	}
	
	public String getString(String key, String param0, String param1) {
		synchronized (L10n.class) {
			return new MessageFormat(getString(key))
					.format(new Object[] { param0, param1 });
		}
	}
	
	public static Locale getSuitableLocale() {
		if (suitLocCache != null)
			return suitLocCache;

		suitLocCache = Locale.ENGLISH; // Default;
		try {
			File localeFile = new File(Raptor.USER_RAPTOR_DIR + File.separator
					+ "locale");
			if (!localeFile.exists()) {
				suitLocCache = new LanguageDialog().open(getSuitableLocaleName());
                                noSavedLocaleFile = true;                                
				return suitLocCache;
			}
			FileInputStream fstream = new FileInputStream(localeFile);
			byte[] lang = new byte[2];
			fstream.read(lang);
			fstream.close();
			suitLocCache = new Locale(new String(lang));
			return suitLocCache;
		} catch (IOException e) {
		}
		return suitLocCache;
	}

	public static Locale getSuitableLocaleName() {
		Locale systemLang = Locale.getDefault();
		for (Locale loc: availableLocales) {
			if (loc.getLanguage().equals(systemLang.getLanguage()))
				return systemLang;
		}		
		
		return Locale.ENGLISH; // Default
	}
}
