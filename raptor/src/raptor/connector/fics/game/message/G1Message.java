/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
	public String blackRating;

	public long blackRemainingTime;
	public String gameId;
	public String gameTypeDescription;
	public long initialBlackIncMillis;
	public long initialBlackTimeMillis;
	public long initialWhiteIncMillis;
	public long initialWhiteTimeMillis;
	public boolean isBlackRegistered;
	public boolean isBlackUsingTimeseal;
	public boolean isPrivate;
	public boolean isRated;
	public boolean isWhiteUsingTimeseal;
	public boolean isWhtieRegistered;
	public String parterGameId;
	public String whiteRating;
	public long whiteRemainingTime;

	@Override
	public String toString() {
		return "G1Message: gameId=" + gameId;
	}
}