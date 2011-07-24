/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.swt.chess.analysis;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import raptor.Raptor;
import raptor.swt.SWTUtils;
import raptor.swt.chess.controller.AutomaticAnalysisController;
import raptor.swt.chess.controller.InactiveController;

public class AutomaticAnalysisDialog extends Dialog {
	
	private InactiveController controller;

	public AutomaticAnalysisDialog(InactiveController controller) {
		super(Raptor.getInstance().getWindow().getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.controller = controller;
		setText("Automatic Engine Analysis");
	}
	
	public void open() {
		// Create the dialog window
		Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		createContents(shell);
		shell.pack();
		SWTUtils.center(shell);
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	protected void createContents(final Shell parent) {
		final Label timePerMove, threshold;
		final Button start, cancel;
		final Spinner timeSpinner, thresholdSpinner;
		
		parent.setLayout(new FillLayout());
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		timePerMove = new Label(composite, SWT.NONE);
		timePerMove.setText("Time per move (seconds): ");
		timeSpinner = new Spinner(composite, SWT.NONE);
		timeSpinner.setSelection(1);
		timeSpinner.setMinimum(1);
		threshold = new Label(composite, SWT.NONE);
		threshold.setText("Blunder threshold (pawns): ");
		thresholdSpinner = new Spinner(composite, SWT.NONE);
		thresholdSpinner.setDigits(2);
		thresholdSpinner.setMinimum(1);
		thresholdSpinner.setMaximum(10000);
		thresholdSpinner.setIncrement(1);
		thresholdSpinner.setSelection(100);
		start = new Button(composite, SWT.PUSH);
		start.setText("Start");
		start.addSelectionListener(new SelectionListener()  {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				new AutomaticAnalysisController(controller).startAnalysis(timeSpinner.getSelection(), 
						thresholdSpinner.getSelection());
				parent.close();
			}

		});
		cancel = new Button(composite, SWT.PUSH);
		cancel.setText("Cancel");
	}

}
