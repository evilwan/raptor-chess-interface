/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.swt.chess.movelist;

import java.util.ArrayList;
import java.util.List;

import raptor.util.RaptorLogger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import raptor.Raptor;
import raptor.chess.Game;
import raptor.chess.GameCursor;
import raptor.chess.Move;
import raptor.chess.pgn.Comment;
import raptor.chess.pgn.Nag;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardMoveList;
import raptor.swt.chess.controller.InactiveController;
import raptor.swt.chess.controller.ToolBarItemKey;

/**
 * Simple movelist that uses SWT StyledText. This would allow to support
 * annotation and variation trees within the widget.
 * 
 * TODO support customization
 */
public class TextAreaMoveList implements ChessBoardMoveList {
	private static final RaptorLogger LOG = RaptorLogger
			.getLog(TextAreaMoveList.class);

	protected ChessBoardController controller;
	protected StyledText textPanel;

	/**
	 * Defines offsets for each move node, where the list index is the move
	 * number.
	 */
	protected List<Integer> moveNodes;

	/**
	 * Defines lengths (in chars) for each move node.
	 */
	protected List<Integer> moveNodesLengths;

	/**
	 * The list contains all variations present in this game.
	 */
	protected List<MoveListVariation> vars = new ArrayList<MoveListVariation>();
	
	/**
	 * Current selected variation, null if not selected any. Should be handled with care.
	 */
	protected MoveListVariation currVariation;

	/**
	 * Offset where the header ends.
	 */
	protected int movesTextStart;
	
	/**
	 * The color of the move highlighting in the move list.
	 */
	protected Color moveSelectionColor;
	
	/**
	 * Currently highlighted half move.
	 */
	private int selectedHalfmove;
	protected long lastWheel;
	private String lastMoveSan;

	/**
	 * True if any variation selected.
	 */
	private boolean variationMode;

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		textPanel.replaceTextRange(0, textPanel.getCharCount(), "");
		moveNodes.clear();
		moveNodesLengths.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public Composite create(Composite parent) {
		if (textPanel == null) {
			createControls(parent);
		}
		return textPanel;
	}

	/**
	 * Inserts a text string comment near the specified half move.
	 * 
	 * @param halfMoveIndex
	 *            Half move number
	 * @param text
	 *            Text to insert
	 * @param coloring
	 *            Whether to color the text with red colour
	 */
	public void addCommentToMove(int halfMoveIndex, String text,
			boolean coloring) {
		int origCaretPos = textPanel.getCaretOffset();
		int start = moveNodes.get(halfMoveIndex);
		int length = moveNodesLengths.get(halfMoveIndex);
		textPanel.setCaretOffset(start + length);
		textPanel.insert(" " + text);
		StyleRange styleRange = new StyleRange();
		styleRange.start = start + length;
		styleRange.length = text.length() + 1;
		styleRange.fontStyle = SWT.ITALIC;
		if (coloring)
			styleRange.foreground = Display.getCurrent().getSystemColor(
					SWT.COLOR_RED);

		textPanel.setStyleRange(styleRange);
		textPanel.setCaretOffset(origCaretPos);

		// correct offsets for the following moves
		int moveIndex = halfMoveIndex + 1;
		while (moveIndex < moveNodes.size()) {
			moveNodes.set(moveIndex, moveNodes.get(moveIndex) + text.length()
					+ 1);
			moveIndex++;
		}
	}

	private void createControls(Composite parent) {
		textPanel = new StyledText(parent, SWT.BORDER | SWT.V_SCROLL);
		textPanel.setEditable(false);
		textPanel.setWordWrap(true);
		textPanel.setFont(Raptor.getInstance().getPreferences().getFont(
				"chat-input-font"));
		moveSelectionColor = new Color(Display.getCurrent(), 165, 192, 255);
		moveNodes = new ArrayList<Integer>();
		moveNodesLengths = new ArrayList<Integer>();
		textPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				int caretOffset = textPanel.getCaretOffset();

				if (caretOffset > moveNodes.get(moveNodes.size() - 1)) {
					controller.userSelectedMoveListMove(moveNodes.size());
					return;
				}

				// check if user selected a variation
				int count = 0;
				for (int nodeOffset : moveNodes) {
					for (MoveListVariation var : vars) {
						if (var.getStartingMove() == count
								&& var.getStartOffset() < caretOffset
								&& var.getStartOffset() + var.getTotalLength() > caretOffset) {
							int count2 = count;
							for (int varNodeOffset : var.getMoveNodes()) {								
								if (varNodeOffset < caretOffset
										&& varNodeOffset
												+ var.getMoveNodesLengths()
														.get(count2	- var
																.getStartingMove()) > caretOffset) {

									if (!variationMode) {

										currVariation = var;
										((InactiveController) controller)
												.setVariationMode(true);
									}

									controller
											.userSelectedMoveListMove(count2 + 1);							
									return;
								}

								count2++;
							}
						}
					}

					if (nodeOffset <= caretOffset
							&& nodeOffset + moveNodesLengths.get(count) >= caretOffset) {
						if (variationMode) { // user has a variation selected, but clicked on an ordinary move
							clearMoveSelection(selectedHalfmove);
							((InactiveController) controller)
									.setVariationMode(false);
						}

						controller.userSelectedMoveListMove(count + 1);
						break;
					}
					count++;
				}
			}
		});
		textPanel.addMouseWheelListener(new MouseWheelListener() {
			public void mouseScrolled(MouseEvent e) {
				if (System.currentTimeMillis() - lastWheel > 100
						&& Raptor.getInstance().getPreferences().getBoolean(
								PreferenceKeys.BOARD_TRAVERSE_WITH_MOUSE_WHEEL)) {
					getChessBoardController().userMouseWheeled(e.count);
					lastWheel = System.currentTimeMillis();
				}
			}
		});
		textPanel.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				switch (e.keyCode) {
				case 16777219: // left key
					controller.userSelectedMoveListMove(selectedHalfmove);
					break;
				case 16777220: // right key
					controller.userSelectedMoveListMove(selectedHalfmove + 2);
					break;
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public void forceRedraw() {
		updateToGame();
	}

	/**
	 * {@inheritDoc}
	 */
	public ChessBoardController getChessBoardController() {
		return controller;
	}

	/**
	 * {@inheritDoc}
	 */
	public Composite getControl() {
		return textPanel;
	}

	/**
	 * {@inheritDoc}
	 */
	public void select(int halfMoveIndex) {

		if ((!variationMode && selectedHalfmove + 1 == halfMoveIndex)
				|| moveNodes.size() == 0 || textPanel.getCharCount() == 0)
			return;

		if (variationMode) { 
			int index = halfMoveIndex - currVariation.getStartingMove() - 1;
			if (index < 0)
				index = 0;

			if (currVariation.getMoveNodes().size() == 0)
				return;

			clearMoveSelection(selectedHalfmove);
			StyleRange sR = new StyleRange();
			sR.background = moveSelectionColor;
			sR.start = currVariation.getMoveNodes().get(index);
			sR.length = currVariation.getMoveNodesLengths().get(index) - 1;
			
			textPanel.setStyleRange(sR);
			textPanel.setCaretOffset(sR.start + sR.length);
			selectedHalfmove = halfMoveIndex;
			textPanel.setSelection(sR.start + sR.length);
			return;
		}

		if (halfMoveIndex >= moveNodes.size())
			halfMoveIndex = moveNodes.size();

		if (halfMoveIndex <= 0)
			halfMoveIndex = 1;

		halfMoveIndex--;
		StyleRange sR = new StyleRange();
		sR.background = moveSelectionColor;
		sR.start = moveNodes.get(halfMoveIndex);
		sR.length = moveNodesLengths.get(halfMoveIndex);

		// clear previous move selection
		clearMoveSelection(selectedHalfmove);
		//System.out.println("halfMoveIndex " + halfMoveIndex);
		//System.out.println("size " + textPanel.getCharCount());
		textPanel.setStyleRange(sR);
		textPanel.setCaretOffset(sR.start + sR.length);
		selectedHalfmove = halfMoveIndex;
		textPanel.setSelection(sR.start + sR.length);
	}

	/**
	 * Unhighlight move.
	 * @param halfMove
	 */
	private void clearMoveSelection(int halfMove) {
		if (variationMode) {
			int index = halfMove - currVariation.getStartingMove() - 1;
			if (index < 0)
				index = 0;
			else if (index >= currVariation.getMoveNodes().size())
				index = currVariation.getMoveNodes().size() - 1;

			StyleRange cL = new StyleRange();
			cL.start = currVariation.getMoveNodes().get(index);
			cL.length = currVariation.getMoveNodesLengths().get(index);
			try {
				textPanel.setStyleRange(cL);
			} catch (IllegalArgumentException iae) {
				return;
			}
		}

		if (halfMove >= 0 && halfMove < moveNodes.size()) {
			StyleRange cL = new StyleRange();
			cL.start = moveNodes.get(halfMove);
			cL.length = moveNodesLengths.get(halfMove);
			try {
				textPanel.setStyleRange(cL);
			} catch (IllegalArgumentException iae) {
				return;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setController(ChessBoardController controller) {
		this.controller = controller;
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateToGame() {
		if (textPanel.isVisible()) {
			long startTime = System.currentTimeMillis();

			if (textPanel.getCharCount() == 0) {
				String sd = textPanel.getLineDelimiter();
				String title = controller.getGame().getHeader(PgnHeader.White)
						+ " vs "
						+ controller.getGame().getHeader(PgnHeader.Black) + sd
						+ sd;
				textPanel.append(title);
				StyleRange styleRange = new StyleRange();
				styleRange.start = 0;
				styleRange.length = title.length();
				styleRange.fontStyle = SWT.BOLD;
				textPanel.setStyleRange(styleRange);
				movesTextStart = textPanel.getCharCount();
			}
			Game game = controller.getGame();

			int moveListSize = game.getMoveList().getSize();
			
			if (moveListSize == 0 && textPanel.getCharCount() != 0) {
				textPanel.setText("");
				return;
			} else {
				if (moveListSize == moveNodes.size()) {
					if (vars.size() == 0
							&& !lastMoveSan.equals(game.getMoveList().get(
									moveListSize - 1).toString())) {
						prepareForRepaint();
					} else
						return;
				} else if (!variationMode && moveListSize != moveNodes.size()) {
					// move list shrinked
					prepareForRepaint();
				}

				StringBuffer buff = new StringBuffer();
				int lastMoveNLength = -1;
				for (int i = moveNodes.size(); i < moveListSize; i++) {
					boolean hasComments = false;
					int start, length;
					start = textPanel.getCharCount() + buff.length();
					String move = getMoveNumber(i)
							+ GameUtils.convertSanToUseUnicode(game
									.getMoveList().get(i).toString(), true);
					buff.append(move);
					length = move.length();
					if (lastMoveNLength != -1) {
						start -= lastMoveNLength;
						length += lastMoveNLength;
						lastMoveNLength = -1;
					}

					for (Nag nag : game.getMoveList().get(i).getNags()) {
						if (nag.hasSymbol()) {
							buff.append(nag.getSymbol());
							length += nag.getSymbol().length();
						}
					}

					for (Comment comment : game.getMoveList().get(i)
							.getComments()) {
						String sd = textPanel.getLineDelimiter();
						buff.append(sd);
						buff.append("  [" + comment + "]");
						buff.append(sd);
						hasComments = true;
					}

					moveNodes.add(start);
					moveNodesLengths.add(length);

					if (!hasComments)
						buff.append(" ");
					else if (moveListSize - 1 != i) {
						String num = getMoveNumberBlack(i - 1);
						buff.append(num);
						lastMoveNLength = num.length();
					}
				}
				textPanel.append(buff.toString());
				if (!variationMode)
					select(game.getHalfMoveCount());

				if (game.getMoveList().get(moveListSize - 1) != null) {
					lastMoveSan = game.getMoveList().get(moveListSize - 1)
							.toString();
				}
			}			

			updateVariations();

			if (LOG.isDebugEnabled()) {
				LOG.debug("Updated to game in : "
						+ (System.currentTimeMillis() - startTime));
			}
		}
	}

	/**
	 * Clear all info about painted object and prepare for a new repaint operation.
	 */
	private void prepareForRepaint() {		
		textPanel.replaceTextRange(movesTextStart, textPanel.getCharCount()
				- movesTextStart, "");
		moveNodes.clear();
		moveNodesLengths.clear();
	}

	protected void appendMove(int moveListSize) {
		textPanel.append(getMoveNumber(moveListSize)
				+ GameUtils.convertSanToUseUnicode(controller.getGame()
						.getMoveList().get(moveListSize).toString(), true));
		select(moveListSize - 1);
	}

	private String getMoveNumber(int i) {
		return (i % 2 == 0) ? Integer.toString((i + 3) / 2) + "." : "";
	}

	private String getMoveNumberBlack(int i) {
		int number = (i + 3) / 2;
		return (i % 2 == 0) ? Integer.toString(number) + "..." : "";
	}

	/**
	 * Sets the variation mode. currVariation should be null for it to work properly.
	 * @param variationMode
	 */
	public void setVariationMode(boolean variationMode) {
		this.variationMode = variationMode;
		controller.setToolItemSelected(ToolBarItemKey.TRY_VARIATION,
				variationMode);
		if (currVariation == null && variationMode) {
			Game game = new GameCursor(controller.getGame().deepCopy(true),
					GameCursor.Mode.MakeMovesOnCursor);
			int lastMoveN = game.getHalfMoveCount();
			currVariation = new MoveListVariation(lastMoveN, moveNodes
					.get(lastMoveN)
					+ moveNodesLengths.get(lastMoveN), game);
		} else if (!variationMode) {
			currVariation = null;
		}
	}

	/**
	 * Raptor is in variation mode and a move was made by user.
	 * @param lastMove The move
	 */
	public void variationMove(Move lastMove) {
		if (!vars.contains(currVariation)) {
			vars.add(currVariation);
			updateVariations();
		}

		currVariation.addMove(GameUtils.getPseudoSan(lastMove.getPiece(),
				lastMove.getCapture(), lastMove.getFrom(), lastMove.getTo()));
		updateVariations();
		updateToGame();
	}

	/**
	 * Paint variations on the text panel.
	 */
	private void updateVariations() {
		for (MoveListVariation var : vars) {
			StringBuilder buff = new StringBuilder();
			String toAdd = "";
			int deltaLength = 0;
			int mNumber = var.getStartingMove();
			if (var.getTotalLength() == 0) {
				buff.append("  [");
				var.increaseTotalLengthBy(6);
				deltaLength += 6;
			}

			int paintedCntr = var.getPaintedMovesCounter();
			int start = moveNodes.get(mNumber);
			int length = moveNodesLengths.get(mNumber);
			for (int i = paintedCntr + mNumber; i < var.getMoveSans().size()
					+ mNumber; i++) {
				String number;
				if (var.getPaintedMovesCounter() < 1)
					number = (i % 2 == 0) ? getMoveNumber(i)
							: getMoveNumberBlack(i - 1);
				else
					number = getMoveNumber(i);

				toAdd = " " + number + var.getMoveSans().get(i - mNumber);
				buff.append(toAdd);
				var.getMoveNodes().add(var.getNextNodeOffset() + 1);
				var.getMoveNodesLengths().add(toAdd.length());
				var.increaseTotalLengthBy(toAdd.length());
				deltaLength += toAdd.length();
				var.increasePaintedMovesCounter();
			}

			if (buff.toString().startsWith("  [")) {
				textPanel.setCaretOffset(start + length);
				final String delim = textPanel.getLineDelimiter();
				textPanel.insert(delim);
				textPanel.setCaretOffset(textPanel.getCaretOffset()
						+ delim.length());
				textPanel.insert(delim);
				textPanel.insert(buff.toString() + "]");
				var.setNextNodeOffset(textPanel.getCaretOffset()
						+ buff.toString().length());
			} else if (buff.length() != 0) {
				textPanel.setCaretOffset(var.getNextNodeOffset());

				textPanel.insert(buff.toString());
				var.setNextNodeOffset(textPanel.getCaretOffset()
						+ buff.length());
			} else
				continue;

			// correct offsets for the following moves
			int moveIndex = mNumber + 1;
			while (moveIndex < moveNodes.size()) {
				moveNodes
						.set(moveIndex, moveNodes.get(moveIndex) + deltaLength);
				moveIndex++;
			}
		}
	}

	public MoveListVariation getCurrVariation() {
		return currVariation;
	}

	/**
	 * When user selected first move in a variation and moves backwards this method is called.
	 * @param cursorPos Current cursor position
	 */
	public void arrangeForVarMode(int cursorPos) {
		if (variationMode && currVariation.getStartingMove() >= cursorPos) {
			clearMoveSelection(selectedHalfmove);
			((InactiveController) controller).setVariationMode(false);
		}
	}
}
