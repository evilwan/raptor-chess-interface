package raptor.game.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

import raptor.game.Game;
import raptor.game.Move;
import raptor.game.MoveList;

/**
 * @author John Nahlen (johnthegreat)
 */
public class ECOParser {
	
	/**
	 * @param game Instance of Game that an ECOParser object needs to be retrieved from.
	 * @return ECOParser instance if found, <code>null</code> if not.
	 */
	public static ECOParser getECOParser(Game game) {
		MoveList m = game.getMoves();
		Move[] moves = m.asArray();
		
		String str = "";
		//for(int i=moves.length-1;i>0;i--) {
		for(int i=0;i<moves.length;i++) {
			if (i >= 10) break;
			str += moves[i].getSan() + " ";
			str = str.toUpperCase();
		}
		str = str.trim();
		System.out.println("str = " + str);
		if (ECOParser.getMap().containsKey(str)) {
			ECOParser parser = ECOParser.getMap().get(str);
			return parser;
		}
		
		return null;
	}
	
	
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
		System.err.println("parse() begin");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		while(reader.ready()) {
			String line = reader.readLine();
			String[] arr = line.split("\\|");
			//System.out.println(java.util.Arrays.toString(arr));
			String varName = "";
			if (arr.length == 4) varName = arr[3];
			ECOParser parser = new ECOParser(arr[0],arr[1],arr[2],varName);
			ECOParser.getMap().put(arr[0],parser);
		}
		System.err.println("parse() end");
	}
	
	private String moveSequence;
	private String EcoCode;
	private String openingName;
	private String variationName = "";
	
	public ECOParser(String moves,String eco,String opening,String variation) {
		this.moveSequence = moves;
		this.EcoCode = eco;
		this.openingName = opening;
		this.variationName = variation;
	}
	
	/**
	 * @return The ECO code.
	 */
	public String getECOCode() {
		return EcoCode;
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
	 * @return The move sequence required to get to this ECO code.
	 */
	public String getMoves() {
		return moveSequence;
	}
	
	/**
	 * @return <code>getOpening() + " : " + getVariation()</code>
	 */
	@Override
	public String toString() {
		return getOpening() + " : " + getVariation();
	}
}
