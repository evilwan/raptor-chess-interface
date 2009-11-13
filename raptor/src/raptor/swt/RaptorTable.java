package raptor.swt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
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

	public static interface TableListener {
		public void rowDoubleClicked(MouseEvent event, String[] rowData);

		public void rowRightClicked(MouseEvent event, String[] rowData);

		public void tableSorted();

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
	protected List<TableListener> tableListeners = new ArrayList<TableListener>(
			2);

	public RaptorTable(Composite parent, int tableStyle) {
		super(parent, SWT.NONE);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				tableListeners.clear();
			}
		});

		// setLayout(new FillLayout());
		table = new Table(this, tableStyle);
		table.setLocation(0, 0);
		table.setHeaderVisible(true);
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

				for (TableListener listener : tableListeners) {
					listener.rowDoubleClicked(e, getData(item));
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 3) {
					TableItem item = table.getItem(new Point(e.x, e.y));
					for (TableListener listener : tableListeners) {
						listener.rowRightClicked(e, getData(item));
					}
				}
			}
		});
	}

	public void addColumn(String name, int style, int widthPercentage,
			boolean isSortable, Comparator<String> compartor) {
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

	public void addRowListener(TableListener listener) {
		tableListeners.add(listener);
	}

	public void appendRow(String[] data) {
		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(data);

		for (TableListener listener : tableListeners) {
			listener.tableUpdated();
		}
	}

	public void clearTable() {
		synchronized (table) {
			TableItem[] items = table.getItems();
			for (TableItem item : items) {
				item.dispose();
			}

			for (TableListener listener : tableListeners) {
				listener.tableUpdated();
			}
		}
	}

	public Table getTable() {
		return table;
	}

	// public void swapRows(int index1, int index2) {
	// TableItem item1 = table.getItem(index1);
	// TableItem item2 = table.getItem(index2);
	// String[] data1 = getData(item1);
	// String[] data2 = getData(item2);
	//
	// TableItem newItem1 = new TableItem(table, SWT.NONE, index2);
	// newItem1.setText(data2);
	// item2.dispose();
	//
	// TableItem newItem2 = new TableItem(table, SWT.NONE, index1);
	// newItem2.setText(data1);
	// item1.dispose();
	// }

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
				for (int i = data.length; i < table.getItemCount(); i++) {
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

			for (TableListener listener : tableListeners) {
				listener.tableUpdated();
			}
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("refreshTable in "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	public void removeRow(int index) {
		table.getItem(index).dispose();

		for (TableListener listener : tableListeners) {
			listener.tableUpdated();
		}
	}

	public void removeRowListener(TableListener listener) {
		tableListeners.remove(listener);
	}

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

			for (TableListener listener : tableListeners) {
				listener.tableSorted();
			}
		}

	}

	protected String[] getData(TableItem item) {
		Table table = item.getParent();
		int colCount = table.getColumnCount();
		String[] result = new String[colCount];
		for (int i = 0; i < colCount; i++) {
			result[i] = item.getText(i);
		}
		return result;
	}

	protected void resizeColumns(int width) {
		for (ColumnInfo info : columnInfos) {
			info.column.setWidth((int) (width * info.widthPercentage / 100.0));
		}
	}

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
