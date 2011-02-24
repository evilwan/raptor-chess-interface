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

import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;

import raptor.Raptor;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.pref.fields.LabelButtonFieldEditor;

public class ChatConsolePage extends FieldEditorPreferencePage {
	
	protected static L10n local = L10n.getInstance();

	public static final String[][] CONSOLE_CHARS = {
			{ local.getString("char100t"), "100000" },
			{ local.getString("char250t"), "250000" },
			{ local.getString("char500t"), "500000" },
			{ local.getString("char1000t"), "1000000" } };

	LabelButtonFieldEditor labelButtonFieldEditor;

	public ChatConsolePage() {
		super(GRID);
		setTitle(local.getString("chatCons"));
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		ComboFieldEditor consoleChars = new ComboFieldEditor(
				PreferenceKeys.CHAT_MAX_CONSOLE_CHARS,
				local.getString("chatConsP1"), CONSOLE_CHARS,
				getFieldEditorParent());
		addField(consoleChars);

		ColorFieldEditor inputTextBackground = new ColorFieldEditor(
				PreferenceKeys.CHAT_INPUT_BACKGROUND_COLOR,
				local.getString("chatConsP2"), getFieldEditorParent());
		addField(inputTextBackground);

		ColorFieldEditor consoleBackground = new ColorFieldEditor(
				PreferenceKeys.CHAT_CONSOLE_BACKGROUND_COLOR,
				local.getString("chatConsP3"), getFieldEditorParent());
		addField(consoleBackground);

		FontFieldEditor inputFont = new FontFieldEditor(
				PreferenceKeys.CHAT_INPUT_FONT, local.getString("chatConsP4"),
				getFieldEditorParent());
		addField(inputFont);

		ColorFieldEditor outputTextBackground = new ColorFieldEditor(
				PreferenceKeys.CHAT_OUTPUT_BACKGROUND_COLOR,
				local.getString("chatConsP5"), getFieldEditorParent());
		addField(outputTextBackground);

		ColorFieldEditor promptColor = new ColorFieldEditor(
				PreferenceKeys.CHAT_PROMPT_COLOR,
				local.getString("chatConsP6"), getFieldEditorParent());
		addField(promptColor);

		ColorFieldEditor outputTextForeground = new ColorFieldEditor(
				PreferenceKeys.CHAT_OUTPUT_TEXT_COLOR,
				local.getString("chatConsP7"), getFieldEditorParent());
		addField(outputTextForeground);

		addField(new FontFieldEditor(
				PreferenceKeys.CHAT_OUTPUT_FONT, local.getString("chatConsP8"),
				getFieldEditorParent()));

		ColorFieldEditor linkTextColor = new ColorFieldEditor(
				PreferenceKeys.CHAT_LINK_UNDERLINE_COLOR, local.getString("chatConsP9"),
				getFieldEditorParent());
		addField(linkTextColor);

		ColorFieldEditor quoteUnderlineColor = new ColorFieldEditor(
				PreferenceKeys.CHAT_QUOTE_UNDERLINE_COLOR,
				local.getString("chatConsP10"), getFieldEditorParent());
		addField(quoteUnderlineColor);

		Label warningLabel = new Label(getFieldEditorParent(), SWT.NONE);
		warningLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));
		warningLabel
				.setText(WordUtils
						.wrap(local.getString("chatConsP11"), 70));
	}
}