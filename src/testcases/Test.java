/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package testcases;

import java.util.Calendar;

import raptor.Quadrant;

public class Test {
	public static final long TIMEZONE_OFFSET = -Calendar.getInstance().get(
			Calendar.ZONE_OFFSET);

	public static void main(String args[]) throws Exception {

		for (Quadrant quad : Quadrant.values()) {
			System.err.println("Quadarnt.toString()=" + quad.toString()
					+ " Quadrant.name()=" + quad.name() + " Quadrant.ordinal="
					+ quad.ordinal());
		}

		// System.out.println(FicsUtils.replaceUnicode("&#x3b1;&#x3b2;&#x3b3;&#x3b4;&#x3b5;&#x3b6;"));
		// StringBuilder builder = new StringBuilder(""
		// + (char) Integer.valueOf("2654", 16).intValue());
		// FicsUtils.filterOutbound(builder);
		// System.out.println(builder.toString());
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
