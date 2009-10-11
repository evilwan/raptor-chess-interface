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
package raptor.pref;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Quadrant;
import raptor.Raptor;

public class RaptorWindowLayoutPage extends FieldEditorPreferencePage {
	protected static final String[][] Quadrants = {
			{ Quadrant.I.name(), Quadrant.I.name() },
			{ Quadrant.II.name(), Quadrant.II.name() },
			{ Quadrant.III.name(), Quadrant.III.name() },
			{ Quadrant.IV.name(), Quadrant.IV.name() },
			{ Quadrant.V.name(), Quadrant.V.name() },
			{ Quadrant.VI.name(), Quadrant.VI.name() },
			{ Quadrant.VII.name(), Quadrant.VII.name() },
			{ Quadrant.VIII.name(), Quadrant.VIII.name() } };
	protected String layoutPrefix;

	public RaptorWindowLayoutPage(String layoutName, String layoutPrefix) {
		super(GRID);
		setTitle("Layout " + layoutName);
		setPreferenceStore(Raptor.getInstance().getPreferences());
		this.layoutPrefix = layoutPrefix;
	}

	@Override
	protected void createFieldEditors() {

		ComboFieldEditor mainQuad = new ComboFieldEditor(layoutPrefix
				+ "-main-quadrant", "Main Chat Console Quadrant:", Quadrants,
				getFieldEditorParent());
		addField(mainQuad);

		ComboFieldEditor channelQuad = new ComboFieldEditor(layoutPrefix
				+ "-channel-quadrant", "Channel Chat Console Quadrant:",
				Quadrants, getFieldEditorParent());
		addField(channelQuad);

		ComboFieldEditor personQuad = new ComboFieldEditor(layoutPrefix
				+ "-person-quadrant", "Person Chat Console Quadrant:",
				Quadrants, getFieldEditorParent());
		addField(personQuad);

		ComboFieldEditor regexQuad = new ComboFieldEditor(layoutPrefix
				+ "-regex-quadrant",
				"Regular Expression Chat Console Quadrant:", Quadrants,
				getFieldEditorParent());
		addField(regexQuad);

		ComboFieldEditor ptellQuad = new ComboFieldEditor(layoutPrefix
				+ "-partner-quadrant", "Partner Tells Chat Console Quadrant:",
				Quadrants, getFieldEditorParent());
		addField(ptellQuad);

		ComboFieldEditor gameQuad = new ComboFieldEditor(layoutPrefix
				+ "-game-quadrant",
				"Chess Game (includes Bughouse primary Board) Quadrant:",
				Quadrants, getFieldEditorParent());
		addField(gameQuad);

		ComboFieldEditor bughosueQuad = new ComboFieldEditor(layoutPrefix
				+ "-bughosue-game-2-quadrant",
				"Bughosue Game Secondary Board Quadrant:", Quadrants,
				getFieldEditorParent());
		addField(bughosueQuad);

		ComboFieldEditor internalBrowserQuad = new ComboFieldEditor(
				layoutPrefix + "-browser-quadrant",
				"Internal Web Browser Quadrant:", Quadrants,
				getFieldEditorParent());
		addField(internalBrowserQuad);

		ComboFieldEditor bughouseArena = new ComboFieldEditor(layoutPrefix
				+ "-bug-arena-quadrant", "Bughouse Arena Quadrant:", Quadrants,
				getFieldEditorParent());
		addField(bughouseArena);

		ComboFieldEditor seekGraphQuad = new ComboFieldEditor(layoutPrefix
				+ "-seek-graph-quadrant", "Seek Graph Quadrant:", Quadrants,
				getFieldEditorParent());
		addField(seekGraphQuad);

		ComboFieldEditor bugButtonQuad = new ComboFieldEditor(layoutPrefix
				+ "-seek-graph-quadrant",
				"Bughosue Communication Buttons Quadrant:", Quadrants,
				getFieldEditorParent());
		addField(bugButtonQuad);
	}
}