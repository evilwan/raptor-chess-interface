package raptor.pref.fields;

import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.widgets.Composite;

import raptor.util.RaptorStringUtils;

public class ListFieldEditor extends ListEditor {
	char delimiter;

	public ListFieldEditor(String name, String labelText, Composite parent,
			char fieldDelimiter) {
		super(name, labelText, parent);
		delimiter = fieldDelimiter;
	}

	@Override
	protected String createList(String[] items) {
		return RaptorStringUtils.toDelimitedString(items, "" + delimiter);

	}

	@Override
	protected String getNewInputObject() {
		return "";
	}

	@Override
	protected String[] parseString(String stringList) {
		return RaptorStringUtils.stringArrayFromString(stringList, delimiter);
	}
}
