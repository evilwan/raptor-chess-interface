package raptor.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;

public class SWTUtils {

	public static FillLayout createMarginlessFillLayout() {
		FillLayout result = new FillLayout();
		result.marginHeight = 0;
		result.marginWidth = 0;
		result.spacing = 0;
		result.type = SWT.HORIZONTAL;
		return result;
	}

	public static GridLayout createMarginlessGridLayout(int columns,
			boolean areColumnsSameSize) {
		GridLayout result = new GridLayout(columns, areColumnsSameSize);
		result.marginLeft = 0;
		result.marginTop = 0;
		result.marginRight = 0;
		result.marginBottom = 0;
		result.horizontalSpacing = 0;
		result.verticalSpacing = 0;
		result.marginHeight = 0;
		result.marginWidth = 0;
		return result;
	}

	public static Font getProportionalFont(Display display, Font oldFont,
			double resizePercentage, int controlHeight) {
		return new Font(display, oldFont.getFontData()[0].getName(),
				(int) (controlHeight * resizePercentage),
				oldFont.getFontData()[0].getStyle());
	}

}
