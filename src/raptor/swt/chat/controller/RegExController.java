package raptor.swt.chat.controller;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.connector.Connector;
import raptor.swt.RegExDialog;
import raptor.swt.chat.ChatConsoleController;
import raptor.swt.chat.ChatUtils;

public class RegExController extends ChatConsoleController {
	public static final String ADJUST_BUTTON = "";

	protected Pattern pattern;

	public RegExController(Connector connector, String regularExpression) {
		super(connector);
		pattern = Pattern.compile(regularExpression, Pattern.MULTILINE
				| Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	}

	@Override
	public String getName() {
		return pattern.pattern();
	}

	@Override
	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getCurrentLayoutQuadrant(
				REGEX_TAB_QUADRANT);
	}

	@Override
	public String getPrompt() {
		return connector.getPrompt();
	}

	@Override
	public Control getToolbar(Composite parent) {
		if (toolbar == null) {
			toolbar = new ToolBar(parent, SWT.FLAT);

			ToolItem adjustButton = new ToolItem(toolbar, SWT.FLAT);
			adjustButton.setImage(Raptor.getInstance().getIcon("wrench"));
			adjustButton
					.setToolTipText("Adjust the regular expression being used.");
			adjustButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					RegExDialog regExDialog = new RegExDialog(Raptor
							.getInstance().getRaptorWindow().getShell(),
							connector.getShortName()
									+ " Adjust regular expression dialog",
							"Enter the regular expression the new regular expression below:");
					regExDialog.setInput(pattern.pattern());
					String regEx = regExDialog.open();
					if (StringUtils.isNotBlank(regEx)) {
						chatConsole.getInputText().setText("");
						pattern = Pattern.compile(regEx, Pattern.MULTILINE
								| Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
						fireItemChanged();
						ChatUtils.appendPreviousChatsToController(chatConsole);
					}

				}
			});
			addToolItem(ADJUST_BUTTON, adjustButton);

			ToolItem saveButton = new ToolItem(toolbar, SWT.FLAT);
			saveButton.setImage(Raptor.getInstance().getIcon("save"));
			saveButton
					.setToolTipText("Save the current console text to a file.");
			saveButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					onSave();

				}
			});
			addToolItem(SAVE_BUTTON, saveButton);

			if (isSearchable()) {
				ToolItem searchButton = new ToolItem(toolbar, SWT.FLAT);
				searchButton.setImage(Raptor.getInstance().getIcon("search"));
				searchButton
						.setToolTipText("Searches backward for the message in the console text. "
								+ "The search is case insensitive and does not use regular expressions.");
				searchButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						onSearch();
					}
				});
				addToolItem(SEARCH_BUTTON, searchButton);
			}

			final ToolItem autoScroll = new ToolItem(toolbar, SWT.FLAT);
			autoScroll.setImage(Raptor.getInstance().getIcon("down"));
			autoScroll.setToolTipText("Forces auto scrolling.");
			autoScroll.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					onForceAutoScroll();

				}
			});
			addToolItem(AUTO_SCROLL_BUTTON, autoScroll);

			new ToolItem(toolbar, SWT.SEPARATOR);
		} else if (toolbar.getParent() != parent) {
			toolbar.setParent(parent);
		}

		return toolbar;

	}

	@Override
	public boolean isAcceptingChatEvent(ChatEvent event) {
		try {
			System.err.println("Match");
			return pattern.matcher(event.getMessage()).matches();
		} catch (Throwable t) {
			System.err.println("No Match");
			return false;
		}
	}

	@Override
	public boolean isAwayable() {
		return false;
	}

	@Override
	public boolean isCloseable() {
		return true;
	}

	@Override
	public boolean isPrependable() {
		return true;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}
}