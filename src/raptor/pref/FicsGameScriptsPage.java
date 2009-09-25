package raptor.pref;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import raptor.Raptor;
import raptor.script.GameScript;

public class FicsGameScriptsPage extends PreferencePage {
	Composite parent;
	Combo scriptName;
	Composite scriptNameComposite;
	Text name;
	Text description;
	Text script;
	Button isAvailableInExamineState;
	Button isAvailableInPlayingState;
	Button isAvailableInObserveState;
	Button isAvailableInSetupState;
	Button isAvailableInFreeFormState;
	Button save;
	Button delete;
	Button test;

	public FicsGameScriptsPage() {
		// Use the "flat" layout
		super();
		setPreferenceStore(Raptor.getInstance().getPreferences());
		setTitle("Fics Script Editor");
	}

	@Override
	protected Control createContents(Composite arg0) {
		parent = new Composite(arg0, SWT.NONE);
		parent.setLayout(new GridLayout(1, false));

		scriptNameComposite = new Composite(parent, SWT.NONE);
		scriptNameComposite.setLayout(new RowLayout());
		scriptNameComposite
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label scriptNameLabel = new Label(scriptNameComposite, SWT.NONE);
		scriptNameLabel.setText("Script:");
		scriptName = new Combo(scriptNameComposite, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		scriptName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GameScript gameScript = Raptor.getInstance().getFicsConnector()
						.getGameScript(
								scriptName.getItem(scriptName
										.getSelectionIndex()));
				description.setText(gameScript.getDescription());
				script.setText(gameScript.getScript());
				name.setText(gameScript.getName());
				isAvailableInExamineState.setSelection(gameScript
						.isAvailableInExamineState());
				isAvailableInPlayingState.setSelection(gameScript
						.isAvailableInPlayingState());
				isAvailableInObserveState.setSelection(gameScript
						.isAvailableInObserveState());
				isAvailableInSetupState.setSelection(gameScript
						.isAvailableInSetupState());
				isAvailableInFreeFormState.setSelection(gameScript
						.isAvailableInFreeformState());
			}
		});

		Composite nameComposite = new Composite(parent, SWT.NONE);
		nameComposite.setLayout(new GridLayout(2, false));
		nameComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setText("Name:");
		name = new Text(nameComposite, SWT.BORDER | SWT.SINGLE);
		name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite descriptionComposite = new Composite(parent, SWT.NONE);
		descriptionComposite.setLayout(new GridLayout(2, false));
		descriptionComposite.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		Label descriptionLabel = new Label(descriptionComposite, SWT.NONE);
		descriptionLabel.setText("Description:");
		description = new Text(descriptionComposite, SWT.BORDER);
		description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		isAvailableInExamineState = new Button(parent, SWT.CHECK);
		isAvailableInExamineState.setText("Display in Examine Mode");
		isAvailableInPlayingState = new Button(parent, SWT.CHECK);
		isAvailableInPlayingState.setText("Display in Playing Mode");
		isAvailableInObserveState = new Button(parent, SWT.CHECK);
		isAvailableInObserveState.setText("Display in Observing Mode");
		isAvailableInSetupState = new Button(parent, SWT.CHECK);
		isAvailableInSetupState.setText("Display in Setup Mode");
		isAvailableInFreeFormState = new Button(parent, SWT.CHECK);
		isAvailableInFreeFormState.setText("Display in Free Form Mode");

		Label scriptLabel = new Label(parent, SWT.NONE | SWT.SINGLE);
		scriptLabel.setText("Script:");
		script = new Text(parent, SWT.BORDER | SWT.MULTI);
		script.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(new RowLayout());
		save = new Button(buttonComposite, SWT.PUSH);
		save.setText("Save");
		save.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String nameString = name.getText();
				GameScript gameScript = Raptor.getInstance().getFicsConnector()
						.getGameScript(nameString);
				if (gameScript != null) {
					gameScript.setDescription(description.getText());
					gameScript.setName(name.getText());
					gameScript.setScript(script.getText());
					gameScript
							.setAvailableInExamineState(isAvailableInExamineState
									.getSelection());
					gameScript
							.setAvailableInFreeformState(isAvailableInFreeFormState
									.getSelection());
					gameScript
							.setAvailableInObserveState(isAvailableInObserveState
									.getSelection());
					gameScript
							.setAvailableInPlayingState(isAvailableInPlayingState
									.getSelection());
					gameScript.setAvailableInSetupState(isAvailableInSetupState
							.getSelection());
					gameScript.save();
					Raptor.getInstance().getFicsConnector()
							.refreshGameScripts();
					populateScriptNames();

				} else {
					gameScript = new GameScript();
					gameScript.setConnector(Raptor.getInstance()
							.getFicsConnector());
					gameScript.setDescription(description.getText());
					gameScript.setName(name.getText());
					gameScript.setScript(script.getText());
					gameScript
							.setAvailableInExamineState(isAvailableInExamineState
									.getSelection());
					gameScript
							.setAvailableInFreeformState(isAvailableInFreeFormState
									.getSelection());
					gameScript
							.setAvailableInObserveState(isAvailableInObserveState
									.getSelection());
					gameScript
							.setAvailableInPlayingState(isAvailableInPlayingState
									.getSelection());
					gameScript.setAvailableInSetupState(isAvailableInSetupState
							.getSelection());
					gameScript.save();
					Raptor.getInstance().getFicsConnector()
							.refreshGameScripts();
					populateScriptNames();
				}
			}
		});
		delete = new Button(buttonComposite, SWT.PUSH);
		delete.setText("Delete");
		delete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String nameString = name.getText();
				GameScript gameScript = Raptor.getInstance().getFicsConnector()
						.getGameScript(nameString);
				if (gameScript != null) {
					Raptor.getInstance().getFicsConnector().removeGameScript(
							gameScript);
					Raptor.getInstance().getFicsConnector()
							.refreshGameScripts();
					populateScriptNames();

				}
			}
		});

		populateScriptNames();
		return parent;
	}

	public void populateScriptNames() {
		scriptName.removeAll();
		GameScript[] scripts = Raptor.getInstance().getFicsConnector()
				.getGameScripts();
		for (int i = 0; i < scripts.length; i++) {
			scriptName.add(scripts[i].getName());
		}
		scriptNameComposite.pack();
		scriptNameComposite.layout();
	}

}
