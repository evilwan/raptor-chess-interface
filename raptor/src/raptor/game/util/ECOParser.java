package raptor.game.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;


/**
 * @author John Nahlen (johnthegreat)
 */
public class ECOParser {
	
	/**
	 * Maps move sequences to ECOParser instances. 
	 */
	private static LinkedHashMap<String,ECOParser> map = new LinkedHashMap<String,ECOParser>();

	/**
	 * @see map
	 */
	public static LinkedHashMap<String,ECOParser> getMap() {
		return map;
	}
	
	/**
	 * <b>NOTE: This must be called before you can use any services that this class provides.</b><br />
	 * Parses ECO information from File <code>file</code>.<br />
	 * Example line in File <code>file</code>: A3|A00|Anderssen's Opening|Romford counter-gambit
	 * @param file File containing the ECO information.
	 * @throws IOException If something goes wrong during reading.
	 */
	public static void parse(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		while(reader.ready()) {
			String line = reader.readLine();
			String[] arr = line.split("|");
			ECOParser parser = new ECOParser(arr[0],arr[1],arr[2],arr[3]);
			ECOParser.getMap().put(arr[0],parser);
		}
	}
	
	private String MoveSequence;
	private String ECOCode;
	private String OpeningName;
	private String VariationName = "";
	
	public ECOParser(String moves,String eco,String opening,String variation) {
		this.MoveSequence = moves;
		this.ECOCode = eco;
		this.OpeningName = opening;
		this.VariationName = variation;
	}
	
	/**
	 * @return The ECO code.
	 */
	public String getECOCode() {
		return ECOCode;
	}
	
	/**
	 * @return The name of the opening.
	 */
	public String getOpening() {
		return OpeningName;
	}
	
	/**
	 * @return The variation name of the opening.
	 */
	public String getVariation() {
		return VariationName;
	}
	
	/**
	 * @return The move sequence required to get to this ECO code.
	 */
	public String getMoves() {
		return MoveSequence;
	}
	
	/**
	 * @return <code>getOpening() + ":" + getVariation()</code>
	 */
	@Override
	public String toString() {
		return getOpening() + ":" + getVariation();
	}
}
