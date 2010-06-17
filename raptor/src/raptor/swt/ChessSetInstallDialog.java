/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.swt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import raptor.util.RaptorLogger;
 
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
import raptor.chess.pgn.PgnUtils;
import raptor.service.ThreadService;
import raptor.util.RaptorRunnable;
import raptor.util.RaptorStringTokenizer;

/**
 * THe chess set installation dialog.
 */
public class ChessSetInstallDialog extends Dialog {

	public static final String[] VALID_FILES = { "license.txt", "author.txt",
			"wp.png", "wb.png", "wn.png", "wr.png", "wq.png", "wk.png",
			"bp.png", "bb.png", "bn.png", "br.png", "bq.png", "bk.png",
			"wp.svg", "wb.svg", "wn.svg", "wr.svg", "wq.svg", "wk.svg",
			"bp.svg", "bb.svg", "bn.svg", "br.svg", "bq.svg", "bk.svg" };
	static {
		Arrays.sort(VALID_FILES);
	}

	private static final RaptorLogger LOG = RaptorLogger.getLog(ChessSetInstallDialog.class);

	private Button cancelButton;
	private Composite cancelComposite;
	protected int executeTime = 50;
	protected File file;
	protected volatile boolean isClosed = false;
	private Label lineLabel;

	protected boolean mayCancel = true;
	private CLabel message;

	protected int processBarStyle = SWT.SMOOTH; // process bar style
	protected String processMessage = "Processing...";
	protected int unzipCounter = 0;
	private Label processMessageLabel;
	private ProgressBar progressBar = null;
	private Composite progressBarComposite;

	private Shell shell;

	public ChessSetInstallDialog(Shell parent, String file) {
		super(parent);
		this.file = new File(file);
	}

	public void open() {
		createContents(); // create window
		shell.open();
		shell.layout();

		ThreadService.getInstance().scheduleOneShot(250, new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				try {
					// start work
					ZipFile zipFile = new ZipFile(file);

					int entryCounter = 0;
					Raptor.getInstance().getDisplay().asyncExec(
							new RaptorRunnable() {
								@Override
								public void execute() {
									processMessageLabel
											.setText("Verifying zip file...");
									shell.layout(true, true);
								}
							});

					Enumeration entries = zipFile.entries();
					boolean isValid = true;
					String invalidEntryName = "";
					while (!isClosed && entries.hasMoreElements()) {
						ZipEntry entry = (ZipEntry) entries.nextElement();

						if (!entry.isDirectory()) {
							String entryName = entry.getName();
							String fileName = "";
							RaptorStringTokenizer tok = new RaptorStringTokenizer(
									entryName, "\\/", true);
							while (tok.hasMoreTokens()) {
								fileName = tok.nextToken();
							}
							if (Arrays.binarySearch(VALID_FILES, fileName) == -1) {
								isValid = false;
								invalidEntryName = entry.getName();
							}
						}
						entryCounter++;
					}

					if (!isValid) {
						final String finalInvalidEntryName = invalidEntryName;
						Raptor.getInstance().getDisplay().asyncExec(
								new RaptorRunnable() {
									@Override
									public void execute() {
										processMessageLabel
												.setText("Installiation failed: Invalid chess set file\n"
														+ finalInvalidEntryName);
										cancelButton.setVisible(true);
										cancelButton.setText("Close");
										shell.layout(true, true);
									}
								});
					} else {
						final int finalEntryCounter = entryCounter;
						Raptor.getInstance().getDisplay().asyncExec(
								new RaptorRunnable() {
									@Override
									public void execute() {
										progressBar
												.setMaximum(finalEntryCounter);
									}
								});

						entries = zipFile.entries();
						while (!isClosed && entries.hasMoreElements()) {
							unzipEntry(zipFile, (ZipEntry) entries
									.nextElement());
						}

						Raptor.getInstance().getDisplay().asyncExec(
								new RaptorRunnable() {
									@Override
									public void execute() {
										processMessageLabel
												.setText("Installation Complete");
										cancelButton.setVisible(true);
										cancelButton.setText("Close");
										shell.layout(true, true);
									}
								});
					}
				} catch (Throwable t) {
					if (!isClosed) {
						LOG.error("Error installing chess set", t);
						Raptor.getInstance().onError(
								"Error installing chess set from zip: " + file,
								t);
					}
				} finally {
				}
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

	protected void cleanUp() {

	}

	protected void createContents() {
		shell = new Shell(getParent(), SWT.TITLE | SWT.PRIMARY_MODAL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 10;

		shell.setLayout(gridLayout);
		shell.setSize(483, 181);
		shell.setText("Installing chess set(s) from " + file.getName());

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
		progressBar.setMaximum(PgnUtils.getApproximateGameCount(file
				.getAbsolutePath()));

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
				shell.dispose();
			}
		});
		cancelButton.setLayoutData(new GridData(78, SWT.DEFAULT));
		cancelButton.setText("Cancel");
		cancelButton.setEnabled(mayCancel);
		cancelButton.setVisible(false);
	}

	protected void copyInputStream(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}

	protected void unzipEntry(final ZipFile zipFile, final ZipEntry zipEntry)
			throws IOException {
		if (isClosed) {
			return;
		} else {
			if (zipEntry.isDirectory()) {
				Raptor.getInstance().getDisplay().asyncExec(
						new RaptorRunnable() {
							@Override
							public void execute() {
								processMessageLabel
										.setText("Extracting directory: "
												+ zipEntry.getName());
								progressBar.setSelection(++unzipCounter);
							}
						});
				(new File(Raptor.RESOURCES_DIR + "/set/" + zipEntry.getName()))
						.mkdirs();
			} else {
				Raptor.getInstance().getDisplay().asyncExec(
						new RaptorRunnable() {
							@Override
							public void execute() {
								processMessageLabel.setText("Extracting file: "
										+ zipEntry.getName());
								progressBar.setSelection(++unzipCounter);
							}
						});
				(new File(zipEntry.getName())).mkdirs();
				copyInputStream(zipFile.getInputStream(zipEntry),
						new BufferedOutputStream(new FileOutputStream(
								Raptor.RESOURCES_DIR + "/set/"
										+ zipEntry.getName())));
			}
		}
	}

}
