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
package raptor.util;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RegExUtils {
	private static final Log LOG = LogFactory.getLog(RegExUtils.class);

	public static Pattern getPattern(String regularExpression) {
		try {
			return Pattern.compile(regularExpression, Pattern.MULTILINE
					| Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		} catch (Throwable t) {
			LOG.warn(
					"RegularExpression pattern creation threw an exception. regex="
							+ regularExpression, t);
			return null;
		}
	}

	public static String getRegularExpressionHelpHtml() {
		StringBuilder builder = new StringBuilder(5000);
		builder.append("<html>\n<body>\n");
		builder.append("<h1>Raptor Regular Expressions</h1>\n");
		builder
				.append("<p>Regular expressions in Raptor are similar but not identical to regular "
						+ "expressions in perl and other languages. The main difference is the use of the '*' character."
						+ "'*text*' is actually not a valid regular expression. To match on something like "
						+ "this try '.*text.*' instead'.</p>"
						+ "<p>Current raptor supports the DOTALL,and CASE_INSENSITVE flags in regular expressions."
						+ "This means matches are case insensitive and the '.' is treated as every character."
						+ "It also uses the MULTINE flag. The following is a description of MULTILINE from the Pattern javadoc:<br/>"
						+ "<span style=\"font-style:italic\">"
						+ "   By default, the regular expressions ^ and $ ignore line terminators and only match at the beginning "
						+ "and the end, respectively, of the entire input sequence. If MULTILINE mode is activated then ^ matches at "
						+ "the beginning of input and after any line terminator except at the end of input. When in MULTILINE mode $ "
						+ "matches just before a line terminator or the end of the input sequence."
						+ "</span>"
						+ "</p>"
						+ " <p>For futher information on regular expressions in java check out: <br/>"
						+ "<a href=\"http://java.sun.com/j2se/1.5.0/docs/api/java/util/regex/Pattern.html\">java.util.regex.Pattern</a></p>");
		builder.append("</body></html>");
		return builder.toString();
	}

	public static boolean matches(Pattern pattern, String stringToTest) {
		try {
			return pattern.matcher(stringToTest).matches();
		} catch (Throwable t) {
			LOG.warn("matches threw exception. regex=" + pattern.pattern()
					+ " test=" + stringToTest, t);
			return false;
		}
	}

	public static boolean matches(String regularExpression, String stringToTest) {
		try {
			return getPattern(regularExpression).matcher(stringToTest)
					.matches();
		} catch (Throwable t) {
			LOG.warn("matches threw exception. regex=" + regularExpression
					+ " test=" + stringToTest, t);
			return false;
		}
	}
}
