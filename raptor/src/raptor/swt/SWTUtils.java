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
package raptor.swt;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.connector.Connector;

/**
 * A class containing SWT and JFace utilities.
 */
public class SWTUtils {

	public static final String[] OSX_MONOSPACED_FONTS = { "Minlo", "Monaco",
			"Andale", "Andale Mono", "Courier" };
	public static final String[] WINDOWS_MONOSPACED_FONTS = { "Monaco",
			"Andale Mono", "Consolas", "Lucida Console", "Fixedsys", "Courier" };
	public static final String[] OTHER_MONOSPACED_FONTS = { "Monospace",
			"Monaco", "DejaVu Sans Mono", "Nimbus Mono L",
			"WenQuanYi Zen Hei Mono", "Courier 10 Pitch", "Courier" };
	
	public static boolean isRightClick(MouseEvent e) {
		boolean isRightClickKeyMask = (e.stateMask & SWT.ALT) != 0
		|| (e.stateMask & SWT.COMMAND) != 0
		|| (e.stateMask & SWT.CONTROL) != 0;
        return e.button == 3 || (e.button == 1 && isRightClickKeyMask);
	}

	/**
	 * Centers the shell in the RaptorWindow.
	 */
	public static void centerInRaptorWindow(Dialog dialog) {
		Shell parent = Raptor.getInstance().getWindow().getShell();

		Rectangle parentSize = parent.getBounds();
		Point mySize = dialog.getShell().computeSize(0, 0);

		int locationX, locationY;
		locationX = (parentSize.width - mySize.x) / 2 + mySize.x;
		locationY = (parentSize.height - mySize.y) / 2 - mySize.y;

		dialog.getShell().setLocation(new Point(locationX, locationY));

	}

	/**
	 * Disposes of all the items in a toolbar.
	 * 
	 * @param toolbar
	 *            THe toolbar to clear.
	 */
	public static void clearToolbar(ToolBar toolbar) {
		if (toolbar != null) {
			ToolItem[] toolItems = toolbar.getItems();
			for (ToolItem toolItem : toolItems) {
				toolItem.dispose();
			}
		}
	}

	public static ToolBar createToolbar(Composite parent) {
		return new ToolBar(parent, SWT.FLAT | SWT.WRAP | SWT.RIGHT);
	}

	/**
	 * Returns a RowLayout without any margins or spacing.
	 */
	public static RowLayout createCenteredRowLayout(int type) {
		RowLayout result = new RowLayout(type);
		result.center = true;
		return result;
	}

	/**
	 * Returns a HORIZONTAL fill layout without any margins or spacing.
	 */
	public static FillLayout createMarginlessFillLayout() {
		FillLayout result = new FillLayout();
		result.marginHeight = 0;
		result.marginWidth = 0;
		result.spacing = 0;
		result.type = SWT.HORIZONTAL;
		return result;
	}

	/**
	 * Returns a GridLayout without any margins or spacing.
	 */
	public static GridLayout createMarginlessGridLayout(int columns,
			boolean areColumnsSameSize) {
		GridLayout result = new GridLayout(columns, areColumnsSameSize);
		result.marginLeft = 0;
		result.marginTop = 0;
		result.marginRight = 0;
		result.marginBottom = 0;
		result.horizontalSpacing = 0;
		result.verticalSpacing = 0;
		result.marginHeight = 0;
		result.marginWidth = 0;
		return result;
	}

	/**
	 * Returns a RowLayout without any margins or spacing.
	 */
	public static RowLayout createMarginlessRowLayout(int type) {
		RowLayout result = new RowLayout(type);
		result.marginLeft = 0;
		result.marginTop = 0;
		result.marginRight = 0;
		result.marginBottom = 0;
		result.fill = true;
		result.marginHeight = 0;
		result.marginWidth = 0;
		result.justify = false;
		result.spacing = 0;
		result.wrap = false;
		return result;
	}

	public static int getHeightInPixels(int fontPointSize) {
		Point pixelsPerInch = Raptor.getInstance().getDisplay().getDPI();
		int result = (int) (pixelsPerInch.y * (1.0 / 70.0) * fontPointSize);
		return result;
	}

	/**
	 * Returns a new font resizing the font size by resizePercentage of the
	 * controls height. This method caches fonts in Raptor's font registry so
	 * there is no need to dispose of any fonts.
	 * 
	 * @param fontToAdjust
	 *            The old font to adjust.
	 * @param resizePercentage
	 *            The percent of the controls height the new fonts height should
	 *            be. (i.e. 80 for 80%).
	 * @param controlHeight
	 *            The controls height to adjust for.
	 * @return The new font.
	 */
	public static Font getProportionalFont(Font fontToAdjust,
			int resizePercentage, int controlHeight) {
		return getProportionalFont(fontToAdjust, resizePercentage,
				controlHeight, -1);
	}

	/**
	 * Returns a new font resizing the font size by resizePercentage of the
	 * controls height. This method caches fonts in Raptor's font registry so
	 * there is no need to dispose of any fonts.
	 * 
	 * @param fontToAdjust
	 *            The old font to adjust.
	 * @param resizePercentage
	 *            The percent of the controls height the new fonts height should
	 *            be. (i.e. 80 for 80%).
	 * @param controlHeight
	 *            The controls height to adjust for.
	 * @param maxPointSize
	 *            The font will never be larger than the specified point size.
	 *            -1 if there is no max point size.
	 * @return The new font.
	 */
	public static Font getProportionalFont(Font fontToAdjust,
			int resizePercentage, int controlHeight, int maxPointSize) {
		if (controlHeight <= 0) {
			return fontToAdjust;
		}

		Point pixelsPerInch = Raptor.getInstance().getDisplay().getDPI();
		double pointsPerPixel = 70.0 / pixelsPerInch.y;

		double heightInPoints = controlHeight * pointsPerPixel;

		int requestedHeightInPoints = (int) (heightInPoints * resizePercentage / 100.0);

		if (maxPointSize > 0 && requestedHeightInPoints > maxPointSize) {
			requestedHeightInPoints = maxPointSize;
		}

		String key = fontToAdjust.getFontData()[0].getName() + "_"
				+ requestedHeightInPoints + "_"
				+ fontToAdjust.getFontData()[0].getStyle();

		if (Raptor.getInstance().getFontRegistry().hasValueFor(key)) {
			return Raptor.getInstance().getFontRegistry().get(key);
		} else {
			FontData[] oldFontDataArray = fontToAdjust.getFontData();
			FontData[] fontDataArray = new FontData[oldFontDataArray.length];
			for (int i = 0; i < fontDataArray.length; i++) {
				fontDataArray[i] = new FontData();
				fontDataArray[i].setHeight(requestedHeightInPoints);
				fontDataArray[i].setLocale(oldFontDataArray[i].getLocale());
				fontDataArray[i].setName(oldFontDataArray[i].getName());
				fontDataArray[i].setStyle(oldFontDataArray[i].getStyle());
			}
			Raptor.getInstance().getFontRegistry().put(key, fontDataArray);
			return Raptor.getInstance().getFontRegistry().get(key);
		}
	}

	/**
	 * Opens a games window item if one is not already open.
	 */
	public static void openGamesWindowItem(Connector connector) {
		RaptorWindowItem[] items = Raptor.getInstance().getWindow()
				.getWindowItems(GamesWindowItem.class);

		boolean openNewWindow = true;

		for (RaptorWindowItem item : items) {
			GamesWindowItem gameWindowsItem = (GamesWindowItem) item;
			if (gameWindowsItem.getConnector() == connector) {
				Raptor.getInstance().getWindow().forceFocus(item);
				openNewWindow = false;
				break;
			}
		}

		if (openNewWindow) {
			Raptor.getInstance().getWindow().addRaptorWindowItem(
					new GamesWindowItem(connector));
		}
	}

	/**
	 * Opens a bug buttons window item if one is not already open.
	 */
	public static void openBugButtonsWindowItem(Connector connector) {
		RaptorWindowItem[] items = Raptor.getInstance().getWindow()
				.getWindowItems(BugButtonsWindowItem.class);

		boolean openNewWindow = true;

		for (RaptorWindowItem item : items) {
			BugButtonsWindowItem bugButtonsItem = (BugButtonsWindowItem) item;
			if (bugButtonsItem.getConnector() == connector) {
				Raptor.getInstance().getWindow().forceFocus(item);
				openNewWindow = false;
				break;
			}
		}

		if (openNewWindow) {
			Raptor.getInstance().getWindow().addRaptorWindowItem(
					new BugButtonsWindowItem(connector));
		}
	}

	/**
	 * Opens a bug games window item if one is not already open for the
	 * specified connector.
	 */
	public static void openBugWhoWindowItem(Connector connector) {
		RaptorWindowItem[] items = Raptor.getInstance().getWindow()
				.getWindowItems(BugWhoWindowItem.class);

		boolean openNewWindow = true;

		for (RaptorWindowItem item : items) {
			BugWhoWindowItem bugWhoWindowItem = (BugWhoWindowItem) item;
			if (bugWhoWindowItem.getConnector() == connector) {
				Raptor.getInstance().getWindow().forceFocus(item);
				openNewWindow = false;
				break;
			}
		}

		if (openNewWindow) {
			BugWhoWindowItem item = new BugWhoWindowItem(connector
					.getBughouseService());
			Raptor.getInstance().getWindow().addRaptorWindowItem(item);
		}
	}

	/**
	 * Opens a seek table window item if one is not already open for the
	 * specified connector.
	 */
	public static void openSeekTableWindowItem(Connector connector) {
		RaptorWindowItem[] items = Raptor.getInstance().getWindow()
				.getWindowItems(SeekTableWindowItem.class);

		boolean openNewWindow = true;

		for (RaptorWindowItem item : items) {
			SeekTableWindowItem seekTableItem = (SeekTableWindowItem) item;
			if (seekTableItem.getConnector() == connector) {
				Raptor.getInstance().getWindow().forceFocus(item);
				openNewWindow = false;
				break;
			}
		}

		if (openNewWindow) {
			SeekTableWindowItem item = new SeekTableWindowItem(connector
					.getSeekService());
			Raptor.getInstance().getWindow().addRaptorWindowItem(item);
		}
	}

}
