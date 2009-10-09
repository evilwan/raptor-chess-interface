package raptor.swt;

import java.io.FileReader;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import raptor.Raptor;
import raptor.chess.Game;
import raptor.chess.pgn.LenientPgnParserListener;
import raptor.chess.pgn.PgnParserError;
import raptor.chess.pgn.PgnUtils;
import raptor.chess.pgn.StreamingPgnParser;
import raptor.service.ThreadService;
import raptor.swt.chess.PgnParseResultsWindowItem;

public class PgnProcessingDialog extends Dialog {
	public class ProfressPgnParserListener extends LenientPgnParserListener {
		private ArrayList<Game> games = new ArrayList<Game>();

		private ArrayList<PgnParserError> errors = new ArrayList<PgnParserError>();

		public ProfressPgnParserListener() {
			super();
		}

		@Override
		public void errorEncountered(PgnParserError error) {
			if (isClosed) {
				throw new RuntimeException("Closed");
			} else {

				errors.add(error);
			}
		}

		@Override
		public void gameParsed(Game game, final int lineNumber) {
			if (isClosed) {
				throw new RuntimeException("Closed");
			} else {
				games.add(game);
				if (games.size() % 20 == 0) {
					shell.getDisplay().asyncExec(new Runnable() {
						public void run() {
							processMessageLabel.setText("Line number "
									+ lineNumber + " Games " + games.size()
									+ " Errors " + errors.size());
							progressBar.setSelection(games.size());
						}
					});
				}
			}
		}

		public ArrayList<PgnParserError> getErrors() {
			return errors;
		}

		public ArrayList<Game> getGames() {
			return games;
		}

	}

	private static final Log LOG = LogFactory.getLog(PgnProcessingDialog.class);;

	private Label processMessageLabel;
	private Button cancelButton;
	private Composite cancelComposite;
	private Label lineLabel;
	private Composite progressBarComposite;
	private CLabel message;

	private ProgressBar progressBar = null;
	private Shell shell;

	protected volatile boolean isClosed = false;
	protected int executeTime = 50;
	protected String processMessage = "Processing...";
	protected boolean mayCancel = true;
	protected String file;

	protected int processBarStyle = SWT.SMOOTH; // process bar style

	public PgnProcessingDialog(Shell parent, String file) {
		super(parent);
		this.file = file;
	}

	protected void cleanUp() {

	}

	protected void createContents() {
		shell = new Shell(getParent(), SWT.TITLE | SWT.PRIMARY_MODAL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 10;

		shell.setLayout(gridLayout);
		shell.setSize(483, 181);
		shell.setText("Parsing " + file);

		final Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false));
		composite.setLayout(new GridLayout());

		message = new CLabel(composite, SWT.NONE);
		message.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false));
		message.setText(processMessage);

		progressBarComposite = new Composite(shell, SWT.NONE);
		progressBarComposite.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, false, false));
		progressBarComposite.setLayout(new FillLayout());

		progressBar = new ProgressBar(progressBarComposite, processBarStyle);
		progressBar.setMaximum(PgnUtils.getApproximateGameCount(file));

		processMessageLabel = new Label(shell, SWT.NONE);
		processMessageLabel.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, false, false));
		lineLabel = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
		lineLabel.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				false, false));
		processMessageLabel.setText("                                     ");

		cancelComposite = new Composite(shell, SWT.NONE);
		cancelComposite.setLayoutData(new GridData(GridData.END,
				GridData.CENTER, false, false));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		cancelComposite.setLayout(gridLayout_1);

		cancelButton = new Button(cancelComposite, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isClosed = true;
			}
		});
		cancelButton.setLayoutData(new GridData(78, SWT.DEFAULT));
		cancelButton.setText("Cancel");
		cancelButton.setEnabled(mayCancel);

	}

	public void open() {
		createContents(); // create window
		shell.open();
		shell.layout();

		ThreadService.getInstance().scheduleOneShot(250, new Runnable() {
			public void run() {
				try {
					// start work
					StreamingPgnParser parser = new StreamingPgnParser(
							new FileReader(file), Integer.MAX_VALUE);
					ProfressPgnParserListener listener = new ProfressPgnParserListener();
					parser.addPgnParserListener(listener);

					long startTime = System.currentTimeMillis();
					parser.parse();

					if (LOG.isDebugEnabled()) {
						LOG.debug("Parsed " + listener.getGames().size()
								+ " games " + " in "
								+ (System.currentTimeMillis() - startTime)
								+ "ms");
					}

					PgnParseResultsWindowItem windowItem = new PgnParseResultsWindowItem(
							file, listener.getErrors(), listener.getGames());
					Raptor.getInstance().getRaptorWindow().addRaptorWindowItem(
							windowItem);
				} catch (Throwable t) {
					if (!isClosed) {
						LOG.error("Error parsing pgn file", t);
						Raptor.getInstance().onError(
								"Error parsing pgn file: " + file, t);
					}
				}
				shell.getDisplay().asyncExec(new Runnable() {
					public void run() {

						shell.close();
					}
				});
			}
		});

		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

	}

	public void setProcessBarStyle(boolean pStyle) {
		if (pStyle) {
			processBarStyle = SWT.SMOOTH;
		} else {
			processBarStyle = SWT.NONE;
		}

	}
}
