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

public enum ChatType {
	/**
	 * Message sent when someone challenges you to a match.
	 */
	CHALLENGE,
	/**
	 * Channel tells. The source will be the person sending the tell. Will
	 * always have a channel.
	 */
	CHAN_TELL,
	/**
	 * Global c-shouts. Will always have a source.
	 */
	CSHOUT,
	/**
	 * Message sent when you are following a persons games. Source will be the
	 * person you are following.
	 */
	FOLLOWING,
	/**
	 * Used for messages sent from within Raptor. Error messages and
	 * informational messages.
	 */
	INTERNAL,
	/**
	 * Kibitzes pertaining to a game. Will always have a game id set and a
	 * source.
	 */
	KIBITZ,
	/**
	 * Used for a message containing a game moves list.
	 */
	MOVES,
	/**
	 * Message sent when you are no longer following a persons games.
	 */
	NOT_FOLLOWING,
	/**
	 * Used for messages sent to a connector.
	 */
	OUTBOUND,
	/**
	 * Tells from a bughouse partner. Source will be the partners name.
	 */
	PARTNER_TELL,
	/**
	 * Message pertaining to a bughouse partnership being created. Source will
	 * be the name of the partner.
	 */
	PARTNERSHIP_CREATED,
	/**
	 * Message pertaining to a bughouse partnership being destroyed.
	 */
	PARTNERSHIP_DESTROYED,
	/**
	 * Global shouts. Will always have a source.
	 */
	SHOUT,

	/**
	 * Used for direct tells and say. Source will be the person sending the
	 * tell.
	 */
	TELL,

	/**
	 * Used for the told message after a tell. Source will be the person told.
	 */
	TOLD,
	/**
	 * Used to identify types that don't match any of the others.
	 */
	UNKNOWN,

	/**
	 * Whispers pertaining to a game. Will always have a source and a game id.
	 */
	WHISPER
}
