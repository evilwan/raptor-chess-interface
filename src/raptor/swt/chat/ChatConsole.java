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
package raptor.swt.chat;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.swt.RaptorStyledText;

/**
 * The ChatConsole GUI control. All ChatConsoles have a controller that manages
 * sending/receiving of text to and from a connector.
 */
public class ChatConsole extends Composite implements PreferenceKeys {

	static final Log LOG = LogFactory.getLog(ChatConsole.class);

	protected Composite buttonComposite;
	protected ChatConsoleController controller;
	protected StyledText inputText;

	// There is a good reason this is not a StyledText.
	// Making it a regular Text fixed some issues around focus and forwarding
	// characters.
	protected Text outputText;
	protected Label promptLabel;
	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().startsWith("chat")) {
				updateFromPrefs();
				redraw();
			}
		}
	};

	protected Composite southControlsComposite;

	public ChatConsole(Composite parent, int style) {
		super(parent, style);
	}

	public void createControls() {

		addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				if (propertyChangeListener != null) {
					Raptor.getInstance().getPreferences()
							.removePropertyChangeListener(
									propertyChangeListener);
					propertyChangeListener = null;
				}
				if (controller != null) {
					controller.dispose();
				}
				LOG.info("Disposed chat console.");
			}
		});
		setLayout(new GridLayout(1, true));

		Raptor.getInstance().getPreferences().addPropertyChangeListener(
				propertyChangeListener);
		inputText = new RaptorStyledText(this, SWT.V_SCROLL | SWT.MULTI
				| SWT.BORDER);
		inputText.setLayoutData(new GridData(GridData.FILL_BOTH));
		inputText.setEditable(false);
		inputText.setWordWrap(true);

		southControlsComposite = new Composite(this, SWT.NONE);
		southControlsComposite.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.marginBottom = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginRight = 0;
		gridLayout.marginLeft = 0;
		gridLayout.marginTop = 0;
		southControlsComposite.setLayout(gridLayout);

		promptLabel = new Label(southControlsComposite, SWT.NONE);
		promptLabel.setText(controller.getPrompt());

		outputText = new Text(southControlsComposite, SWT.SINGLE | SWT.BORDER);
		outputText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		outputText.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event e) {
				String string = e.text;
				if (e.text.contains("\n") || e.text.contains("\r")) {
					e.doit = false;
					outputText.insert(StringUtils.replaceChars(string, "\n\r",
							""));
				}
			}
		});
		
		
		outputText.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				outputText.setFocus();
			}

			public void widgetSelected(SelectionEvent e) {
			}
		});

		buttonComposite = new Composite(southControlsComposite, SWT.NONE);
		RowLayout rowLayout = new RowLayout();
		rowLayout.marginBottom = 0;
		rowLayout.marginHeight = 0;
		rowLayout.marginRight = 0;
		rowLayout.marginLeft = 0;
		rowLayout.marginTop = 0;
		rowLayout.pack = false;
		rowLayout.wrap = false;
		buttonComposite.setLayout(rowLayout);
		addButtons();

		updateFromPrefs();
	}

	public ChatConsoleController getController() {
		return controller;
	}

	public StyledText getInputText() {
		return inputText;
	}

	public Text getOutputText() {
		return outputText;
	}

	public void setController(ChatConsoleController controller) {
		this.controller = controller;
	}

	public void updateFromPrefs() {
		RaptorPreferenceStore prefs = getPreferences();
		Color consoleBackground = prefs.getColor(CHAT_CONSOLE_BACKGROUND_COLOR);

		buttonComposite.setBackground(consoleBackground);
		southControlsComposite.setBackground(consoleBackground);
		setBackground(consoleBackground);

		inputText.setBackground(prefs.getColor(CHAT_INPUT_BACKGROUND_COLOR));
		inputText.setForeground(prefs.getColor(CHAT_INPUT_DEFAULT_TEXT_COLOR));
		inputText.setFont(prefs.getFont(CHAT_INPUT_FONT));

		outputText.setBackground(prefs.getColor(CHAT_OUTPUT_BACKGROUND_COLOR));
		outputText.setForeground(prefs.getColor(CHAT_OUTPUT_TEXT_COLOR));
		outputText.setFont(prefs.getFont(CHAT_OUTPUT_FONT));

		promptLabel.setFont(prefs.getFont(CHAT_PROMPT_FONT));
		promptLabel.setForeground(prefs.getColor(CHAT_PROMPT_COLOR));
		promptLabel
				.setBackground(prefs.getColor(CHAT_CONSOLE_BACKGROUND_COLOR));
	}

	protected void addButtons() {
		Button sendButton = new Button(buttonComposite, SWT.FLAT);
		sendButton.setImage(Raptor.getInstance().getIcon("enter"));
		sendButton.setToolTipText("Sends the message in the input field.");
		sendButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				controller.onSendOutputText();
			}
		});
	}

	protected RaptorPreferenceStore getPreferences() {
		return Raptor.getInstance().getPreferences();
	}

}
