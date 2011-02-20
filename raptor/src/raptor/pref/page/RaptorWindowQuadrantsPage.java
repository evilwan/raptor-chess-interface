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
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.fields.ListFieldEditor;
import raptor.swt.BrowserWindowItem;
import raptor.swt.chess.ChessBoardWindowItem;

public class RaptorWindowQuadrantsPage extends FieldEditorPreferencePage {

	protected String layoutPrefix;

	public RaptorWindowQuadrantsPage(String layoutName) {
		super(GRID);
		setTitle("Quadrants");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	protected String[][] buildQuadrantArray(Quadrant[] quadrants) {
		String[][] result = new String[quadrants.length][2];
		for (int i = 0; i < quadrants.length; i++) {
			result[i][0] = quadrants[i].name();
			result[i][1] = quadrants[i].name();
		}
		return result;
	}

	@Override
	protected void createFieldEditors() {
		Label textLabel = new Label(getFieldEditorParent(), SWT.WRAP);
		textLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 2, 1));
		textLabel
				.setText(WordUtils
						.wrap("\tRaptor uses a quadrant system to layout content. "
								+ "If a quadrant contains no items, the quadrant disappears and the "
								+ "remaining quadrants consume the space."
								+ "You may drag and drop items between quadrants by dragging "
								+ "the tab and dropping anywhere in another quadrant."
								+ "Double click on a tab to maximize a quadrant, and double "
								+ "click again to restore it. Right clicking on a tab brings up a "
								+ "list of options as well.", 70)
						+ "\n\t"
						+ WordUtils.wrap(
								"On this page you can customize the quadrant the following content "
										+ "is originally created in.", 70));

		Label label = new Label(getFieldEditorParent(), SWT.NONE);
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false,
				2, 1));
		label.setImage(Raptor.getInstance().getImage(
				Raptor.RESOURCES_DIR + "/images/quadrants.png"));

		ComboFieldEditor internalBrowserQuad = new ComboFieldEditor(
				PreferenceKeys.APP_BROWSER_QUADRANT, "Internal Web Browser:",
				buildQuadrantArray(BrowserWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent());
		addField(internalBrowserQuad);

		ComboFieldEditor pgnResuoltsPageQuad = new ComboFieldEditor(
				PreferenceKeys.APP_PGN_RESULTS_QUADRANT, "Pgn Game List:",
				buildQuadrantArray(ChessBoardWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent());
		addField(pgnResuoltsPageQuad);

		label = new Label(getFieldEditorParent(), SWT.NONE);
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false,
				2, 1));

		addField(new ListFieldEditor(PreferenceKeys.APP_CHESS_BOARD_QUADRANTS,
				"Chess board quadrants: (I,II,III,IV,V,VI,VII,VIII,IX)",
				getFieldEditorParent(), ',', 300) {

			@Override
			public boolean isTextValid(String text) {
				try {
					return Quadrant.valueOf(text) != null;
				} catch (Exception e) {
					return false;
				}
			}

		});
	}
}