package raptor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class Test14 {
	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("Change Cursor");
		shell.setSize(320, 150);
		shell.open();

		final Cursor cursor1 = new Cursor(display, SWT.CURSOR_HAND);
		final Button b1 = new Button(shell, SWT.PUSH);
		b1.setBounds(40, 50, 80, 20);
		b1.setText("Hand Cursor");
		b1.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				b1.setCursor(cursor1);
			}
		});
		Color white = display.getSystemColor(SWT.COLOR_WHITE);
		Color black = display.getSystemColor(SWT.COLOR_BLACK);
		PaletteData palette = new PaletteData(new RGB[] { white.getRGB(),
				black.getRGB(), });
		ImageData sourceData = new ImageData(20, 20, 1, palette);
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 20; j++) {
				sourceData.setPixel(i, j, 1);
			}
		}
		final Cursor cursor2 = new Cursor(display, sourceData, 10, 10);
		@SuppressWarnings("unused")
		final Image source = new Image(display, sourceData);
		final Button b2 = new Button(shell, SWT.PUSH);
		b2.setBounds(120, 50, 80, 20);
		b2.setText("Source Cursor");
		b2.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				b2.setCursor(cursor2);
			}
		});
		final Cursor[] cursor3 = new Cursor[1];
		final Button b3 = new Button(shell, SWT.PUSH);
		b3.setBounds(200, 50, 80, 20);
		b3.setText("Image Cursor");
		b3.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				FileDialog fileDialog = new FileDialog(shell);
				fileDialog.setFilterExtensions(new String[] { "*.ico", "*.gif",
						"*.jpg", "*.bmp" });
				String image = fileDialog.open();
				if (image == null)
					return;
				ImageData imageData = new ImageData(image);
				Cursor cursor = cursor3[0];
				cursor3[0] = new Cursor(display, imageData, 0, 0);
				shell.setCursor(cursor3[0]);
				if (cursor != null)
					cursor.dispose();
			}
		});
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		cursor1.dispose();
		cursor2.dispose();
		cursor3[0].dispose();
		display.dispose();
	}

}
