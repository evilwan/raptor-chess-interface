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

import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.fields.ListFieldEditor;

public class ConnectorMessageBlockPage extends FieldEditorPreferencePage {
	protected String connectorShortName;

	public ConnectorMessageBlockPage(String connectorShortName) {
		super(GRID);
		setTitle("Message Filters");
		setPreferenceStore(Raptor.getInstance().getPreferences());
		this.connectorShortName = connectorShortName;
	}

	@Override
	protected void createFieldEditors() {
		Label textLabel = new Label(getFieldEditorParent(), SWT.WRAP);
		textLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 2, 1));
		textLabel
				.setText(WordUtils
						.wrap(
								"\t On this page you can add regular expressions that match messages to supress. "
										+ "This is mostly used to filter out some of the superfolous messages fics sends. "
										+ "Currently the regular expressions used can not contain a comma. Changes to this list will take effect on the next reconnect.",
								70)
						+ "\n\t See Help->Raptor Help-> Regular Expressions for help with regular expressions.");

		Label label = new Label(getFieldEditorParent(), SWT.NONE);
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false,
				2, 1));

		addField(new ListFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.REGULAR_EXPRESSIONS_TO_BLOCK,
				"Regular Expressions Matching Messages To Supress:",
				getFieldEditorParent(), ',', 300));
	}
}
