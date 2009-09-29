package raptor.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;

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
}
