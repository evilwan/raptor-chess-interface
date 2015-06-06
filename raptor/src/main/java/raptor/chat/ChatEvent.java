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

/**
 * A class defining an inbound message received from a server.
 */
public class ChatEvent {
	protected String channel;
	protected String gameId;
	protected String message;
	protected String source;
	protected long time;
	protected ChatType type;
	protected boolean hasSoundBeenHandled;

	public ChatEvent() {
		time = System.currentTimeMillis();
	}

	public ChatEvent(String source, ChatType type, String message) {
		this();
		this.source = source;
		this.type = type;
		this.message = message;
	}

	public ChatEvent(String source, ChatType type, String message, String gameId) {
		this(source, type, message);
		this.gameId = gameId;
	}

	/**
	 * If the chat event represents a channel tell this contains the channel
	 * number. Otherwise it is null.
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * If the chat event represents a tell about a game, i.e.
	 * kibitz,whisper,etc, this is the game id. Otherwise it is null.
	 * 
	 * @return
	 */
	public String getGameId() {
		return gameId;
	}

	/**
	 * Returns the entire server message.
	 * 
	 * @return Entire message involved in this ChatEvent.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * This is the source of the ChatEvent. If it is a tell event, it is the
	 * user sending the tell. If it is a channel tell event, it is the user
	 * sending the tell to the channel. If its a shout or c-shout, its the
	 * person shouting. If its a kibitz or whisper, its the person kibitzing or
	 * whispering. If its a say, its the person sending the say.
	 * 
	 * @return The user name of the person involved in this ChatEvent.
	 */
	public String getSource() {
		return source;
	}

	/**
	 * The time in EPOC the chat event was created.
	 * 
	 * @return ChatEvent creation timestamp in EPOC millisconds.
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @return The type of ChatEvent.
	 * @see ChatTypes.
	 */
	public ChatType getType() {
		return type;
	}

	/**
	 * Returns true if this event has been handled.
	 */
	public boolean hasSoundBeenHandled() {
		return hasSoundBeenHandled;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	/**
	 * Sets the flag denoting this event has been handled.
	 */
	public void setHasSoundBeenHandled(boolean hasSoundBeenHandled) {
		this.hasSoundBeenHandled = hasSoundBeenHandled;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @param time
	 *            The time that this chat event occurred.
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * @param type
	 *            The type of ChatEvent this is.
	 * @see ChatTypes.
	 */
	public void setType(ChatType type) {
		this.type = type;
	}

	/**
	 * Dumps information about this ChatEvent to a string.
	 */
	@Override
	public String toString() {
		return "ChatEvent: source=" + source + " type=" + type.name()
				+ " gameId=" + gameId + " channel=" + channel
				+ " message='" + message + "'";
	}

}
