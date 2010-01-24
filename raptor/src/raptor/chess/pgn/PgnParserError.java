/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.chess.pgn;

import java.io.Serializable;

import raptor.util.RaptorStringUtils;

public class PgnParserError implements Serializable {
	public static enum Action {
		IGNORING, IGNORING_CURRENT_GAME, IGNORING_CURRENT_SUBLINE, IGNORIONG_HEADER, NONE,
	}

	/**
	 * Provided for internationalization support. To add a new cause add it to
	 * this enum and update the resource bundles which use it.
	 */
	public static enum Type {
		DANGLING_ANNOTATION, DANGLING_NAG,
		/**
		 * String[0] = annotation (This can happen in chessbase and its a real
		 * pain to support).
		 */
		DANGLING_SUBLINE, ILLEGAL_MOVE_ENCOUNTERED, INVALID_SUBLINE_STATE, UNABLE_TO_PARSE_INITIAL_FEN,
		/**
		 * String[0] = annotation text.
		 */
		UNEXPECTED_GAME_END, UNEXPECTED_GAME_START, UNEXPECTED_HEADER,
		/**
		 * String[0] = text.
		 */
		UNEXPECTED_MOVE_ANNOTATION, UNEXPECTED_MOVE_WORD, /**
		 * String[0] =word.
		 */
		UNEXPECTED_SUBLINE_END, UNEXPECTED_SUBLINE_START, /**
		 * String[0] = move
		 **/
		UNKNOWN_TEXT_ENCOUNTERED, /**
		 * String[0] =nag
		 */
		UNKNWON_ERROR,
		/**
		 * String[0] = header name. String[1] = header value.
		 **/
		UNSUPPORTED_PGN_HEADER
	}

	static final long serialVersionUID = 1;

	private Action action;

	private String[] args;

	private int lineNumber;

	private Type type;

	public PgnParserError(Type type, Action action, int lineNumber) {
		this.type = type;
		this.action = action;
		this.lineNumber = lineNumber;
	}

	public PgnParserError(Type type, Action action, int lineNumber,
			String... args) {
		this(type, action, lineNumber);
		this.args = args;
	}

	public Action getAction() {
		return action;
	}

	public String[] getArgs() {
		return args;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return type.name() + " " + action.name() + " "
				+ RaptorStringUtils.toDelimitedString(args, "'") + " "
				+ getLineNumber();
	}
}
