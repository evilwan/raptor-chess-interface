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
package raptor.script;

import raptor.connector.MessageCallback;

/**
 * This interface contains the core Raptor features you have access to in
 * scripting. If you would like new ones added you can make enhancement requests
 * on the request page or post in the Google groups page.
 */
public interface ScriptContext {

	/**
	 * Displays an alert message to the user.
	 */
	public void alert(String message);

	/**
	 * Appends the specified message to the specified file name.
	 * 
	 * @param fileName
	 *            The path to the file.
	 * @param message
	 *            The message to append.
	 */
	public void appendToFile(String fileName, String message);

	/**
	 * Returns the current ping time in milliseconds.
	 */
	public long getPingMillis();

	/**
	 * Returns the current user you are following, if you are not following
	 * anyone null is returned.
	 */
	public String getUserFollowing();

	/**
	 * Returns the number of seconds you have been idle, i.e. have not sent any
	 * messages.
	 */
	public int getUserIdleSeconds();

	/**
	 * Returns the logged in user name.
	 */
	public String getUserName();

	/**
	 * Retrieves a stored value.
	 * 
	 * @param key
	 *            The key to retrieve.
	 * @return May return null if there is no value stored.
	 */
	public Object getValue(String key);

	/**
	 * Launches the process with the specified arguments. This can be used to
	 * tie external scripts to Raptor.
	 * 
	 * @param commandAndArgs
	 *            can be just a command name or you can pass in the command and
	 *            multiple arguments i.e.
	 *            launchProcess("say","this is the text to say").
	 */
	public void launchProcess(String... commandAndArgs);

	/**
	 * Opens a channel tab for the specified channel if one is not already
	 * opened.
	 */
	public void openChannelTab(String channel);

	/**
	 * Opens a game chat tab for the specified game id.
	 */
	public void openGameChatTab(String gameId);

	/**
	 * Opens a partner, ptell tab, if one is not already opened.
	 */
	public void openPartnerTab();

	/**
	 * Opens a person tab if one is not already open for the specified person.
	 */
	public void openPersonTab(String person);

	/**
	 * Opens a regex tab if one is not already open.
	 */
	public void openRegularExpressionTab(String regularExpression);

	/**
	 * Opens the specified url.
	 */
	public void openUrl(String url);

	/**
	 * Plays the specified sound. Supports all formats.
	 * 
	 * @param pathToSound
	 *            The fully qualified path to the sound.
	 * @since v.91
	 */
	public void play(String pathToSound);

	/**
	 * Plays the specified bughouse sound.
	 * 
	 * @param soundName
	 *            The name of the sound in the resources/sound/bughouse
	 *            directory without the .wav. Spaces should be included if they
	 *            are in the file name.
	 */
	public void playBughouseSound(String soundName);

	/**
	 * Plays the specified sound.
	 * 
	 * @param soundName
	 *            The name of the sound in the resources/sound directory without
	 *            the .wav.
	 */
	public void playSound(String soundName);

	/**
	 * Prompts the user for text with the specified message. The text the user
	 * types in is returned.
	 * 
	 * @param message
	 *            The message used to prompt the user.
	 * @return
	 */
	public String prompt(String message);

	/**
	 * Registers a callback which will be invoked when the next message arrives
	 * that matches the specified regular expression.
	 * 
	 * @param regularExpression
	 *            The regular expression to match.
	 * @param callback
	 *            The callback which gets invoked. MessageCallback must
	 *            implement the method
	 * 
	 *            <pre>
	 * public void matchReceived(ChatEvent event);
	 * </pre>
	 */
	public void registerMessageCallback(String regularExpression,
			MessageCallback callback);

	/**
	 * Sends the specified message to the connector.
	 * 
	 * @param message
	 *            The message to send.
	 */
	public void send(String message);

	/**
	 * Sends the specified message to the connector and hides the message from
	 * the user.
	 * 
	 * @param message
	 *            The message to send.
	 */
	public void sendHidden(String message);

	/**
	 * Displays a message to the user in the chat console. The message will not
	 * be sent to the connector.
	 * 
	 * @param message
	 *            The message to display to the user.
	 */
	public void sendToConsole(String message);

	/**
	 * Speaks the specified message. Requires that you have speech setup. OS X
	 * users have speech enabled automatically; however, other users must
	 * configure speech in the Speech preferences.
	 */
	public void speak(String message);

	/**
	 * Stores a value which can be obtained later in another script.
	 * 
	 * @param key
	 *            The key of the value to store.
	 * @param value
	 *            The value to store.
	 */
	public void storeValue(String key, Object value);

	/**
	 * MIME encodes the passed in string.
	 * 
	 * @param stringToEncode
	 *            The string to encode.
	 * @return The encoded string.
	 */
	public String urlEncode(String stringToEncode);

	/**
	 * Writes the specified message to the specified file. The contents of the
	 * file will be replaced with the specified message.
	 * 
	 * @param fileName
	 *            The name of the file.
	 * @param message
	 *            The message.
	 */
	public void writeToFile(String fileName, String message);

}
