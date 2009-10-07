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
 * Special information for bughouse games --------------------------------------
 * 
 * When showing positions from bughouse games, a second line showing piece
 * holding is given, with "<b1>" at the beginning, for example:
 * 
 * <b1> game 6 white [PNBBB] black [PNB]
 * 
 * Also, when pieces are "passed" during bughouse, a short data string -- not
 * the entire board position -- is sent. For example:
 * 
 * <b1> game 52 white [NB] black [N] <- BN
 * 
 * The final two letters indicate the piece that was passed; in the above
 * example, a knight (N) was passed to Black.
 * 
 * A prompt may preceed the <b1> header.
 */
public class B1Message {
	public String gameId;

	/**
	 * Indexed by GameConstants piece type, valued by the number of pieces.
	 */
	public int[] blackHoldings;
	/**
	 * Indexed by GameConstants piece type, valued by the number of pieces.
	 */
	public int[] whiteHoldings;

	@Override
	public String toString() {
		return "B1Message: gameId=" + gameId;
	}
}