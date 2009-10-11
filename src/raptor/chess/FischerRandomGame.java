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
package raptor.chess;

import raptor.chess.pgn.PgnHeader;

/**
 * <pre>
 * White &quot;a&quot;-side castling (0-0-0):
 * --------------------------------
 * Before: Kg1; Rf1, e1, d1, c1, b1 or a1	     After: Kc1; Rd1.
 * Before: Kf1; Re1, d1, c1, b1, or a1	     After: Kc1; Rd1.
 * Before: Ke1; Rd1, c1, b1, or a1 	     After: Kc1; Rd1.
 * Before: Kd1; Rc1, b1 or a1		     After: Kc1; Rd1.
 * Before: Kc1; Rb1 or a1			     After: Kc1; Rd1.
 * Before: Kb1; Ra1			     After: Kc1; Rd1.
 * 
 * White &quot;h&quot;-side castling (0-0):
 * ------------------------------
 * Before: Kb1; Rc1, d1, e1, f1, g1 or h1.      After: Kg1; Rf1.
 * Before: Kc1; Rd1, e1, f1, g1 or h1	     After: Kg1; Rf1.
 * Before: Kd1; Re1, f1, g1 or h1		     After: Kg1; Rf1.
 * Before: Ke1; Rf1, g1 or h1		     After: Kg1; Rf1.
 * Before: Kf1; Rg1 or h1			     After: Kg1; Rf1.
 * Before: Kg1; Rh1			     After: Kg1; Rf1.
 * Black &quot;a&quot;-side castling (... 0-0-0):
 * ------------------------------------
 * Before: Kg8; Rf8, e8, d8, c8, b8 or a8	     After: Kc8; Rd8.
 * Before: Kf8; Re8, d8, c8, b8 or a8	     After: Kc8; Rd8.
 * Before: Ke8; Rd8, c8, b8 or a8		     After: Kc8; Rd8.
 * Before: Kd8; Rc8, b8 or a8		     After: Kc8; Rd8.
 * Before: Kc8; Rb8 or a8			     After: Kc8; Rd8.
 * Before: Kb8; Ra8			     After: Kc8; Rd8.
 * 
 * Black &quot;h&quot;-side castling (... 0-0): 
 * ----------------------------------
 * Before: Kb8; Rc8, d8, e8, f8, g8 or h8	     After: Kg8; Rf8.
 * Before: Kc8; Rd8, e8, f8, g8 or h8	     After: Kg8; Rf8.
 * Before: Kd8; Re8, f8, g8 or h8		     After: Kg8; Rf8.
 * Before: Ke8; Rf8, g8 or h8		     After: Kg8; Rf8.
 * Before: Kf8; Rg8 or h8			     After: Kg8; Rf8.
 * Before: Kg8; Rh8			     After: Kg8; Rf8.
 * </pre>
 * 
 * TO DO: add in castling support.
 */
public class FischerRandomGame extends ClassicGame {

	public FischerRandomGame() {
		setHeader(PgnHeader.Variant, Variant.fischerRandom.name());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FischerRandomGame deepCopy(boolean ignoreHashes) {
		FischerRandomGame result = new FischerRandomGame();
		overwrite(result, ignoreHashes);
		return result;
	}

	@Override
	public PriorityMoveList getLegalMoves() {
		return null;
	}

	@Override
	public boolean isLegalPosition() {
		return false;
	}
}
