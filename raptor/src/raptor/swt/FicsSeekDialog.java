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
import raptor.international.MessageUtil;
import raptor.international.Messages;
import raptor.pref.PreferenceKeys;

public class FicsSeekDialog extends Dialog implements PreferenceKeys,Messages {
	protected String result = null;
	protected static final String[][] GAME_TYPE = {
			{ MessageUtil.getMessage(CHESS), "" },
			{ MessageUtil.getMessage(ATOMIC), "atomic" },
			{ MessageUtil.getMessage(CRAZYHOUSE), "zh" },
			{ MessageUtil.getMessage(FISCHER_RANDOM), "wild fr" },
			{ MessageUtil.getMessage(LOSERS), "losers" },
			{ MessageUtil.getMessage(PAWNS_ONLY), "pawns pawns-only" },
			{ MessageUtil.getMessage(ODDS_PAWN), "odds pawn" },
			{ MessageUtil.getMessage(ODDS_KNIGHT), "odds knight" },
			{ MessageUtil.getMessage(ODDS_ROOK), "odds rook" },
			{ MessageUtil.getMessage(ODDS_QUEEN), "odds queen" },
			{ MessageUtil.getMessage(SUICIDE), "suicide" },
			{ MessageUtil.getMessage(WILD_0), "wild 0" },
			{ MessageUtil.getMessage(WILD_1), "wild 1" },
			{ MessageUtil.getMessage(WILD_2), "wild 2" },
			{ MessageUtil.getMessage(WILD_3), "wild 3" },
			{ MessageUtil.getMessage(WILD_4), "wild 4" },
			{ MessageUtil.getMessage(WILD_5),
					"wild 5" },
			{ MessageUtil.getMessage(WILD_8), "wild 8" },
			{ MessageUtil.getMessage(WILD_8a), "wild 8a" }, };

	protected static final String[][] MINUTES = { { MessageUtil.getMessage(UNTIMED), "0" },
			{ MessageUtil.getMessage(X_MINUTES,0), "0" }, { MessageUtil.getMessage(X_MINUTES,1), "1" }, { MessageUtil.getMessage(X_MINUTES,2), "2" },
			{ MessageUtil.getMessage(X_MINUTES,3), "3" }, { MessageUtil.getMessage(X_MINUTES,4), "4" }, { MessageUtil.getMessage(X_MINUTES,5), "5" },
			{ MessageUtil.getMessage(X_MINUTES,10), "10" }, { MessageUtil.getMessage(X_MINUTES,15), "15" },
			{ MessageUtil.getMessage(X_MINUTES,30), "30" }, { MessageUtil.getMessage(X_MINUTES,45), "45" },
			{ MessageUtil.getMessage(X_MINUTES,60), "60" }, { MessageUtil.getMessage(X_MINUTES,90), "90" },
			{ MessageUtil.getMessage(X_MINUTES,120), "120" } };

	protected static final String[][] INC = { { MessageUtil.getMessage(X_SECONDS,0), "0" },
			{ MessageUtil.getMessage(X_SECONDS,1), "1" }, { MessageUtil.getMessage(X_SECONDS,2), "2" }, { MessageUtil.getMessage(X_SECONDS,3), "3" },
			{ MessageUtil.getMessage(X_SECONDS,4), "4" }, { MessageUtil.getMessage(X_SECONDS,5), "5" }, { MessageUtil.getMessage(X_SECONDS,10), "10" },
			{ MessageUtil.getMessage(X_SECONDS,12), "12" }, { MessageUtil.getMessage(X_SECONDS,15), "15" },
			{ MessageUtil.getMessage(X_SECONDS,30), "30" }, { MessageUtil.getMessage(X_SECONDS,45), "45" },
			{ MessageUtil.getMessage(X_SECONDS,60), "60" }, { MessageUtil.getMessage(X_SECONDS,90), "90" } };

	protected static final String[][] FROM_RANGE = { { MessageUtil.getMessage(ANY), "Any" },
			{ "0000", "0" }, { "500", "500" }, { "600", "600" },
			{ "700", "700" }, { "800", "800" }, { "900", "900" },
			{ "1000", "1000" }, { "1100", "1100" }, { "1200", "1200" },
			{ "1300", "1300" }, { "1400", "1400" }, { "1500", "1500" },
			{ "1600", "1600" }, { "1700", "1700" }, { "1800", "1800" },
			{ "1900", "1900" }, { "2000", "2000" }, { "2100", "2100" },
			{ "2200", "2300" }, { "2400", "2400" }, { "2500", "2500" },
			{ "2600", "2600" }, { "2700", "2700" }, { "2800", "2800" },
			{ "2900", "2900" } };

	protected static final String[][] TO_RANGE = { { MessageUtil.getMessage(ANY), "Any" },
			{ "600", "600" }, { "700", "700" }, { "800", "800" },
			{ "900", "900" }, { "1000", "1000" }, { "1100", "1100" },
			{ "1200", "1200" }, { "1300", "1300" }, { "1400", "1400" },
			{ "1500", "1500" }, { "1600", "1600" }, { "1700", "1700" },
			{ "1800", "1800" }, { "1900", "1900" }, { "2000", "2000" },
			{ "2100", "2100" }, { "2200", "2300" }, { "2400", "2400" },
			{ "2500", "2500" }, { "2600", "2600" }, { "2700", "2700" },
			{ "2800", "2800" }, { "2900", "2900" }, { "3000", "3000" },
			{ "4000", "4000" }, { "9999", "9999" } };

	protected static final String[][] COLOR = { { MessageUtil.getMessage(NONE_SPECIFIED), "" },
			{ MessageUtil.getMessage(WHITE), "w" }, { MessageUtil.getMessage(BLACK), "b" } };

	protected Shell shell;

	public FicsSeekDialog(Shell parent) {
		super(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
		setText(MessageUtil.getMessage(FicsSeekDialog_Title));
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
		gameTypeLabel.setText(MessageUtil.getMessage(FicsSeekDialog_GameType));
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
		minutesLabel.setText(MessageUtil.getMessage(FicsSeekDialog_Minutes));

		final Combo minutesCombo = new Combo(timeComposite, SWT.BORDER
				| SWT.READ_ONLY);
		populateCombo(minutesCombo, MINUTES);
		setValue(minutesCombo, MINUTES, FICS_SEEK_MINUTES);

		Label secondsLabel = new Label(timeComposite, SWT.NONE);
		secondsLabel.setText(MessageUtil.getMessage(FicsSeekDialog_Inc));

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
		ratingRangeLabel.setText(MessageUtil.getMessage(FicsSeekDialog_Rating));

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
		colorLabel.setText(MessageUtil.getMessage(FicsSeekDialog_Color));

		final Combo colorCombo = new Combo(colorComposite, SWT.BORDER
				| SWT.READ_ONLY);
		populateCombo(colorCombo, COLOR);
		setValue(colorCombo, COLOR, FICS_SEEK_COLOR);

		final Button ratedButton = new Button(composite, SWT.CHECK);
		ratedButton.setText(MessageUtil.getMessage(RATED));
		ratedButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 4, 1));
		ratedButton.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(FICS_SEEK_RATED));

		final Button formulaButton = new Button(composite, SWT.CHECK);
		formulaButton.setText(MessageUtil.getMessage(MATCHES_FORMULA));
		formulaButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 4, 1));
		formulaButton.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(FICS_SEEK_FORMULA));

		final Button manualButton = new Button(composite, SWT.CHECK);
		manualButton.setText(MessageUtil.getMessage(MANUAL));
		manualButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 4, 1));
		manualButton.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(FICS_SEEK_MANUAL));

		Composite actionButtonComposite = new Composite(composite, SWT.NONE);
		actionButtonComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		actionButtonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				true, false, 4, 1));

		Button okButton = new Button(actionButtonComposite, SWT.PUSH);
		okButton.setText(MessageUtil.getMessage(OK));
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
		cancel.setText(MessageUtil.getMessage(CANCEL));
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