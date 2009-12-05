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

/**
 * An enum used to classify variants in chess.
 */
public enum Variant {
	/**
	 * Atomic chess.
	 */
	atomic,
	/**
	 * Bughouse chess.
	 */
	bughouse,
	/**
	 * Fischer Random Bughouse.
	 */
	fischerRandomBughouse,
	/**
	 * Normal chess.
	 */
	classic,
	/**
	 * Blitz classic
	 */
	blitz,
	/**
	 * Standard classic.
	 */
	standard,
	/**
	 * Lightning classic.
	 */
	lightning,
	/**
	 * Crazyhouse chess.
	 */
	crazyhouse,
	/**
	 * Fischer Random crazyhouse.
	 */
	fischerRandomCrazyhouse,
	/**
	 * Fischer random chess.
	 */
	fischerRandom,
	/**
	 * Losers chess.
	 */
	losers,
	/**
	 * Suicide chess.
	 */
	suicide,
	/**
	 * Wild Chess. Follow the classical chess rules but starts from different
	 * positions.
	 */
	wild;

	/**
	 * Returns the ics match type to use for a specified variant given a game.
	 * Currently wild is not supported. wild 5 bug on bics will also match to
	 * bughouse.
	 * 
	 * If null is returned its not supported.
	 */
	public static String getIcsMatchType(Game game) {
		switch (game.getVariant()) {
		case atomic:
			return "atomic";
		case bughouse:
			return "bughouse";
		case fischerRandomBughouse:
			return "bughouse fr";
		case classic:
			return "";
		case crazyhouse:
			return "zh";
		case fischerRandomCrazyhouse:
			return "zh fr";
		case losers:
			return "losers";
		case suicide:
			return "suicide";
		case wild:
			return null;
		case fischerRandom:
			return "wild fr";
		default:
			return null;
		}
	}

	public static boolean isBughouse(Variant variant) {
		return variant == Variant.bughouse
				|| variant == Variant.fischerRandomBughouse;
	}

	public static boolean isClassic(Variant variant) {
		return variant == Variant.wild || variant == Variant.classic
				|| variant == Variant.blitz || variant == Variant.standard
				|| variant == Variant.lightning;
	}

	public static boolean isCrazyhouse(Variant variant) {
		return variant == Variant.crazyhouse
				|| variant == Variant.fischerRandomCrazyhouse;
	}
}
