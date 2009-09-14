package raptor.pref;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import raptor.gui.board.Background;
import raptor.gui.board.Set;
import raptor.service.SWTService;

class ChessBoardGraphicsPage extends FieldEditorPreferencePage {

	ImageComposite setWhiteImagesComposite = null;
	ImageComposite setBlackImagesComposite = null;
	ImageComposite backgroundImageComposite = null;

	private static class ImageComposite extends Composite {
		private Label[] imageLabels;

		public ImageComposite(Composite parent, int numImages) {
			super(parent, SWT.NONE);
			System.out.println("Creating image composite with " + numImages);
			GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = numImages;
			setLayout(gridLayout);
			imageLabels = new Label[numImages];
			for (int i = 0; i < imageLabels.length; i++) {
				imageLabels[i] = new Label(this, SWT.NONE);
			}
		}

		public void setImages(Image[] images) {
			for (int i = 0; i < imageLabels.length; i++) {
				imageLabels[i].setImage(images[i]);
			}
			pack();
		}
	}

	public ChessBoardGraphicsPage() {
		// Use the "grid" layout
		super(GRID);
		setPreferenceStore(SWTService.getInstance().getStore());
		setTitle("Chess Board Graphics");
	}

	@Override
	protected void createFieldEditors() {

		Composite parent = getFieldEditorParent();
		
		Composite needToLearnLayouts = new Composite(parent,SWT.NONE);
		needToLearnLayouts.setLayout(new GridLayout(1,false));

		String[] sets = Set.getChessSetNames();
		String[][] setNameValues = new String[sets.length][2];

		for (int i = 0; i < sets.length; i++) {
			setNameValues[i][0] = sets[i];
			setNameValues[i][1] = sets[i];
		}
		Composite setComposite = new Composite(needToLearnLayouts, SWT.BORDER
				| SWT.BORDER_SOLID);
		GridLayout gridLayout = new GridLayout(1,false);
		ComboFieldEditor setFieldEditor = new ComboFieldEditor(
				SWTService.BOARD_SET_KEY, "Chess Set", setNameValues,
				setComposite) {

			@Override
			protected void fireValueChanged(String property, Object oldValue,
					Object newValue) {
				updateSetImages((String) newValue);
				super.fireValueChanged(property, oldValue, newValue);
			}
		};
		setFieldEditor.load();
		addField(setFieldEditor);

		Composite setCompositeII = new Composite(setComposite, SWT.BORDER
				| SWT.BORDER_SOLID);
		setCompositeII.setLayout(new GridLayout(1, false));
		setWhiteImagesComposite = new ImageComposite(setCompositeII, 6);
		setBlackImagesComposite = new ImageComposite(setCompositeII, 6);
		updateSetImages(SWTService.getInstance().getChessSet().getName());

		String[] backgrounds = Background.getBackgrounds();
		String[][] backgroundNameValues = new String[backgrounds.length][2];
		for (int i = 0; i < backgrounds.length; i++) {
			backgroundNameValues[i][0] = backgrounds[i];
			backgroundNameValues[i][1] = backgrounds[i];
		}

		Composite backgroundComposite = new Composite(needToLearnLayouts, SWT.BORDER
				| SWT.BORDER_SOLID);
		GridLayout backgroundGridLayout = new GridLayout(1,false);

		backgroundComposite.setLayout(backgroundGridLayout);
		ComboFieldEditor backgroundFieldEditor = new ComboFieldEditor(
				SWTService.BOARD_BACKGROUND_KEY, "Chess Board Square",
				backgroundNameValues, backgroundComposite) {
			@Override
			protected void fireValueChanged(String property, Object oldValue,
					Object newValue) {
				updateBackgroundImages((String) newValue);
				super.fireValueChanged(property, oldValue, newValue);
			}
		};
		addField(backgroundFieldEditor);
		backgroundImageComposite = new ImageComposite(backgroundComposite, 2);
		updateBackgroundImages(SWTService.getInstance().getStore().getString(
				SWTService.BOARD_BACKGROUND_KEY));

	}

	void updateSetImages(String setName) {
		Image[] whiteImages = new Image[6];
		Image[] blackImages = new Image[6];
		Image[] stockImages = Set.getImageMolds(((String) setName));

		whiteImages[0] = stockImages[Set.WP];
		whiteImages[1] = stockImages[Set.WN];
		whiteImages[2] = stockImages[Set.WB];
		whiteImages[3] = stockImages[Set.WR];
		whiteImages[4] = stockImages[Set.WQ];
		whiteImages[5] = stockImages[Set.WK];

		blackImages[0] = stockImages[Set.BP];
		blackImages[1] = stockImages[Set.BN];
		blackImages[2] = stockImages[Set.BB];
		blackImages[3] = stockImages[Set.BR];
		blackImages[4] = stockImages[Set.BQ];
		blackImages[5] = stockImages[Set.BK];

		setWhiteImagesComposite.setImages(whiteImages);
		setBlackImagesComposite.setImages(blackImages);

	}

	void updateBackgroundImages(String backgroundName) {
		backgroundImageComposite.setImages(Background
				.getImageMolds(backgroundName));
	}

}
