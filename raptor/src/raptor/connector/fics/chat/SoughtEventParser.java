package raptor.connector.fics.chat;

public class SoughtEventParser {

	// private static final Logger logger = Logger
	// .getLogger(SoughtEventParser.class);
	//
	// public SoughtEventParser(int icsId) {
	// super(icsId);
	// }
	//
	// @Override
	// public IcsNonGameEvent parse(String text) {
	// if (text.endsWith(ADS_DISPLAYED) || text.endsWith(AD_DISPLAYED)) {
	//			
	// //Make sure the first word is an integer this
	// //is to make sure events are not running together.
	// //Since its rare if it happens just return null.
	// int firstSpace = text.indexOf(" ");
	// try
	// {
	// if (firstSpace == -1)
	// {
	// return null;
	// }
	// Integer.parseInt(text.substring(0,firstSpace));
	// }
	// catch (Throwable t)
	// {
	// return null;
	// }
	//			
	// String[] lines = text.split("\n\\s*");
	//
	// List<Seek> seeks = new LinkedList<Seek>();
	//
	// for (int i = 0; i < lines.length - 1; i++) { // we don't care
	// // about last line
	// String line = lines[i];
	// if (logger.isDebugEnabled()) {
	// logger.debug("Sought line: " + line);
	// }
	//
	// String[] parts = line.split("\\s+");
	// // for (String part : parts) {
	// // System.err.println("Part: '" + part + "'");
	// // }
	//
	// int rating = -1;
	//
	// try {
	// rating = Integer.parseInt(parts[1].trim());
	// } catch (NumberFormatException e) {
	//
	// }
	//
	// Seek in = new Seek(Integer.parseInt(parts[0].trim()), rating,
	// parts[2].trim(), Integer.parseInt(parts[3].trim()),
	// Integer.parseInt(parts[4].trim()), "rated"
	// .equals(parts[5].trim()) ? true : false);
	// in.setType(parts[6].trim());
	// seeks.add(in);
	// }
	//
	// return new SoughtEvent(getIcsId(), text, seeks);
	// } else {
	// return null;
	// }
	// }

	private static final String AD_DISPLAYED = "ad displayed.";
	private static final String ADS_DISPLAYED = "ads displayed.";

}
