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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;

public class ChessBoardFontsPage extends FieldEditorPreferencePage {
	public ChessBoardFontsPage() {
		super(GRID);
		setTitle("Fonts");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		FontFieldEditor coordinatesFont = new FontFieldEditor(
				PreferenceKeys.BOARD_COORDINATES_FONT, "Coordinates Font:",
				getFieldEditorParent());
		addField(coordinatesFont);

		FontFieldEditor lagFont = new FontFieldEditor(
				PreferenceKeys.BOARD_LAG_FONT, "Total Lag Font:",
				getFieldEditorParent());
		addField(lagFont);

		FontFieldEditor playerNameFont = new FontFieldEditor(
				PreferenceKeys.BOARD_PLAYER_NAME_FONT,
				"Player Name/Rating Font:", getFieldEditorParent());
		addField(playerNameFont);

		FontFieldEditor pieceJailFont = new FontFieldEditor(
				PreferenceKeys.BOARD_PIECE_JAIL_FONT,
				"Piece Jail/Drop Square Number Font:", getFieldEditorParent());
		addField(pieceJailFont);

		FontFieldEditor openingDescriptionFont = new FontFieldEditor(
				PreferenceKeys.BOARD_OPENING_DESC_FONT, "Opening Font:",
				getFieldEditorParent());
		addField(openingDescriptionFont);

		FontFieldEditor premovesFont = new FontFieldEditor(
				PreferenceKeys.BOARD_OPENING_DESC_FONT, "Premoves Font:",
				getFieldEditorParent());
		addField(premovesFont);

		FontFieldEditor gameDescriptionFont = new FontFieldEditor(
				PreferenceKeys.BOARD_GAME_DESCRIPTION_FONT, "Game Type Font:",
				getFieldEditorParent());
		addField(gameDescriptionFont);

		FontFieldEditor lastMoveFont = new FontFieldEditor(
				PreferenceKeys.BOARD_STATUS_FONT, "Last Move Font:",
				getFieldEditorParent());
		addField(lastMoveFont);
	}
}