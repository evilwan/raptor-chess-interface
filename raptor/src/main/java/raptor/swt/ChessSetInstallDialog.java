/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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
import raptor.international.L10n;
import raptor.service.ThreadService;
import raptor.util.RaptorLogger;
import raptor.util.RaptorRunnable;
import raptor.util.RaptorStringTokenizer;

/**
 * THe chess set installation dialog.
 */
public class ChessSetInstallDialog extends Dialog {

	public static final String[] VALID_FILES = { "license.txt", "author.txt",
			"definition", "wp.png", "wb.png", "wn.png", "wr.png", "wq.png",
			"wk.png", "bp.png", "bb.png", "bn.png", "br.png", "bq.png",
			"bk.png", "wp.svg", "wb.svg", "wn.svg", "wr.svg", "wq.svg",
			"wk.svg", "bp.svg", "bb.svg", "bn.svg", "br.svg", "bq.svg",
			"bk.svg" };
	static {
		Arrays.sort(VALID_FILES);
	}

	private static final RaptorLogger LOG = RaptorLogger
			.getLog(ChessSetInstallDialog.class);

	protected static L10n local = L10n.getInstance();
	private Button cancelButton;
	private Composite cancelComposite;
	protected int executeTime = 50;
	protected File file;
	protected volatile boolean isClosed = false;
	private Label lineLabel;

	protected boolean mayCancel = true;
	private CLabel message;

	protected int processBarStyle = SWT.SMOOTH; // process bar style
	protected String processMessage = local.getString("chessSetID1");
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
			public void run() {
				try {
					// start work
					ZipFile zipFile = new ZipFile(file);

					int entryCounter = 0;
					Raptor.getInstance().getDisplay()
							.asyncExec(new RaptorRunnable() {
								@Override
								public void execute() {
									processMessageLabel
											.setText(local.getString("chessSetID2"));
									shell.layout(true, true);
								}
							});

					Enumeration<? extends ZipEntry> entries = zipFile.entries();
					boolean isValid = true;
					String invalidEntryName = "";
					boolean isJinSet = false;

					while (entries.hasMoreElements()) {
						ZipEntry entry = (ZipEntry) entries.nextElement();
						RaptorStringTokenizer tok = new RaptorStringTokenizer(
								entry.getName(), "/\\");
						try {
							Integer.parseInt(tok.nextToken());
							isJinSet = true;
						} catch (NumberFormatException nfe) {
						}
					}

					entries = zipFile.entries();

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
						Raptor.getInstance().getDisplay()
								.asyncExec(new RaptorRunnable() {
									@Override
									public void execute() {
										processMessageLabel
												.setText(local.getString("chessSetID3")
														+ finalInvalidEntryName);
										cancelButton.setVisible(true);
										cancelButton.setText(local.getString("chessSetID4"));
										shell.layout(true, true);
									}
								});
					} else {
						final int finalEntryCounter = entryCounter;
						Raptor.getInstance().getDisplay()
								.asyncExec(new RaptorRunnable() {
									@Override
									public void execute() {
										progressBar
												.setMaximum(finalEntryCounter);
									}
								});

						entries = zipFile.entries();
						while (!isClosed && entries.hasMoreElements()) {
							unzipEntry(zipFile,
									(ZipEntry) entries.nextElement(), isJinSet);
						}

						Raptor.getInstance().getDisplay()
								.asyncExec(new RaptorRunnable() {
									@Override
									public void execute() {
										processMessageLabel
												.setText(local.getString("chessSetID5"));
										cancelButton.setVisible(true);
										cancelButton.setText(local.getString("chessSetID4"));
										shell.layout(true, true);
									}
								});
					}
				} catch (Throwable t) {
					if (!isClosed) {
						LOG.error("Error installing chess set", t);
						Raptor.getInstance().onError(
								local.getString("chessSetID6") + file,
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
		shell.setText(local.getString("chessSetID7") + file.getName());

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
		cancelButton.setText(local.getString("chessSetID8"));
		cancelButton.setEnabled(mayCancel);
		cancelButton.setVisible(false);
		
		SWTUtils.center(shell);
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

	private String getJinSetName(ZipFile zipFile) {
		String name = zipFile.getName();
		RaptorStringTokenizer tok = new RaptorStringTokenizer(name, "./\\");
		String lastToken = null;
		while (tok.hasMoreTokens()) {
			String currentToken = tok.nextToken();
			if (tok.hasMoreTokens()) {
				lastToken = currentToken;
			}
		}
		return lastToken;
	}

	protected void unzipEntry(final ZipFile zipFile, final ZipEntry zipEntry,
			boolean isJinSet) throws IOException {
		if (isClosed) {
        } else {
			if (zipEntry.isDirectory()) {
				Raptor.getInstance().getDisplay()
						.asyncExec(new RaptorRunnable() {
							@Override
							public void execute() {
								processMessageLabel
										.setText(local.getString("chessSetID9")
												+ zipEntry.getName());
								progressBar.setSelection(++unzipCounter);
							}
						});
				if (isJinSet) {
					(new File(Raptor.RESOURCES_DIR + "/set/"
							+ getJinSetName(zipFile) + "/" + zipEntry.getName()))
							.mkdirs();
				} else {
					(new File(Raptor.RESOURCES_DIR + "/set/"
							+ zipEntry.getName())).mkdirs();
				}

			} else {
				Raptor.getInstance().getDisplay()
						.asyncExec(new RaptorRunnable() {
							@Override
							public void execute() {
								processMessageLabel.setText(local.getString("chessSetID10")
										+ zipEntry.getName());
								progressBar.setSelection(++unzipCounter);
							}
						});


				if (isJinSet) {
					copyInputStream(zipFile.getInputStream(zipEntry),
							new BufferedOutputStream(new FileOutputStream(
									Raptor.RESOURCES_DIR + "/set/"
											+ getJinSetName(zipFile) + "/"
											+ zipEntry.getName())));
				} else {
					copyInputStream(
							zipFile.getInputStream(zipEntry),
							new BufferedOutputStream(new FileOutputStream(
									Raptor.RESOURCES_DIR + "/set/"
											+ zipEntry.getName())));
				}

			}
		}
	}

}