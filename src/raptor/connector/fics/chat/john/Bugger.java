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
package raptor.connector.fics.chat.john;

/**
 * 
 * @author John Nahlen (johnthegreat)
 * @since Friday, September 25, 2009
 */
public class Bugger {
	private String rating;
	private char status;
	private String username;

	protected Bugger() {

	}

	/**
	 * @return The bugger's rating.
	 */
	public String getRating() {
		return rating;
	}

	/**
	 * @return The bugger's availability status.
	 */
	public char getStatus() {
		return status;
	}

	/**
	 * @return The bugger's username.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return If this bugger is currently (at the time of parsing) available
	 *         for match requests.
	 */
	public boolean isOpenForMatches() {
		return (status == ' ' || status == '.'); // available or idle
	}

	/**
	 * @return If this bugger is currently (at the time of parsing) playing a
	 *         game.
	 */
	public boolean isPlaying() {
		return (status == '^'); // playing
	}

	protected void setRating(String rating) {
		this.rating = rating;
	}

	protected void setStatus(char status) {
		this.status = status;
	}

	protected void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return <code>getUsername();</code>
	 */
	@Override
	public String toString() {
		return getUsername();
	}

}