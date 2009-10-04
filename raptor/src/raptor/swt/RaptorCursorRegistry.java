package raptor.swt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;

public class RaptorCursorRegistry {
	protected Map<String, Cursor> map = new HashMap<String, Cursor>();
	protected Display display;
	protected Cursor defaultCursor;

	/**
	 * Constructs a raptor registry using the specified display.
	 */
	public RaptorCursorRegistry(Display display) {
		this.display = display;
	}

	/**
	 * Disposes this cursor registry, disposing any cursors that were allocated
	 * for it, and clearing its entries.
	 */
	public void dispose() {
		String[] keys = getKeys();
		for (String key : keys) {
			remove(key);
		}
		map.clear();
	}

	/**
	 * Returns the cursor in the registry for the specified key.
	 */
	public Cursor get(String key) {
		return map.get(key);
	}

	public Cursor getDefaultCursor() {
		return defaultCursor;
	}

	public Display getDisplay() {
		return display;
	}

	/**
	 * Returns a String[] of all keys in the cursor registry.
	 */
	public String[] getKeys() {
		return map.keySet().toArray(new String[0]);
	}

	/**
	 * Returns the number of cursors in the registry.
	 */
	public int getSize() {
		return map.keySet().size();
	}

	/**
	 * Adds a cursor to the cursor registry.
	 */
	public void put(String key, Cursor cursor) {
		if (get(key) != null) {
			throw new IllegalArgumentException("Cursor " + key
					+ " already in use.");
		} else {
			map.put(key, cursor);
		}
	}

	/**
	 * Removes a cursor from the registry.
	 */
	public void remove(String key) {
		Cursor cursor = get(key);
		if (cursor != null) {
			cursor.dispose();
			map.remove(key);
		}
	}

	public void setDefaultCursor(Cursor defaultCursor) {
		this.defaultCursor = defaultCursor;
	}
}
