package raptor.pref.page;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Label;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.swt.chess.controller.PlayingMouseAction;

public class PlayingMouseActionsPage extends FieldEditorPreferencePage {

	public static final String[][] OPTIONS = {
			{ "None", PlayingMouseAction.None.toString() },
			{ "Clear Premoves", PlayingMouseAction.ClearPremoves.toString() },
			{ "Offer Draw", PlayingMouseAction.OfferDraw.toString() },
			{ "Crazyhouse/Bughouse Popup Menu",
					PlayingMouseAction.PopupMenu.toString() },
			{ "Random Move", PlayingMouseAction.RandomMove.toString() },
			{ "Random Move Involving Capture",
					PlayingMouseAction.RandomCapture.toString() },
			{ "Random Move Involving Recapture",
					PlayingMouseAction.RandomRecapture.toString() },
			{ "Smart Move", PlayingMouseAction.SmartMove.toString() } };

	public PlayingMouseActionsPage() {
		// Use the "flat" layout
		super(GRID);
		setTitle("Playing Mouse Buttons");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new ComboFieldEditor(PreferenceKeys.PLAYING_CONTROLLER
				+ PreferenceKeys.RIGHT_MOUSE_BUTTON_ACTION,
				"Right Mouse Button Action:", OPTIONS, getFieldEditorParent()));

		addField(new ComboFieldEditor(PreferenceKeys.PLAYING_CONTROLLER
				+ PreferenceKeys.MIDDLE_MOUSE_BUTTON_ACTION,
				"Middle Mouse Button Action:", OPTIONS, getFieldEditorParent()));

		addField(new ComboFieldEditor(PreferenceKeys.PLAYING_CONTROLLER
				+ PreferenceKeys.MISC1_MOUSE_BUTTON_ACTION,
				"Misc1 Mouse Button Action:", OPTIONS, getFieldEditorParent()));

		addField(new ComboFieldEditor(PreferenceKeys.PLAYING_CONTROLLER
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
