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
package raptor.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.game.EcoInfo;
import raptor.game.Game;
import raptor.game.Move;
import raptor.game.MoveList;

/**
 * A singleton service which can be used to lookup EcoInfo on a game.
 * 
 * This service was modeled from initial code John Nahlen produced.
 * 
 * ECO.txt is a modified version of the ECO.txt in BabasChess.
 * 
 * Currently this service only supports Classic but hopefully others will
 * contribute files to match other variants (bug,zh,suicide,losers,etc).
 */
public class EcoService {

	private static final Log LOG = LogFactory.getLog(EcoService.class);

	private static final EcoService singletonInstance = new EcoService();

	public static EcoService getInstance() {
		return singletonInstance;
	}

	private Map<Game.Type, List<EcoInfo>> typeToInfoMap = new HashMap<Game.Type, List<EcoInfo>>();

	private Comparator<EcoInfo> moveLengthComparator = new Comparator<EcoInfo>() {

		public int compare(EcoInfo arg0, EcoInfo arg1) {
			return arg0.getMoves().length() > arg1.getMoves().length() ? 1
					: arg0.getMoves().length() == arg1.getMoves().length() ? 0
							: -1;
		}
	};

	private EcoService() {
		initClassic();
	}

	/**
	 * Disposes the EcoService.
	 */
	public void dispose() {
		typeToInfoMap.clear();
	}

	/**
	 * @param game
	 *            Instance of Game that an ECOParser object needs to be
	 *            retrieved from.
	 * @return ECOParser instance if found, <code>null</code> if not.
	 */
	public EcoInfo getEcoInfo(Game game) {
		if (LOG.isDebugEnabled()) {
			LOG.info("getEcoInfo()");
		}
		long startTime = System.currentTimeMillis();

		List<EcoInfo> list = typeToInfoMap.get(game.getType());
		EcoInfo result = null;

		if (list != null) {
			MoveList m = game.getMoveList();
			Move[] moves = m.asArray();

			StringBuilder builder = new StringBuilder(100);
			for (int i = 0; i < moves.length; i++) {
				if (i >= 22)
					break;
				builder.append(moves[i].getSan().toUpperCase() + " ");
			}
			String key = builder.toString().trim();

			for (EcoInfo info : list) {
				if (key.startsWith(info.getMoves())) {
					if (result == null) {
						result = info;
					} else if (result.getMoves().length() < info.getMoves()
							.length()) {
						result = info;
					}
				}

				// No need to continue searching we did'nt find anything.
				// The list will always be sorted by moves length ascending.
				if (info.getMoves().length() > key.length()) {
					break;
				}
			}
		}

		if (LOG.isDebugEnabled()) {
			LOG.info("getEcoInfo() executed in "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}
		return result;
	}

	private void initClassic() {
		File file = new File(raptor.Raptor.RESOURCES_COMMON_DIR + "ECO.txt");
		typeToInfoMap.put(Game.Type.CLASSIC, parse(file));
	}

	/**
	 * Parses ECO information from File <code>file</code>.<br />
	 * Example line in File <code>file</code>: A3|A00|Anderssen's
	 * Opening|Romford counter-gambit.
	 * 
	 * The result list will be sorted ascending by key length.
	 * 
	 * @param file
	 *            File containing the ECO information.
	 * @throws IOException
	 *             If something goes wrong during reading.
	 */
	private List<EcoInfo> parse(File file) {
		if (LOG.isDebugEnabled()) {
			LOG.info("parse(" + file.getAbsolutePath() + ")");
		}
		long startTime = System.currentTimeMillis();
		List<EcoInfo> result = new ArrayList<EcoInfo>(2000);

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			while (reader.ready()) {
				String line = reader.readLine();
				String[] arr = line.split("\\|");
				String varName = "";
				if (arr.length == 4)
					varName = arr[3];
				if (arr.length < 3)
					continue;
				EcoInfo parser = new EcoInfo(arr[0], arr[1], arr[2], varName);
				result.add(parser);
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
				}
			}
		}

		Collections.sort(result, moveLengthComparator);

		if (LOG.isDebugEnabled()) {
			LOG.info("parse( " + file.getAbsolutePath() + ") executed in "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}

		return result;
	}
}
