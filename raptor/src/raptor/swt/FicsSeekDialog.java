package raptor.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;

public class FicsSeekDialog extends Dialog implements PreferenceKeys {
	protected String result = null;
	protected static final String[][] GAME_TYPE = {
			{ "Chess", "" },
			{ "Atomic", "atomic" },
			{ "Crazyhouse", "zh" },
			{ "Fischer Random", "wild fr" },
			{ "Losers", "losers" },
			{ "Pawns Only", "pawns pawns-only" },
			{ "Odds Pawn", "odds pawn" },
			{ "Odds Knight", "odds knight" },
			{ "Odds Rook", "odds rook" },
			{ "Odds Queen", "odds queen" },
			{ "Suicide", "suicide" },
			{ "Wild 0 (Reveresed queen and king)", "wild 0" },
			{ "Wild 1 (Random shuffle different on each side))", "wild 1" },
			{ "Wild 2 (Random shuffle mirror sides)", "wild 2" },
			{ "Wild 3 (Random pieces)", "wild 3" },
			{ "Wild 4 (Random pieces balanced bishops)", "wild 4" },
			{ "Wild 5 (White pawns start on 7th with pieces behind pawns)",
					"wild 5" },
			{ "Wild 8 (Pawns start on 4th rank)", "wild 8" },
			{ "Wild 8a (Pawns on 5th rank)", "wild 8a" }, };

	protected static final String[][] MINUTES = { { "Untimed", "untimed" },
			{ "0 minutes", "0" }, { "1 minutes", "1" }, { "2 minutes", "2" },
			{ "3 minutes", "3" }, { "4 minutes", "4" }, { "5 minutes", "5" },
			{ "10 minutes", "10" }, { "15 minutes", "15" },
			{ "30 minutes", "30" }, { "45 minutes", "45" },
			{ "60 minutes", "60" }, { "90 minutes", "90" },
			{ "120 minutes", "120" } };

	protected static final String[][] INC = { { "0 seconds", "0" },
			{ "1 second", "1" }, { "2 seconds", "2" }, { "3 seconds", "3" },
			{ "4 seconds", "4" }, { "5 seconds", "5" }, { "10 seconds", "10" },
			{ "12 seconds", "12" }, { "15 seconds", "15" },
			{ "30 seconds", "30" }, { "45 seconds", "45" },
			{ "60 seconds", "60" }, { "90 seconds", "90" } };

	protected static final String[][] FROM_RANGE = { { "Any", "Any" },
			{ "0000", "0" }, { "500", "500" }, { "600", "600" },
			{ "700", "700" }, { "800", "800" }, { "900", "900" },
			{ "1000", "1000" }, { "1100", "1100" }, { "1200", "1200" },
			{ "1300", "1300" }, { "1400", "1400" }, { "1500", "1500" },
			{ "1600", "1600" }, { "1700", "1700" }, { "1800", "1800" },
			{ "1900", "1900" }, { "2000", "2000" }, { "2100", "2100" },
			{ "2200", "2300" }, { "2400", "2400" }, { "2500", "2500" },
			{ "2600", "2600" }, { "2700", "2700" }, { "2800", "2800" },
			{ "2900", "2900" } };

	protected static final String[][] TO_RANGE = { { "Any", "Any" },
			{ "600", "600" }, { "700", "700" }, { "800", "800" },
			{ "900", "900" }, { "1000", "1000" }, { "1100", "1100" },
			{ "1200", "1200" }, { "1300", "1300" }, { "1400", "1400" },
			{ "1500", "1500" }, { "1600", "1600" }, { "1700", "1700" },
			{ "1800", "1800" }, { "1900", "1900" }, { "2000", "2000" },
			{ "2100", "2100" }, { "2200", "2300" }, { "2400", "2400" },
			{ "2500", "2500" }, { "2600", "2600" }, { "2700", "2700" },
			{ "2800", "2800" }, { "2900", "2900" }, { "3000", "3000" },
			{ "4000", "4000" }, { "9999", "9999" } };

	protected static final String[][] COLOR = { { "None Specified", "" },
			{ "White", "w" }, { "Black", "b" } };

	protected Shell shell;

	public FicsSeekDialog(Shell parent) {
		super(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
		setText("Fics Seek Dialog");
	}

	/**
	 * Opens the dialog and returns the input
	 * 
	 * @return String
	 */
	public String open() {
		// Create the dialog window
		shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		createContents(shell);
		shell.pack();
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Creates the dialog's contents
	 * 
	 * @param shell
	 *            the dialog window
	 */
	protected void createContents(final Composite composite) {
		composite.setLayout(new GridLayout(4, false));

		Label gameTypeLabel = new Label(composite, SWT.NONE);
		gameTypeLabel.setText("Game Type:");
		gameTypeLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));

		final Combo gameTypeCombo = new Combo(composite, SWT.BORDER
				| SWT.READ_ONLY);
		populateCombo(gameTypeCombo, GAME_TYPE);
		gameTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));
		setValue(gameTypeCombo, GAME_TYPE, FICS_SEEK_GAME_TYPE);

		Composite timeComposite = new Composite(composite, SWT.NONE);
		timeComposite.setLayout(SWTUtils
				.createCenteredRowLayout(SWT.HORIZONTAL));
		timeComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
				false, 4, 1));

		Label minutesLabel = new Label(timeComposite, SWT.NONE);
		minutesLabel.setText("Minutes:");

		final Combo minutesCombo = new Combo(timeComposite, SWT.BORDER
				| SWT.READ_ONLY);
		populateCombo(minutesCombo, MINUTES);
		setValue(minutesCombo, MINUTES, FICS_SEEK_MINUTES);

		Label secondsLabel = new Label(timeComposite, SWT.NONE);
		secondsLabel.setText("Inc:");

		final Combo secondsCombo = new Combo(timeComposite, SWT.BORDER
				| SWT.READ_ONLY);
		populateCombo(secondsCombo, INC);
		setValue(secondsCombo, INC, FICS_SEEK_INC);

		Composite ratingRangeComposite = new Composite(composite, SWT.NONE);
		ratingRangeComposite.setLayout(SWTUtils
				.createCenteredRowLayout(SWT.HORIZONTAL));
		ratingRangeComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				true, false, 4, 1));

		final Combo ratingGreaterThanCombo = new Combo(ratingRangeComposite,
				SWT.BORDER | SWT.READ_ONLY);
		populateCombo(ratingGreaterThanCombo, FROM_RANGE);
		setValue(ratingGreaterThanCombo, FROM_RANGE, FICS_SEEK_MIN_RATING);

		Label ratingRangeLabel = new Label(ratingRangeComposite, SWT.NONE);
		ratingRangeLabel.setText(" >= Rating <= ");

		final Combo ratingLessThanCombo = new Combo(ratingRangeComposite,
				SWT.BORDER | SWT.READ_ONLY);
		populateCombo(ratingLessThanCombo, TO_RANGE);
		setValue(ratingLessThanCombo, TO_RANGE, FICS_SEEK_MAX_RATING);

		Composite colorComposite = new Composite(composite, SWT.NONE);
		colorComposite.setLayout(SWTUtils
				.createCenteredRowLayout(SWT.HORIZONTAL));
		colorComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
				false, 4, 1));

		Label colorLabel = new Label(colorComposite, SWT.NONE);
		colorLabel.setText("Color:");

		final Combo colorCombo = new Combo(colorComposite, SWT.BORDER
				| SWT.READ_ONLY);
		populateCombo(colorCombo, COLOR);
		setValue(colorCombo, COLOR, FICS_SEEK_COLOR);

		final Button ratedButton = new Button(composite, SWT.CHECK);
		ratedButton.setText("Rated");
		ratedButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 4, 1));
		ratedButton.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(FICS_SEEK_RATED));

		final Button formulaButton = new Button(composite, SWT.CHECK);
		formulaButton.setText("Matches Forumla");
		formulaButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 4, 1));
		formulaButton.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(FICS_SEEK_FORMULA));

		final Button manualButton = new Button(composite, SWT.CHECK);
		manualButton.setText("Manual");
		manualButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 4, 1));
		manualButton.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(FICS_SEEK_MANUAL));

		Composite actionButtonComposite = new Composite(composite, SWT.NONE);
		actionButtonComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		actionButtonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				true, false, 4, 1));

		Button okButton = new Button(actionButtonComposite, SWT.PUSH);
		okButton.setText("OK");
		okButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				result = "seek " + getValue(minutesCombo, MINUTES) + " ";
				result += getValue(secondsCombo, INC) + " ";

				if (!getValue(gameTypeCombo, GAME_TYPE).equals("")) {
					result += getValue(gameTypeCombo, GAME_TYPE) + " ";
				}

				if (!getValue(colorCombo, COLOR).equals("")) {
					result += getValue(colorCombo, COLOR) + " ";
				}
				result += ratedButton.getSelection() ? "r " : "u ";
				String minRange = getValue(ratingGreaterThanCombo, FROM_RANGE);
				String maxRange = getValue(ratingLessThanCombo, TO_RANGE);

				if (!(minRange.equals("Any") && maxRange.equals("Any"))) {
					result += minRange.equals("Any") ? "0-" : minRange + "-";
					result += maxRange.equals("Any") ? "9999 " : maxRange + " ";
				}

				if (manualButton.getSelection()) {
					result += "m ";
				}
				if (formulaButton.getSelection()) {
					result += "f";
				}
				result = result.trim();

				Raptor.getInstance().getPreferences()
						.setValue(FICS_SEEK_GAME_TYPE,
								getValue(gameTypeCombo, GAME_TYPE));
				Raptor.getInstance().getPreferences().setValue(
						FICS_SEEK_MINUTES, getValue(minutesCombo, MINUTES));
				Raptor.getInstance().getPreferences().setValue(FICS_SEEK_INC,
						getValue(secondsCombo, INC));
				Raptor.getInstance().getPreferences().setValue(
						FICS_SEEK_MIN_RATING,
						getValue(ratingGreaterThanCombo, FROM_RANGE));
				Raptor.getInstance().getPreferences().setValue(
						FICS_SEEK_MAX_RATING,
						getValue(ratingGreaterThanCombo, TO_RANGE));
				Raptor.getInstance().getPreferences().setValue(FICS_SEEK_COLOR,
						getValue(colorCombo, COLOR));
				Raptor.getInstance().getPreferences().setValue(
						FICS_SEEK_MANUAL, manualButton.getSelection());
				Raptor.getInstance().getPreferences().setValue(
						FICS_SEEK_FORMULA, formulaButton.getSelection());
				Raptor.getInstance().getPreferences().setValue(FICS_SEEK_RATED,
						formulaButton.getSelection());

				shell.dispose();
			}

		});

		Button cancel = new Button(actionButtonComposite, SWT.PUSH);
		cancel.setText("Cancel");
		cancel.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				result = null;
				shell.dispose();
			}
		});
	}

	protected String getValue(Combo combo, String[][] values) {
		int index = combo.getSelectionIndex();
		return values[index][1];
	}

	protected void populateCombo(Combo combo, String[][] values) {
		for (String[] array : values) {
			combo.add(array[0]);
		}

	}

	protected void setValue(Combo combo, String[][] values, String preference) {
		String value = Raptor.getInstance().getPreferences().getString(
				preference);
		int index = -1;
		for (int i = 0; i < values.length; i++) {
			if (values[i][1].equals(value)) {
				index = i;
				break;
			}
		}
		if (index == -1) {
			Raptor.getInstance().onError(
					"Error setting combo " + preference
							+ " could not find value " + value);
		} else {
			combo.select(index);
		}
	}
}