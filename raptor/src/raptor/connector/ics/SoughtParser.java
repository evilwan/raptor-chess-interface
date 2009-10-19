package raptor.connector.ics;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.chat.Seek;
import raptor.chat.Seek.GameType;
import raptor.util.RaptorStringTokenizer;

/**
 * This code was adapted from Decaf Code written by kozyr
 */
public class SoughtParser {

	private static final Log LOG = LogFactory.getLog(SoughtParser.class);
	private static final String AD_DISPLAYED = "ad displayed.\nfics% ";
	private static final String ADS_DISPLAYED = "ads displayed.\nfics% ";

	public SoughtParser() {
	}

	public Seek[] parse(String message) {
		if (message.endsWith(ADS_DISPLAYED) || message.endsWith(AD_DISPLAYED)) {

			String[] lines = message.split("\n\\s*");

			List<Seek> seeks = new LinkedList<Seek>();

			for (int i = 0; i < lines.length - 2; i++) {
				// we don't care
				// about last 2 lines
				String line = lines[i];
				if (LOG.isDebugEnabled()) {
					LOG.debug("Sought line: " + line);
				}

				Seek seek = new Seek();
				RaptorStringTokenizer tok = new RaptorStringTokenizer(line,
						" -[]", true);
				seek.setAd(tok.nextToken());
				seek.setRating(tok.nextToken());
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
				} else {
					Raptor.getInstance().onError(
							"Unkown game type encountered in seek: "
									+ seek.getTypeDescription(),
							new Exception());
				}
				seeks.add(seek);
			}
			return seeks.toArray(new Seek[0]);
		} else {
			return null;
		}
	}
}
