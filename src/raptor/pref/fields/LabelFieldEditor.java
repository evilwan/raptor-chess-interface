package raptor.pref.fields;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A field editor that just displays a label and does nothing else. Its not
 * linked to preferences in any way..
 */
public class LabelFieldEditor extends FieldEditor {

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
	public LabelFieldEditor(String name, String labelText, Composite parent) {
		init(name, labelText);
		this.labelText = labelText;
		createControl(parent);
	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	protected void adjustForNumColumns(int numColumns) {
		((GridData) label.getLayoutData()).horizontalSpan = numColumns - 1;
	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		label = new Label(parent, SWT.NONE);
		label.setText(labelText);
	}

	/*
	 * (non-) Method declared on FieldEditor. Loads the value from the
	 * preference store and sets it to the check box.
	 */
	protected void doLoad() {
	}

	/*
	 * (non-) Method declared on FieldEditor. Loads the default value from the
	 * preference store and sets it to the check box.
	 */
	protected void doLoadDefault() {

	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	protected void doStore() {

	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	public int getNumberOfControls() {
		return 1;
	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	public void setFocus() {
	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	public void setLabelText(String text) {
		super.setLabelText(text);
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

	/*
	 * @see FieldEditor.setEnabled
	 */
	public void setEnabled(boolean enabled, Composite parent) {
	}

}
