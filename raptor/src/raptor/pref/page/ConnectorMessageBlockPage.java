package raptor.pref.page;

import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.fields.ListFieldEditor;

public class ConnectorMessageBlockPage extends FieldEditorPreferencePage {
	protected String connectorShortName;

	public ConnectorMessageBlockPage(String connectorShortName) {
		super(GRID);
		setTitle(connectorShortName + " Message Blocks");
		setPreferenceStore(Raptor.getInstance().getPreferences());
		this.connectorShortName = connectorShortName;
	}

	@Override
	protected void createFieldEditors() {
		Label textLabel = new Label(getFieldEditorParent(), SWT.WRAP);
		textLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 2, 1));
		textLabel
				.setText(WordUtils
						.wrap(
								"\t On this page you can add regular expressions that match messages to supress. "
										+ "This is mostly used to filter out some of the superfolous messages fics sends. "
										+ "Currently the regular expressions used can not contain a comma. Changes to this list will take effect on the next reconnect.",
								70)
						+ "\n\t See Help->Raptor Help-> Regular Expressions for help with regular expressions.");

		Label label = new Label(getFieldEditorParent(), SWT.NONE);
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false,
				2, 1));

		addField(new ListFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.REGULAR_EXPRESSIONS_TO_BLOCK,
				"Regular Expressions Matching Messages To Supress:",
				getFieldEditorParent(), ',', 300));
	}
}
