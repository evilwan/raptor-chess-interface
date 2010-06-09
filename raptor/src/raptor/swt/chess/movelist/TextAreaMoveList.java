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
package raptor.swt.chess.movelist;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import raptor.chess.pgn.Comment;
import raptor.chess.pgn.Nag;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardMoveList;

/**
 * Simple movelist that uses SWT StyledText. This would allow to support
 * annotation and variation trees within the widget.
 * 
 * TODO support customization
 */
public class TextAreaMoveList implements ChessBoardMoveList {
	private static final Log LOG = LogFactory.getLog(TextAreaMoveList.class);
	
	protected ChessBoardController controller;
	protected StyledText textPanel;
	
	/**
	 * Defines offsets for each move node, where the list index is the move number
	 */
	protected List<Integer> moveNodes;
	
	/**
	 * Defines lengths (in chars) for each move node
	 */
	protected List<Integer> moveNodesLengths;
	
	/**
	 * Offset where the header ends
	 */
	protected int movesTextStart;
	protected Color moveSelectionColor;
	private int selectedHalfmove;
	protected long lastWheel;
	private String lastMoveSan;

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

	private void createControls(Composite parent) {		
		textPanel = new StyledText(parent, SWT.BORDER | SWT.V_SCROLL);
		textPanel.setEditable(false);
		textPanel.setWordWrap(true);
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

				int count = 0;
				for (int nodeOffset: moveNodes) {
					if (nodeOffset > caretOffset) {
						controller.userSelectedMoveListMove(count);
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
					controller.userSelectedMoveListMove(selectedHalfmove-1);
					break;
				case 16777220: // right key
					controller.userSelectedMoveListMove(selectedHalfmove+1);
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
		if (selectedHalfmove == halfMoveIndex || moveNodes.size() == 0)
			return;
		
		if (halfMoveIndex >= moveNodes.size())
			halfMoveIndex = moveNodes.size();

		if (halfMoveIndex <= 0)
			halfMoveIndex = 1;

		halfMoveIndex--;
		StyleRange sR = new StyleRange();
		sR.background = moveSelectionColor;
		sR.start = moveNodes.get(halfMoveIndex);
		sR.length = moveNodesLengths.get(halfMoveIndex);

		selectedHalfmove--;
		if (selectedHalfmove >= 0 && selectedHalfmove < moveNodes.size()) { 
			// clear previous move selection
			StyleRange cL = new StyleRange();
			cL.start = moveNodes.get(selectedHalfmove);
			cL.length = moveNodesLengths.get(selectedHalfmove);
			textPanel.setStyleRange(cL);
		}
		
		textPanel.setStyleRange(sR);
		textPanel.setCaretOffset(sR.start + sR.length);
		selectedHalfmove = halfMoveIndex+1;
		textPanel.setSelection(sR.start+sR.length);
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
				textPanel.replaceTextRange(movesTextStart, textPanel
						.getCharCount(), "");
			} else {
				if (moveListSize == moveNodes.size()) {
					if (!lastMoveSan.equals(game
							.getMoveList().get(moveListSize-1).toString())) {
						prepareForRepaint();
					}
					else
						return;					
				}					
				else if (moveListSize <= moveNodes.size()) { 
					// move list shrinked
					prepareForRepaint();
				}

				StringBuffer buff = new StringBuffer();
				for (int i = moveNodes.size(); i < moveListSize; i++) {
					int start, length;
					start = textPanel.getCharCount() + buff.length();
					String move = getMoveNumber(i)
							+ GameUtils.convertSanToUseUnicode(game
									.getMoveList().get(i).toString(), true);
					buff.append(move);
					length = move.length();
					
					for (Nag nag : game.getMoveList().get(i).getNags()) {
						if (nag.hasSymbol()) {
							buff.append(nag.getSymbol());
							length += nag.getSymbol().length();
						}
					}

					for (Comment comment : game.getMoveList().get(i)
							.getComments()) {
						buff.append(" ");
						buff.append(comment);
					}
					
					moveNodes.add(start);
					moveNodesLengths.add(length);
					buff.append(" ");
				}
				textPanel.append(buff.toString());
				select(game.getHalfMoveCount());
				lastMoveSan = game.getMoveList().get(moveListSize - 1)
						.toString();
			}
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("Updated to game in : "
						+ (System.currentTimeMillis() - startTime));
			}
		}
		
	}

	private void prepareForRepaint() {
		textPanel.replaceTextRange(movesTextStart, textPanel
				.getCharCount()
				- movesTextStart, "");
		moveNodes.clear();
		moveNodesLengths.clear();		
	}

	protected void appendMove(int moveListSize) {
		textPanel.append(getMoveNumber(moveListSize)
				+ GameUtils.convertSanToUseUnicode(controller.getGame()
						.getMoveList().get(moveListSize).toString(), true));
		select(moveListSize-1);
	}
	
	private String getMoveNumber(int i) {
		return (i % 2 == 0) ? Integer.toString((i+3)/2) + "." : "";
	}
	
}
