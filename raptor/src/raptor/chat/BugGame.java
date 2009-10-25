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
package raptor.chat;

/**
 * This code was adapted from some code johnthegreat for Raptor.
 */
public class BugGame {
	public boolean isRated;
	public String timeControl;
	public String game1Id;
	public String game2Id;
	public Bugger game1White;
	public Bugger game1Black;
	public Bugger game2White;
	public Bugger game2Black;

	public Bugger getGame1Black() {
		return game1Black;
	}

	public String getGame1Id() {
		return game1Id;
	}

	public Bugger getGame1White() {
		return game1White;
	}

	public Bugger getGame2Black() {
		return game2Black;
	}

	public String getGame2Id() {
		return game2Id;
	}

	public Bugger getGame2White() {
		return game2White;
	}

	public String getTimeControl() {
		return timeControl;
	}

	public boolean isRated() {
		return isRated;
	}

	public void setGame1Black(Bugger game1Black) {
		this.game1Black = game1Black;
	}

	public void setGame1Id(String game1Id) {
		this.game1Id = game1Id;
	}

	public void setGame1White(Bugger game1White) {
		this.game1White = game1White;
	}

	public void setGame2Black(Bugger game2Black) {
		this.game2Black = game2Black;
	}

	public void setGame2Id(String game2Id) {
		this.game2Id = game2Id;
	}

	public void setGame2White(Bugger game2White) {
		this.game2White = game2White;
	}

	public void setRated(boolean isRated) {
		this.isRated = isRated;
	}

	public void setTimeControl(String timeControl) {
		this.timeControl = timeControl;
	}
}
