/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.swt.chess;

import org.eclipse.swt.graphics.Color;

/**
 * A class representing a highlight of chess squares. A highlight may fade away
 * or it may be constant. It can also represent a single square or contain a
 * start and end square. Currently only the start and end squares are
 * highlighted however in the future the pattern between those squares may also
 * be highlighted.
 */
public class Highlight {
	protected int startSquare;
	protected int endSquare;
	protected Color color;
	protected int frame;
	protected boolean isFadeAway;

	public Highlight(int square, Color color) {
		startSquare = square;
		endSquare = -1;
		this.color = color;
		frame = -1;
	}

	public Highlight(int square, Color color, boolean isFadeAway) {
		startSquare = square;
		endSquare = -1;
		this.color = color;
		this.isFadeAway = isFadeAway;
		if (!isFadeAway) {
			frame = -1;
		} else {
			frame = 3;
		}
	}

	public Highlight(int startSquare, int endSquare, Color color) {
		this.startSquare = startSquare;
		this.endSquare = endSquare;
		this.color = color;
		frame = -1;
	}

	public Highlight(int startSquare, int endSquare, Color color,
			boolean isFadeAway) {
		this.startSquare = startSquare;
		this.endSquare = endSquare;
		this.color = color;
		this.isFadeAway = isFadeAway;
		if (!isFadeAway) {
			frame = -1;
		} else {
			frame = 3;
		}
	}

	@Override
	public boolean equals(Object object) {
		Highlight highlight = (Highlight) object;
		return startSquare == highlight.startSquare
				&& endSquare == highlight.endSquare
				&& isFadeAway == highlight.isFadeAway;
	}

	public Color getColor() {
		return color;
	}

	public int getEndSquare() {
		return endSquare;
	}

	public int getStartSquare() {
		return startSquare;
	}

	public boolean isFadeAway() {
		return isFadeAway;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setEndSquare(int endSquare) {
		this.endSquare = endSquare;
	}

	public void setFadeAway(boolean isFadeAway) {
		this.isFadeAway = isFadeAway;
	}

	public void setStartSquare(int startSquare) {
		this.startSquare = startSquare;
	}
}