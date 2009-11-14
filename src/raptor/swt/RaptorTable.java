package raptor.swt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import raptor.util.RaptorStringUtils;

/**
 * A class that wraps a table. It handles the things that are a pain with the
 * SWT table class. You add columns specifying width percentages and
 * Comparators. The widths will be adjusted when the components size changes.
 * Also supports sorting. And adding a row listener for double clicks and
 * clicks.
 * 
 * You can also add a TableListener which informs you when double clicks and
 * right clicks occur. It also informs on table updates and when the table is
 * sorted.
 */
public class RaptorTable extends Composite {

	private static final Log LOG = LogFactory.getLog(RaptorTable.class);

	protected static final Comparator<String> STRING_COMPARATOR = new Comparator<String>() {
		public int compare(String arg0, String arg1) {
			return arg0.compareTo(arg1);
		}
	};

	public static class RaptorTableAdapter implements RaptorTableListener {
		/**
		 * @{inheritDoc
		 */
		public void cursorMoved(int row, int column) {
		}

		/**
		 * @{inheritDoc
		 */
		public void rowDoubleClicked(MouseEvent event, String[] rowData) {
		}

		/**
		 * @{inheritDoc
		 */
		public void rowRightClicked(MouseEvent event, String[] rowData) {
		}

		/**
		 * @{inheritDoc
		 */
		public void tableSorted() {
		}

		public void tableUpdated() {
		}
	}

	public static interface RaptorTableListener {
		/**
		 * Invoked when the cursor is moved.
		 * 
		 * @param row
		 *            New cursor row
		 * @param column
		 *            New cursor column.
		 */
		public void cursorMoved(int row, int column);

		/**
		 * Invoked when a row is double clicked.
		 * 
		 * @param event
		 *            The mouse event of the double click.
		 * @param rowData
		 *            The row data in the row that was double clicked.
		 */
		public void rowDoubleClicked(MouseEvent event, String[] rowData);

		/**
		 * Invoked when a row is right clicked.
		 * 
		 * @param event
		 *            The mouse event of the right click.
		 * @param rowData
		 *            The row data in the row that was right clicked.
		 */
		public void rowRightClicked(MouseEvent event, String[] rowData);

		/**
		 * Invoked after the table is sorted.
		 */
		public void tableSorted();

		/**
		 * Invoked when the table is updated.
		 */
		public void tableUpdated();
	}

	protected static class ColumnInfo {
		int index;
		TableColumn column;
		int widthPercentage;
		Comparator<String> comparator;
	}

	protected static class TableItemComparator implements Comparator<TableItem> {
		protected Comparator<String> comparator;
		protected int columnIndex;
		protected boolean isAscending;

		public TableItemComparator(Comparator<String> comparator,
				int columnIndex, boolean isAscending) {
			this.comparator = comparator;
			this.columnIndex = columnIndex;
			this.isAscending = isAscending;
		}

		public int compare(TableItem o1, TableItem o2) {
			String string1 = o1.getText(columnIndex);
			String string2 = o2.getText(columnIndex);
			return isAscending ? comparator.compare(string1, string2) : -1
					* comparator.compare(string1, string2);
		}
	}

	protected int lastIndex = -1;
	protected Table table;
	protected List<ColumnInfo> columnInfos = new ArrayList<ColumnInfo>(10);
	protected TableColumn lastStortedColumn;
	protected boolean wasLastSortAscending;
	protected TableItemComparator lastComparator;
	protected List<RaptorTableListener> tableListeners = new ArrayList<RaptorTableListener>(
			2);
	protected int fixedWidth;
	protected TableCursor cursor;
	protected boolean ignoreCursorSelection;

	/**
	 * Creates a RaptorTable that shows headers and does'nt use a table cursor.
	 * 
	 * @param parent
	 *            Parent component
	 * @param tableStyle
	 *            Style for the swt Table object.
	 */
	public RaptorTable(Composite parent, int tableStyle) {
		this(parent, tableStyle, false, true);
	}

	/**
	 * Constructs a RaptorTable
	 * 
	 * @param parent
	 *            Parent component
	 * @param tableStyle
	 *            Style for the swt Table object.
	 * @param usesTableCursor
	 *            True if a table cursor should be used, false otherwise.
	 * @param showHeaders
	 *            True if headers should be shown, false otherwise.
	 */
	public RaptorTable(Composite parent, int tableStyle,
			boolean usesTableCursor, boolean showHeaders) {
		super(parent, SWT.NONE);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				tableListeners.clear();
			}
		});

		table = new Table(this, tableStyle);
		table.setLocation(0, 0);
		table.setHeaderVisible(showHeaders);
		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				table.setSize(getSize());
				resizeColumns(getClientArea().width
						- table.getVerticalBar().getSize().x - 2);
				table.redraw();
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				TableItem item = table.getItem(new Point(e.x, e.y));

				if (item != null) {
					for (RaptorTableListener listener : tableListeners) {
						listener.rowDoubleClicked(e, getData(item));
					}
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 3) {
					TableItem item = table.getItem(new Point(e.x, e.y));
					if (item != null) {
						for (RaptorTableListener listener : tableListeners) {
							listener.rowRightClicked(e, getData(item));
						}
					}
				}
			}
		});
		if (usesTableCursor) {

			// A trick to hide the row selection.
			table.addListener(SWT.EraseItem, new Listener() {
				public void handleEvent(Event event) {
					if ((event.detail & SWT.SELECTED) != 0) {
						event.detail &= ~SWT.SELECTED;
					}
				}
			});

			cursor = new TableCursor(table, SWT.NONE);
			cursor.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!ignoreCursorSelection) {
						for (RaptorTableListener listener : tableListeners) {
							listener.cursorMoved(table.getSelectionIndex(),
									cursor.getColumn());
						}
					}
				}
			});

			// Adds up/down arrow key listener.
			cursor.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					synchronized (table) {
						if (e.keyCode == SWT.ARROW_UP) {
							int currentRow = table.getSelectionIndex();
							int currentColumn = cursor.getColumn();

							if (currentRow != 0) {
								table.deselectAll();
								table.setSelection(currentRow - 1);
								cursor.setSelection(currentRow - 1,
										currentColumn);
								cursor.setVisible(true);
								cursor.redraw();
							}

						} else if (e.keyCode == SWT.ARROW_DOWN) {
							int currentRow = table.getSelectionIndex();
							int currentColumn = cursor.getColumn();

							if (currentRow != table.getItemCount() - 1) {
								table.deselectAll();
								table.setSelection(currentRow + 1);
								cursor.setSelection(currentRow + 1,
										currentColumn);
								cursor.setVisible(true);
								cursor.redraw();
							}
						}
					}
				}
			});
		}
	}

	/**
	 * Adds a column to this table.
	 * 
	 * @param name
	 *            The name of the column.
	 * @param style
	 *            The style of the column (SWT.LEFT,SWT.RIGHT,SWT.CENTER).
	 * @param widthPercentage
	 *            The width percentage of this column.
	 * @param isSortable
	 *            True of the column is sortable.
	 * @param compartor
	 *            The comparator to use for the sort if isSortable. If null a
	 *            String.compareTo will be used.
	 */
	public void addColumn(String name, int style, int widthPercentage,
			boolean isSortable, Comparator<String> compartor) {
		synchronized (table) {
			TableColumn column = new TableColumn(table, style);
			column.setText(name);
			final ColumnInfo info = new ColumnInfo();
			info.column = column;
			info.widthPercentage = widthPercentage;
			info.index = ++lastIndex;
			if (isSortable) {
				if (compartor == null) {
					info.comparator = STRING_COMPARATOR;
				} else {
					info.comparator = compartor;
				}
				column.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						sort(info.index);
					}
				});
			}
			columnInfos.add(info);
		}
	}

	/**
	 * Adds a RaptorTableListener to this table.
	 */
	public void addRaptorTableListener(RaptorTableListener listener) {
		tableListeners.add(listener);
	}

	/**
	 * Appends a row to this table.
	 */
	public void appendRow(String[] data) {
		if (cursor != null) {
			cursor.setVisible(true);
		}
		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(data);

		for (RaptorTableListener listener : tableListeners) {
			listener.tableUpdated();
		}
	}

	/**
	 * Clears the contents of this table.
	 */
	public void clearTable() {
		synchronized (table) {
			if (cursor != null) {
				cursor.setVisible(false);
			}
			TableItem[] items = table.getItems();
			for (TableItem item : items) {
				item.dispose();
			}

			for (RaptorTableListener listener : tableListeners) {
				listener.tableUpdated();
			}
		}
	}

	/**
	 * Overridden to support fixed width if its set.
	 */
	@Override
	public Point computeSize(int hint, int hint2, boolean changed) {
		if (fixedWidth != 0) {
			return super.computeSize(fixedWidth, hint2, changed);
		} else {
			return super.computeSize(hint, hint2, changed);
		}
	}

	/**
	 * Returns the number of columns in this table.
	 */
	public int getColumnCount() {
		return table.getColumnCount();
	}

	/**
	 * Returns the number of rows in this table.
	 */
	public int getRowCount() {
		return table.getItemCount();
	}

	/**
	 * Returns the text in the specified row.
	 */
	public String[] getRowText(int row) {
		return getData(table.getItem(row));
	}

	/**
	 * Returns the data in the selected row if one is selected, otherwise
	 * returns null.
	 */
	public String[] getSelectedRowData() {
		if (table.getSelectionIndex() == -1) {
			return null;
		} else {
			return getRowText(table.getSelectionIndex());
		}
	}

	/**
	 * Returns the backing table this RaptorTable uses.
	 */
	public Table getTable() {
		return table;
	}

	/**
	 * If usesTableCursor was passed into the constructor then this will return
	 * the table cursor. Otherwise it will return null.
	 */
	public TableCursor getTableCursor() {
		return cursor;
	}

	/**
	 * Returns the text in the specified cell.
	 */
	public String getText(int row, int column) {
		return table.getItem(row).getText(column);
	}

	/**
	 * Refreshes the table with the specified data. Selections are preserved.
	 * The table is also sorted by the last sort criteria.
	 */
	public void refreshTable(String[][] data) {
		long startTime = System.currentTimeMillis();
		synchronized (table) {
			table.setRedraw(false);

			// There is a minor bug in here where at times you will have
			// duplicates of the last item
			// I can't figure out why.
			// It might have something to do with sorting but I have a feeling
			// its in here some where.
			// Its minor so I am not worrying about it right now.

			// First overwrite all rows in items
			int[] selectedIndexes = table.getSelectionIndices();
			List<String[]> selectionsBeforeRefresh = new ArrayList<String[]>(
					selectedIndexes.length);
			for (int i = 0; i < selectedIndexes.length; i++) {
				selectionsBeforeRefresh.add(getData(table
						.getItem(selectedIndexes[i])));
			}
			table.deselectAll();

			TableItem[] items = table.getItems();
			for (int i = 0; i < data.length; i++) {
				TableItem item = new TableItem(table, SWT.NONE, i);
				item.setText(data[i]);
				if (i < items.length) {
					items[i].dispose();
				}
			}
			if (data.length < items.length) {
				for (int i = data.length; i < items.length; i++) {
					items[i].dispose();
				}
			}

			// sort
			if (lastComparator != null) {
				sort(lastComparator);
			}

			outer: for (String[] selectedData : selectionsBeforeRefresh) {
				for (int i = 0; i < table.getItemCount(); i++) {
					if (RaptorStringUtils.equals(getData(table.getItem(i)),
							selectedData)) {
						table.select(i);
						continue outer;
					}
				}
			}
			table.setRedraw(true);
			table.layout(true);
			table.redraw();

			if (cursor != null) {
				cursor.setVisible(true);
				cursor.redraw();
			}

			for (RaptorTableListener listener : tableListeners) {
				listener.tableUpdated();
			}
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("refreshTable in "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	/**
	 * Removes a TableListener from this table.
	 */
	public void removeRaptorTableListener(RaptorTableListener listener) {
		tableListeners.remove(listener);
	}

	/**
	 * Removes the specified row.
	 */
	public void removeRow(int index) {
		table.getItem(index).dispose();

		for (RaptorTableListener listener : tableListeners) {
			listener.tableUpdated();
		}
	}

	/**
	 * If usesTableCursor this will select the specified row and column.
	 */
	public void select(int row, int column) {
		table.deselectAll();
		table.setSelection(row);
		if (cursor != null) {
			cursor.setSelection(row, column);
			cursor.setVisible(true);
		}
		table.redraw();
		if (cursor != null) {
			try {
				cursor.redraw();
			} catch (NullPointerException npe) {
			}
		}
	}

	/**
	 * Sets the cursor to the first non null column in the last row.
	 */
	public void setCursorEnd() {
		synchronized (table) {
			if (table.getItemCount() > 0) {
				table.deselectAll();
				table.setSelection(table.getItemCount() - 1);
				int column = 0;
				for (int i = table.getColumnCount() - 1; i >= 0; i--) {
					if (StringUtils.isNotBlank(table.getItem(
							table.getItemCount() - 1).getText(i))) {
						column = i;
						break;
					}
				}
				if (cursor != null) {
					cursor.setSelection(table.getSelectionIndex(), column);
					cursor.setVisible(true);
					cursor.redraw();
				}
			}
		}
	}

	/**
	 * Sets a fixed width for this table. It will always return the passed in
	 * width in computeSize.
	 */
	public void setFixedWidth(int width) {
		fixedWidth = width;
	}

	/**
	 * Sets the text in the specified cell.
	 */
	public void setText(int row, int column, String text) {
		table.getItem(row).setText(column, text);
	}

	/**
	 * Sorts the specified column. Keeps track of ascending,descending and
	 * toggles them if the same column is sorted twice.
	 */
	public void sort(int index) {
		synchronized (table) {
			table.setRedraw(false);

			ColumnInfo info = columnInfos.get(index);
			wasLastSortAscending = lastStortedColumn == null ? true
					: lastStortedColumn == info.column ? !wasLastSortAscending
							: true;
			lastStortedColumn = info.column;
			lastComparator = new TableItemComparator(info.comparator,
					info.index, wasLastSortAscending);
			sort(lastComparator);

			table.setRedraw(true);
			table.layout(true);
			table.redraw();

			for (RaptorTableListener listener : tableListeners) {
				listener.tableSorted();
			}
		}

	}

	/**
	 * Returns a String[] of the data in the specified item.
	 */
	protected String[] getData(TableItem item) {
		Table table = item.getParent();
		int colCount = table.getColumnCount();
		String[] result = new String[colCount];
		for (int i = 0; i < colCount; i++) {
			result[i] = item.getText(i);
		}
		return result;
	}

	/**
	 * Resizes the columns to the specified width according to their
	 * percentages.
	 */
	protected void resizeColumns(int width) {
		for (ColumnInfo info : columnInfos) {
			info.column.setWidth((int) (width * info.widthPercentage / 100.0));
		}
	}

	/**
	 * Sorts the table using the specified comparator.
	 */
	protected void sort(Comparator<TableItem> comparator) {
		long startTime = System.currentTimeMillis();

		TableItem[] beforeSort = table.getItems();
		TableItem[] sorted = new TableItem[beforeSort.length];

		System.arraycopy(beforeSort, 0, sorted, 0, beforeSort.length);

		Arrays.sort(sorted, comparator);
		String[][] data = new String[sorted.length][];
		for (int i = 0; i < sorted.length; i++) {
			data[i] = getData(sorted[i]);
		}

		for (int i = 0; i < sorted.length; i++) {
			TableItem item = new TableItem(table, SWT.NONE, i);
			item.setText(data[i]);
			beforeSort[i].dispose();
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Sorted in " + (System.currentTimeMillis() - startTime));
		}
	}
}
