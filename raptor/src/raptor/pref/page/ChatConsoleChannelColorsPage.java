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
package raptor.pref.page;

import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import raptor.Raptor;
import raptor.chat.ChatType;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;

public class ChatConsoleChannelColorsPage extends PreferencePage {
	Text channelName;
	Combo channels;
	ColorSelector colorSelector;
	Button deleteButton;
	Composite parent;
	RaptorPreferenceStore raptorPreferenceStore;
	Button saveButton;
	
	protected static L10n local = L10n.getInstance();

	public ChatConsoleChannelColorsPage() {
		// Use the "flat" layout
		super();
		setPreferenceStore(raptorPreferenceStore = Raptor.getInstance()
				.getPreferences());
		setTitle(local.getString("chanCol"));
	}

	public String getKey(String channel) {
		return PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
				+ ChatType.CHANNEL_TELL + "-" + channel + "-color";
	}

	public void updateChannelsCombo() {
		for (int i = 0; i < 255; i++) {
			String key = getKey("" + i);
			if (raptorPreferenceStore.contains(key)) {
				boolean contains = false;
				for (int j = 0; j < channels.getItemCount(); j++) {
					if (channels.getItem(j).equals("" + i)) {
						contains = true;
						break;
					}
				}

				if (!contains) {
					channels.add("" + i);
				}
			}
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout(3, false));

		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3,
				1));
		label
				.setText(WordUtils
						.wrap(local.getString("chatConColP1"), 70)
						+ WordUtils
								.wrap(local.getString("chatConColP2"),70));

		Label channelNamesLabel = new Label(parent, SWT.NONE);
		channelNamesLabel.setText(local.getString("channels"));
		channelNamesLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1));

		channels = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		channels.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false,
				2, 1));
		channels.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectedChannelName = channels.getItem(channels
						.getSelectionIndex());
				channelName.setText(selectedChannelName);
				String key = getKey("" + selectedChannelName);
				colorSelector.setColorValue(raptorPreferenceStore.getColor(key)
						.getRGB());
			}
		});

		Label channelNameLabel = new Label(parent, SWT.NONE);
		channelNameLabel.setText(local.getString("chanName"));
		channelNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1));

		channelName = new Text(parent, SWT.SINGLE | SWT.BORDER);
		channelName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		colorSelector = new ColorSelector(parent);
		colorSelector.getButton().setLayoutData(
				new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		saveButton = new Button(parent, SWT.PUSH);
		saveButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 3, 1));
		saveButton.setText(local.getString("svAddChCol"));
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onSave();
			}
		});

		// deleteButton = new Button(parent, SWT.PUSH);
		// deleteButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
		// false, 2, 1));
		// deleteButton.setText("Delete Channel Color");
		// deleteButton.addSelectionListener(new SelectionAdapter() {
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// try {
		// int channelInt = Integer.parseInt(channelName.getText());
		// String key = getKey("" + channelInt);
		// channels.remove("" + channelInt);
		// raptorPreferenceStore.putValue(key, null);
		// } catch (NumberFormatException nfe) {
		// MessageDialog
		// .openInformation(Raptor.getInstance()
		// .getRaptorWindow().getShell(), "Alert",
		// "Channel name must be an integer greater than -1 and less than 256.");
		// }
		// }
		// });

		updateChannelsCombo();
		if (channels.getItemCount() > 0) {
			channels.select(0);
			String selectedChannelName = channels.getItem(channels
					.getSelectionIndex());
			channelName.setText(selectedChannelName);
			String key = getKey("" + selectedChannelName);
			colorSelector.setColorValue(raptorPreferenceStore.getColor(key)
					.getRGB());
		}

		return parent;
	}

	protected void onSave() {
		try {
			int channelInt = Integer.parseInt(channelName.getText());
			if (channelInt < 0 || channelInt > 256) {
				throw new Exception();
			}
			String key = getKey("" + channelInt);
			raptorPreferenceStore.setValue(key, colorSelector.getColorValue());

			boolean channelsHasSelection = false;
			for (int i = 0; i < channels.getItemCount(); i++) {
				if (channels.getItem(i).equals("" + channelInt)) {
					channelsHasSelection = true;
					break;
				}
			}

			if (!channelsHasSelection) {
				channels.add("" + channelInt);
			}
		} catch (Throwable t) {
			MessageDialog
					.openInformation(Raptor.getInstance().getWindow()
							.getShell(), local.getString("alert"),
							local.getString("chatConColP3"));
		}
	}

	@Override
	protected void performApply() {
		onSave();
		super.performApply();
	}
}