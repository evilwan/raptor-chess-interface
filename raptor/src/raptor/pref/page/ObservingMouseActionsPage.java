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
package raptor.pref.page;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Label;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.swt.chess.controller.ObservingMouseAction;

public class ObservingMouseActionsPage extends FieldEditorPreferencePage {

	public static final String[][] OPTIONS = {
			{ "None", ObservingMouseAction.None.toString() },
			{ "Make Primary Game (For kib and whisper)",
					ObservingMouseAction.MakePrimaryGame.toString() },
			{ "Match Winner", ObservingMouseAction.MatchWinner.toString() },
			{ "Open Game Tell Tab",
					ObservingMouseAction.AddGameChatTab.toString() } };

	public ObservingMouseActionsPage() {
		// Use the "flat" layout
		super(GRID);
		setTitle("Observing");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new ComboFieldEditor(PreferenceKeys.OBSERVING_CONTROLLER
				+ PreferenceKeys.LEFT_MOUSE_BUTTON_ACTION,
				"Left Mouse Button Action:", OPTIONS, getFieldEditorParent()));

		addField(new ComboFieldEditor(PreferenceKeys.OBSERVING_CONTROLLER
				+ PreferenceKeys.RIGHT_MOUSE_BUTTON_ACTION,
				"Right Mouse Button Action:", OPTIONS, getFieldEditorParent()));

		addField(new ComboFieldEditor(PreferenceKeys.OBSERVING_CONTROLLER
				+ PreferenceKeys.MIDDLE_MOUSE_BUTTON_ACTION,
				"Middle Mouse Button Action:", OPTIONS, getFieldEditorParent()));

		addField(new ComboFieldEditor(PreferenceKeys.OBSERVING_CONTROLLER
				+ PreferenceKeys.MISC1_MOUSE_BUTTON_ACTION,
				"Misc1 Mouse Button Action:", OPTIONS, getFieldEditorParent()));

		addField(new ComboFieldEditor(PreferenceKeys.OBSERVING_CONTROLLER
				+ PreferenceKeys.MISC2_MOUSE_BUTTON_ACTION,
				"Misc2 Mouse Button Action:", OPTIONS, getFieldEditorParent()));

		final Label label = new Label(getFieldEditorParent(), SWT.LEFT);
		label.setText("\n      Click here to determine mouse button.      \n ");
		label.setBackground(getShell().getDisplay().getSystemColor(
				SWT.COLOR_BLUE));
		label.setForeground(getShell().getDisplay().getSystemColor(
				SWT.COLOR_WHITE));
		label.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {
				if (e.button == 1) {
					label.setText("Left Double Click");
				}
			}

			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					label.setText("Left Click");
				} else if (e.button == 2) {
					label.setText("Middle Click");
				} else if (e.button == 3) {
					label.setText("Right Click");
				} else if (e.button == 4) {
					label.setText("Misc1 Click");
				} else if (e.button == 5) {
					label.setText("Misc2 Click");
				}
			}

			public void mouseUp(MouseEvent e) {
			}
		});
	}
}