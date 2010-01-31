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

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import raptor.Raptor;
import raptor.chess.GameConstants;
import raptor.chess.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.swt.SWTUtils;
import raptor.swt.chess.ChessBoardUtils;
import raptor.swt.chess.ChessSquare;
import raptor.swt.chess.PieceJailChessSquare;

public class ChessBoardPage extends FieldEditorPreferencePage {
	public class PieceJailSquarePageSquare extends PieceJailChessSquare {

		public PieceJailSquarePageSquare(Composite parent, int id,
				int pieceJailPiece) {
			super(parent, id, pieceJailPiece);
		}

		@Override
		protected Image getChessPieceImage(int piece, int size) {
			try {
				return ChessBoardUtils.getChessPieceImage(setFieldEditor
						.getValue(), piece, size);
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

		protected int getPieceJailLabelPercentage() {
			return Integer.parseInt(pieceJailPercentageCombo.getValue());
		}

		@Override
		protected int getPieceJailShadowAlpha() {
			return Integer.parseInt(pieceJailHidingAlphaCombo.getValue());
		}

		@Override
		protected String getRankLabel() {
			return null;
		}
	}

	protected class ChessBoardPageComboFieldEditor extends ComboFieldEditor {

		String key = null;
		String value = null;

		public ChessBoardPageComboFieldEditor(String name, String labelText,
				String[][] entryNamesAndValues, Composite parent) {
			super(name, labelText, entryNamesAndValues, parent);
			key = name;
		}

		public String getValue() {
			return value == null ? getPreferenceStore().getString(key) : value;
		}

		@Override
		protected void fireValueChanged(String property, Object oldValue,
				Object newValue) {
			value = newValue.toString();
			super.fireValueChanged(property, oldValue, newValue);
			valuesChanged();
		}
	}

	protected class ChessBoardPageSquare extends ChessSquare {

		public ChessBoardPageSquare(Composite parent, int id, boolean isLight) {
			super(parent, id, isLight);
		}

		@Override
		protected Image getBackgrondImage(boolean isLight, int width, int height) {
			try {
				return ChessBoardUtils.getSquareBackgroundImage(
						backgroundFieldEditor.getValue(), isLight, width,
						height);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected Image getChessPieceImage(int piece, int size) {
			try {
				return ChessBoardUtils.getChessPieceImage(setFieldEditor
						.getValue(), piece, size);
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected int getCoordinatesSizePercentage() {
			return Integer.parseInt(coordinatesPercentageCombo.getValue());
		}

		@Override
		protected String getFileLabel() {
			if ((GameUtils.getBitboard(id) & GameConstants.RANK1) != 0) {
				return "" + GameConstants.SQUARE_TO_FILE_SAN.charAt(id);
			} else {
				return null;
			}
		}

		@Override
		protected int getHidingAlpha() {
			return Integer.parseInt(boardHidingAlphaCombo.getValue());
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
			if ((GameUtils.getBitboard(id) & GameConstants.AFILE) != 0) {
				return "" + GameConstants.SQUARE_TO_RANK_SAN.charAt(id);
			} else {
				return null;
			}
		}

		@Override
		protected boolean isShowingCoordinates() {
			return true;
		}

	}

	public static final String[][] LAYOUTS = {
			{ "Right Oriented Layout (Fonts adjust to board size)",
					"raptor.swt.chess.layout.RightOrientedLayout" },
			{ "Right Oriented Layout (Fonts do not adjust to board size)",
					"raptor.swt.chess.layout.RightOrientedFixedLayout" },
			{ "Top/Bottom Oriented Layout",
					"raptor.swt.chess.layout.TopBottomOrientedLayout" } };

	public static final String[][] MOVE_LISTS = {
			{ "Table", "raptor.swt.chess.movelist.TableMoveList" },
			{ "Text", "raptor.swt.chess.movelist.TextAreaMoveList" } };

	public static final String[][] PIECE_RESIZE_PERCENTAGE = {
			{ "None", "0.0" }, { "1%", "0.01" }, { "2%", "0.02" },
			{ "4%", "0.04" }, { "6%", "0.06" }, { "7%", "0.07" },
			{ "8%", "0.08" }, { "9%", "0.09" }, { "10%", "0.1" },
			{ "11%", "0.09" }, { "12%", "0.12" }, { "14%", "0.14" },
			{ "16%", "0.16" }, { "18%", "0.18" }, { "20%", "0.20" },
			{ "22%", "0.22" }, { "25%", "0.25" }, { "30%", "0.30" },
			{ "35%", "0.35" }, { "40%", "0.40" }, { "45%", "0.45" },
			{ "50%", "0.50" } };

	public static final String[][] COORDINATES_SIZE_PERCENTAGE = {
			{ "18%", "18" }, { "20%", "20" }, { "22%", "22" }, { "24%", "24" },
			{ "26%", "26" }, { "28%", "28" }, { "30%", "30" }, { "32%", "32" },
			{ "34%", "34" }, { "36%", "36" }, { "38%", "38" }, { "40%", "40" } };

	public static final String[][] DROP_SQUARE_PERCENT = { { "20%", "20" },
			{ "25%", "25" }, { "30%", "30" }, { "35%", "35" }, { "40%", "40" },
			{ "45%", "45" }, { "50%", "50" } };

	public static final String[][] ALPHAS = { { "0", "0" }, { "5", "5" },
			{ "10", "10" }, { "15", "15" }, { "25", "25" }, { "30", "30" },
			{ "35", "35" }, { "40", "40" }, { "50", "50" }, { "60", "60" },
			{ "70", "70" }, { "80", "80" }, { "90", "90" }, { "100", "100" },
			{ "125", "125" }, { "150", "150" } };

	ChessBoardPageComboFieldEditor backgroundFieldEditor;
	ChessBoardPageComboFieldEditor highlightPercentage;
	Composite dropSquaresBoard;
	Composite miniBoard;
	Composite shadowsComposite;
	ChessBoardPageComboFieldEditor pieceResize;
	ChessBoardPageComboFieldEditor setFieldEditor;
	ChessBoardPageComboFieldEditor boardHidingAlphaCombo;
	ChessBoardPageComboFieldEditor pieceJailHidingAlphaCombo;
	ChessBoardPageComboFieldEditor coordinatesPercentageCombo;
	ChessBoardPageComboFieldEditor pieceJailPercentageCombo;
	ChessBoardPageSquare[][] squares = null;
	PieceJailSquarePageSquare[] dropSquares = null;
	ChessBoardPageSquare[] hiddenPieceAlphas;
	PieceJailSquarePageSquare[] pieceJailAlphas;

	public ChessBoardPage() {
		// Use the "grid" layout
		super(GRID);
		setPreferenceStore(Raptor.getInstance().getPreferences());
		setTitle("Chess Board");
	}

	public void valuesChanged() {
		for (ChessBoardPageSquare[] square : squares) {
			for (ChessBoardPageSquare element : square) {
				element.clearCache();
				element.redraw();
			}
		}
		miniBoard.layout(true, true);

		for (PieceJailSquarePageSquare element : dropSquares) {
			element.clearCache();
			element.redraw();
		}
		dropSquaresBoard.layout(true, true);

		for (ChessBoardPageSquare element : hiddenPieceAlphas) {
			element.clearCache();
			element.redraw();
		}
		for (PieceJailSquarePageSquare element : pieceJailAlphas) {
			element.clearCache();
			element.redraw();
		}
		shadowsComposite.layout(true, true);
	}

	@Override
	protected void createFieldEditors() {
		String[] sets = ChessBoardUtils.getChessSetNames();
		String[][] setNameValues = new String[sets.length][2];

		for (int i = 0; i < sets.length; i++) {
			setNameValues[i][0] = sets[i];
			setNameValues[i][1] = sets[i];
		}

		setFieldEditor = new ChessBoardPageComboFieldEditor(
				PreferenceKeys.BOARD_CHESS_SET_NAME, "Chess Set:",
				setNameValues, getFieldEditorParent());
		addField(setFieldEditor);

		String[] backgrounds = ChessBoardUtils.getSquareBackgroundNames();
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
		
		ComboFieldEditor moveListLayoutEditor = new ComboFieldEditor(
				PreferenceKeys.BOARD_MOVE_LIST_CLASS, "Move List Type:",
				MOVE_LISTS, getFieldEditorParent());
		addField(moveListLayoutEditor);

		pieceResize = new ChessBoardPageComboFieldEditor(
				PreferenceKeys.BOARD_PIECE_SIZE_ADJUSTMENT,
				"Piece size reduction percentage:\n(Higher values decrease piece size.)",
				PIECE_RESIZE_PERCENTAGE, getFieldEditorParent());
		addField(pieceResize);

		coordinatesPercentageCombo = new ChessBoardPageComboFieldEditor(
				PreferenceKeys.BOARD_COORDINATES_SIZE_PERCENTAGE,
				"Coordinates size as a percentage of square size:",
				COORDINATES_SIZE_PERCENTAGE, getFieldEditorParent());
		addField(coordinatesPercentageCombo);

		pieceJailPercentageCombo = new ChessBoardPageComboFieldEditor(
				PreferenceKeys.BOARD_PIECE_JAIL_LABEL_PERCENTAGE,
				"Piece jail label as a percentage of square size:",
				DROP_SQUARE_PERCENT, getFieldEditorParent());
		addField(pieceJailPercentageCombo);

		Composite boards = new Composite(getFieldEditorParent(), SWT.NONE);
		boards.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false,
				3, 1));
		boards.setLayout(new GridLayout(3, false));
		squares = new ChessBoardPageSquare[3][3];
		miniBoard = new Composite(boards, SWT.NONE);

		miniBoard.setLayout(SWTUtils.createMarginlessGridLayout(3, true));

		boolean isLight = true;
		for (int i = 2; i >= 0; i--) {
			for (int j = 0; j < squares[i].length; j++) {
				squares[i][j] = new ChessBoardPageSquare(miniBoard, j + i * 8,
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

		Label boardStrut = new Label(boards, SWT.NONE);
		boardStrut.setText("          ");

		dropSquaresBoard = new Composite(boards, SWT.NONE);
		dropSquaresBoard.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false,
				false));
		dropSquaresBoard
				.setLayout(SWTUtils.createMarginlessGridLayout(1, true));
		dropSquares = new PieceJailSquarePageSquare[] {
				new PieceJailSquarePageSquare(dropSquaresBoard, 9,
						GameConstants.WN),
				new PieceJailSquarePageSquare(dropSquaresBoard, 10,
						GameConstants.BN) };
		dropSquares[0].setLayoutData(new GridData(50, 50));
		dropSquares[1].setLayoutData(new GridData(50, 50));
		dropSquares[0].setBackground(Raptor.getInstance().getPreferences()
				.getColor(PreferenceKeys.BOARD_PIECE_JAIL_BACKGROUND_COLOR));
		dropSquares[1].setBackground(Raptor.getInstance().getPreferences()
				.getColor(PreferenceKeys.BOARD_PIECE_JAIL_BACKGROUND_COLOR));
		dropSquares[0].setPiece(GameConstants.WN);
		dropSquares[0].setText("" + 2);
		dropSquares[1].setPiece(GameConstants.BN);
		dropSquares[0].setText("" + 2);

		boardHidingAlphaCombo = new ChessBoardPageComboFieldEditor(
				PreferenceKeys.BOARD_PIECE_SHADOW_ALPHA,
				"Piece Shadow (0 is transparent, 255 is opaque):", ALPHAS,
				getFieldEditorParent());
		addField(boardHidingAlphaCombo);

		pieceJailHidingAlphaCombo = new ChessBoardPageComboFieldEditor(
				PreferenceKeys.BOARD_PIECE_JAIL_SHADOW_ALPHA,
				"Piece Jail Shadow (0 is transparent, 255 is opaque):", ALPHAS,
				getFieldEditorParent());
		addField(pieceJailHidingAlphaCombo);

		shadowsComposite = new Composite(getFieldEditorParent(), SWT.NONE);
		shadowsComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 5, 1));
		shadowsComposite
				.setLayout(SWTUtils.createMarginlessGridLayout(5, true));

		hiddenPieceAlphas = new ChessBoardPageSquare[] {
				new ChessBoardPageSquare(shadowsComposite, 9, true),
				new ChessBoardPageSquare(shadowsComposite, 10, false) };
		hiddenPieceAlphas[0].setLayoutData(new GridData(50, 50));
		hiddenPieceAlphas[1].setLayoutData(new GridData(50, 50));
		hiddenPieceAlphas[0].setPiece(GameConstants.WP);
		hiddenPieceAlphas[1].setPiece(GameConstants.BP);
		hiddenPieceAlphas[0].setHidingPiece(true);
		hiddenPieceAlphas[1].setHidingPiece(true);

		Label strut = new Label(shadowsComposite, SWT.NONE);
		strut.setText("          ");

		pieceJailAlphas = new PieceJailSquarePageSquare[] {
				new PieceJailSquarePageSquare(shadowsComposite, 9,
						GameConstants.WR),
				new PieceJailSquarePageSquare(shadowsComposite, 10,
						GameConstants.BR) };
		pieceJailAlphas[0].setLayoutData(new GridData(50, 50));
		pieceJailAlphas[1].setLayoutData(new GridData(50, 50));
		pieceJailAlphas[0].setBackground(Raptor.getInstance().getPreferences()
				.getColor(PreferenceKeys.BOARD_PIECE_JAIL_BACKGROUND_COLOR));
		pieceJailAlphas[1].setBackground(Raptor.getInstance().getPreferences()
				.getColor(PreferenceKeys.BOARD_PIECE_JAIL_BACKGROUND_COLOR));
	}
}
