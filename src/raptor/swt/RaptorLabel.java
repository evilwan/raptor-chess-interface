package raptor.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class RaptorLabel extends Composite {
	protected CLabel label;

	public RaptorLabel(Composite parent, int labelStyle) {
		this(parent, labelStyle, SWT.CENTER);
	}

	public RaptorLabel(Composite parent, int labelStyle, int verticalAlignment) {
		super(parent, SWT.NONE);
		setLayout(SWTUtils.createMarginlessGridLayout(1, true));
		label = new CLabel(this, labelStyle);
		label.setLayoutData(new GridData(SWT.FILL, verticalAlignment, true,
				true));
	}

	@Override
	public void addMouseListener(MouseListener listener) {
		label.addMouseListener(listener);
	}

	public CLabel getLabel() {
		return label;
	}

	public String getText() {
		return label.getText();
	}

	@Override
	public void removeMouseListener(MouseListener listener) {
		label.removeMouseListener(listener);
	}

	public void setAlignment(int style) {
		label.setAlignment(style);
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		label.setBackground(color);
	}

	@Override
	public void setFont(Font font) {
		super.setFont(font);
		label.setFont(font);
	}

	@Override
	public void setForeground(Color color) {
		super.setForeground(color);
		label.setForeground(color);
	}

	public void setImage(Image image) {
		label.setImage(image);
	}

	public void setLabel(CLabel label) {
		this.label = label;
	}

	public void setText(String text) {
		label.setText(text);
	}

}
