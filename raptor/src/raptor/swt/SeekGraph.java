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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;

import raptor.Raptor;
import raptor.chat.Seek;
import raptor.service.SeekService;

/**
 * Seek Plot Component Shows seeks on a time vs. rating scatter plot
 */
public class SeekGraph extends Canvas {

	private static final Log logger = LogFactory.getLog(SeekGraph.class);

	private static final int SEEK_SIZE = 10;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Display.setAppName("Seek Graph");
		Shell shell = new Shell(display, SWT.SHELL_TRIM);

		GridLayout grid = new GridLayout(1, true);
		shell.setLayout(grid);

		final SeekGraph graph = new SeekGraph(shell, null);
		graph.addSeek(24, 1500, "Sergei", 5, 0, true);
		graph.addSeek(48, 1500, "Someone", 5, 0, true);
		graph.addSeek(48, 1500, "Someone", 3, 0, true);
		graph.addSeek(48, 1500, "Someone", 5, 2, true);
		graph.addSeek(48, 1500, "Someone", 3, 1, true);
		graph.addSeek(48, 1500, "Someone", 1, 0, true);

		GridData sg = new GridData(SWT.FILL, SWT.FILL, true, true);
		sg.widthHint = 400;
		sg.heightHint = 400;
		graph.setLayoutData(sg);

		Button acceptButton = new Button(shell, SWT.PUSH);
		acceptButton.setText("Add seek");
		acceptButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				Random random = new Random();
				int rating = random.nextInt(1000) + 1000;
				int mins = random.nextInt(3);
				int incr = random.nextInt(10);
				String[] names = new String[] { "Hi", "Bye", "My", "Try" };
				int gameNumber = random.nextInt(1000);

				System.out.println("Adding seek ( " + rating + ", " + mins
						+ ", " + incr + " )");
				graph.addSeek(gameNumber, rating, names[random
						.nextInt(names.length)], mins, incr, random
						.nextBoolean());
			}
		});

		GridData b = new GridData(GridData.VERTICAL_ALIGN_END);
		b.grabExcessVerticalSpace = false;
		acceptButton.setLayoutData(b);

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	private final Map<Point, List<Seek>> seeks;

	private final Map<Point, Point> screen;

	private int inset;

	private Color manyColor;

	private Color ratedColor;

	private Color unratedColor;

	private Color computerColor;
	private Image legendImage;

	private boolean isDrawingLegend;

	private int hstart = 1000;

	// Allows more space where needed
	private int[][] hscale = { { 1300, 1 }, { 1500, 2 }, { 1700, 2 },
			{ 1900, 2 }, { 2100, 1 }, { 2500, 1 } };

	// this should be the sum of second column
	int hfactor = 9;

	// Same as for hscale
	private int vstart = 0;

	int[][] vscale = { { 1, 1 }, { 3, 2 }, { 5, 2 }, { 10, 1 }, { 15, 1 },
			{ 20, 1 } };

	private int vfactor = 8;
	// popup tooltip
	private Rectangle lastPopupRect;

	private ToolTip tooltip;

	private SeekService seekService;

	public SeekGraph(final Composite parent, final SeekService seekService) {

		super(parent, SWT.NO_REDRAW_RESIZE);

		this.seekService = seekService;

		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				paintComponent(e);
			}
		});

		seeks = new HashMap<Point, List<Seek>>();
		screen = new HashMap<Point, Point>();
		inset = 20;

		computerColor = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
		manyColor = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
		ratedColor = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		unratedColor = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);

		tooltip = new ToolTip(parent.getShell(), SWT.BALLOON);

		addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {
				Point where = new Point(e.x, e.y);
				boolean showing = false;

				for (Point loc : screen.keySet()) {

					Rectangle rect = new Rectangle(loc.x, loc.y, SEEK_SIZE,
							SEEK_SIZE);

					if (rect.contains(where)) {
						showAcceptPopup(where, loc, rect);
						showing = true;
						break;
					}
				}

				if (!showing) {
					tooltip.setVisible(showing);
					// we're not pointing at anything, so reset _lastPopupRect
					lastPopupRect = null;
				}
			}
		});

		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				Point relative = new Point(e.x, e.y);
				acceptGameAt(relative);
			}
		});
	}

	public void redoLegend() {
		legendImage = null;
	}

	/**
	 * This is empirically faster then replace by one, as it just does one
	 * repaint call
	 * 
	 * @param incoming
	 */
	public void replaceBy(final Seek[] incoming) {

		Raptor.getInstance().getDisplay().asyncExec(new Runnable() {

			public void run() {
				if (isDisposed()) {
					return;
				}

				long before = System.nanoTime();

				screen.clear();
				seeks.clear();
				lastPopupRect = null;
				tooltip.setVisible(false);

				for (Seek seek : incoming) {
					addSeek(seek, true);
				}
				redraw();
				long after = System.nanoTime();

				if (logger.isDebugEnabled()) {
					logger.debug("Time to reload: " + (after - before));
				}
			}
		});
	}

	public void setComputerColor(Color c) {
		computerColor = c;
	}

	public void setDrawingLegend(boolean value) {
		isDrawingLegend = value;
	}

	public void setHScale(int[][] scale) {
		hscale = scale;

		hfactor = 0;
		for (int[] range : hscale) {
			hfactor += range[1];
		}
	}

	public void setHStart(int start) {
		hstart = start;
	}

	public void setManyColor(Color c) {
		manyColor = c;
	}

	public void setRatedColor(Color c) {
		ratedColor = c;
	}

	public void setUnratedColor(Color c) {
		unratedColor = c;
	}

	public void setVScale(int[][] scale) {
		vscale = scale;

		vfactor = 0;
		for (int[] range : vscale) {
			vfactor += range[1];
		}
	}

	public void setVStart(int start) {
		vstart = start;
	}

	protected void acceptGameAt(Point where) {
		if (seekService != null) {
			for (Point loc : screen.keySet()) {
				Rectangle rect = new Rectangle(loc.x, loc.y, SEEK_SIZE,
						SEEK_SIZE);

				if (rect.contains(where)) {
					List<Seek> existing = seeks.get(screen.get(loc));
					if (existing.size() == 1) {
						seekService.getConnector().acceptSeek(
								existing.get(0).getAd());
					} else {
						// TODO: show dialog to pick one
						seekService.getConnector().acceptSeek(
								existing.get(0).getAd());
					}

					break;
				}
			}
		}
	}

	protected void paintComponent(PaintEvent event) {

		Rectangle clientArea = getClientArea();
		if (logger.isDebugEnabled()) {
			logger.debug("ClientArea: " + clientArea);
		}

		int width = clientArea.width;
		int height = clientArea.height;

		Rectangle clip = event.gc.getClipping();
		if (logger.isDebugEnabled()) {
			logger.debug("Clip: " + clip);
		}

		// fix resize problem
		if (clip.width == width && clip.height == height) {
			// we're probably resizing, this will invalidate screen map
			screen.clear();
			lastPopupRect = null;
		}

		GC gc = event.gc;

		gc.setAntialias(SWT.ON);

		// Fill with white background
		gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		gc.fillRectangle(inset, inset, width - 2 * inset, height - 2 * inset);

		drawHorizontalLines(gc, height, height - 2 * inset, width, width - 2
				* inset);
		drawVerticalLines(gc, height, height - 2 * inset, width, width - 2
				* inset);

		if (isDrawingLegend) {
			drawLegend(gc, height, width);
		}

		drawPoints(gc, clip, width, height);

		// TODO: dispose of something???
	}

	protected Point scale(Point p, int width, int height) {
		Point result = new Point(-1, -1);

		float one = (float) height / hfactor;

		// scale appropriately
		if (p.y < hstart) {
			result.y = 1;
		} else {
			int factor = 0;
			int prev = hstart;
			for (int[] pair : hscale) {
				if (p.y <= pair[0]) {
					result.y = (int) (factor * one + pair[1] * one
							/ (pair[0] - prev) * (p.y - prev));
					break;
				}
				factor += pair[1];
				prev = pair[0];
			}
		}

		if (result.y == -1) {
			result.y = height;
		}

		float oneW = (float) width / vfactor;

		// scale appropriately
		if (p.x < vstart) {
			result.x = 1;
		} else {
			int factor = 0;
			int prev = vstart;
			float scaled_x = p.x;
			for (int[] pair : vscale) {
				if (scaled_x <= pair[0] * 60) {
					result.x = (int) (factor * oneW + pair[1] * oneW
							/ (pair[0] * 60 - prev) * (scaled_x - prev));
					break;
				}
				factor += pair[1];
				prev = pair[0] * 60;
			}
		}

		if (result.x == -1) {
			result.x = width;
		}

		return result;
	}

	private void addSeek(final int gameNumber, final int rating,
			final String name, final int mins, final int incr,
			final boolean rated) {

		final Seek seek = new Seek();
		seek.setAd(String.valueOf(gameNumber));
		seek.setRating(String.valueOf(rating));
		seek.setName(name);
		seek.setMinutes(mins);
		seek.setIncrement(incr);
		seek.setRated(rated);

		addSeek(seek, false);
	}

	private void addSeek(Seek seek, boolean fullRepaint) {

		Point loc = new Point(getX(seek), getY(seek));
		List<Seek> existing = seeks.get(loc);
		if (existing == null) {
			existing = new LinkedList<Seek>();
			seeks.put(loc, existing);
		}

		boolean already = false;
		for (Seek s : existing) {
			if (s.getAd().equals(seek.getAd())) {
				already = true;
				break;
			}
		}

		if (!already) {
			existing.add(seek);

			if (!fullRepaint && isVisible()) {

				int width = getClientArea().width;
				int height = getClientArea().height;
				Point where = scale(loc, width - 2 * inset, height - 2 * inset);
				where.y = height - inset - where.y;
				where.x = where.x + inset;
				redraw(where.x - SEEK_SIZE / 2, where.y - SEEK_SIZE / 2,
						SEEK_SIZE, SEEK_SIZE, false);
			}
		}
	}

	private Image createSingleLegend(String text, Color color) {
		int height = 15;
		Image legend = new Image(null, 80, height + 2);
		GC lgc = new GC(legend);
		lgc.setBackground(color);
		lgc.fillOval(0, (height + 2 - SEEK_SIZE) / 2, SEEK_SIZE, SEEK_SIZE);
		lgc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		lgc.drawText(text, SEEK_SIZE + 2, 1);
		lgc.dispose();

		return legend;
	}

	private void drawHorizontalLines(GC gc, int height, int plotH, int width,
			int plotW) {

		int one = plotH / hfactor;

		gc.setLineCap(SWT.CAP_ROUND);
		gc.setLineWidth(2);

		int factor = 0;
		for (int[] pair : hscale) {
			factor += pair[1];
			int h = height - one * factor - inset;
			gc.setForeground(Display.getCurrent()
					.getSystemColor(SWT.COLOR_BLUE));
			gc.drawLine(inset, h, inset + plotW, h);
			gc.setForeground(Display.getCurrent().getSystemColor(
					SWT.COLOR_BLACK));
			gc.drawString(String.valueOf(pair[0]), 0, h + 2);
		}
	}

	private void drawLegend(GC gc, int height, int width) {

		if (legendImage == null) {
			Image computer = createSingleLegend("Computer", computerColor);
			Image rated = createSingleLegend("Rated", ratedColor);
			Image unrated = createSingleLegend("Unrated", unratedColor);
			Image many = createSingleLegend("Many", manyColor);

			legendImage = new Image(null, computer.getBounds().width
					+ rated.getBounds().width + unrated.getBounds().width
					+ many.getBounds().width, 25);

			GC lg = new GC(legendImage);
			int cx = 0;
			lg.drawImage(computer, 0, 0);
			cx += computer.getBounds().width;
			lg.drawImage(rated, cx, 0);
			cx += rated.getBounds().width;
			lg.drawImage(unrated, cx, 0);
			cx += unrated.getBounds().width;
			lg.drawImage(many, cx, 0);
			lg.dispose();
			// Raptor.getInstance().getImageRegistry().put(SEEK_LEGEND_KEY,
			// legendImage);
		}

		// int y = inset / 4;
		int y = 0;
		int x = width - legendImage.getBounds().width - inset;
		if (logger.isDebugEnabled()) {
			logger.debug("Drawing legend at: " + x + ", " + y);
		}
		gc.drawImage(legendImage, x, y);
	}

	private void drawPoints(GC gc, Rectangle clip, int width, int height) {
		for (Point sp : seeks.keySet()) {
			Point p = scale(sp, width - 2 * inset, height - 2 * inset);
			p.y = height - inset - p.y - SEEK_SIZE / 2;
			p.x = p.x + inset - SEEK_SIZE / 2;
			// TODO: fix this...
			// if (clip.contains(p)) { // we will honor the clip!
			paintSeeks(gc, p, seeks.get(sp));
			screen.put(p, sp);
			// }
		}
	}

	private void drawVerticalLines(GC gc, int height, int plotH, int width,
			int plotW) {

		int one = plotW / vfactor;

		gc.setLineStyle(SWT.LINE_DASH);
		gc.setLineCap(SWT.CAP_ROUND);
		gc.setLineWidth(2);

		int factor = 0;
		for (int[] pair : vscale) {
			factor += pair[1];
			int w = one * factor + inset;
			gc.setForeground(Display.getCurrent()
					.getSystemColor(SWT.COLOR_BLUE));
			gc.drawLine(w, height - inset, w, inset);
			gc.setForeground(Display.getCurrent().getSystemColor(
					SWT.COLOR_BLACK));
			gc.drawString(String.valueOf(pair[0]), w, height - inset + 2);
		}
	}

	private int getX(Seek seek) {
		return seek.getMinutes() * 60 + seek.getIncrement() * 40;
	}

	private int getY(Seek seek) {
		return seek.getRatingAsInt();
	}

	private void paintSeeks(GC gc, Point p, List<Seek> here) {
		Color color = unratedColor;

		if (here.size() == 1) {
			Seek s = here.get(0);
			if (s.isComputer()) {
				color = computerColor;
			} else if (s.isRated()) {
				color = ratedColor;
			}
		} else {
			color = manyColor;
		}

		gc.setBackground(color);
		gc.fillOval(p.x, p.y, SEEK_SIZE, SEEK_SIZE);
	}

	private void showAcceptPopup(Point clickLoc, Point loc, Rectangle rect) {
		// are we're already showing for this?
		if (lastPopupRect == null || !rect.equals(lastPopupRect)) {
			// recreate the menu
			List<Seek> existing = seeks.get(screen.get(loc));
			StringBuilder all = new StringBuilder();
			for (Seek seek : existing) {
				String rating = seek.getRatingAsInt() == -1 ? " (Guest) "
						: " (" + seek.getRating() + ") ";
				String rated = seek.isRated() ? "r" : "ur";
				String text = new String(seek.getAd()
						+ ": "
						+ seek.getName()
						+ rating
						+ seek.getMinutes()
						+ " "
						+ seek.getIncrement()
						+ " "
						+ rated
						+ " "
						+ (seek.getType() != null ? seek.getType().toString()
								: ""));
				all.append(text + "\n");
			}
			tooltip.setText(all.substring(0, all.length() - 1));
			lastPopupRect = rect;

			tooltip.setLocation(toDisplay(loc.x + SEEK_SIZE - 2, loc.y
					+ SEEK_SIZE - 2));
			tooltip.setVisible(true);
		}
	}
}