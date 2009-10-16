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
package raptor.pref.fields;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A field editor that is not linked to the preferences. It contains a button
 * you can set the SelectionListener for and execute an action when its pressed.
 */
public class LabelButtonFieldEditor extends FieldEditor {

	/**
	 * The button control, or <code>null</code> if none.
	 */
	private Button button = null;
	private SelectionListener buttonSelectionListener;

	private String buttonText;
	private Label label = null;
	private String labelText;

	/**
	 * Creates a label button field editor in the given style.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param style
	 *            the style, either <code>DEFAULT</code> or
	 *            <code>SEPARATE_LABEL</code>
	 * @param parent
	 *            the parent of the field editor's control
	 * @see #DEFAULT
	 * @see #SEPARATE_LABEL
	 */
	public LabelButtonFieldEditor(String name, String labelText,
			Composite parent, String buttonText,
			SelectionListener buttonSelectionListener) {
		init(name, labelText);
		this.labelText = labelText;
		this.buttonText = buttonText;
		this.buttonSelectionListener = buttonSelectionListener;
		createControl(parent);
	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	@Override
	public int getNumberOfControls() {
		return 2;
	}

	/*
	 * @see FieldEditor.setEnabled
	 */
	@Override
	public void setEnabled(boolean enabled, Composite parent) {
	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	@Override
	public void setFocus() {
		if (button != null) {
			button.setFocus();
		}
	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	@Override
	public void setLabelText(String text) {
		super.setLabelText(text);
		label.setText(text);
	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	@Override
	protected void adjustForNumColumns(int numColumns) {
		((GridData) label.getLayoutData()).horizontalSpan = numColumns - 1;
	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		label.setText(labelText);

		button = new Button(parent, SWT.PUSH);
		button.setText(buttonText);
		button.addSelectionListener(buttonSelectionListener);
	}

	/*
	 * (non-) Method declared on FieldEditor. Loads the value from the
	 * preference store and sets it to the check box.
	 */
	@Override
	protected void doLoad() {
	}

	/*
	 * (non-) Method declared on FieldEditor. Loads the default value from the
	 * preference store and sets it to the check box.
	 */
	@Override
	protected void doLoadDefault() {

	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	@Override
	protected void doStore() {

	}

	/**
	 * Informs this field editor's listener, if it has one, about a change to
	 * the value (<code>VALUE</code> property) provided that the old and new
	 * values are different.
	 * 
	 * @param oldValue
	 *            the old value
	 * @param newValue
	 *            the new value
	 */
	protected void valueChanged(boolean oldValue, boolean newValue) {
	}

}
