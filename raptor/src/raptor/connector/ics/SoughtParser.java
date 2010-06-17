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
package raptor.connector.ics;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import raptor.chat.Seek;
import raptor.chat.Seek.GameType;
import raptor.util.RaptorLogger;
import raptor.util.RaptorStringTokenizer;

/**
 * This code was adapted from Decaf Code written by kozyr
 */
public class SoughtParser {

	private static final RaptorLogger LOG = RaptorLogger.getLog(SoughtParser.class);
	private static final String AD_DISPLAYED = "ad displayed.";
	private static final String ADS_DISPLAYED = "ads displayed.";

	public SoughtParser() {
	}

	public Seek[] parse(String message) {
		if (message.endsWith(ADS_DISPLAYED) || message.endsWith(AD_DISPLAYED)) {

			String[] lines = message.split("\n\\s*");

			List<Seek> seeks = new LinkedList<Seek>();

			for (int i = 0; i < lines.length - 1; i++) {
				// we don't care
				// about last 1 lines
				String line = lines[i];
				if (LOG.isDebugEnabled()) {
					LOG.debug("Sought line: " + line);
				}

				Seek seek = new Seek();
				line = StringUtils.replace(line, "----", "****");
				RaptorStringTokenizer tok = new RaptorStringTokenizer(line,
						" -[]", true);
				seek.setAd(tok.nextToken());

				// Sought messages are tricky to parse since they can't be
				// identified clearly with a starting message.
				// Check to make sure the first row starts with an integer, if
				// it does'nt its not a sought message.
				if (i == 0) {
					try {
						Integer.parseInt(seek.getAd());
					} catch (NumberFormatException nfe) {
						return null;
					}
				}

				seek.setRating(tok.nextToken());
				if (seek.getRating().equals("****")) {
					seek.setRating("----");
				}
				seek.setName(tok.nextToken());
				seek.setMinutes(Integer.parseInt(tok.nextToken()));
				seek.setIncrement(Integer.parseInt(tok.nextToken()));
				seek.setRated(tok.nextToken().equals("rated"));
				seek.setTypeDescription(tok.nextToken());

				String lastToken1 = tok.nextToken();
				String lastToken2 = tok.nextToken();
				String lastToken3 = null;
				String lastToken4 = null;
				if (tok.hasMoreTokens()) {
					lastToken3 = tok.nextToken();
					if (tok.hasMoreTokens()) {
						lastToken4 = tok.nextToken();
					}
				}

				if (lastToken1.equals("black") || lastToken1.equals("white")) {
					seek
							.setColor(lastToken1.equals("white") ? Seek.GameColor.white
									: Seek.GameColor.black);
					seek.setMinRating(Integer.parseInt(lastToken2));
					seek.setMaxRating(Integer.parseInt(lastToken3));

					if (lastToken4 != null) {
						seek.setManual(lastToken4.contains("m"));
						seek.setFormula(lastToken4.contains("f"));
					}
				} else {
					seek.setMinRating(Integer.parseInt(lastToken1));
					seek.setMaxRating(Integer.parseInt(lastToken2));
					if (lastToken3 != null) {
						seek.setManual(lastToken3.contains("m"));
						seek.setFormula(lastToken3.contains("f"));
					}
				}

				if (seek.getTypeDescription().contains("blitz")) {
					seek.setType(GameType.blitz);
				} else if (seek.getTypeDescription().contains("lightning")) {
					seek.setType(GameType.lightning);
				} else if (seek.getTypeDescription().contains("standard")) {
					seek.setType(GameType.standard);
				} else if (seek.getTypeDescription().contains("suicide")) {
					seek.setType(GameType.suicide);
				} else if (seek.getTypeDescription().contains("losers")) {
					seek.setType(GameType.losers);
				} else if (seek.getTypeDescription().contains("atomic")) {
					seek.setType(GameType.atomic);
				} else if (seek.getTypeDescription().contains("fr")) {
					seek.setType(GameType.fischerRandom);
				} else if (seek.getTypeDescription().contains("crazyhouse")) {
					seek.setType(GameType.crazyhouse);
				} else if (seek.getTypeDescription().contains("wild")) {
					seek.setType(GameType.wild);
				} else if (seek.getTypeDescription().contains("untimed")) {
					seek.setType(GameType.untimed);
				} else {
					seek.setType(GameType.other);
				}
				seeks.add(seek);
			}
			return seeks.toArray(new Seek[0]);
		} else {
			return null;
		}
	}
}
