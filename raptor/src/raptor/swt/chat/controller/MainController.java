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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.connector.Connector;
import raptor.script.ChatScript;
import raptor.script.ChatScript.ChatScriptType;
import raptor.service.ScriptService;
import raptor.swt.chat.ChatConsoleController;

public class MainController extends ChatConsoleController {
	public MainController(Connector connector) {
		super(connector);
	}

	@Override
	public boolean confirmClose() {
		boolean result = true;
		if (connector.isConnected()) {
			result = Raptor.getInstance().confirm(
					"Closing a main console will disconnect you from "
							+ connector.getShortName()
							+ ". Do you wish to proceed?");

			if (result) {
				connector.disconnect();
			}
		}
		return result;
	}

	@Override
	public String getName() {
		return "Main";
	}

	@Override
	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getCurrentLayoutQuadrant(
				MAIN_TAB_QUADRANT);
	}

	@Override
	public String getPrompt() {
		return connector.getPrompt();
	}

	@Override
	public boolean isAcceptingChatEvent(ChatEvent inboundEvent) {
		return true;
	}

	@Override
	public boolean isAwayable() {
		return true;
	}

	@Override
	public boolean isCloseable() {
		return true;
	}

	@Override
	public boolean isPrependable() {
		return false;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}

	/**
	 * Can be overridden to prepend items to the toolbar.
	 * 
	 * @param toolbar
	 */
	@Override
	protected void prependToolbarItems(ToolBar toolbar) {
		final ChatScript[] scripts = ScriptService.getInstance()
				.getChatScripts(connector, ChatScriptType.ToolbarOneShot);
		boolean wasScriptAdded = false;
		for (int i = 0; i < scripts.length; i++) {
			final ChatScript script = scripts[i];
			ToolItem item = new ToolItem(toolbar, SWT.PUSH);
			item.setText(script.getName());
			item.setToolTipText(script.getDescription());
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// Grab from script service to pick up changes.
					ScriptService.getInstance().getChatScript(script.getName())
							.execute(connector.getChatScriptContext());
				}
			});
			wasScriptAdded = true;
		}
		if (wasScriptAdded) {
			new ToolItem(toolbar, SWT.SEPARATOR);
		}
	}
}