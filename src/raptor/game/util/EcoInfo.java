package raptor.game.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.game.Game;
import raptor.game.Move;
import raptor.game.MoveList;

/**
 * @author John Nahlen (johnthegreat)
 */
public class EcoInfo {
	private static final Log LOG = LogFactory.getLog(EcoInfo.class);

	/**
	 * Maps move sequences to ECOParser instances.
	 */
	private static List<EcoInfo> ecoInfos = new ArrayList<EcoInfo>();

	static {
		File f = new File(raptor.Raptor.RESOURCES_COMMON_DIR + "ECO.txt");
		try {
			EcoInfo.parse(f);
		} catch (Exception e) {
			LOG.error(
					"Error occurred reading file containing ECO information: "
							+ f.getAbsolutePath(), e);
		}
	}

	/**
	 * @param game
	 *            Instance of Game that an ECOParser object needs to be
	 *            retrieved from.
	 * @return ECOParser instance if found, <code>null</code> if not.
	 */
	public static EcoInfo getECOParser(Game game) {
		if (game.getType() == Game.Type.CLASSIC) {
			MoveList m = game.getMoveList();
			Move[] moves = m.asArray();

			StringBuilder builder = new StringBuilder(100);
			for (int i = 0; i < moves.length; i++) {
				if (i >= 22)
					break;
				builder.append(moves[i].getSan().toUpperCase() + " ");
			}
			String key = builder.toString().trim();

			EcoInfo currentCandidate = null;
			for (EcoInfo parser : ecoInfos) {
				if (key.startsWith(parser.moveSequence)) {
					if (currentCandidate == null) {
						currentCandidate = parser;
					} else if (currentCandidate.moveSequence.length() < parser.moveSequence
							.length()) {
						currentCandidate = parser;
					}
				}
			}
			return currentCandidate;
		}
		return null;
	}

	/**
	 * <b>NOTE: This must be called before you can use any services that this
	 * class provides.</b><br />
	 * Parses ECO information from File <code>file</code>.<br />
	 * Example line in File <code>file</code>: A3|A00|Anderssen's
	 * Opening|Romford counter-gambit
	 * 
	 * @param file
	 *            File containing the ECO information.
	 * @throws IOException
	 *             If something goes wrong during reading.
	 */
	public static void parse(File file) throws IOException,
			NullPointerException {
		LOG.info("parse() begin");
		if (EcoInfo.ecoInfos.size() > 0)
			return;

		BufferedReader reader = new BufferedReader(new FileReader(file));
		while (reader.ready()) {
			String line = reader.readLine();
			String[] arr = line.split("\\|");
			String varName = "";
			if (arr.length == 4)
				varName = arr[3];
			if (arr.length < 3)
				continue;
			EcoInfo parser = new EcoInfo(arr[0], arr[1], arr[2], varName);
			EcoInfo.ecoInfos.add(parser);
		}
		reader.close();
		LOG.info("parse() end");
	}

	private String moveSequence;
	private String ecoCode;
	private String openingName;
	private String variationName = "";

	public EcoInfo(String moves, String eco, String opening, String variation) {
		this.moveSequence = moves;
		this.ecoCode = eco;
		this.openingName = opening;
		this.variationName = variation;
	}

	/**
	 * @return The ECO code.
	 */
	public String getEcoCode() {
		return ecoCode;
	}

	/**
	 * @return The move sequence required to get to this ECO code.
	 */
	public String getMoves() {
		return moveSequence;
	}

	/**
	 * @return The name of the opening.
	 */
	public String getOpening() {
		return openingName;
	}

	/**
	 * @return The variation name of the opening.
	 */
	public String getVariation() {
		return variationName;
	}

	/**
	 * @return <code>getOpening() + " : " + getVariation()</code>
	 */
	@Override
	public String toString() {
		return getEcoCode() + " " + getOpening() + "(" + getVariation() + ")";
	}
}
