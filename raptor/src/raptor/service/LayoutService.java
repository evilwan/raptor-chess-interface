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
package raptor.service;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import raptor.Raptor;
import raptor.layout.BughouseButtonsFarLeftLayout;
import raptor.layout.BughouseButtonsLeftLayout;
import raptor.layout.BughouseButtonsMiddleLayout;
import raptor.layout.BughouseButtonsRightLayout;
import raptor.layout.BughouseButtonsTopLayout;
import raptor.layout.ChatOnLeftLayout;
import raptor.layout.ChatOnRightLayout;
import raptor.layout.ChatOnRightSplitPaneChat;
import raptor.layout.ClassicLayout;
import raptor.layout.ClassicSplitPaneChatLayout;
import raptor.layout.CustomLayout;
import raptor.layout.Layout;
import raptor.util.RaptorLogger;

/**
 * A service used to manage layouts.
 */
public class LayoutService {
	private static final RaptorLogger LOG = RaptorLogger.getLog(LayoutService.class);

	public static final String USER_LAYOUT_DIR = Raptor.USER_RAPTOR_HOME_PATH
			+ "/layouts";
	public static final Comparator<Layout> LAYOUT_NAME_COMPARATOR = new Comparator<Layout>() {

		@Override
		public int compare(Layout o1, Layout o2) {
			// TODO Auto-generated method stub
			return o1.getName().compareTo(o2.getName());
		}
	};

	public static final Layout[] BUGHOUSE_SYSTEM_LAYOUTS = new Layout[] {
			new BughouseButtonsFarLeftLayout(),
			new BughouseButtonsLeftLayout(), new BughouseButtonsMiddleLayout(),
			new BughouseButtonsRightLayout(), new BughouseButtonsTopLayout() };

	public static final Layout[] NON_BUGHOUSE_SYSTEM_LAYOUTS = new Layout[] {
			new ChatOnLeftLayout(), new ChatOnRightSplitPaneChat(),
			new ChatOnRightLayout(), new ClassicLayout(),
			new ClassicSplitPaneChatLayout() };

	static {
		Arrays.sort(BUGHOUSE_SYSTEM_LAYOUTS, LAYOUT_NAME_COMPARATOR);
		Arrays.sort(NON_BUGHOUSE_SYSTEM_LAYOUTS, LAYOUT_NAME_COMPARATOR);
	}

	private static LayoutService singletonInstance = new LayoutService();

	private List<Layout> customLayouts = new ArrayList<Layout>(10);

	private LayoutService() {
		init();
	}

	public static LayoutService getInstance() {
		return singletonInstance;
	}

	public void init() {
		loadCustomLayouts();
	}

	public void dispose() {
		customLayouts.clear();
	}

	protected void loadCustomLayouts() {
		long startTime = System.currentTimeMillis();
		int numLayouts = 0;

		File userLayoutsDir = new File(USER_LAYOUT_DIR);
		File[] userLayoutsFiles = userLayoutsDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathName) {
				return pathName.getName().endsWith(".properties");
			}
		});

		customLayouts.clear();

		if (userLayoutsFiles != null) {
			for (File file : userLayoutsFiles) {
				customLayouts.add(CustomLayout.loadFromProperties(file
						.getAbsolutePath()));
				numLayouts++;
			}
		}

		Collections.sort(customLayouts, LAYOUT_NAME_COMPARATOR);

		LOG.info("Initilized Layout Service in "
				+ (System.currentTimeMillis() - startTime) + "ms " + numLayouts
				+ " loaded.");
	}

	public Layout[] getNonBughouseSystemLayouts() {
		return NON_BUGHOUSE_SYSTEM_LAYOUTS;
	}

	public Layout[] getBughouoseSystemLayouts() {
		return BUGHOUSE_SYSTEM_LAYOUTS;
	}

	public Layout[] getCustomLayouts() {
		return customLayouts.toArray(new CustomLayout[0]);

	}

	public CustomLayout saveCurrentAsCustomLayout(String name) {
		CustomLayout layout = CustomLayout.createFromCurrentSettings();
		layout.setName(name);
		Properties properties = CustomLayout.saveAsProperties(layout);

		FileOutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream(USER_LAYOUT_DIR + "/"
					+ layout.getName() + ".properties", false);
			properties.store(fileOut, "Created in Raptor");
		} catch (Throwable t) {
			Raptor.getInstance().onError("Error saving layout: " + name, t);
		} finally {
			if (fileOut != null) {
				try {
					fileOut.close();
				} catch (Throwable t) {
				}
			}
		}

		for (int i = 0; i < customLayouts.size(); i++) {
			if (customLayouts.get(i).getName().equalsIgnoreCase(name)) {
				customLayouts.remove(i);
				i--;
			}
		}
		customLayouts.add(layout);
		Collections.sort(customLayouts, LAYOUT_NAME_COMPARATOR);
		return layout;
	}

}
