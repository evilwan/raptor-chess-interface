package raptor.swt.chat;

public class ChatConsoles {// extends Composite {
// static final Log LOG = LogFactory.getLog(ChatConsoles.class);
// public SashForm sashForm;
// protected CTabFolder topFolder;
// protected CTabFolder bottomFolder;
//
// public ChatConsoles(Composite parent, int style) {
// super(parent, style);
// setLayout(new GridLayout());
//
// sashForm = new SashForm(this, SWT.VERTICAL | SWT.SMOOTH);
// sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//
// topFolder = new RaptorCTabFolder(sashForm, SWT.BORDER | SWT.MULTI);
// topFolder.setSimple(false);
// topFolder.setUnselectedImageVisible(false);
// topFolder.setUnselectedCloseVisible(false);
//
// topFolder.setMaximizeVisible(true);
// topFolder.addSelectionListener(new SelectionAdapter() {
//
// @Override
// public void widgetSelected(SelectionEvent e) {
// forceScrollCurrentConsole();
// }
//
// });
//
// topFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
// @Override
// public void maximize(CTabFolderEvent event) {
// Raptor.getInstance().getRaptorWindow().maximizeChatConsoles();
// }
//
// @Override
// public void restore(CTabFolderEvent event) {
// Raptor.getInstance().getRaptorWindow().restore();
// }
//
// });
// topFolder.addMouseListener(new MouseAdapter() {
//
// @Override
// public void mouseDoubleClick(MouseEvent e) {
// System.err.println("Mouse double click " + e.count);
// if (e.count == 2) {
// if (isMaximized()) {
// Raptor.getInstance().getRaptorWindow().restore();
// } else {
// Raptor.getInstance().getRaptorWindow()
// .maximizeChatConsoles();
// }
// }
// super.mouseDoubleClick(e);
// }
//
// @Override
// public void mouseDown(MouseEvent e) {
// if (e.button == 3) {
// Menu menu = new Menu(topFolder.getShell(), SWT.POP_UP);
// MenuItem item = new MenuItem(menu, SWT.PUSH);
// item.setText("Comming soon.");
// item.addListener(SWT.Selection, new Listener() {
// public void handleEvent(Event e) {
// System.out.println("Item Selected");
// }
// });
// menu.setLocation(topFolder.toDisplay(e.x, e.y));
// menu.setVisible(true);
// while (!menu.isDisposed() && menu.isVisible()) {
// if (!topFolder.getDisplay().readAndDispatch())
// topFolder.getDisplay().sleep();
// }
// menu.dispose();
// }
// }
//
// });
//
// bottomFolder = new RaptorCTabFolder(sashForm, SWT.BORDER | SWT.MULTI);
// bottomFolder.setSimple(false);
// bottomFolder.setUnselectedImageVisible(false);
// bottomFolder.setUnselectedCloseVisible(false);
// bottomFolder.setMaximizeVisible(true);
// bottomFolder.addSelectionListener(new SelectionAdapter() {
//
// @Override
// public void widgetSelected(SelectionEvent e) {
// forceScrollCurrentConsole();
// }
// });
// bottomFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
//
// @Override
// public void close(CTabFolderEvent event) {
// if (bottomFolder.getItemCount() == 1) {
// sashForm.setMaximizedControl(topFolder);
// }
// }
// });
//
// sashForm.setMaximizedControl(topFolder);
// restore();
// pack();
// }
//
// public ChatConsole addChatConsole(ChatConsoleController controller,
// Connector connector, boolean isOnTop) {
// if (isOnTop) {
// CTabItem item = new CTabItem(topFolder, SWT.NONE);
// ChatConsole chatConsole = new ChatConsole(topFolder, SWT.NONE);
// chatConsole.setController(controller);
// chatConsole.setPreferences(Raptor.getInstance().getPreferences());
// chatConsole.setConnector(connector);
// controller.setChatConsole(chatConsole);
// chatConsole.createControls();
// chatConsole.getController().init();
// chatConsole.pack();
// item.setControl(chatConsole);
// item.setText(chatConsole.getController().getTitle());
// item.setShowClose(chatConsole.getController().isCloseable());
// topFolder.layout(true);
// topFolder.setSelection(item);
//			
// if (sashForm.getMaximizedControl() != topFolder) {
// sashForm.setMaximizedControl(topFolder);
// // sashForm.setWeights(new int[] { 50, 50 });
// }
// return chatConsole;
// } else {
// CTabItem item = new CTabItem(bottomFolder, SWT.NONE);
// ChatConsole chatConsole = new ChatConsole(bottomFolder, SWT.NONE);
// chatConsole.setController(controller);
// chatConsole.setPreferences(Raptor.getInstance().getPreferences());
// chatConsole.setConnector(connector);
// controller.setChatConsole(chatConsole);
// chatConsole.createControls();
// chatConsole.getController().init();
// chatConsole.pack();
// item.setControl(chatConsole);
// item.setText(chatConsole.getController().getTitle());
// item.setShowClose(chatConsole.getController().isCloseable());
// bottomFolder.layout(true);
// bottomFolder.setSelection(item);
//
// if (sashForm.getMaximizedControl() == topFolder) {
// sashForm.setMaximizedControl(null);
// sashForm.setWeights(new int[] { 50, 50 });
// }
// return chatConsole;
// }
// }
//
// protected void forceScrollCurrentConsole() {
// if (topFolder != null && topFolder.getItemCount() > 0) {
// final ChatConsole currentConsole = (ChatConsole) topFolder.getItem(
// topFolder.getSelectionIndex()).getControl();
// if (currentConsole != null) {
// getDisplay().timerExec(100, new Runnable() {
// public void run() {
// currentConsole.getController().onForceAutoScroll();
// currentConsole.outputText.forceFocus();
// }
// });
// }
// }
//
// if (bottomFolder != null && bottomFolder.getItemCount() > 0) {
// final ChatConsole currentConsole = (ChatConsole) bottomFolder
// .getItem(bottomFolder.getSelectionIndex()).getControl();
// if (currentConsole != null) {
// getDisplay().timerExec(100, new Runnable() {
// public void run() {
// currentConsole.getController().onForceAutoScroll();
// //currentConsole.outputText.forceFocus();
// }
// });
// }
// }
// }
//
// public boolean isMaximized() {
// return topFolder.getMaximized();
// }
//
// public void maximize() {
// topFolder.setMaximized(true);
// forceScrollCurrentConsole();
// }
//
// public void restore() {
// topFolder.setMaximized(false);
// forceScrollCurrentConsole();
// }
}
