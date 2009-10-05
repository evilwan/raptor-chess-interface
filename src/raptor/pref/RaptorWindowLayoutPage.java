package raptor.pref;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Quadrant;
import raptor.Raptor;

public class RaptorWindowLayoutPage extends FieldEditorPreferencePage {
	protected String layoutPrefix;
	protected static final String[][] Quadrants = {
			{ Quadrant.I.name(), Quadrant.I.name() },
			{ Quadrant.II.name(), Quadrant.II.name() },
			{ Quadrant.III.name(), Quadrant.III.name() },
			{ Quadrant.IV.name(), Quadrant.IV.name() },
			{ Quadrant.V.name(), Quadrant.V.name() },
			{ Quadrant.VI.name(), Quadrant.VI.name() },
			{ Quadrant.VII.name(), Quadrant.VII.name() },
			{ Quadrant.VIII.name(), Quadrant.VIII.name() } };

	public RaptorWindowLayoutPage(String layoutName, String layoutPrefix) {
		super(GRID);
		setTitle("Layout " + layoutName);
		setPreferenceStore(Raptor.getInstance().getPreferences());
		this.layoutPrefix = layoutPrefix;
	}

	@Override
	protected void createFieldEditors() {

		ComboFieldEditor mainQuad = new ComboFieldEditor(layoutPrefix
				+ "-main-quadrant", "Main Chat Console Quadrant:", Quadrants,
				getFieldEditorParent());
		addField(mainQuad);

		ComboFieldEditor channelQuad = new ComboFieldEditor(layoutPrefix
				+ "-channel-quadrant", "Channel Chat Console Quadrant:",
				Quadrants, getFieldEditorParent());
		addField(channelQuad);

		ComboFieldEditor personQuad = new ComboFieldEditor(layoutPrefix
				+ "-person-quadrant", "Person Chat Console Quadrant:",
				Quadrants, getFieldEditorParent());
		addField(personQuad);

		ComboFieldEditor regexQuad = new ComboFieldEditor(layoutPrefix
				+ "-regex-quadrant",
				"Regular Expression Chat Console Quadrant:", Quadrants,
				getFieldEditorParent());
		addField(regexQuad);

		ComboFieldEditor ptellQuad = new ComboFieldEditor(layoutPrefix
				+ "-partner-quadrant", "Partner Tells Chat Console Quadrant:",
				Quadrants, getFieldEditorParent());
		addField(ptellQuad);

		ComboFieldEditor gameQuad = new ComboFieldEditor(layoutPrefix
				+ "-game-quadrant",
				"Chess Game (includes Bughouse primary Board) Quadrant:",
				Quadrants, getFieldEditorParent());
		addField(gameQuad);

		ComboFieldEditor bughosueQuad = new ComboFieldEditor(layoutPrefix
				+ "-bughosue-game-2-quadrant",
				"Bughosue Game Secondary Board Quadrant:", Quadrants,
				getFieldEditorParent());
		addField(bughosueQuad);

		ComboFieldEditor internalBrowserQuad = new ComboFieldEditor(
				layoutPrefix + "-browser-quadrant",
				"Internal Web Browser Quadrant:", Quadrants,
				getFieldEditorParent());
		addField(internalBrowserQuad);

		ComboFieldEditor bughouseArena = new ComboFieldEditor(layoutPrefix
				+ "-bug-arena-quadrant", "Bughouse Arena Quadrant:", Quadrants,
				getFieldEditorParent());
		addField(bughouseArena);

		ComboFieldEditor seekGraphQuad = new ComboFieldEditor(layoutPrefix
				+ "-seek-graph-quadrant", "Seek Graph Quadrant:", Quadrants,
				getFieldEditorParent());
		addField(seekGraphQuad);

		ComboFieldEditor bugButtonQuad = new ComboFieldEditor(layoutPrefix
				+ "-seek-graph-quadrant",
				"Bughosue Communication Buttons Quadrant:", Quadrants,
				getFieldEditorParent());
		addField(bugButtonQuad);
	}
}