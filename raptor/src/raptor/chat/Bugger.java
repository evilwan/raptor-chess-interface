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
package raptor.chat;

import org.apache.commons.lang.StringUtils;

/**
 * This code was adapted from some code that johnthegreat wrote for Raptor.
 */
public class Bugger {

	public static enum BuggerStatus {
		Available, Idle, Closed, Playing, Simul, Examining, InTourney
	}

	private String rating;
	private BuggerStatus status;
	private String name;

	public Bugger() {

	}

	/**
	 * @return The bugger's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The bugger's rating.
	 */
	public String getRating() {
		return rating;
	}

	public int getRatingAsInt() {
		int result = 0;
		try {
			result = Integer.parseInt(StringUtils.replaceChars(getRating(),
					"EP", ""));
		} catch (NumberFormatException nfe) {
		}
		return result;
	}

	/**
	 * @return The bugger's availability status.
	 */
	public BuggerStatus getStatus() {
		return status;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public void setStatus(BuggerStatus status) {
		this.status = status;
	}

	/**
	 * @return <code>getUsername();</code>
	 */
	@Override
	public String toString() {
		return getName() + "(" + rating + ")";
	}

}