package raptor.connector.fics.game;

import raptor.connector.fics.game.message.GameEndMessage;

public class ParserTest {

	public static void main(String args[]) {
		GameEndParser parser = new GameEndParser();
		GameEndMessage endMessage = parser
				.parse("{Game 170 (skeyman vs. vazydjodje) skeyman checkmated} 0-1");
		System.out.println(endMessage);
	}
}
