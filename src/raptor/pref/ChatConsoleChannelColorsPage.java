package raptor.pref;

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

public class ChatConsoleChannelColorsPage extends PreferencePage {
	Composite parent;
	Combo channels;
	Text channelName;
	ColorSelector colorSelector;
	Button saveButton;
	Button deleteButton;
	RaptorPreferenceStore raptorPreferenceStore;

	public ChatConsoleChannelColorsPage() {
		// Use the "flat" layout
		super();
		setPreferenceStore(raptorPreferenceStore = Raptor.getInstance()
				.getPreferences());
		setTitle("Channel Message Colors");
	}

	@Override
	protected Control createContents(Composite parent) {
		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout(3, false));

		Label channelNamesLabel = new Label(parent, SWT.NONE);
		channelNamesLabel.setText("Script:");
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
		channelNameLabel.setText("Channel Name:");
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
		saveButton.setText("Save/Add Channel Color");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					int channelInt = Integer.parseInt(channelName.getText());
					if (channelInt < 0 || channelInt > 256) {
						throw new Exception();
					}
					String key = getKey("" + channelInt);
					raptorPreferenceStore.setValue(key, colorSelector
							.getColorValue());

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
							.openInformation(Raptor.getInstance()
									.getRaptorWindow().getShell(), "Alert",
									"Channel name must be an integer greater than -1 and less than 256.");
				}
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

	public String getKey(String channel) {
		return PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
				+ ChatType.CHAN_TELL + "-" + channel + "-color";
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
}