package raptor.layout;

import java.util.Arrays;
import java.util.Comparator;

public class LayoutUtils {
	public static Layout[] getBughouseLayouts() {
		Layout[] result = new Layout[] { new BughouseButtonsFarLeftLayout(),
				new BughouseButtonsLeftLayout(),
				new BughouseButtonsMiddleLayout(),
				new BughouseButtonsRightLayout(),
				new BughouseButtonsTopLayout() };
		Arrays.sort(result, new Comparator<Layout>() {

			@Override
			public int compare(Layout o1, Layout o2) {
				// TODO Auto-generated method stub
				return o1.getName().compareTo(o2.getName());
			}
		});
		return result;
	}

	public static Layout[] getLayouts() {
		Layout[] result = new Layout[] { new ChatOnLeftLayout(),
				new ChatOnRightSplitPaneChat(), new ChatOnRightLayout(),
				new ClassicLayout(), new ClassicSplitPaneChatLayout() };
		Arrays.sort(result, new Comparator<Layout>() {

			@Override
			public int compare(Layout o1, Layout o2) {
				// TODO Auto-generated method stub
				return o1.getName().compareTo(o2.getName());
			}
		});
		return result;
	}
}
