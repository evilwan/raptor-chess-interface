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
package raptor.pref.page;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;
import raptor.chat.ChatType;
import raptor.pref.PreferenceKeys;

public class ChatConsoleMessageColorsPage extends FieldEditorPreferencePage {
	public ChatConsoleMessageColorsPage() {
		super(GRID);
		setTitle("Message Colors");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		ColorFieldEditor defaultMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_INPUT_DEFAULT_TEXT_COLOR, "Default Color:",
				getFieldEditorParent());
		addField(defaultMessages);

		ColorFieldEditor abortRequest = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.ABORT_REQUEST + "-color", "Abort Request:",
				getFieldEditorParent());
		addField(abortRequest);

		ColorFieldEditor ptellMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.PARTNER_TELL + "-color",
				"Bughouse Partner Tell:", getFieldEditorParent());
		addField(ptellMessages);

		addField(new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.BUGWHO_ALL + "-color", "Bug Who:",
				getFieldEditorParent()));

		ColorFieldEditor challengeMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.CHALLENGE + "-color", "Challenge:",
				getFieldEditorParent());
		addField(challengeMessages);

		ColorFieldEditor cshoutMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.CSHOUT + "-color", "C-Shout:",
				getFieldEditorParent());
		addField(cshoutMessages);

		ColorFieldEditor drawRequest = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.DRAW_REQUEST + "-color", "Draw Request:",
				getFieldEditorParent());
		addField(drawRequest);

		addField(new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.FINGER + "-color", "Finger Notes:",
				getFieldEditorParent()));

		addField(new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.GAMES + "-color", "Games:",
				getFieldEditorParent()));

		addField(new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.HISTORY + "-color", "History:",
				getFieldEditorParent()));

		ColorFieldEditor kibitzMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.KIBITZ + "-color", "Kibitz:",
				getFieldEditorParent());
		addField(kibitzMessages);

		addField(new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.NOTIFICATION_ARRIVAL + "-color",
				"Notification:", getFieldEditorParent()));

		addField(new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.QTELL + "-color", "Qtell:",
				getFieldEditorParent()));

		ColorFieldEditor gameEndStatistics = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.PLAYING_STATISTICS + "-color",
				"Raptor Game Statistics Color:", getFieldEditorParent());
		addField(gameEndStatistics);

		ColorFieldEditor internalMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.INTERNAL + "-color",
				"Raptor Message Color:", getFieldEditorParent());
		addField(internalMessages);

		ColorFieldEditor outboundMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.OUTBOUND + "-color", "Sent Messages:",
				getFieldEditorParent());
		addField(outboundMessages);

		ColorFieldEditor shoutMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.SHOUT + "-color", "Shout:",
				getFieldEditorParent());
		addField(shoutMessages);

		ColorFieldEditor personTellMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.TELL + "-color", "Tell:",
				getFieldEditorParent());
		addField(personTellMessages);

		ColorFieldEditor whisperMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.WHISPER + "-color", "Whisper:",
				getFieldEditorParent());
		addField(whisperMessages);
	}
}