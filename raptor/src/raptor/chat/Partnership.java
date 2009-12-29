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
public class Partnership implements Comparable<Partnership> {
	private Bugger bugger1;
	private Bugger bugger2;

	public int compareTo(Partnership partnership) {
		int teamRating1 = getTeamRating();
		int teamRating2 = partnership.getTeamRating();
		return teamRating1 < teamRating2 ? 1 : teamRating1 == teamRating2 ? 0
				: -1;
	}

	public Bugger getBugger1() {
		return bugger1;
	}

	public Bugger getBugger2() {
		return bugger2;
	}

	public int getTeamRating() {
		return getBugger1().getRatingAsInt() + getBugger2().getRatingAsInt();
	}

	public void setBugger1(Bugger bugger) {
		bugger1 = bugger;
	}

	public void setBugger2(Bugger bugger) {
		bugger2 = bugger;
	}

	@Override
	public String toString() {
		return bugger1 + " " + bugger2;
	}

}
