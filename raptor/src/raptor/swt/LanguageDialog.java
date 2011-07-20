/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2011, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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

import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import raptor.international.L10n;

public class LanguageDialog {

    private Locale lang;

    public Locale open(Locale def) {
        Display display = new Display();
        Shell shell = new Shell(display, SWT.DIALOG_TRIM | SWT.MIN);        
        shell.setText("Language");
        shell.addListener(SWT.Close, new Listener() {

            public void handleEvent(Event event) {
                if (lang == null)
                    System.exit(0);
            }
        });
        createContents(shell, def);
        shell.pack();
        SWTUtils.center(shell);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.close();
        return lang;
    }

    protected void createContents(final Shell shell, Locale def) {
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        shell.setLayout(gridLayout);

        Label label = new Label(shell, SWT.NULL);
        label.setText("Please choose interface language: ");
        final Combo combo1 = new Combo(shell, SWT.VERTICAL | SWT.DROP_DOWN
                | SWT.BORDER | SWT.READ_ONLY);

        int n = 0;
        for (Locale loc : L10n.availableLocales) {
            combo1.add(loc.getDisplayLanguage());
            if (loc.getLanguage().equals(def.getLanguage())) {
                combo1.select(n);
            }

            n++;
        }

        Button bt = new Button(shell, SWT.PUSH);
        bt.setText("OK");
        bt.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
                false, 1, 1));
        bt.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                lang = L10n.availableLocales[combo1.getSelectionIndex()];                
                shell.close();
            }
        });
    }
}
