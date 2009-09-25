package raptor.connector.fics.game.message;

/**
 *iv_gameinfo
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
 * u=white_registered(1/0),black_registered(1/0)
 * it=initial_white_time,initial_black_time
 * i=initial_white_inc,initial_black_inc pt=partner's_game_number(or 0 if none)
 * rt=white_rating(+ provshow character),black_rating(+ provshow character)
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
public class G1Message {
	public String gameId;

	public String parterGameId;
	public String gameTypeDescription;
	public String whiteRating;
	public String blackRating;
	public boolean isRated;
	public boolean isPrivate;
	public boolean isWhtieRegistered;
	public boolean isBlackRegistered;
	public long initialWhiteTimeMillis;
	public long initialBlackTimeMillis;
	public long initialWhiteIncMillis;
	public long initialBlackIncMillis;
	public long whiteRemainingTime;
	public long blackRemainingTime;
	public boolean isBlackUsingTimeseal;
	public boolean isWhiteUsingTimeseal;

	@Override
	public String toString() {
		return "G1Message: gameId=" + gameId;
	}
}