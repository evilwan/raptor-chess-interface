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
			addToolItem(ToolBarItemKey.ADJUST_BUTTON, adjustButton);

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
			addToolItem(ToolBarItemKey.SAVE_BUTTON, saveButton);

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
				addToolItem(ToolBarItemKey.SEARCH_BUTTON, searchButton);
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
			addToolItem(ToolBarItemKey.AUTO_SCROLL_BUTTON, autoScroll);

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