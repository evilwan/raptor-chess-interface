package raptor.game;

import org.apache.commons.lang.StringUtils;

/**
 * @author John Nahlen (johnthegreat)
 */
public class EcoInfo {

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
		if (StringUtils.isBlank(variationName)) {
			return getEcoCode() + " " + getOpening();
		} else {
			return getEcoCode() + " " + getOpening() + "(" + getVariation()
					+ ")";
		}
	}
}
