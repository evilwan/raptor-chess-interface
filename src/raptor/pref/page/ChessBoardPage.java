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
package raptor.pref.page;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import raptor.Raptor;
import raptor.chess.GameConstants;
import raptor.pref.PreferenceKeys;
import raptor.pref.fields.LabelButtonFieldEditor;
import raptor.swt.ChessSetOptimizationDialog;
import raptor.swt.SWTUtils;
import raptor.swt.chess.BoardUtils;
import raptor.swt.chess.ChessSquare;

public class ChessBoardPage extends FieldEditorPreferencePage {

	protected class ChessBoardPageComboFieldEditor extends ComboFieldEditor {

		String key = null;
		String value = null;

		public ChessBoardPageComboFieldEditor(String name, String labelText,
				String[][] entryNamesAndValues, Composite parent) {
			super(name, labelText, entryNamesAndValues, parent);
			key = name;
		}

		@Override
		protected void fireValueChanged(String property, Object oldValue,
				Object newValue) {
			value = newValue.toString();
			super.fireValueChanged(property, oldValue, newValue);
			valuesChanged();
		}

		public String getValue() {
			return value == null ? getPreferenceStore().getString(key) : value;
		}
	}

	protected class ChessBoardPageSquare extends ChessSquare {

		public ChessBoardPageSquare(Composite parent, int id, boolean isLight) {
			super(parent, id, isLight);
		}

		@Override
		protected Image getBackgrondImage(boolean isLight, int width, int height) {
			try {
				return BoardUtils.getSquareBackgroundImage(
						backgroundFieldEditor.getValue(), isLight, width,
						height);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected Image getChessPieceImage(int piece, int width, int height) {
			try {
				return BoardUtils.getChessPieceImage(setFieldEditor.getValue(),
						piece, width, height);
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected String getFileLabel() {
			return null;
		}

		@Override
		protected int getImageSize() {
			try {
				double imageSquareSideAdjustment = Double
						.parseDouble(pieceResize.getValue().toString());
				int imageSide = (int) (getSize().x * (1.0 - imageSquareSideAdjustment));
				if (imageSide % 2 != 0) {
					imageSide = imageSide - 1;
				}
				return imageSide;
			} catch (Exception e) {
				return getSize().y;
			}
		}

		@Override
		protected String getRankLabel() {
			return null;
		}
	}

	public static final String[][] HIGHLIGHT_BORDER_RESIZE_PERCENTAGE = {
			{ "None", "0.0" }, { "1%", "0.01" }, { "2%", "0.02" },
			{ "3%", "0.03" }, { "4%", "0.04" }, { "5%", "0.05" },
			{ "6%", "0.06" }, { "7.5%", "0.075" }, { "10%", "0.1" },
			{ "12.5%", "0.125" } };

	public static final String[][] LAYOUTS = { { "Right Oriented Layout",
			"raptor.swt.chess.layout.RightOrientedLayout" } };

	public static final String[][] PIECE_RESIZE_PERCENTAGE = {
			{ "None", "0.0" }, { "1%", "0.01" }, { "2%", "0.02" },
			{ "3%", "0.03" }, { "4%", "0.04" }, { "5%", "0.05" },
			{ "6%", "0.06" }, { "7.5%", "0.075" } };

	public static final String[][] PIECE_WEIGHT_PERCENTAGE = { {
			"Right Oriented Layout",
			"raptor.swt.chess.layout.RightOrientedLayout" } };

	ChessBoardPageComboFieldEditor backgroundFieldEditor;
	ChessBoardPageComboFieldEditor highlightPercentage;
	Composite miniBoard;
	ChessBoardPageComboFieldEditor pieceResize;
	ChessBoardPageComboFieldEditor setFieldEditor;
	ChessBoardPageSquare[][] squares = null;

	public ChessBoardPage() {
		// Use the "grid" layout
		super(GRID);
		setPreferenceStore(Raptor.getInstance().getPreferences());
		setTitle("Chess Board");
	}

	@Override
	protected void createFieldEditors() {

		String[] sets = BoardUtils.getChessSetNames();
		String[][] setNameValues = new String[sets.length][2];

		for (int i = 0; i < sets.length; i++) {
			setNameValues[i][0] = sets[i];
			setNameValues[i][1] = sets[i];
		}

		setFieldEditor = new ChessBoardPageComboFieldEditor(
				PreferenceKeys.BOARD_CHESS_SET_NAME, "Chess Set:",
				setNameValues, getFieldEditorParent());
		addField(setFieldEditor);

		String[] backgrounds = BoardUtils.getSquareBackgroundNames();
		String[][] backgroundNameValues = new String[backgrounds.length][2];
		for (int i = 0; i < backgrounds.length; i++) {
			backgroundNameValues[i][0] = backgrounds[i];
			backgroundNameValues[i][1] = backgrounds[i];
		}

		backgroundFieldEditor = new ChessBoardPageComboFieldEditor(
				PreferenceKeys.BOARD_SQUARE_BACKGROUND_NAME,
				"Square Background", backgroundNameValues,
				getFieldEditorParent());
		addField(backgroundFieldEditor);

		ComboFieldEditor layoutsFieldEditor = new ComboFieldEditor(
				PreferenceKeys.BOARD_LAYOUT, "Chess Board Control Layout:",
				LAYOUTS, getFieldEditorParent());
		addField(layoutsFieldEditor);

		pieceResize = new ChessBoardPageComboFieldEditor(
				PreferenceKeys.BOARD_PIECE_SIZE_ADJUSTMENT,
				"Piece size reduction percentage:\n(Higher values decrease piece size.)",
				PIECE_RESIZE_PERCENTAGE, getFieldEditorParent());
		addField(pieceResize);

		squares = new ChessBoardPageSquare[3][3];
		miniBoard = new Composite(getFieldEditorParent(), SWT.NONE);
		miniBoard.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1));
		miniBoard.setLayout(SWTUtils.createMarginlessGridLayout(3, true));

		boolean isLight = true;
		for (int i = 0; i < squares.length; i++) {
			for (int j = 0; j < squares[i].length; j++) {
				squares[i][j] = new ChessBoardPageSquare(miniBoard, j * i,
						isLight);
				squares[i][j].setLayoutData(new GridData(50, 50));
				isLight = !isLight;
			}
		}

		squares[0][0].setPiece(GameConstants.BR);
		squares[0][1].setPiece(GameConstants.BN);
		squares[0][2].setPiece(GameConstants.BB);

		squares[1][0].setPiece(GameConstants.BP);
		squares[1][1].setPiece(GameConstants.EMPTY);
		squares[1][2].setPiece(GameConstants.WP);

		squares[2][0].setPiece(GameConstants.WQ);
		squares[2][1].setPiece(GameConstants.WR);
		squares[2][2].setPiece(GameConstants.WK);

		// squares[1][1].highlight();
		// squares[0][2].highlight();

		LabelButtonFieldEditor labelButtonFieldEditor = new LabelButtonFieldEditor(
				"NONE",
				"Optimize chess set (Convert from svg to pgns)\n"
						+ "This may take a few minutes but is highly recommended.",
				getFieldEditorParent(), "Optimize", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						ChessSetOptimizationDialog dialog = new ChessSetOptimizationDialog(
								getShell(), "Chess Set "
										+ setFieldEditor.getValue()
										+ " Optimization.", setFieldEditor
										.getValue());
						dialog.open();
					}
				});
		addField(labelButtonFieldEditor);
	}

	public void valuesChanged() {
		for (ChessBoardPageSquare[] square : squares) {
			for (ChessBoardPageSquare element : square) {
				element.clearCache();
				element.redraw();
				miniBoard.layout(true, true);
			}
		}
	}
}
