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

public class PgnParserWarning implements Serializable {
	/**
	 * Provided for internationalization support. To add a new cause add it to
	 * this enum and update the resource bundles which use it.
	 */
	public static enum Type {
	}

	static final long serialVersionUID = 1;

	private String[] args;

	private int rowNumber;

	private Type type;

	public PgnParserWarning(Type type, int rowNumber) {
		args = new String[0];
		this.rowNumber = rowNumber;
	}

	public PgnParserWarning(Type type, String[] args, int rowNumber) {
		this.type = type;
		this.args = args;
		this.rowNumber = rowNumber;
	}

	public String[] getArgs() {
		return args;
	}

	public int getRowNumber() {
		return rowNumber;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return type.name() + " "
				+ RaptorStringUtils.toDelimitedString(args, "'") + " "
				+ getRowNumber() + " ";
	}
}
