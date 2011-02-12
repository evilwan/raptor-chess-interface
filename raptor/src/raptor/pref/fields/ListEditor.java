/*******************************************************************************
 * Copyright (c) 0, 6 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package raptor.pref.fields;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import raptor.international.L10n;
import raptor.swt.InputDialog;

/**
 * A clone of the SWT list editor. This version has a height hint you can set.
 * Useful to keep lists from growing far to large.
 */
public abstract class ListEditor extends FieldEditor {

	/**
	 * The list widget; <code>null</code> if none (before creation or after
	 * disposal).
	 */
	private List list;

	/**
	 * The button box containing the Add, Remove, Up, and Down buttons;
	 * <code>null</code> if none (before creation or after disposal).
	 */
	private Composite buttonBox;

	/**
	 * The Add button.
	 */
	private Button addButton;

	/**
	 * The Remove button.
	 */
	private Button removeButton;

	/**
	 * The Up button.
	 */
	private Button upButton;

	/**
	 * The edit button.
	 */
	private Button editButton;

	/**
	 * The Down button.
	 */
	private Button downButton;

	/**
	 * The selection listener.
	 */
	private SelectionListener selectionListener;

	private int heightHint = SWT.DEFAULT;

	/**
	 * Creates a new list field editor
	 */
	protected ListEditor() {
	}

	/**
	 * Creates a list field editor.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 */
	protected ListEditor(String name, String labelText, Composite parent) {
		init(name, labelText);
		createControl(parent);
	}

	/**
	 * Creates a list field editor.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 */
	protected ListEditor(String name, String labelText, Composite parent,
			int heightHint) {
		this.heightHint = heightHint;
		init(name, labelText);
		createControl(parent);
	}

	/**
	 * Creates a selection listener.
	 */
	public void createSelectionListener() {
		selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Widget widget = event.widget;
				if (widget == addButton) {
					addPressed();
				} else if (widget == editButton) {
					editPressed();
				} else if (widget == removeButton) {
					removePressed();
				} else if (widget == upButton) {
					upPressed();
				} else if (widget == downButton) {
					downPressed();
				} else if (widget == list) {
					selectionChanged();
				}
			}
		};
	}

	/**
	 * Returns this field editor's button box containing the Add, Remove, Up,
	 * and Down button.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the button box
	 */
	public Composite getButtonBoxControl(Composite parent) {
		if (buttonBox == null) {
			buttonBox = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			buttonBox.setLayout(layout);
			createButtons(buttonBox);
			buttonBox.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					addButton = null;
					removeButton = null;
					editButton = null;
					upButton = null;
					downButton = null;
					buttonBox = null;
				}
			});

		} else {
			checkParent(buttonBox, parent);
		}

		selectionChanged();
		return buttonBox;
	}

	public int getHeightHint() {
		return heightHint;
	}

	/**
	 * Returns this field editor's list control.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the list control
	 */
	public List getListControl(Composite parent) {
		if (list == null) {
			list = new List(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL
					| SWT.H_SCROLL);
			list.setFont(parent.getFont());
			list.addSelectionListener(getSelectionListener());
			list.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					list = null;
				}
			});
		} else {
			checkParent(list, parent);
		}
		return list;
	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	@Override
	public int getNumberOfControls() {
		return 2;
	}

	/**
	 * Prompts a user for the answer to a question. The user enters text. The
	 * text the user entered is returned.
	 */
	public String promptForText(final String question) {
		InputDialog dialog = new InputDialog(getShell(), L10n.getInstance().getString("entText"), question,true);
		return dialog.open();
	}

	/**
	 * Prompts a user for the answer to a question. The user enters text. The
	 * text the user entered is returned.
	 * 
	 * @answer the initial text to place in the users answer.
	 */
	public String promptForText(final String question, String answer) {
		InputDialog dialog = new InputDialog(getShell(), L10n.getInstance().getString("entText"), question,true);
		if (answer != null) {
			dialog.setInput(answer);
		}
		return dialog.open();
	}

	/*
	 * @see FieldEditor.setEnabled(boolean,Composite).
	 */
	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		getListControl(parent).setEnabled(enabled);
		addButton.setEnabled(enabled);
		editButton.setEnabled(enabled);
		removeButton.setEnabled(enabled);
		upButton.setEnabled(enabled);
		downButton.setEnabled(enabled);
	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	@Override
	public void setFocus() {
		if (list != null) {
			list.setFocus();
		}
	}

	public void setHeightHint(int heightHint) {
		this.heightHint = heightHint;
	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	@Override
	protected void adjustForNumColumns(int numColumns) {
		Control control = getLabelControl();
		((GridData) control.getLayoutData()).horizontalSpan = numColumns;
		((GridData) list.getLayoutData()).horizontalSpan = numColumns - 1;
	}

	/**
	 * Combines the given list of items into a single string. This method is the
	 * converse of <code>parseString</code>.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @param items
	 *            the list of items
	 * @return the combined string
	 * @see #parseString
	 */
	protected abstract String createList(String[] items);

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		control.setLayoutData(gd);

		list = getListControl(parent);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = heightHint;
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalSpan = numColumns - 1;
		gd.grabExcessHorizontalSpace = true;
		list.setLayoutData(gd);

		buttonBox = getButtonBoxControl(parent);
		gd = new GridData();
		gd.verticalAlignment = GridData.BEGINNING;
		buttonBox.setLayoutData(gd);
	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	@Override
	protected void doLoad() {
		if (list != null) {
			String s = getPreferenceStore().getString(getPreferenceName());
			String[] array = parseString(s);
			for (int i = 0; i < array.length; i++) {
				list.add(array[i]);
			}
		}
	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	@Override
	protected void doLoadDefault() {
		if (list != null) {
			list.removeAll();
			String s = getPreferenceStore().getDefaultString(
					getPreferenceName());
			String[] array = parseString(s);
			for (int i = 0; i < array.length; i++) {
				list.add(array[i]);
			}
		}
	}

	/*
	 * (non-) Method declared on FieldEditor.
	 */
	@Override
	protected void doStore() {
		String s = createList(list.getItems());
		if (s != null) {
			getPreferenceStore().setValue(getPreferenceName(), s);
		}
	}

	/**
	 * Creates and returns a new item for the list.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @return a new item
	 */
	protected abstract String getNewInputObject();

	/**
	 * Returns this field editor's shell.
	 * <p>
	 * This method is internal to the framework; subclassers should not call
	 * this method.
	 * </p>
	 * 
	 * @return the shell
	 */
	protected Shell getShell() {
		if (addButton == null) {
			return null;
		}
		return addButton.getShell();
	}

	/**
	 * Splits the given string into a list of strings. This method is the
	 * converse of <code>createList</code>.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @param stringList
	 *            the string
	 * @return an array of <code>String</code>
	 * @see #createList
	 */
	protected abstract String[] parseString(String stringList);

	/**
	 * Notifies that the Add button has been pressed.
	 */
	private void addPressed() {
		setPresentsDefaultValue(false);
		String value = promptForText("Enter new value:");
		if (value != null) {
			int index = list.getSelectionIndex();
			if (index >= 0) {
				list.add(value, index + 1);
			} else {
				list.add(value, 0);
			}
			selectionChanged();
		}
	}

	/**
	 * Creates the Add, Remove, Up, and Down button in the given button box.
	 * 
	 * @param box
	 *            the box for the buttons
	 */
	private void createButtons(Composite box) {
		addButton = createPushButton(box, "ListEditor.add", null);//$NON-NLS-1$
		editButton = createPushButton(box, null, "Edit");
		removeButton = createPushButton(box, "ListEditor.remove", null);//$NON-NLS-1$
		upButton = createPushButton(box, "ListEditor.up", null);//$NON-NLS-1$
		downButton = createPushButton(box, "ListEditor.down", null);//$NON-NLS-1$
	}

	/**
	 * Helper method to create a push button.
	 * 
	 * @param parent
	 *            the parent control
	 * @param key
	 *            the resource name used to supply the button's label text
	 * @return Button
	 */
	private Button createPushButton(Composite parent, String key,
			String keyOverride) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(key != null ? JFaceResources.getString(key)
				: keyOverride);
		button.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		int widthHint = convertHorizontalDLUsToPixels(button,
				IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT,
				SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		button.addSelectionListener(getSelectionListener());
		return button;
	}

	/**
	 * Notifies that the Down button has been pressed.
	 */
	private void downPressed() {
		swap(false);
	}

	private void editPressed() {
		setPresentsDefaultValue(false);
		String value = list.getItem(list.getSelectionIndex());
		value = promptForText("Enter new value:", value);
		if (value != null) {
			list.setItem(list.getSelectionIndex(), value);
			selectionChanged();
		}
	}

	/**
	 * Returns this field editor's selection listener. The listener is created
	 * if nessessary.
	 * 
	 * @return the selection listener
	 */
	private SelectionListener getSelectionListener() {
		if (selectionListener == null) {
			createSelectionListener();
		}
		return selectionListener;
	}

	/**
	 * Notifies that the Remove button has been pressed.
	 */
	private void removePressed() {
		setPresentsDefaultValue(false);
		int index = list.getSelectionIndex();
		if (index >= 0) {
			list.remove(index);
			selectionChanged();
		}
	}

	/**
	 * Notifies that the list selection has changed.
	 */
	private void selectionChanged() {

		int index = list.getSelectionIndex();
		int size = list.getItemCount();

		removeButton.setEnabled(index >= 0);
		editButton.setEnabled(index >= 0);
		upButton.setEnabled(size > 1 && index > 0);
		downButton.setEnabled(size > 1 && index >= 0 && index < size - 1);
	}

	/**
	 * Moves the currently selected item up or down.
	 * 
	 * @param up
	 *            <code>true</code> if the item should move up, and
	 *            <code>false</code> if it should move down
	 */
	private void swap(boolean up) {
		setPresentsDefaultValue(false);
		int index = list.getSelectionIndex();
		int target = up ? index - 1 : index + 1;

		if (index >= 0) {
			String[] selection = list.getSelection();
			Assert.isTrue(selection.length == 1);
			list.remove(index);
			list.add(selection[0], target);
			list.setSelection(target);
		}
		selectionChanged();
	}

	/**
	 * Notifies that the Up button has been pressed.
	 */
	private void upPressed() {
		swap(true);
	}
}
