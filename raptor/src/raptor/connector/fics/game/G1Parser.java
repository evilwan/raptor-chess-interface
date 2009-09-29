package raptor.connector.fics.game;

import raptor.connector.fics.game.message.G1Message;
import raptor.util.RaptorStringTokenizer;

/**
 *iv_gameinfo help file on fics. CDays comments are in []s because some of the
 * documentation is just wrong.
 * 
 * Setting ivariable gameinfo provides the interface with extra notifications
 * when the start starts a game or simul or a game is observed.
 * 
 *Example output:
 * 
 *- <g1> 1 p=0 t=blitz r=1 u=1,1 it=5,5 i=8,8 pt=0 rt=1586E,2100 ts=1,0
 * 
 * (note the - was added so as not to confuse interfaces displaying this
 * helpfile)
 * 
 * This is in the format: game_number p=private(1/0) t=type r=rated(1/0)
 * 
 * u=white_registered(1/0),black_registered(1/0) [ CDay: ***INCORRECT they are
 * set to 1 if white/black are UNregistered]
 * 
 * it=initial_white_time,initial_black_time [ CDay: **INCORRECT** its
 * it=initialWhiteTimeSec,initialWhiteIncSec]
 * 
 * i=initial_white_inc,initial_black_inc pt=partner's_game_number(or 0 if none)
 * [CDay: **INCORRECT** its it=initialBlackTimeSec,initialBlackIncSec]
 * 
 * rt=white_rating(+ provshow character),black_rating(+ provshow character)
 * 
 * ts=white_uses_timeseal(0/1),black_uses_timeseal(0/1)
 * 
 * Note any new fields will be appended to the end so the interface must be able
 * to handle this.
 * 
 * See Also: iset ivariables
 * 
 * [Last modified: August 3rd, 2000 -- DAV]
 * 
 * m=0 n=0 were added to the end. I have yet to figure out what they are
 */
public class G1Parser {
	public static final String G1 = "<g1>";

	/**
	 * Orphaned case.
	 */
	public G1Message parse(String message) {
		G1Message result = null;
		if (message.startsWith(G1)) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(message,
					" =,", true);
			result = new G1Message();

			// parse past <g1>
			tok.nextToken();

			result.gameId = tok.nextToken();

			// parse past p
			tok.nextToken();
			result.isPrivate = tok.nextToken().equals("1");

			// parse past t
			tok.nextToken();
			result.gameTypeDescription = tok.nextToken();

			// parse past r
			tok.nextToken();
			result.isRated = tok.nextToken().equals("1");

			// parse past u
			tok.nextToken();
			result.isWhtieRegistered = !tok.nextToken().equals("1");
			result.isBlackRegistered = !tok.nextToken().equals("1");

			// parse past it
			tok.nextToken();
			result.initialWhiteTimeMillis = Long.parseLong(tok.nextToken()) * 1000;
			result.initialWhiteIncMillis = Long.parseLong(tok.nextToken()) * 1000;

			// parse past i
			tok.nextToken();
			result.initialBlackTimeMillis = Long.parseLong(tok.nextToken()) * 1000;
			result.initialBlackIncMillis = Long.parseLong(tok.nextToken()) * 1000;

			// parse past pt
			tok.nextToken();
			result.parterGameId = tok.nextToken();

			// parse past rt
			tok.nextToken();
			result.whiteRating = tok.nextToken();
			result.blackRating = tok.nextToken();

			// parse past ts
			tok.nextToken();
			result.isWhiteUsingTimeseal = tok.nextToken().equals("1");
			result.isBlackUsingTimeseal = tok.nextToken().equals("1");

			// m and n are still there i have no idea what they are.
			// If you do please update the documentation and let the team know.
			// They might be useful.

			if (!result.isBlackRegistered) {
				result.blackRating = "++++";
			}
			if (!result.isWhtieRegistered) {
				result.whiteRating = "++++";
			}

			return result;
		}
		return result;
	}
}
