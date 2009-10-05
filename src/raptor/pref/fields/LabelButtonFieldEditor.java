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
	private Label label = null;

	private String labelText;
	private String buttonText;
	private SelectionListener buttonSelectionListener;

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
	protected void adjustForNumColumns(int numColumns) {
		((GridData) label.getLayoutData()).horizontalSpan = numColumns - 1;
	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		label = new Label(parent, SWT.NONE);
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
