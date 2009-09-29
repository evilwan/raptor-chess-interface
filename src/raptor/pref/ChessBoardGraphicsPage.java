package raptor.pref;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import raptor.Raptor;
import raptor.swt.chess.BoardUtils;

class ChessBoardGraphicsPage extends FieldEditorPreferencePage {

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

	ImageComposite setWhiteImagesComposite = null;
	ImageComposite setBlackImagesComposite = null;

	ImageComposite backgroundImageComposite = null;

	public ChessBoardGraphicsPage() {
		// Use the "grid" layout
		super(GRID);
		setPreferenceStore(Raptor.getInstance().getPreferences());
		setTitle("Chess Board Graphics");
	}

	@Override
	protected void createFieldEditors() {

		Composite parent = getFieldEditorParent();

		Composite needToLearnLayouts = new Composite(parent, SWT.NONE);
		needToLearnLayouts.setLayout(new GridLayout(1, false));

		String[] sets = BoardUtils.getChessSetNames();
		String[][] setNameValues = new String[sets.length][2];

		for (int i = 0; i < sets.length; i++) {
			setNameValues[i][0] = sets[i];
			setNameValues[i][1] = sets[i];
		}
		Composite setComposite = new Composite(needToLearnLayouts, SWT.BORDER
				| SWT.BORDER_SOLID);
		ComboFieldEditor setFieldEditor = new ComboFieldEditor(
				PreferenceKeys.BOARD_CHESS_SET_NAME, "Chess Set",
				setNameValues, setComposite) {

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
		updateSetImages(Raptor.getInstance().getPreferences().getString(
				PreferenceKeys.BOARD_CHESS_SET_NAME));

		String[] backgrounds = BoardUtils.getSquareBackgroundNames();
		String[][] backgroundNameValues = new String[backgrounds.length][2];
		for (int i = 0; i < backgrounds.length; i++) {
			backgroundNameValues[i][0] = backgrounds[i];
			backgroundNameValues[i][1] = backgrounds[i];
		}

		Composite backgroundComposite = new Composite(needToLearnLayouts,
				SWT.BORDER | SWT.BORDER_SOLID);
		GridLayout backgroundGridLayout = new GridLayout(1, false);

		backgroundComposite.setLayout(backgroundGridLayout);
		ComboFieldEditor backgroundFieldEditor = new ComboFieldEditor(
				PreferenceKeys.BOARD_SQUARE_BACKGROUND_NAME,
				"Chess Board Square", backgroundNameValues, backgroundComposite) {
			@Override
			protected void fireValueChanged(String property, Object oldValue,
					Object newValue) {
				updateBackgroundImages((String) newValue);
				super.fireValueChanged(property, oldValue, newValue);
			}
		};
		addField(backgroundFieldEditor);
		backgroundImageComposite = new ImageComposite(backgroundComposite, 2);
		updateBackgroundImages(Raptor.getInstance().getPreferences().getString(
				PreferenceKeys.BOARD_SQUARE_BACKGROUND_NAME));

	}

	void updateBackgroundImages(String backgroundName) {
		backgroundImageComposite.setImages(new Image[] {
				BoardUtils.getSquareBackgroundMold(backgroundName, false),
				BoardUtils.getSquareBackgroundMold(backgroundName, true) });
	}

	void updateSetImages(String setName) {
		Image[] whiteImages = new Image[6];
		Image[] blackImages = new Image[6];

		// whiteImages[0] = ChessBoardResources.getChessPieceImageMold(setName,
		// GameConstants.WP);
		// whiteImages[1] = ChessBoardResources.getChessPieceImageMold(setName,
		// GameConstants.WN);
		// whiteImages[2] = ChessBoardResources.getChessPieceImageMold(setName,
		// GameConstants.WB);
		// whiteImages[3] = ChessBoardResources.getChessPieceImageMold(setName,
		// GameConstants.WR);
		// whiteImages[4] = ChessBoardResources.getChessPieceImageMold(setName,
		// GameConstants.WQ);
		// whiteImages[5] = ChessBoardResources.getChessPieceImageMold(setName,
		// GameConstants.WK);
		//
		// blackImages[0] = ChessBoardResources.getChessPieceImageMold(setName,
		// GameConstants.BP);
		// blackImages[1] = ChessBoardResources.getChessPieceImageMold(setName,
		// GameConstants.BN);
		// blackImages[2] = ChessBoardResources.getChessPieceImageMold(setName,
		// GameConstants.BB);
		// blackImages[3] = ChessBoardResources.getChessPieceImageMold(setName,
		// GameConstants.BR);
		// blackImages[4] = ChessBoardResources.getChessPieceImageMold(setName,
		// GameConstants.BQ);
		// blackImages[5] = ChessBoardResources.getChessPieceImageMold(setName,
		// GameConstants.BK);

		setWhiteImagesComposite.setImages(whiteImages);
		setBlackImagesComposite.setImages(blackImages);

	}

}
