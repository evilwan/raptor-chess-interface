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
package raptor.swt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;

public class RaptorCursorRegistry {
	protected Cursor defaultCursor;
	protected Display display;
	protected Map<String, Cursor> map = new HashMap<String, Cursor>();

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
