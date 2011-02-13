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
package raptor.pref.page;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.fields.LabelButtonFieldEditor;

public class ChatConsoleBehaviorPage extends FieldEditorPreferencePage {
	LabelButtonFieldEditor labelButtonFieldEditor;

	public ChatConsoleBehaviorPage() {
		super(GRID);
		setTitle("Behavior");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		StringFieldEditor timestampFormat = new StringFieldEditor(
				PreferenceKeys.CHAT_TIMESTAMP_CONSOLE_FORMAT,
				"Message Timestamp Format (Java SimpleDateFormat):",
				getFieldEditorParent());
		addField(timestampFormat);

		BooleanFieldEditor addTimestamps = new BooleanFieldEditor(
				PreferenceKeys.CHAT_TIMESTAMP_CONSOLE,
				"Add Timestamps To Messages", getFieldEditorParent());
		addField(addTimestamps);

		addField(new BooleanFieldEditor(
				PreferenceKeys.CHAT_REMOVE_SUB_TAB_MESSAGES_FROM_MAIN_TAB,
				"Filter messages handled by other tabs from the main console tab",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.CHAT_OPEN_CHANNEL_TAB_ON_CHANNEL_TELLS,
				"Open channel tabs on new channel tells.",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.CHAT_OPEN_PARTNER_TAB_ON_PTELLS,
				"Open partner tab on new partner tells.",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.CHAT_OPEN_PERSON_TAB_ON_PERSON_TELLS,
				"Open person tabs on new person tells.", getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.CHAT_IS_PLAYING_CHAT_ON_PTELL,
				"Play 'chat' sound on all partner tells.",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.CHAT_IS_PLAYING_CHAT_ON_PERSON_TELL,
				"Play 'chat' sound on all person tells.",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.CHAT_PLAY_NOTIFICATION_SOUND_ON_ARRIVALS,
				"Play 'notificationArrived' sound on all notification arrivals.",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.CHAT_PLAY_NOTIFICATION_SOUND_ON_DEPARTURES,
				"Play 'notificationDeparted' sound on all notification departures.",
				getFieldEditorParent()));

		BooleanFieldEditor smartScroll = new BooleanFieldEditor(
				PreferenceKeys.CHAT_IS_SMART_SCROLL_ENABLED,
				"Smart Scroll (Toggles auto scroll based on the "
						+ "vertical scrolllbar position)",
				getFieldEditorParent());
		addField(smartScroll);

		addField(new BooleanFieldEditor(
				PreferenceKeys.CHAT_COMMAND_LINE_SPELL_CHECK,
				"Spell Check Enabled (Command Line)", getFieldEditorParent()));

		addField(new BooleanFieldEditor(PreferenceKeys.CHAT_UNDERLINE_COMMANDS,
				"Underline link commands (history,journal,bugwho,etc)",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.CHAT_UNDERLINE_QUOTED_TEXT,
				"Underline quoted text", getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.CHAT_UNDERLINE_SINGLE_QUOTES,
				"Underline single quoted text", getFieldEditorParent()));

		addField(new BooleanFieldEditor(PreferenceKeys.CHAT_UNDERLINE_URLS,
				"Underline urls", getFieldEditorParent()));
	}
}