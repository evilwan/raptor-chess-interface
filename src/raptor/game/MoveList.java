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
package raptor.game;

import java.util.ArrayList;
import java.util.List;

public final class MoveList implements GameConstants {
	private Move[] moves;
	private int size = 0;

	public MoveList() {
		this(MAX_HALF_MOVES_IN_GAME);
	}

	public MoveList(int maxSize) {
		moves = new Move[maxSize];
	}

	public void append(Move move) {
		moves[size++] = move;
	}

	public Move[] asArray() {
		Move[] result = new Move[size];
		System.arraycopy(moves, 0, result, 0, size);
		return result;
	}

	public List<Move> asList() {
		List<Move> result = new ArrayList<Move>(size);
		for (int i = 0; i < size; i++) {
			result.add(moves[i]);
		}
		return result;
	}

	public void clear() {
		size = 0;
	}

	public MoveList deepCopy() {
		MoveList result = new MoveList();
		for (int i = 0; i < moves.length; i++) {
			result.moves[i] = moves[i];
		}
		result.size = size;
		return result;
	}

	public Move get(int index) {
		return moves[index];
	}

	public Move getLast() {
		return moves[size - 1];
	}

	public int getSize() {
		return size;
	}

	public Move removeLast() {
		return moves[--size];
	}

	@Override
	public String toString() {
		return asList().toString();
	}
}
