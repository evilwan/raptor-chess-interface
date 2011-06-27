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
        Shell shell = new Shell(display);
        shell.setText("Language");
        shell.addListener(SWT.Close, new Listener() {

            public void handleEvent(Event event) {
                if (lang == null)
                    System.exit(0);
            }
        });
        createContents(shell, def);
        shell.setSize(370, 100);
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
