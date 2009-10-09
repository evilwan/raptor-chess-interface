package raptor.chess.pgn;

public interface PgnParser {

	public void addPgnParserListener(PgnParserListener listener);

	public int getLineNumber();

	public void parse();

	public void removePgnParserListener(PgnParserListener listener);

}
