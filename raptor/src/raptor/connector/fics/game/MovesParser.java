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
package raptor.connector.fics.game;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.connector.fics.game.message.MovesMessage;
import raptor.connector.ics.IcsUtils;
import raptor.util.RaptorStringTokenizer;

/***
 * <pre>
 * Movelist for game 317:
 * 
 * GMFridman (2621) vs. GMLami (2617) --- Fri Nov 23, 10:07 EST 2007
 * 
 * Unrated standard match, initial time: 120 minutes, increment: 0 seconds.
 * 
 * 
 * 
 * Move GMFridman GMLami
 * 
 * ---- --------------------- ---------------------
 * 
 * 1. d4 (0:00.000) Nf6 (0:00.000)
 * 
 * 2. c4 (0:04.795) g6 (0:04.678)
 * 
 * 3. Nc3 (0:04.241) d5 (0:04.243)
 * 
 * 4. Nf3 (0:03.529) Bg7 (0:04.225)
 * 
 * 5. Bf4 (0:04.442) dxc4 (0:35.023)
 * 
 * 6. e4 (1:07.852) Bg4 (1:08.073)
 * 
 * 7. Bxc4 (2:18.737) O-O (0:05.781)
 * 
 * 8. Be2 (6:46.331) Nfd7 (4:37.310)
 * 
 * 9. O-O (3:25.254) Nc6 (9:17.124)
 * 
 * 10. Be3 (9:11.211) Nb6 (4:41.689)
 * 
 * 11. d5 (3:27.965) Bxf3 (0:04.249)
 * 
 * 12. gxf3 (1:06.836) Ne5 (5:50.054)
 * 
 * 13. Qb3 (5:52.835) c6 (6:57.075)
 * 
 * 14. f4 (9:21.271) Ned7 (1:10.942)
 * 
 * 15. Rfd1 (2:21.745) Qe8 (16:30.391)
 * 
 * 16. a4 (3:31.260) cxd5 (7:01.569)
 * 
 * 17. a5 (3:32.960) d4 (4:41.201)
 * 
 * 18. Bxd4 (3:30.739) Bxd4 (1:11.232)
 * 
 * 19. Rxd4 (0:04.295) Nc8 (22:21.275)
 * 
 * 20. e5 (4:42.737) a6 (4:43.694)
 * 
 * 21. Qd5 (5:53.150) Nb8 (1:10.783)
 * 
 * 22. Qxb7 (1:14.463) Nc6 (7:03.021)
 * 
 * 23. Rc4 (2:21.858) N8a7 (0:04.283)
 * 
 * 24. Bf3 (0:04.220) Rc8 (0:04.230)
 * 
 * 25. Qxa6 (14:03.500) Qd7 (3:30.980)
 * 
 * 26. Rd1 (4:47.296) Qe6 (0:04.757)
 * 
 * 27. Bg2 (5:52.019) Nxa5 (0:04.266)
 * 
 * 28. Qxe6 (1:05.047) fxe6 (0:04.263)
 * 
 * 29. Ra4 (1:06.484) N7c6 (0:04.286)
 * 
 * 30. Bh3 (0:04.237) Rb8 (2:14.263)
 * 
 * 31. Bxe6+ (0:04.284) Kg7 (0:04.266)
 * 
 * 32. Nd5 (8:15.413) Nb3 (4:47.634)
 * 
 * 33. Bd7 (3:41.072) Nd8 (3:35.402)
 * 
 * 34. Nxe7 (2:21.723) Nc5 (0:04.247)
 * 
 * 35. Rc4 (1:14:24.313) Nde6 (0:05.403)
 * 
 * 36. b4 (0:04.291) Nxd7 (0:04.294)
 * 
 * 37. Rxd7 (0:04.388) Rf7 (0:05.140)
 * 
 * 38. f5 (0:05.673)
 * 
 * {Still in progress}
 * </pre>
 */
public class MovesParser {
	private static final String EVENT_START = "\nMovelist for game ";

	private static final String EVENT_START_2 = "fics% \nMovelist for game ";

	private static final Log LOG = LogFactory.getLog(MovesParser.class);

	public MovesParser() {
		super();
	}

	public MovesMessage parse(String string) {
		if (string.startsWith(EVENT_START) || string.startsWith(EVENT_START_2)) {
			int lastDash = string.lastIndexOf("--");
			int firstColon = string.indexOf(':', 0);
			boolean startsWithEventStart = string.startsWith(EVENT_START);
			String gameNumber = string.substring(
					startsWithEventStart ? EVENT_START.length() : EVENT_START_2
							.length(), firstColon);

			if (lastDash != -1) {
				try {
					String afterDash = string.substring(lastDash);
					RaptorStringTokenizer multiLineTok = new RaptorStringTokenizer(
							afterDash, "\n", true);
					multiLineTok.nextToken();

					List<String> moves = new ArrayList<String>(100);
					List<Long> moveTimes = new ArrayList<Long>(100);

					while (multiLineTok.hasMoreTokens()) {
						String currentLine = multiLineTok.nextToken();
						if (currentLine.trim()
								.startsWith("{Still in progress}")) {
							break;
						} else if (currentLine.trim().startsWith("{")) {
							break;
						} else {
							RaptorStringTokenizer lineTok = new RaptorStringTokenizer(
									currentLine, " ", true);

							// Parse past white move number.
							if (lineTok.hasMoreTokens()) {
								lineTok.nextToken();
							}

							// White move number.
							if (lineTok.hasMoreTokens()) {
								moves.add(lineTok.nextToken());
							}

							// White time.
							if (lineTok.hasMoreTokens()) {
								String whiteTime = lineTok.nextToken();
								moveTimes.add(IcsUtils.timeToLong(whiteTime));
							}

							// Blacks move
							if (lineTok.hasMoreTokens()) {
								moves.add(lineTok.nextToken());
							}

							// Blacks time
							if (lineTok.hasMoreTokens()) {
								String blackTime = lineTok.nextToken();
								moveTimes.add(IcsUtils.timeToLong(blackTime));
							}
						}
					}
					MovesMessage result = new MovesMessage();
					result.moves = moves.toArray(new String[0]);
					result.timePerMove = moveTimes.toArray(new Long[0]);
					result.gameId = gameNumber;
					return result;
				} catch (Exception e) {
					LOG.error("Error occured parsing movelist", e);
					return null;
				}
			} else {
				return null;
			}
		}
		return null;
	}
}
