package identic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

/*
 *  2018-11-09   3.0.0 Adding concurrent processing for File I/O part of application
 */
public class Identic {

	private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

	private AdapterIdentic identicAdapter = new AdapterIdentic();
	private AdapterManageLists manageListsAdapter = new AdapterManageLists();
	private AdapterCatalog catalogAdapter = new AdapterCatalog();
	private AdapterAction actionAdaper = new AdapterAction();
	private AdapterUtility utilityAdapter = new AdapterUtility();
	private AdapterLog logAdaper = new AdapterLog();

	private AppLogger log = AppLogger.getInstance();

	private ButtonGroup bgFindType = new ButtonGroup();
	private ButtonGroup bgSummary = new ButtonGroup();
	private ButtonGroup bgActions = new ButtonGroup();

	// Find
	private static ConcurrentLinkedQueue<FileStat> qSubjects = new ConcurrentLinkedQueue<>();
	private static ConcurrentLinkedQueue<FileStatReject> qRejects = new ConcurrentLinkedQueue<>();
	private static ConcurrentLinkedQueue<FileStat> qHashes = new ConcurrentLinkedQueue<FileStat>();

	private AbstractMap<String, Integer> fileTypes = new ConcurrentHashMap<>();

	private SubjectTableModel subjectTableModel = new SubjectTableModel();
	private ActionTableModel actionTableModel = new ActionTableModel();

	private RejectTableModel rejectTableModel = new RejectTableModel();
	private DefaultListModel<String> excludeModel = new DefaultListModel<>();

	private UtilityCensusTableModel utilityCensusTableModel = new UtilityCensusTableModel();
	private UtilityEmptyFolderTableModel utilityEmptyFolderTableModel = new UtilityEmptyFolderTableModel();

	private JTable resultsTable = new JTable();
	private JTable actionTable = new JTable();
	private JTable utilityTable = new JTable();

	private static ConcurrentHashMap<String, Integer> hashCounts = new ConcurrentHashMap<String, Integer>();

	// Type List
	private DefaultListModel<String> availableListsModel = new DefaultListModel<>();
	private DefaultListModel<String> editListModel = new DefaultListModel<>();
	private ArrayList<String> targetSuffixes = new ArrayList<>();
	private DefaultListModel<String> targetModel = new DefaultListModel<>();

	private String workingDirectory;
	private String activeTypeList;
	String targetListRegex;

	// Catalog
	private CatalogItemModel availableCatalogItemModel = new CatalogItemModel();
	private JList<CatalogItem> lstCatalogAvailable;// = new JList<CatalogItem>(availableCatalogItemModel);
	private CatalogItemModel inUseCatalogItemModel = new CatalogItemModel();
	private JList<CatalogItem> lstCatalogInUse;// = new JList<CatalogItem>(inUseCatalogItemModel);

	// private JList<CatalogItem2> lstCatalogInUse1;// = new JList<CatalogItem>(inUseCatalogItemModel);

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Identic window = new Identic();
					window.frmIdentic.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}// main

	private String getApplcationWorkingDirectory() {
		String folder = System.getProperty("java.io.tmpdir");
		folder = folder.replace("Temp", "Identic");
		return folder;
	}// getApplcationWorkingDirectory

	// Tab Catalog Code /////////////////////////////////////////////////////////
	private void doCatalogLoad() {
		// see if the directory has been set up already
		Path p = Paths.get(workingDirectory);
		if (!Files.exists(p)) {
			JOptionPane.showMessageDialog(frmIdentic, "Initializing Catalog lists in " + p.toString(), "Initialization",
					JOptionPane.INFORMATION_MESSAGE);
			// System.err.println("Making new directory");
			log.addInfo("Making new Catalog directory");
			try {
				Files.createDirectories(p);
			} catch (IOException e) {
				e.printStackTrace();
			} // try
		} // if exits

		availableCatalogItemModel.clear();
		inUseCatalogItemModel.clear();
		File targetDirectory = new File(workingDirectory);
		File[] files = targetDirectory.listFiles(new ListFilter(CATALOG_SUFFIX));

		// we have the directory, do we have lists ?
		if (files == null) {
			return;
		} // if
		if (files.length == 0) {
			return;
		} // if done if no catalog
		CatalogItem catalogItem;
		for (File file : files) {
			try {
				FileInputStream fis = new FileInputStream(file);
				ObjectInputStream ois = new ObjectInputStream(fis);
				catalogItem = (CatalogItem) ois.readObject();
				availableCatalogItemModel.add(catalogItem);
				ois.close();
				fis.close();
			} catch (ClassNotFoundException | IOException e) {
				log.addError("[doCatalogLoad()] unable to load Catalog");
			} // try
		} // for file
		lstCatalogAvailable.updateUI();
		lstCatalogInUse.updateUI();
	}// doCatalogLoad

	private void doCatalogSave() {
		if (subjectTableModel.getRowCount() == 0) {
			JOptionPane.showMessageDialog(frmIdentic, "No Catalog has been created, by FIND");
			return;
		} // if

		CatalogDialog catalogDialog = CatalogDialog.makeNewCatalogDialog();
		if (catalogDialog.showDialog() == JOptionPane.OK_OPTION) {
			// System.out.printf("state: JOptionPane.OK_OPTION%n");
			log.addInfo(String.format("Name: %s", catalogDialog.getName()));
			log.addInfo(String.format("Description: %s", catalogDialog.getDescription()));

			CatalogItem catalogItem = new CatalogItem(catalogDialog.getName(), catalogDialog.getDescription(),
					lblSourceFolder.getText(), collectFileInfo(subjectTableModel));

			try {
				FileOutputStream fos = new FileOutputStream(
						getApplcationWorkingDirectory() + catalogDialog.getName() + CATALOG_SUFFIX_DOT);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(catalogItem);
				oos.close();
				fos.close();
			} catch (IOException e) {
				String message = String.format(
						"[Identic] doCatalogNew() failed writing catalog object%nName: %s%n Description : %s%n",
						catalogDialog.getName(), catalogDialog.getDescription());
				log.addError(message);
				e.printStackTrace();
			} // try
		} else {
			// if valid
			System.out.printf("state: NOT OK_OPTION%n");
		} // if
		catalogDialog = null;
		doCatalogLoad();
	}// doCatalogNew

	private ArrayList<FileStat> collectFileInfo(SubjectTableModel subjectTableModel) {
		ArrayList<FileStat> result = new ArrayList<>();
		for (int row = 0; row < subjectTableModel.getRowCount(); row++) {
			result.add(subjectTableModel.getFileStat(row));
		} // for - row
		return result;
	}// collectFileInfo

	private void doCatalogImport() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Catalog Files", CATALOG_SUFFIX);
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(frmIdentic);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		} // if Not selected

		String name = chooser.getSelectedFile().getName();
		String sourceAbsolutePath = chooser.getSelectedFile().getAbsolutePath();
		Path source = Paths.get(sourceAbsolutePath);

		Path destination = Paths.get(getApplcationWorkingDirectory(), name);

		try {
			Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			String message = String.format("[doCatalogImport()] name : %s", name);
			log.addError(message);
			e.printStackTrace();
		} // try copy
		doCatalogLoad();
	}// doCatalogImport

	private void doCatalogExport() {
		CatalogItem catalogItem = lstCatalogAvailable.getSelectedValue();
		if (catalogItem == null) {
			JOptionPane.showMessageDialog(frmIdentic, "No Catalog has been selected for Export");
			return;
		} // if
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Catalog Files", CATALOG_SUFFIX);
		chooser.setFileFilter(filter);
		int returnVal = chooser.showSaveDialog(frmIdentic);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		} // if Not selected
		String newName = chooser.getSelectedFile().getName();
		newName = newName.replaceAll("\\..*", "");
		String oldName = catalogItem.getEntryName();
		catalogItem.entryName = newName;
		String absolutePath = chooser.getSelectedFile().getAbsolutePath();
		absolutePath = absolutePath.replaceAll("\\..*", "") + CATALOG_SUFFIX_DOT;

		try {
			FileOutputStream fos = new FileOutputStream(absolutePath);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(catalogItem);
			oos.close();
			fos.close();
		} catch (IOException e) {
			String message = String.format(
					"[Identic] doCatalogNew() failed writing catalog object%nName: %s%n Description : %s%n",
					catalogItem.getEntryName(), catalogItem.getEntryDescription());
			log.addError(message);
			e.printStackTrace();
		} // try
		catalogItem.entryName = oldName;
	}// doCatalogExport

	private void doCatalogRemove() {
		CatalogItem catalogItem = lstCatalogAvailable.getSelectedValue();
		if (catalogItem == null) {
			JOptionPane.showMessageDialog(frmIdentic, "No Catalog has been selected", "Remove Catalog Item",
					JOptionPane.ERROR_MESSAGE);
			return;
		} // if
		String name = catalogItem.getEntryName() + CATALOG_SUFFIX_DOT;
		try {
			Files.deleteIfExists(Paths.get(getApplcationWorkingDirectory(), name));
		} catch (IOException e) {
			log.addError("[doCatalogRemove] failed to remove catalog: " + name);
			e.printStackTrace();
		} // try remove
		doCatalogLoad();
	}// doCatalogRemove

	private void doCatalogListSelected(ListSelectionEvent listSelectionEvent) {
		@SuppressWarnings("unchecked")
		JList<CatalogItem> list = (JList<CatalogItem>) listSelectionEvent.getSource();
		CatalogItem catalogItem = list.getSelectedValue();
		lblCatalogName.setText(catalogItem.getEntryName());
		lblCatalogDescription.setText(catalogItem.getEntryDescription());
		lblCatalogDirectory.setText(catalogItem.getEntryStartDirectory());
		String rowCount = String.format("%,d Rows", catalogItem.getFileStats().size());
		lblCatalogCount.setText(rowCount);
	}// doCatalogListSelected

	// Tab Lists Code ///////////////////////////////////////////////////////////

	private void changeTargetList() {
		String selectedValue = (String) listTypesAvailable.getSelectedValue();
		if (selectedValue == null) {
			JOptionPane.showMessageDialog(frmIdentic, "Select an item from Available List", "Make List Active",
					JOptionPane.WARNING_MESSAGE);
			log.addSpecial("[Make List Active] Did not select an availbe item");
			return;
		} // if - no selection
		activeTypeList = selectedValue;
		loadTargetList();
	}// changeTargetList

	private void loadTargetList() {
		String listFile = getApplcationWorkingDirectory() + activeTypeList + LIST_SUFFIX_DOT;
		lblStatusTypeList.setText(activeTypeList);

		Path pathTypeList = Paths.get(listFile);
		try {
			targetSuffixes = (ArrayList<String>) Files.readAllLines(pathTypeList);
		} catch (IOException e) {
			e.printStackTrace();
		} // try targetModel
		targetModel.removeAllElements();

		StringBuilder sb = new StringBuilder("(?i)"); // case insensitive
		for (String line : targetSuffixes) {
			line = line.trim();
			targetModel.addElement(line);
			sb.append(line);
			sb.append("|");
		} // for
		sb.deleteCharAt(sb.length() - 1); // remove trailing |
		targetListRegex = sb.toString();
		String message = String.format("[loadTargetList()] sb: %s%n%n", targetListRegex);
		log.addInfo(message);
	}// loadTargetList

	private void doLoadTargetEdit() {
		log.addInfo("doLoadTargetEdit()");

		if (listTypesAvailable.isSelectionEmpty()) {
			listTypesAvailable.setSelectedIndex(0);
		} // ensure a selection
		String editName = (String) listTypesAvailable.getSelectedValue();
		lblListEdit.setText(editName);
		txtActive.setText(editName);
		txtEdit.setText(EMPTY_STRING);

		String editFile = workingDirectory + editName + LIST_SUFFIX_DOT;
		Path p = Paths.get(editFile);
		try {
			ArrayList<String> lines = (ArrayList<String>) Files.readAllLines(p);
			editListModel.removeAllElements();
			for (String line : lines) {
				editListModel.addElement(line);
			} // for
		} catch (IOException e) {
			e.printStackTrace();
		} // try

	}// doLoadTargetEdit

	private void doNewTargetEdit() {
		log.addInfo("doLoadNewTargetEdit()");

		String editName = NEW_LIST;
		txtActive.setText(editName);
		lblListEdit.setText(editName);
		txtEdit.setText(EMPTY_STRING);
		editListModel.removeAllElements();
		manageEditButtons("New");

	}// doLoadNewTargetEdit

	private void doSaveList() {
		log.addInfo("doSaveList()");
		String listFile = workingDirectory + txtActive.getText().toUpperCase() + LIST_SUFFIX_DOT;
		Path listPath = Paths.get(listFile);
		if (Files.exists(listPath)) {

			int ans = JOptionPane.showConfirmDialog(frmIdentic, "List Exits, Do you want to overwrite?",
					"Save File Suffix List", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (ans == JOptionPane.NO_OPTION) {
				return;
			} // inner if
		} // if file exists

		try {
			Files.deleteIfExists(listPath);
			Files.createFile(listPath);
		} catch (IOException e) {
			log.addError("[doSaveList] failed to delete file: " + listFile);
			e.printStackTrace();
		} // try

		ArrayList<String> lines = new ArrayList<>();
		for (int i = 0; i < editListModel.getSize(); i++) {
			lines.add(editListModel.getElementAt(i));
		} // for

		try {
			Files.write(listPath, lines, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		} // try

		initFileTypes();
		manageEditButtons("Save");
	}// doSaveList

	private void doDeleteList() {
		log.addInfo("doDeleteList()");
		String listFile = workingDirectory + txtActive.getText().toUpperCase() + LIST_SUFFIX_DOT;
		Path listPath = Paths.get(listFile);
		if (Files.exists(listPath)) {
			int ans = JOptionPane.showConfirmDialog(frmIdentic, "Do you want to delete the list?", "Delete Suffix List",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (ans == JOptionPane.NO_OPTION) {
				return;
			} // if
		} // if file exists
		try {
			Files.deleteIfExists(listPath);
		} catch (IOException e) {
			e.printStackTrace();
		} // try
		initFileTypes();
		doNewTargetEdit(); // loadNewTargetEdit();

	}// doDeleteList

	private void doAddRemove() {
		if (btnAddRemove.getText().equals(EDIT_REMOVE)) {
			int index = listTypesEdit.getSelectedIndex();
			editListModel.removeElementAt(index);
		} else if (btnAddRemove.getText().equals(EDIT_ADD)) {
			editListModel.insertElementAt(txtEdit.getText().toUpperCase(), 0);
			txtEdit.setText(EMPTY_STRING);
		} // if
		btnAddRemove.setText(EDIT_ADD_REMOVE);
	}// doAddRemove

	private void doEditListMember() {
		if (!txtEdit.getText().equals(EMPTY_STRING)) {
			listTypesEdit.clearSelection();
			btnAddRemove.setText(EDIT_ADD);
		} else {
			btnAddRemove.setText(EDIT_ADD_REMOVE);
		} // if
	}// doEditListMember

	private void doNameChanged() {
		String newName = txtActive.getText().trim();
		if (!lblListEdit.getText().equals(newName)) {
			lblListEdit.setText(newName);
			manageEditButtons("NameChanged");
		} // if
		listTypesEdit.clearSelection();
	}// doNameChanged

	private void manageEditButtons(String action) {
		switch (action) {
		case "Load":
			btnLoad.setEnabled(true);
			btnSave.setEnabled(true);
			btnNew.setEnabled(true);
			btnDelete.setEnabled(true);
			break;
		case "New":
			btnLoad.setEnabled(true);
			btnNew.setEnabled(false);
			btnSave.setEnabled(false);
			btnDelete.setEnabled(false);
			break;
		case "NameChanged":
			btnLoad.setEnabled(true);
			btnNew.setEnabled(true);
			btnSave.setEnabled(true);
			btnDelete.setEnabled(false);
			break;
		case "Save":
			btnLoad.setEnabled(true);
			btnNew.setEnabled(true);
			btnSave.setEnabled(true);
			btnDelete.setEnabled(true);
			break;
		}// switch

	}// manageEditButtons

	// Tab Lists Code ///////////////////////////////////////////////////////////

	private void initFileTypes() {
		Path path = Paths.get(workingDirectory);
		if (!Files.exists(path)) {
			JOptionPane.showMessageDialog(frmIdentic, "Initializing File Type lists in " + path.toString(),
					"Initialization", JOptionPane.INFORMATION_MESSAGE);
			log.addSpecial("Making new directory");
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				log.addError("Unable to create working directory: " + workingDirectory);
				e.printStackTrace();
			} // try
		} // if exits
			// we have the directory, do we have lists ?

		File targetDirectory = new File(workingDirectory);
		File[] files = targetDirectory.listFiles(new ListFilter(LIST_SUFFIX_DOT));

		// if files empty - initialize the directory
		boolean needToInitializeDirectory = false;
		if (files == null) {
			needToInitializeDirectory = true;
		} else if (files.length == 0) {
			needToInitializeDirectory = true;
		} // if

		if (needToInitializeDirectory) {

			for (int i = 0; i < INITIAL_LISTFILES.length; i++) {
				try {
					InputStream inputStream = this.getClass().getResourceAsStream(INITIAL_LISTFILES[i]);
					Path targetPath = Paths.get(workingDirectory, INITIAL_LISTFILES[i]);
					Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				} // try
			} // for
			files = targetDirectory.listFiles(new ListFilter(LIST_SUFFIX_DOT));
		} // if no type list files in target directory

		availableListsModel.removeAllElements();
		if (files != null) {
			for (File file : files) {
				availableListsModel.addElement(file.getName().replace(LIST_SUFFIX_DOT, EMPTY_STRING));
			} // for - file
		} // if
	}// initFileTypes

	private void doSourceFolder() {
		JFileChooser fc = new JFileChooser(lblSourceFolder.getText());
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		fc.setDialogTitle("Select Starting Directory");
		fc.setApproveButtonText("Select");

		if (fc.showOpenDialog(frmIdentic) == JFileChooser.APPROVE_OPTION) {
			lblSourceFolder.setText(fc.getSelectedFile().getAbsolutePath());
		} // if
	}// doSourceFolder

	private void doStart() {
		qSubjects.clear();
		qRejects.clear();
		qHashes.clear();
		initialiseFind();
		log.addNL();
		Date startTime = log.addTimeStamp("Start :");

		if (rbNoCatalog.isSelected()) {
			log.addInfo("Start - No Catalogs");
			doStartNoCatalog();
		} else if (rbWithCatalog.isSelected()) {
			if (!isCatalogSelected()) {
				return;
			} // if catalog selected
			log.addInfo("Start - With Catalogs");
			doStartOnlyCatalogs();
			doStartNoCatalog();
		} else if (rbOnlyCatalogs.isSelected()) {
			if (!isCatalogSelected()) {
				return;
			} // if catalog selected
			log.addInfo("Start - OnlyCatalogs");
			cbSaveExcludedFiles.setSelected(false);
			doStartOnlyCatalogs();
		} // if start type
		btnSummaryExcluded.setVisible(cbSaveExcludedFiles.isSelected());
		log.addElapsedTime(startTime, "End :");
		markTheDuplicates();
		displaySummary();
		//
	}// doStart

	/*
	 * Start button initiates the scanning of the directories,identifying candidate and reject files, and buils a model
	 * that contains the results for later display or saving.
	 * 
	 * It uses 3 queues: qRejects, qSubject (candidates) & qHashes. It produces 2 Models: rejectTableModel &
	 * subjectTableModel
	 * 
	 * the work is done by multiple threads: 1) first thread - identifySubjects generates two queues qRejects &
	 * qSubjects 2) two threads are then run: showRejects( reads qRejects) produces RejectTableModel. & MakeFileKey
	 * (reads qSubjects) to produce the qHashes queue 3) then identifySubjects is started ( reads qHashes) to product
	 * the subjectTableModel, which is used by the rest of the application
	 * 
	 * 
	 */

	private void doStartNoCatalog() {
		// startPath = Paths.get(lblSourceFolder.getText());
		if (!Files.exists(Paths.get(lblSourceFolder.getText()))) {
			JOptionPane.showConfirmDialog(frmIdentic, "Starting Folder NOT Valid!", "Find Duplicates - Start",
					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		} // if starting folder not there

		log.addInfo("Starting Folder:");
		log.addInfo("     " + lblSourceFolder.getText());
		// ------------------------------------------------------------------------------------

		MakeFileKeys makeFileKeys = new MakeFileKeys(qSubjects, qHashes);
		ThreadPoolExecutor executeMakeFileKeys = (ThreadPoolExecutor) Executors.newFixedThreadPool(PROCESSORS);

		FillMainTable fillMainTable = new FillMainTable(subjectTableModel, qHashes, hashCounts);
		ThreadPoolExecutor executeFillMainTable = (ThreadPoolExecutor) Executors.newFixedThreadPool(PROCESSORS);

		Pattern patternSubjects = Pattern.compile(targetListRegex);

		ForkJoinPool poolIdentify = new ForkJoinPool(PROCESSORS);
		IdentifyFiles identifyFiles = new IdentifyFiles(new File(lblSourceFolder.getText()), patternSubjects, qSubjects,
				qRejects);
		poolIdentify.execute(identifyFiles);

		for (int i = 0; i < PROCESSORS; i++) {
			executeMakeFileKeys.execute(makeFileKeys);
		} // for - i

		for (int i = 0; i < PROCESSORS; i++) {
			executeFillMainTable.execute(fillMainTable);
		} // for - i

		while (!poolIdentify.isQuiescent()) {
			// psuedo join
		} // while
		qSubjects.add(END_OF_SUBJECT);
		qRejects.add(END_OF_REJECT);

		try {
			executeMakeFileKeys.shutdown();
			executeMakeFileKeys.awaitTermination(1, TimeUnit.DAYS);

			qHashes.add(END_OF_SUBJECT);
			Thread threadFillRejectTable = new Thread(new FillRejectTable(rejectTableModel, qRejects));
			threadFillRejectTable.start();

			executeFillMainTable.shutdown();
			executeFillMainTable.awaitTermination(1, TimeUnit.DAYS);
			threadFillRejectTable.join();

		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} // try

	}// doStartNoCatalog

	private void doStartOnlyCatalogs() {
		log.addInfo("Catalogs:");
		CatalogItem catalogItem;
		for (int i = 0; i < inUseCatalogItemModel.getSize(); i++) {
			catalogItem = inUseCatalogItemModel.get(i);
			log.addInfo(" " + catalogItem.getEntryName());
			System.out.println(catalogItem.getEntryName());
		} // for - each catalog Item

		GatherFromCatalogs gatherFromCatalogs = new GatherFromCatalogs();
		Thread threadGather = new Thread(gatherFromCatalogs);
		threadGather.start();

		FillMainTable fillMainTable = new FillMainTable(subjectTableModel, qHashes, hashCounts);
		Thread threadFillMainTable = new Thread(fillMainTable);
		threadFillMainTable.start();

		try {
			threadGather.join();
			threadFillMainTable.join();
			// threadIdentifyDuplicates.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} // try

	}// doStartWithCatalog

	private boolean isCatalogSelected() {
		return isCatalogSelected(1);
	}// isCatalogSelected

	private boolean isCatalogSelected(int minimum) {
		boolean result = true;
		if (inUseCatalogItemModel.getSize() < minimum) {
			JOptionPane.showMessageDialog(frmIdentic, "Not enough Catalog Items found\n on \"In Use\" List",
					"Find With Catalog(s)", JOptionPane.ERROR_MESSAGE);
			result = false;
		} // if less than one
		return result;
	}// isCatalogSelected

	private void initialiseFind() {
		qSubjects.clear();
		qRejects.clear();

		excludeModel.clear();
		hashCounts.clear();
		subjectTableModel.clear();
		actionTableModel.clear();
		rejectTableModel.clear();
		// hashIDs.clear();
		hashCounts.clear();

		resultsTable.setModel(new DefaultTableModel());
		;
		actionTable.setModel(new DefaultTableModel());

	}// initialiseFind

	// ---------------Find Duplicates--------------------------

	private void markTheDuplicates() {
		int rows = subjectTableModel.getRowCount();
		String hashKey;
		// long excessStorage = 0l;
		for (int row = 0; row < rows; row++) {
			hashKey = (String) subjectTableModel.getValueAt(row, 6);
			try {
				if (hashCounts.get(hashKey) > 1) {
					subjectTableModel.setValueAt(true, row, 4);
				} // set dup flag if > 1
			} catch (Exception e) {
				System.out.println("HashKey = " + hashKey);
			} // try
		} // for
	}// markTheDuplicates

	private void displaySummary() {

		int totalCountOfExcludedFiles = rejectTableModel.getRowCount();

		int totalFileCount = totalCountOfExcludedFiles + subjectTableModel.getRowCount();
		lblTotalFiles.setText(String.format("%,d  Total Files", totalFileCount));
		setButtonLabel(btnSummaryExcluded, totalCountOfExcludedFiles);
		setButtonLabel(btnSummaryTargets, subjectTableModel.getRowCount());

		int filesWithNoDups = 0;
		Set<String> hashKeys = hashCounts.keySet();
		for (String hashKey : hashKeys) {
			if (hashCounts.get(hashKey) == 1) {
				filesWithNoDups++;
			} // if
		} // for

		int filesWithDups = hashCounts.size() - filesWithNoDups;

		setButtonLabel(btnSummaryDistinct, hashCounts.size());
		setButtonLabel(btnSummaryUnique, filesWithNoDups);
		setButtonLabel(btnSummaryDuplicates, filesWithDups);

		bgSummary.setSelected(btnSummaryTargets.getModel(), true);
		doShowResults(BTN_SUMMARY_TARGETS);
	}// displaySummary

	private void setButtonLabel(JToggleButton btn, int value) {
		String buttonLabel = "";

		switch (btn.getName()) {
		case BTN_SUMMARY_EXCLUDED:
			buttonLabel = "Excluded";
			break;
		case BTN_SUMMARY_TARGETS:
			buttonLabel = "Targets";
			break;
		case BTN_SUMMARY_DISTINCT:
			buttonLabel = "Distinct";
			break;
		case BTN_SUMMARY_UNIQUE:
			buttonLabel = "Unique";
			break;
		case BTN_SUMMARY_DUPLICATES:
			buttonLabel = "Duplicates";
			break;
		default:
			buttonLabel = "Error";
			log.addError("[setButtonLabel] Bad button name - " + btn.getName());
		}// switch - button name

		btn.setText(String.format(" %,-10d\t%s", value, buttonLabel));

		btn.setEnabled(value != 0);
		return;
	}// setButtonLabel

	private void doShowResults(String buttonName) {
		switch (buttonName) {
		case BTN_SUMMARY_EXCLUDED:
			// rejectTableModel
			if (rejectTableModel.getRowCount() > 0) {
				resultsTable.setModel(rejectTableModel);
				resultsTable.setRowSorter(new TableRowSorter<RejectTableModel>(rejectTableModel));
			} // if

			break;

		case BTN_SUMMARY_TARGETS:
			if (subjectTableModel.getRowCount() > 0) {
				resultsTable.setModel(subjectTableModel);
				resultsTable.setRowSorter(new TableRowSorter<SubjectTableModel>(subjectTableModel));
				setSubjectColumns();
			} // if
			break;

		case BTN_SUMMARY_DISTINCT:
			if (subjectTableModel.getRowCount() > 0) {
				// -----------------------------------------
				RowFilter<Object, Object> dupFilter = new RowFilter<Object, Object>() {
					AbstractSet<String> hashIDs = new HashSet<String>();

					public boolean include(Entry<? extends Object, ? extends Object> entry) {
						String hashID = (String) entry.getValue(6);
						if (hashIDs.contains(hashID)) {
							return false;
						} else
							hashIDs.add(hashID);
						return true;
					}// include
				};
				// -------------------------------------------
				TableRowSorter<SubjectTableModel> tableRowSorter = new TableRowSorter<SubjectTableModel>(
						subjectTableModel);
				tableRowSorter.setRowFilter(dupFilter);
				resultsTable.setModel(subjectTableModel);
				resultsTable.setRowSorter(tableRowSorter);
				setSubjectColumns();
			} // if
			break;
		case BTN_SUMMARY_UNIQUE:
			if (subjectTableModel.getRowCount() > 0) {
				// -----------------------------------------
				RowFilter<Object, Object> dupFilter = new RowFilter<Object, Object>() {

					public boolean include(Entry<? extends Object, ? extends Object> entry) {
						return !((boolean) entry.getValue(4));
					}// include
				};

				// -------------------------------------------
				TableRowSorter<SubjectTableModel> tableRowSorter = new TableRowSorter<SubjectTableModel>(
						subjectTableModel);
				tableRowSorter.setRowFilter(dupFilter);
				resultsTable.setModel(subjectTableModel);
				resultsTable.setRowSorter(tableRowSorter);

				setSubjectColumns();
			} // if
			break;
		case BTN_SUMMARY_DUPLICATES:
			if (subjectTableModel.getRowCount() > 0) {

				RowFilter<Object, Object> dupFilter = new RowFilter<Object, Object>() {
					public boolean include(Entry<? extends Object, ? extends Object> entry) {
						return (boolean) entry.getValue(4);
					}// include
				};

				TableRowSorter<SubjectTableModel> tableRowSorter = new TableRowSorter<SubjectTableModel>(
						subjectTableModel);
				tableRowSorter.setRowFilter(dupFilter);
				resultsTable.setRowSorter(tableRowSorter);
				resultsTable.setModel(subjectTableModel);

				setSubjectColumns();

			} // if
			break;
		default:
			log.addError("[doShowResults] Bad button name - " + buttonName);
		}// switch - button name

	}// doShowResults

	private void setSubjectColumns() {
		resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		for (int i = 0; i < resultsTable.getColumnModel().getColumnCount(); i++) {
			TableColumn tc = resultsTable.getColumnModel().getColumn(i);
			switch (i) {
			case 0: // Name
				break;
			case 1: // Directory
				break;
			case 2: // Size
				// tc.setMaxWidth(460);
				tc.setPreferredWidth(40);
				break;
			case 3: // Date
				// tc.setMaxWidth(100);
				tc.setPreferredWidth(40);

				break;
			case 4: // Dup
				tc.setMaxWidth(40);
				break;
			case 5: // ID
				tc.setMaxWidth(40);
				break;
			default:
				tc.setPreferredWidth(40);
			}// switch
		} // for each column
	}// setSubjectColumns

	private void setActionColumns() {
		actionTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		for (int i = 0; i < actionTable.getColumnModel().getColumnCount(); i++) {
			TableColumn tc = actionTable.getColumnModel().getColumn(i);
			switch (i) {
			case 0: // Action
				tc.setMaxWidth(40);
				tc.setPreferredWidth(40);
				break;
			case 1: // Name
				break;
			case 2: // Directory
				break;
			case 3: // Size
				// tc.setMaxWidth(460);
				tc.setPreferredWidth(40);
				break;
			case 4: // Date
				// tc.setMaxWidth(100);
				tc.setPreferredWidth(40);
				break;
			case 5: // Dup
				tc.setMaxWidth(40);
				break;
			case 6: // ID
				tc.setMaxWidth(40);
				break;
			default:
				tc.setPreferredWidth(40);
			}// switch
		} // for each column
	}// setSubjectColumns

	private void setCensusColumns() {
		utilityTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		for (int i = 0; i < utilityTable.getColumnModel().getColumnCount(); i++) {
			TableColumn tc = utilityTable.getColumnModel().getColumn(i);
			switch (i) {
			case 0: // count
				tc.setMaxWidth(140);
				tc.setPreferredWidth(100);
				break;
			case 1: // Directory
				break;
			}// switch
		} // for each column
	}// setSubjectColumns

	private void setEmptyFoldersColumns() {
		utilityTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		for (int i = 0; i < utilityTable.getColumnModel().getColumnCount(); i++) {
			TableColumn tc = utilityTable.getColumnModel().getColumn(i);
			switch (i) {
			case 0: // count
				tc.setMaxWidth(40);
				tc.setPreferredWidth(40);
				break;
			case 1: // Directory
				break;
			}// switch
		} // for each column
	}// setEmptyFoldersColumns

	private void doPrintResults() {
		String reportType = bgSummary.getSelection().getActionCommand();
		MessageFormat headerFormat = new MessageFormat(reportType);
		String ff = MessageFormat.format("{0,time} - {0,date}    Page  ", new Date());
		MessageFormat footerFormat = new MessageFormat(ff + "{0}");
		try {
			resultsTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);
		} catch (PrinterException e) {
			log.addError("[failed Print]" + e.getMessage());
			e.printStackTrace();
		} // try
	}// doPrintResults

	private void doActionLoadResults() {
		Object[] subjectRow;
		actionTableModel.clear();
		actionTable.setModel(actionTableModel);

		switch (bgActions.getSelection().getActionCommand()) {
		case RB_AC_TARGETS:
			for (int row = 0; row < subjectTableModel.getRowCount(); row++) {
				subjectRow = subjectTableModel.getRow(row);
				actionTableModel.addRowForAction(subjectRow);
			} // for rows
			break;
		case RB_AC_DISTINCT:
			AbstractSet<Integer> hashIDs = new HashSet<Integer>();
			Integer hashID;
			for (int row = 0; row < subjectTableModel.getRowCount(); row++) {
				subjectRow = subjectTableModel.getRow(row);

				hashID = (Integer) subjectRow[COLUMN_ID_SUBJECT];
				if (!hashIDs.contains(hashID)) {
					actionTableModel.addRowForAction(subjectRow);
					hashIDs.add(hashID);
				} // if
			} // for rows

			break;
		case RB_AC_UNIQUE:
			for (int row = 0; row < subjectTableModel.getRowCount(); row++) {
				subjectRow = subjectTableModel.getRow(row);// COLUMN_DIRECTORY_DUP
				if (!(boolean) subjectRow[COLUMN_DIRECTORY_DUP]) {
					actionTableModel.addRowForAction(subjectRow);
				} // if no duplicate
			} // for rows

			break;
		case RB_AC_DUPLICATES:
			for (int row = 0; row < subjectTableModel.getRowCount(); row++) {
				subjectRow = subjectTableModel.getRow(row);// COLUMN_DIRECTORY_DUP
				if ((boolean) subjectRow[COLUMN_DIRECTORY_DUP]) {
					actionTableModel.addRowForAction(subjectRow);
				} // if has duplicate
			} // for rows

			break;
		default:
			log.addError("[doActionLoadResults] unknown action: " + bgActions.getSelection().getActionCommand());
		}// switch
		actionTable.setRowSorter(new TableRowSorter<ActionTableModel>(actionTableModel));

		setActionColumns();
		actionTable.updateUI();

	}// doActionLoadResults

	private void makeFolder(Path path) {
		if (path.equals(path.getRoot())) {
			return;
		} // if - root

		if (Files.exists(path)) {
			return;
		} // if exists

		makeFolder(path.getParent());

		try {
			Files.createDirectory(path);
			log.addInfo("[makeFolder] created: " + path);
			return;
		} catch (IOException e) {
			String msg = String.format("[makeFolder] failed to create: %s %n   message: %s", path, e.getMessage());
			log.addError(msg);
			e.printStackTrace();
		} // try

		return;
	}// makeFolder

	private void doActionMultiSelect() {
		doUtilityBulkSelection(actionTable, true);
	}// doActionMultiSelect

	private void doActionMultiDeselect() {
		doUtilityBulkSelection(actionTable, false);
	}// doActionMultiSelect

	private void doUtilityMultiSelect() {
		doUtilityBulkSelection(utilityTable, true);
	}// doUtilityMultiSelect

	private void doUtilityMultiDeselect() {
		doUtilityBulkSelection(utilityTable, false);
	}// doUtilittyMultiDeselect

	private void doUtilityBulkSelection(JTable table, boolean state) {
		int[] selectedRows = table.getSelectedRows();
		// int column = actionTableModel.findColumn("Action");
		int column = ((AbstractTableModel) table.getModel()).findColumn("Action");// actionTableModel.findColumn("Action");
		for (int row = 0; row < selectedRows.length; row++) {
			table.setValueAt(state, selectedRows[row], column);
		} // for each selected row
			// actionTable.clearSelection();
		table.updateUI();
	}// doUtilityBulkSelection

	private void doActionMoveCopy(String action) {
		if (!doesFolderExist()) {
			return;
		} // if LinkedList<Integer> rowsToRemove = new LinkedList<Integer>();

		LinkedList<Integer> rowsToRemove = new LinkedList<Integer>();
		String msgAction = action.equals(BTN_ACTION_COPY) ? "Copy" : "Move";

		String lcd = getLeastCommonDirectory(actionTableModel);
		String targetBaseString = lblSourceFolder.getText();
		String sourcePathString = "";
		String targetPathString = "";
		String sourceName = "";

		int actionColumn = actionTableModel.findColumn(ActionTableModel.ACTION);
		int directoryColumn = actionTableModel.findColumn(ActionTableModel.DIRECTORY);
		int nameColumn = actionTableModel.findColumn(ActionTableModel.NAME);

		for (int row = 0; row < actionTableModel.getRowCount(); row++) {
			if (!(boolean) actionTableModel.getValueAt(row, actionColumn)) {
				continue; // skip this row
			} // if - action checked
			sourcePathString = (String) actionTableModel.getValueAt(row, directoryColumn);
			sourceName = (String) actionTableModel.getValueAt(row, nameColumn);
			targetPathString = sourcePathString.replace(lcd, targetBaseString);

			makeFolder(Paths.get(targetPathString));

			try {
				if (action.equals(BTN_ACTION_COPY)) {
					Files.copy(Paths.get(sourcePathString, sourceName), Paths.get(targetPathString, sourceName));
				} else {
					Files.move(Paths.get(sourcePathString, sourceName), Paths.get(targetPathString, sourceName));
					rowsToRemove.addFirst(row);
				}
				String msg = String.format("[doActionMoveCopy] File %s : %s, %s", msgAction, targetPathString,
						sourceName);
				log.addInfo(msg);
			} catch (FileAlreadyExistsException ex) {
				String msg = String.format("[doActionMoveCopy] File Already Exists: %s, %s", targetPathString,
						sourceName);
				log.addInfo(msg);
			} catch (IOException e) {
				String msg = String.format("[doActionMoveCopy] Failed %s: %s, %s", msgAction, targetPathString,
						sourceName);
				log.addError(msg, e.getMessage());
			} // try
		} // for row
		if (action.equals(BTN_ACTION_MOVE)) {
			removeRows(rowsToRemove, actionTable, actionTableModel);
			actionTable.updateUI();
		} // if move

	}// doActionMove

	private void doActionDelete() {
		Path sourcePath;
		SortedSet<Path> targetFolders = new TreeSet<Path>(Collections.reverseOrder());

		int actionColumn = actionTableModel.findColumn(ActionTableModel.ACTION);
		int directoryColumn = actionTableModel.findColumn(ActionTableModel.DIRECTORY);
		int nameColumn = actionTableModel.findColumn(ActionTableModel.NAME);
		LinkedList<Integer> rowsToRemove = new LinkedList<Integer>();

		for (int row = 0; row < actionTableModel.getRowCount(); row++) {
			if (!(boolean) actionTableModel.getValueAt(row, actionColumn)) {
				continue; // skip this row
			} //

			sourcePath = Paths.get((String) actionTableModel.getValueAt(row, directoryColumn),
					(String) actionTableModel.getValueAt(row, nameColumn));
			try {
				Files.delete(sourcePath);
				targetFolders.add(sourcePath);
				String msg = String.format("[doActionDelete] File Deleted: %s", sourcePath);
				rowsToRemove.addFirst(row);
				log.addInfo(msg);

			} catch (AccessDeniedException ade) {
				String msg = String.format("[doActionDelete] Access Denied: %s", sourcePath);
				log.addInfo(msg);
			} catch (NoSuchFileException ne) {
				String msg = String.format("[doActionDelete] File Does Not Exist : %s", sourcePath);
				log.addInfo(msg);
			} catch (IOException e) {
				String msg = String.format("[doActionDelete] Failed Delete: %s", sourcePath);
				log.addError(msg, e.getMessage());
				e.printStackTrace();
			} // try delete
		} // for rows
		removeRows(rowsToRemove, actionTable, actionTableModel);
		removeEmptyFolders(targetFolders, false);
	}// doActionDelete

	private void removeRows(LinkedList<Integer> rows, JTable table, MyTableModel tableModel) {
		int row;
		String msg;
		msg = String.format("[Before] Table: %d, Model %d", actionTable.getRowCount(), tableModel.getRowCount());
		log.addSpecial(msg);
		while (!rows.isEmpty()) {
			row = rows.poll();
			tableModel.removeRow(row);
		} // while

		table.setModel(tableModel);
		setActionColumns();
		table.updateUI();

		msg = String.format("[After] Table: %d, Model %d", table.getRowCount(), tableModel.getRowCount());
		log.addSpecial(msg);

	}// removeRows

	private boolean doesFolderExist() {
		if (Files.exists(Paths.get(lblSourceFolder.getText()), LinkOption.NOFOLLOW_LINKS)) {
			return true;
		} else {
			log.addWarning(String.format("Source Folder %s does not exist", lblSourceFolder.getText()));
			return false;
		} // if
	}// doesSourceFolderExist

	private String getLeastCommonDirectory(AbstractTableModel tableModel) {
		int directoryColumn = tableModel.findColumn(ActionTableModel.DIRECTORY);

		String[] fullPathParts = new String[] {};
		String regexString = System.getProperty("file.separator").equals("\\") ? "\\\\"
				: System.getProperty("file.separator");

		String fullPath = (String) tableModel.getValueAt(0, directoryColumn);
		String[] baseParts = fullPath.split(regexString);
		int matchCount = baseParts.length;

		for (int i = 1; i < tableModel.getRowCount(); i++) {
			int matchCountTemp = 0;
			fullPath = (String) tableModel.getValueAt(i, directoryColumn);
			fullPathParts = fullPath.split(regexString);
			matchCount = Math.min(matchCount, fullPathParts.length);

			for (int mc = 0; mc < matchCount; mc++) {
				if (baseParts[mc].equals(fullPathParts[mc])) {
					matchCountTemp++;
				} else {
					break;
				} // if
			} // for mc
			matchCount = matchCountTemp;
		} // for - outer

		StringJoiner sj = new StringJoiner(System.getProperty("file.separator"));
		for (int i = 0; i < matchCount; i++) {
			sj.add(fullPathParts[i]);
		} // for

		return sj.toString();
	}// getLeastCommonDirectory

	private void doLogClear() {
		log.clear();
	}// doLogClear

	private void doLogPrint() {

		Font originalFont = txtLog.getFont();
		try {
			txtLog.setFont(originalFont.deriveFont(8.0f));
			MessageFormat header = new MessageFormat("Identic Log");
			MessageFormat footer = new MessageFormat(new Date().toString() + "           Page - {0}");
			txtLog.print(header, footer);
			txtLog.setFont(originalFont);
		} catch (PrinterException e) {
			e.printStackTrace();
		} // try

	}// doLogPrint

	// =============================Do Test =================================================

	private void doTest() {

	}// doBtnOne

	// =============================Do Test =================================================

	private void doTakeCensus() {
		log.addInfo("Take Census");
		log.addInfo("   Starting Folder: " + lblSourceFolder.getText());
		utilityCensusTableModel.clear();
		utilityTable.setModel(utilityCensusTableModel);

		fileTypes.clear();
		FileTypeCensus fileTypeCensus = new FileTypeCensus(new File(lblSourceFolder.getText()), fileTypes);

		ForkJoinPool poolCensus = new ForkJoinPool(PROCESSORS);
		poolCensus.execute(fileTypeCensus);
		while (!poolCensus.isQuiescent()) {
			// psuedo join
		} // while

		Set<Entry<String, Integer>> fileTypeCensusSet = fileTypes.entrySet();
		// Set<Entry<String, Integer>> fileTypeCensusSet = fileTypeCensus.entrySet();
		for (Entry<String, Integer> entry : fileTypeCensusSet) {
			utilityCensusTableModel.addRow(new Object[] { entry.getValue(), entry.getKey() });
		} // for - entry
		setCensusColumns();

		utilityTable.setRowSorter(new TableRowSorter<UtilityCensusTableModel>(utilityCensusTableModel));

		utilityTable.updateUI();
	}// doTest

	private void doFindEmptyFolders() {
		log.addInfo("Find Empty Folders");
		log.addInfo("   Starting Folder: " + lblSourceFolder.getText());

		utilityEmptyFolderTableModel.clear();
		// utilityTable.setModel(utilityEmptyFolderTableModel);
		utilityTable.updateUI();

		FindEmptyFolders findEmptyFolders = new FindEmptyFolders(new File(lblSourceFolder.getText()),
				utilityEmptyFolderTableModel);

		ForkJoinPool poolEmptyFolders = new ForkJoinPool(PROCESSORS); // PROCESSORS
		poolEmptyFolders.execute(findEmptyFolders);
		while (!poolEmptyFolders.isQuiescent()) {
			// psuedo join
		} // while

		utilityTable.setModel(utilityEmptyFolderTableModel);
		utilityTable.updateUI();

		setEmptyFoldersColumns();
		utilityTable.setRowSorter(new TableRowSorter<UtilityEmptyFolderTableModel>(utilityEmptyFolderTableModel));

		setRemoveFoldersState(utilityTable.getRowCount() != 0);// turn remove buttons on/off
	}// doFindEmptyFolders

	private void setRemoveFoldersState(boolean state) {
		btnRemoveEmptyFoldersTree.setEnabled(state);
		btnRemoveEmptyFolders.setEnabled(state);
	}// setRemoveFoldersState

	private void doRemoveEmptyFoldersTree() {
		log.addInfo("Identic.doRemoveEmptyFoldersTree()");
		doRemoveEmptyFolders(true);
	}// doRemoveEmptyFoldersTree

	private void doRemoveEmptyFolders() {
		log.addInfo("Identic.doRemoveEmptyFolders()");
		doRemoveEmptyFolders(false);
	}// doRemoveEmptyFolders - worker

	private void doRemoveEmptyFolders(boolean tree) {
		Path sourcePath;
		SortedSet<Path> targetFolders = new TreeSet<Path>(Collections.reverseOrder());
		int actionColumn = utilityEmptyFolderTableModel.findColumn(UtilityEmptyFolderTableModel.ACTION);
		int folderColumn = utilityEmptyFolderTableModel.findColumn(UtilityEmptyFolderTableModel.FOLDER);
		LinkedList<Integer> rowsToRemove = new LinkedList<Integer>();

		for (int row = 0; row < utilityEmptyFolderTableModel.getRowCount(); row++) {
			if (!(boolean) utilityEmptyFolderTableModel.getValueAt(row, actionColumn)) {
				continue; // skip this row
			} // if
			sourcePath = (Path) utilityEmptyFolderTableModel.getValueAt(row, folderColumn);
			targetFolders.add(sourcePath);
			rowsToRemove.addFirst(row);
		} // for rows

		removeRows(rowsToRemove, utilityTable, utilityEmptyFolderTableModel);
		removeEmptyFolders(targetFolders, tree);
			}// doRemoveEmptyFolders

	private boolean isDirectoryWithNoFiles(File file) {
		boolean ans = true;
		if (file.isFile()) {
			ans = false;
		} else if (file.list() == null) {
			ans = true;
		} else if (file.list().length == 0) {
			ans = true;
		} // if
		return ans;
	}// isDirectoryWithFiles

	private void removeEmptyFolders(SortedSet<Path> targetFolders, boolean tree) {

		File file;
		for (Path folder : targetFolders) {
			file = folder.toFile();
			if (isDirectoryWithNoFiles(file)) {
				removeEmptyDirectory(file, tree);
			} // if
		} // for
			//
	}// removeEmptyFolders

	private void removeEmptyDirectory(File file, boolean tree) {
		String action = "Deleted: %s";
		if (file.delete()) {
			if (tree) {
				Path path = file.toPath();
				Path parentPath = path.getParent();
				if (parentPath == path.getRoot()) {
					return;
				} // if root
				File parentFile = parentPath.toFile();
				if (isDirectoryWithNoFiles(parentFile)) {
					removeEmptyDirectory(parentFile, tree);
				} // if
			} // if tree
		} else {
			action = "**Not Deleted: %s";
		} // if delete
		String msg = String.format(action, file.getAbsolutePath());
		log.addInfo(msg);

	}// removeEmptyDirectory

	// Swing code ///////////////////////////////////////////////////////////////

	private void doFileExit() {
		appClose();
		System.exit(0);
	}// doFileExit

	private void appClose() {
		Preferences myPrefs = Preferences.userNodeForPackage(Identic.class).node(this.getClass().getSimpleName());
		Dimension dim = frmIdentic.getSize();
		myPrefs.putInt("Height", dim.height);
		myPrefs.putInt("Width", dim.width);
		Point point = frmIdentic.getLocation();
		myPrefs.putInt("LocX", point.x);
		myPrefs.putInt("LocY", point.y);
		myPrefs.putInt("mainDivider", splitPaneMain.getDividerLocation());
		myPrefs.putInt("summaryDivider", splitPaneSummary.getDividerLocation());
		myPrefs.put("SourceFolder", lblSourceFolder.getText());
		myPrefs.put("ActiveList", activeTypeList);
		// // myPrefs.put("ListDirectory", fileListDirectory);
		// myPrefs.putInt("SideButtonIndex", sideButtonIndex);
		String findTypeButton = bgFindType.getSelection().getActionCommand();
		myPrefs.put("findTypeButton", findTypeButton);
		myPrefs = null;
	}// appClose

	private void appInit() {
		txtLog.setText(EMPTY_STRING);
		log.setDoc(txtLog.getStyledDocument());

		workingDirectory = getApplcationWorkingDirectory();

		Preferences myPrefs = Preferences.userNodeForPackage(Identic.class).node(this.getClass().getSimpleName());
		// frmIdentic.setSize(916, 749);
		frmIdentic.setSize(myPrefs.getInt("Width", 916), myPrefs.getInt("Height", 749));

		frmIdentic.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));
		lblSourceFolder.setText(myPrefs.get("SourceFolder", EMPTY_STRING));
		activeTypeList = myPrefs.get("ActiveList", "Pictures");
		// splitPane1.setDividerLocation(myPrefs.getInt("Divider", 174));
		//
		// // fileListDirectory = myPrefs.get("ListDirectory", EMPTY_STRING);
		// sideButtonIndex = myPrefs.getInt("SideButtonIndex", 0);
		//
		String findTypeButton = myPrefs.get("findTypeButton", RB_CATALOG_NO);
		switch (findTypeButton) {
		case RB_CATALOG_NO:
			rbNoCatalog.setSelected(true);
			break;
		case RB_CATALOG_WITH:
			rbWithCatalog.setSelected(true);
			break;
		case RB_CATALOG_ONLY:
			rbOnlyCatalogs.setSelected(true);
			break;
		}// switch find Type

		splitPaneMain.setDividerLocation(myPrefs.getInt("mainDivider", 150));
		splitPaneSummary.setDividerLocation(myPrefs.getInt("summaryDivider", 200));
		// mainDivider
		myPrefs = null;

		// TypeLists //////
		listTypesAvailable.setModel(availableListsModel);
		listTypesEdit.setModel(editListModel);
		initFileTypes();

		bgFindType.add(rbNoCatalog);
		bgFindType.add(rbWithCatalog);
		bgFindType.add(rbOnlyCatalogs);

		bgSummary.add(btnSummaryExcluded);
		bgSummary.add(btnSummaryTargets);
		bgSummary.add(btnSummaryDistinct);
		bgSummary.add(btnSummaryUnique);
		bgSummary.add(btnSummaryDuplicates);

		bgActions.add(rbLoadTargetFiles);
		bgActions.add(rbLoadDistinctFiles);
		bgActions.add(rbLoadUniqueFiles);
		bgActions.add(rbLoadHaveDuplicates);
		rbLoadTargetFiles.setSelected(true);

		availableCatalogItemModel.clear();
		lstCatalogAvailable.setDragEnabled(true);
		lstCatalogAvailable.setDropMode(DropMode.INSERT);
		lstCatalogAvailable.setTransferHandler(new ListTransferHandler());

		inUseCatalogItemModel.clear();
		lstCatalogInUse.setDragEnabled(true);
		lstCatalogInUse.setDropMode(DropMode.INSERT);
		lstCatalogInUse.setTransferHandler(new ListTransferHandler());

		doCatalogLoad();
		loadTargetList();

		// @SuppressWarnings("unused")
		// TableColumnManager tcmResults = new TableColumnManager(resultsTable);

		setRemoveFoldersState(false);

	}// appInit

	/**
	 * Create the application.
	 */
	public Identic() {
		initialize();
		appInit();
	}// Constructor

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmIdentic = new JFrame();
		frmIdentic.setTitle("Identic 3.0");
		frmIdentic.setBounds(100, 100, 450, 621);

		frmIdentic.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmIdentic.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose();
			}
		});
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 30, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		frmIdentic.getContentPane().setLayout(gridBagLayout);

		splitPaneMain = new JSplitPane();
		splitPaneMain.setEnabled(false);
		GridBagConstraints gbc_splitPaneMain = new GridBagConstraints();
		gbc_splitPaneMain.insets = new Insets(0, 0, 5, 0);
		gbc_splitPaneMain.fill = GridBagConstraints.BOTH;
		gbc_splitPaneMain.gridx = 0;
		gbc_splitPaneMain.gridy = 0;
		frmIdentic.getContentPane().add(splitPaneMain, gbc_splitPaneMain);

		JPanel panelForTabbedPane = new JPanel();
		splitPaneMain.setRightComponent(panelForTabbedPane);
		GridBagLayout gbl_panelForTabbedPane = new GridBagLayout();
		gbl_panelForTabbedPane.columnWidths = new int[] { 672, 0 };
		gbl_panelForTabbedPane.rowHeights = new int[] { 0, 521, 0 };
		gbl_panelForTabbedPane.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelForTabbedPane.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panelForTabbedPane.setLayout(gbl_panelForTabbedPane);

		JPanel panelTop = new JPanel();
		GridBagConstraints gbc_panelTop = new GridBagConstraints();
		gbc_panelTop.anchor = GridBagConstraints.NORTH;
		gbc_panelTop.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelTop.insets = new Insets(0, 0, 5, 0);
		gbc_panelTop.gridx = 0;
		gbc_panelTop.gridy = 0;
		panelForTabbedPane.add(panelTop, gbc_panelTop);
		GridBagLayout gbl_panelTop = new GridBagLayout();
		gbl_panelTop.columnWidths = new int[] { 120, 72, 0 };
		gbl_panelTop.rowHeights = new int[] { 0, 0 };
		gbl_panelTop.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelTop.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelTop.setLayout(gbl_panelTop);

		JButton btnSourceFolder = new JButton(" Folder...");
		btnSourceFolder.setName(BTN_SOURCE_FOLDER);
		btnSourceFolder.addActionListener(identicAdapter);
		btnSourceFolder.setHorizontalAlignment(SwingConstants.LEFT);
		btnSourceFolder.setForeground(Color.BLACK);
		btnSourceFolder.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_btnSourceFolder = new GridBagConstraints();
		gbc_btnSourceFolder.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnSourceFolder.insets = new Insets(0, 0, 0, 5);
		gbc_btnSourceFolder.gridx = 0;
		gbc_btnSourceFolder.gridy = 0;
		panelTop.add(btnSourceFolder, gbc_btnSourceFolder);

		lblSourceFolder = new JLabel("");
		GridBagConstraints gbc_lblSourceFolder = new GridBagConstraints();
		gbc_lblSourceFolder.anchor = GridBagConstraints.WEST;
		gbc_lblSourceFolder.insets = new Insets(0, 0, 0, 5);
		gbc_lblSourceFolder.gridx = 1;
		gbc_lblSourceFolder.gridy = 0;
		panelTop.add(lblSourceFolder, gbc_lblSourceFolder);
		lblSourceFolder.setHorizontalAlignment(SwingConstants.CENTER);
		lblSourceFolder.setForeground(new Color(0, 0, 205));
		lblSourceFolder.setFont(new Font("Tahoma", Font.BOLD, 14));

		tpMain = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tpMain = new GridBagConstraints();
		gbc_tpMain.fill = GridBagConstraints.BOTH;
		gbc_tpMain.gridx = 0;
		gbc_tpMain.gridy = 1;
		panelForTabbedPane.add(tpMain, gbc_tpMain);

		JPanel tabSummary = new JPanel();
		tabSummary.setName(TAB_SUMMARY);
		tpMain.addTab("Summary", null, tabSummary, null);
		GridBagLayout gbl_tabSummary = new GridBagLayout();
		gbl_tabSummary.columnWidths = new int[] { 0, 0 };
		gbl_tabSummary.rowHeights = new int[] { 0, 0 };
		gbl_tabSummary.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_tabSummary.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		tabSummary.setLayout(gbl_tabSummary);

		splitPaneSummary = new JSplitPane();
		splitPaneSummary.setOneTouchExpandable(true);
		GridBagConstraints gbc_splitPaneSummary = new GridBagConstraints();
		gbc_splitPaneSummary.fill = GridBagConstraints.BOTH;
		gbc_splitPaneSummary.gridx = 0;
		gbc_splitPaneSummary.gridy = 0;
		tabSummary.add(splitPaneSummary, gbc_splitPaneSummary);

		JPanel panelLeftSummary = new JPanel();
		panelLeftSummary.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		splitPaneSummary.setLeftComponent(panelLeftSummary);
		GridBagLayout gbl_panelLeftSummary = new GridBagLayout();
		gbl_panelLeftSummary.columnWidths = new int[] { 70, 0 };
		gbl_panelLeftSummary.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelLeftSummary.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelLeftSummary.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panelLeftSummary.setLayout(gbl_panelLeftSummary);

		JPanel panelResults = new JPanel();
		panelResults.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Results",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panelResults = new GridBagConstraints();
		gbc_panelResults.insets = new Insets(0, 0, 5, 0);
		gbc_panelResults.fill = GridBagConstraints.BOTH;
		gbc_panelResults.gridx = 0;
		gbc_panelResults.gridy = 0;
		panelLeftSummary.add(panelResults, gbc_panelResults);
		GridBagLayout gbl_panelResults = new GridBagLayout();
		gbl_panelResults.columnWidths = new int[] { 0, 0 };
		gbl_panelResults.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelResults.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelResults.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, Double.MIN_VALUE };
		panelResults.setLayout(gbl_panelResults);

		Component verticalStrut_9 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_9 = new GridBagConstraints();
		gbc_verticalStrut_9.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_9.gridx = 0;
		gbc_verticalStrut_9.gridy = 0;
		panelResults.add(verticalStrut_9, gbc_verticalStrut_9);

		lblTotalFiles = new JLabel("Total Files");
		lblTotalFiles.setFont(new Font("Tahoma", Font.PLAIN, 10));
		GridBagConstraints gbc_lblTotalFiles = new GridBagConstraints();
		gbc_lblTotalFiles.insets = new Insets(0, 0, 5, 0);
		gbc_lblTotalFiles.gridx = 0;
		gbc_lblTotalFiles.gridy = 1;
		panelResults.add(lblTotalFiles, gbc_lblTotalFiles);

		Component verticalStrut_11 = Box.createVerticalStrut(5);
		GridBagConstraints gbc_verticalStrut_11 = new GridBagConstraints();
		gbc_verticalStrut_11.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_11.gridx = 0;
		gbc_verticalStrut_11.gridy = 2;
		panelResults.add(verticalStrut_11, gbc_verticalStrut_11);

		btnSummaryTargets = new JToggleButton("Target Files");
		btnSummaryTargets.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_btnSummaryTargets = new GridBagConstraints();
		gbc_btnSummaryTargets.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSummaryTargets.insets = new Insets(0, 0, 5, 0);
		gbc_btnSummaryTargets.gridx = 0;
		gbc_btnSummaryTargets.gridy = 3;
		panelResults.add(btnSummaryTargets, gbc_btnSummaryTargets);
		btnSummaryTargets.setName(BTN_SUMMARY_TARGETS);
		btnSummaryTargets.setActionCommand(BTN_AC_TARGETS);

		Component verticalStrut_12 = Box.createVerticalStrut(5);
		GridBagConstraints gbc_verticalStrut_12 = new GridBagConstraints();
		gbc_verticalStrut_12.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_12.gridx = 0;
		gbc_verticalStrut_12.gridy = 4;
		panelResults.add(verticalStrut_12, gbc_verticalStrut_12);

		btnSummaryDistinct = new JToggleButton("Distinct Files");
		btnSummaryDistinct.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_btnSummaryDistinct = new GridBagConstraints();
		gbc_btnSummaryDistinct.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSummaryDistinct.insets = new Insets(0, 0, 5, 0);
		gbc_btnSummaryDistinct.gridx = 0;
		gbc_btnSummaryDistinct.gridy = 5;
		panelResults.add(btnSummaryDistinct, gbc_btnSummaryDistinct);
		btnSummaryDistinct.setName(BTN_SUMMARY_DISTINCT);
		btnSummaryDistinct.setActionCommand(BTN_AC_DISTINCT);

		Component verticalStrut_13 = Box.createVerticalStrut(5);
		GridBagConstraints gbc_verticalStrut_13 = new GridBagConstraints();
		gbc_verticalStrut_13.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_13.gridx = 0;
		gbc_verticalStrut_13.gridy = 6;
		panelResults.add(verticalStrut_13, gbc_verticalStrut_13);

		btnSummaryUnique = new JToggleButton("Unique Files");
		btnSummaryUnique.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_btnSummaryUnique = new GridBagConstraints();
		gbc_btnSummaryUnique.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSummaryUnique.insets = new Insets(0, 0, 5, 0);
		gbc_btnSummaryUnique.gridx = 0;
		gbc_btnSummaryUnique.gridy = 7;
		panelResults.add(btnSummaryUnique, gbc_btnSummaryUnique);
		btnSummaryUnique.setName(BTN_SUMMARY_UNIQUE);
		btnSummaryUnique.setActionCommand(BTN_AC_UNIQUE);

		Component verticalStrut_14 = Box.createVerticalStrut(5);
		GridBagConstraints gbc_verticalStrut_14 = new GridBagConstraints();
		gbc_verticalStrut_14.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_14.gridx = 0;
		gbc_verticalStrut_14.gridy = 8;
		panelResults.add(verticalStrut_14, gbc_verticalStrut_14);

		btnSummaryDuplicates = new JToggleButton("Have Duplicates");
		btnSummaryDuplicates.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_btnSummaryDuplicates = new GridBagConstraints();
		gbc_btnSummaryDuplicates.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSummaryDuplicates.insets = new Insets(0, 0, 5, 0);
		gbc_btnSummaryDuplicates.gridx = 0;
		gbc_btnSummaryDuplicates.gridy = 9;
		panelResults.add(btnSummaryDuplicates, gbc_btnSummaryDuplicates);
		btnSummaryDuplicates.setName(BTN_SUMMARY_DUPLICATES);
		btnSummaryDuplicates.setActionCommand(BTN_AC_DUPLICATES);

		Component verticalStrut_15 = Box.createVerticalStrut(10);
		GridBagConstraints gbc_verticalStrut_15 = new GridBagConstraints();
		gbc_verticalStrut_15.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_15.gridx = 0;
		gbc_verticalStrut_15.gridy = 10;
		panelResults.add(verticalStrut_15, gbc_verticalStrut_15);

		btnSummaryExcluded = new JToggleButton("Total Excluded");
		btnSummaryExcluded.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_btnSummaryExcluded = new GridBagConstraints();
		gbc_btnSummaryExcluded.insets = new Insets(0, 0, 5, 0);
		gbc_btnSummaryExcluded.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSummaryExcluded.gridx = 0;
		gbc_btnSummaryExcluded.gridy = 11;
		panelResults.add(btnSummaryExcluded, gbc_btnSummaryExcluded);
		btnSummaryExcluded.setName(BTN_SUMMARY_EXCLUDED);
		btnSummaryExcluded.setActionCommand(BTN_AC_EXCLUDED);

		Component verticalStrut_16 = Box.createVerticalStrut(10);
		GridBagConstraints gbc_verticalStrut_16 = new GridBagConstraints();
		gbc_verticalStrut_16.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_16.gridx = 0;
		gbc_verticalStrut_16.gridy = 12;
		panelResults.add(verticalStrut_16, gbc_verticalStrut_16);

		btnPrintResults = new JButton("Print Results");
		btnPrintResults.setName(BTN_PRINT_RESULTS);
		btnPrintResults.addActionListener(identicAdapter);
		GridBagConstraints gbc_btnPrintResults = new GridBagConstraints();
		gbc_btnPrintResults.gridx = 0;
		gbc_btnPrintResults.gridy = 13;
		panelResults.add(btnPrintResults, gbc_btnPrintResults);

		btnSummaryExcluded.addActionListener(identicAdapter);
		btnSummaryDuplicates.addActionListener(identicAdapter);
		btnSummaryUnique.addActionListener(identicAdapter);
		btnSummaryDistinct.addActionListener(identicAdapter);
		btnSummaryTargets.addActionListener(identicAdapter);

		JPanel panelRightSummary = new JPanel();
		splitPaneSummary.setRightComponent(panelRightSummary);
		GridBagLayout gbl_panelRightSummary = new GridBagLayout();
		gbl_panelRightSummary.columnWidths = new int[] { 0, 0 };
		gbl_panelRightSummary.rowHeights = new int[] { 0, 0 };
		gbl_panelRightSummary.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelRightSummary.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelRightSummary.setLayout(gbl_panelRightSummary);

		JScrollPane scrollPaneSummary = new JScrollPane();
		scrollPaneSummary.setViewportView(resultsTable);

		GridBagConstraints gbc_scrollPaneSummary = new GridBagConstraints();
		gbc_scrollPaneSummary.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneSummary.gridx = 0;
		gbc_scrollPaneSummary.gridy = 0;
		panelRightSummary.add(scrollPaneSummary, gbc_scrollPaneSummary);

		lblSummaryLegend = new JLabel("Summary Results");
		lblSummaryLegend.setForeground(Color.BLUE);
		lblSummaryLegend.setFont(new Font("Tahoma", Font.BOLD, 13));
		scrollPaneSummary.setColumnHeaderView(lblSummaryLegend);
		splitPaneSummary.setDividerLocation(200);

		JSplitPane tabActions = new JSplitPane();
		tpMain.addTab("Actions", null, tabActions, null);

		JPanel panelLeftActions = new JPanel();
		tabActions.setLeftComponent(panelLeftActions);
		GridBagLayout gbl_panelLeftActions = new GridBagLayout();
		gbl_panelLeftActions.columnWidths = new int[] { 0, 0 };
		gbl_panelLeftActions.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_panelLeftActions.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelLeftActions.rowWeights = new double[] { 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE };
		panelLeftActions.setLayout(gbl_panelLeftActions);

		Component verticalStrut_10 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_10 = new GridBagConstraints();
		gbc_verticalStrut_10.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_10.gridx = 0;
		gbc_verticalStrut_10.gridy = 0;
		panelLeftActions.add(verticalStrut_10, gbc_verticalStrut_10);

		JPanel panelLoadResults = new JPanel();
		panelLoadResults.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelLoadResults = new GridBagConstraints();
		gbc_panelLoadResults.insets = new Insets(0, 0, 5, 0);
		gbc_panelLoadResults.fill = GridBagConstraints.BOTH;
		gbc_panelLoadResults.gridx = 0;
		gbc_panelLoadResults.gridy = 1;
		panelLeftActions.add(panelLoadResults, gbc_panelLoadResults);
		GridBagLayout gbl_panelLoadResults = new GridBagLayout();
		gbl_panelLoadResults.columnWidths = new int[] { 0, 0 };
		gbl_panelLoadResults.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelLoadResults.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelLoadResults.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelLoadResults.setLayout(gbl_panelLoadResults);

		JButton btnActionLoadResults = new JButton("Load Results");
		btnActionLoadResults.setName(BTN_ACTION_LOAD_RESULTS);
		btnActionLoadResults.addActionListener(identicAdapter);
		GridBagConstraints gbc_btnActionLoadResults = new GridBagConstraints();
		gbc_btnActionLoadResults.insets = new Insets(0, 0, 5, 0);
		gbc_btnActionLoadResults.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnActionLoadResults.gridx = 0;
		gbc_btnActionLoadResults.gridy = 1;
		panelLoadResults.add(btnActionLoadResults, gbc_btnActionLoadResults);

		rbLoadTargetFiles = new JRadioButton("Target Files");
		rbLoadTargetFiles.setActionCommand(RB_AC_TARGETS);
		GridBagConstraints gbc_rbLoadTargetFiles = new GridBagConstraints();
		gbc_rbLoadTargetFiles.insets = new Insets(0, 0, 5, 0);
		gbc_rbLoadTargetFiles.gridx = 0;
		gbc_rbLoadTargetFiles.gridy = 3;
		panelLoadResults.add(rbLoadTargetFiles, gbc_rbLoadTargetFiles);

		rbLoadDistinctFiles = new JRadioButton("Distinct Files");
		rbLoadDistinctFiles.setActionCommand(RB_AC_DISTINCT);
		GridBagConstraints gbc_rbLoadDistinctFiles = new GridBagConstraints();
		gbc_rbLoadDistinctFiles.insets = new Insets(0, 0, 5, 0);
		gbc_rbLoadDistinctFiles.gridx = 0;
		gbc_rbLoadDistinctFiles.gridy = 4;
		panelLoadResults.add(rbLoadDistinctFiles, gbc_rbLoadDistinctFiles);

		rbLoadUniqueFiles = new JRadioButton("Unique Files");
		rbLoadUniqueFiles.setActionCommand(RB_AC_UNIQUE);
		GridBagConstraints gbc_rbLoadUniqueFiles = new GridBagConstraints();
		gbc_rbLoadUniqueFiles.insets = new Insets(0, 0, 5, 0);
		gbc_rbLoadUniqueFiles.anchor = GridBagConstraints.BASELINE;
		gbc_rbLoadUniqueFiles.gridx = 0;
		gbc_rbLoadUniqueFiles.gridy = 5;
		panelLoadResults.add(rbLoadUniqueFiles, gbc_rbLoadUniqueFiles);

		rbLoadHaveDuplicates = new JRadioButton("Have Duplicates");
		rbLoadHaveDuplicates.setActionCommand(RB_AC_DUPLICATES);
		GridBagConstraints gbc_rbLoadHaveDuplicates = new GridBagConstraints();
		gbc_rbLoadHaveDuplicates.insets = new Insets(0, 0, 5, 0);
		gbc_rbLoadHaveDuplicates.gridx = 0;
		gbc_rbLoadHaveDuplicates.gridy = 6;
		panelLoadResults.add(rbLoadHaveDuplicates, gbc_rbLoadHaveDuplicates);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Actions",

				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 3;
		panelLeftActions.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_2.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);

		JButton btnActionCopy = new JButton("Copy");
		btnActionCopy.setName(BTN_ACTION_COPY);
		btnActionCopy.addActionListener(identicAdapter);
		GridBagConstraints gbc_btnActionCopy = new GridBagConstraints();
		gbc_btnActionCopy.insets = new Insets(0, 0, 5, 0);
		gbc_btnActionCopy.gridx = 0;
		gbc_btnActionCopy.gridy = 1;
		panel_2.add(btnActionCopy, gbc_btnActionCopy);

		JButton btnActionMove = new JButton("Move");
		btnActionMove.setName(BTN_ACTION_MOVE);
		btnActionMove.addActionListener(identicAdapter);
		GridBagConstraints gbc_btnActionMove = new GridBagConstraints();
		gbc_btnActionMove.insets = new Insets(0, 0, 5, 0);
		gbc_btnActionMove.gridx = 0;
		gbc_btnActionMove.gridy = 3;
		panel_2.add(btnActionMove, gbc_btnActionMove);

		JButton btnActionDelete = new JButton("Delete");
		btnActionDelete.setName(BTN_ACTION_DELETE);
		btnActionDelete.addActionListener(identicAdapter);
		GridBagConstraints gbc_btnActionDelete = new GridBagConstraints();
		gbc_btnActionDelete.gridx = 0;
		gbc_btnActionDelete.gridy = 5;
		panel_2.add(btnActionDelete, gbc_btnActionDelete);

		JPanel panelRightActions = new JPanel();
		tabActions.setRightComponent(panelRightActions);
		GridBagLayout gbl_panelRightActions = new GridBagLayout();
		gbl_panelRightActions.columnWidths = new int[] { 0, 0 };
		gbl_panelRightActions.rowHeights = new int[] { 0, 0 };
		gbl_panelRightActions.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelRightActions.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelRightActions.setLayout(gbl_panelRightActions);

		JScrollPane scrollPaneActions = new JScrollPane();

		scrollPaneActions.setViewportView(actionTable);
		GridBagConstraints gbc_scrollPaneActions = new GridBagConstraints();
		gbc_scrollPaneActions.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneActions.gridx = 0;
		gbc_scrollPaneActions.gridy = 0;
		panelRightActions.add(scrollPaneActions, gbc_scrollPaneActions);
		popupActions = new JPopupMenu();
		addPopup(actionTable, popupActions);

		JMenuItem popupActionsSelect = new JMenuItem("Select");
		popupActionsSelect.setName(PUM_ACTION_SELECT);
		popupActionsSelect.setActionCommand(PUM_ACTION_SELECT);
		popupActionsSelect.addActionListener(actionAdaper);
		popupActions.add(popupActionsSelect);

		JMenuItem popupActionsDeselect = new JMenuItem("Deselect");
		popupActionsDeselect.setName(PUM_ACTION_DESELECT);
		popupActionsDeselect.setActionCommand(PUM_ACTION_DESELECT);
		popupActionsDeselect.addActionListener(actionAdaper);
		popupActions.add(popupActionsDeselect);

		tabActions.setDividerLocation(200);

		JPanel tabTypes = new JPanel();
		tabTypes.setName(TAB_TYPES);
		tpMain.addTab("File Types", null, tabTypes, "Manage and see the file types lists");
		GridBagLayout gbl_tabTypes = new GridBagLayout();
		gbl_tabTypes.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_tabTypes.rowHeights = new int[] { 0, 0, 0 };
		gbl_tabTypes.columnWeights = new double[] { 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_tabTypes.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		tabTypes.setLayout(gbl_tabTypes);

		JPanel panelListsAvailable = new JPanel();
		GridBagConstraints gbc_panelListsAvailable = new GridBagConstraints();
		gbc_panelListsAvailable.insets = new Insets(0, 0, 5, 5);
		gbc_panelListsAvailable.fill = GridBagConstraints.BOTH;
		gbc_panelListsAvailable.gridx = 0;
		gbc_panelListsAvailable.gridy = 0;
		tabTypes.add(panelListsAvailable, gbc_panelListsAvailable);
		GridBagLayout gbl_panelListsAvailable = new GridBagLayout();
		gbl_panelListsAvailable.columnWidths = new int[] { 0, 0 };
		gbl_panelListsAvailable.rowHeights = new int[] { 0, 0 };
		gbl_panelListsAvailable.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelListsAvailable.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelListsAvailable.setLayout(gbl_panelListsAvailable);

		JScrollPane scrollListsAvailable = new JScrollPane();
		GridBagConstraints gbc_scrollListsAvailable = new GridBagConstraints();
		gbc_scrollListsAvailable.fill = GridBagConstraints.BOTH;
		gbc_scrollListsAvailable.gridx = 0;
		gbc_scrollListsAvailable.gridy = 0;
		panelListsAvailable.add(scrollListsAvailable, gbc_scrollListsAvailable);

		JLabel label_1 = new JLabel("Available Lists");
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		label_1.setForeground(new Color(139, 69, 19));
		label_1.setFont(new Font("Arial", Font.BOLD, 14));
		scrollListsAvailable.setColumnHeaderView(label_1);

		listTypesAvailable = new JList<String>();
		listTypesAvailable.addMouseListener(manageListsAdapter);
		listTypesAvailable.setName(LIST_TYPES_AVAILABLE);

		scrollListsAvailable.setViewportView(listTypesAvailable);

		JPanel panelListActions = new JPanel();
		panelListActions.setPreferredSize(new Dimension(140, 0));
		panelListActions.setMinimumSize(new Dimension(140, 0));
		panelListActions.setMaximumSize(new Dimension(140, 0));
		GridBagConstraints gbc_panelListActions = new GridBagConstraints();
		gbc_panelListActions.insets = new Insets(0, 0, 5, 5);
		gbc_panelListActions.fill = GridBagConstraints.BOTH;
		gbc_panelListActions.gridx = 1;
		gbc_panelListActions.gridy = 0;
		tabTypes.add(panelListActions, gbc_panelListActions);
		GridBagLayout gbl_panelListActions = new GridBagLayout();
		gbl_panelListActions.columnWidths = new int[] { 0, 0 };
		gbl_panelListActions.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelListActions.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelListActions.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panelListActions.setLayout(gbl_panelListActions);

		Component verticalStrut = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut.gridx = 0;
		gbc_verticalStrut.gridy = 0;
		panelListActions.add(verticalStrut, gbc_verticalStrut);

		JPanel panelListActions_1 = new JPanel();
		GridBagConstraints gbc_panelListActions_1 = new GridBagConstraints();
		gbc_panelListActions_1.fill = GridBagConstraints.BOTH;
		gbc_panelListActions_1.gridx = 0;
		gbc_panelListActions_1.gridy = 1;
		panelListActions.add(panelListActions_1, gbc_panelListActions_1);
		GridBagLayout gbl_panelListActions_1 = new GridBagLayout();
		gbl_panelListActions_1.columnWidths = new int[] { 231, 0 };
		gbl_panelListActions_1.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelListActions_1.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelListActions_1.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		panelListActions_1.setLayout(gbl_panelListActions_1);

		txtActive = new JTextField();
		txtActive.addFocusListener(manageListsAdapter);
		txtActive.setName(TXT_ACTIVE);

		txtActive.setPreferredSize(new Dimension(40, 23));
		txtActive.setMaximumSize(new Dimension(40, 23));
		// txtActive.setName("txtActive");
		txtActive.setHorizontalAlignment(SwingConstants.CENTER);
		txtActive.setColumns(10);
		GridBagConstraints gbc_txtActive = new GridBagConstraints();
		gbc_txtActive.anchor = GridBagConstraints.NORTH;
		gbc_txtActive.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtActive.insets = new Insets(0, 0, 5, 0);
		gbc_txtActive.gridx = 0;
		gbc_txtActive.gridy = 1;
		panelListActions_1.add(txtActive, gbc_txtActive);

		Component verticalStrut_1 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_1 = new GridBagConstraints();
		gbc_verticalStrut_1.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_1.gridx = 0;
		gbc_verticalStrut_1.gridy = 2;
		panelListActions_1.add(verticalStrut_1, gbc_verticalStrut_1);

		btnLoad = new JButton("Load");
		btnLoad.setName(BTN_LOAD);
		btnLoad.addActionListener(manageListsAdapter);
		btnLoad.setPreferredSize(new Dimension(63, 23));
		btnLoad.setName("btnLoad");
		btnLoad.setMinimumSize(new Dimension(63, 23));
		btnLoad.setMaximumSize(new Dimension(63, 23));
		GridBagConstraints gbc_btnLoad = new GridBagConstraints();
		gbc_btnLoad.insets = new Insets(0, 0, 5, 0);
		gbc_btnLoad.gridx = 0;
		gbc_btnLoad.gridy = 3;
		panelListActions_1.add(btnLoad, gbc_btnLoad);

		Component verticalStrut_2 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_2 = new GridBagConstraints();
		gbc_verticalStrut_2.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_2.gridx = 0;
		gbc_verticalStrut_2.gridy = 4;
		panelListActions_1.add(verticalStrut_2, gbc_verticalStrut_2);

		btnNew = new JButton("New");
		btnNew.setName(BTN_NEW);
		btnNew.addActionListener(manageListsAdapter);
		btnNew.setPreferredSize(new Dimension(63, 23));
		btnNew.setName("btnNew");
		btnNew.setMinimumSize(new Dimension(63, 23));
		btnNew.setMaximumSize(new Dimension(63, 23));
		GridBagConstraints gbc_btnNew = new GridBagConstraints();
		gbc_btnNew.insets = new Insets(0, 0, 5, 0);
		gbc_btnNew.gridx = 0;
		gbc_btnNew.gridy = 5;
		panelListActions_1.add(btnNew, gbc_btnNew);

		Component verticalStrut_3 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_3 = new GridBagConstraints();
		gbc_verticalStrut_3.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_3.gridx = 0;
		gbc_verticalStrut_3.gridy = 6;
		panelListActions_1.add(verticalStrut_3, gbc_verticalStrut_3);

		btnSave = new JButton("Save");
		btnSave.setName(BTN_SAVE);
		btnSave.addActionListener(manageListsAdapter);
		btnSave.setPreferredSize(new Dimension(63, 23));
		btnSave.setName("btnSave");
		btnSave.setMinimumSize(new Dimension(63, 23));
		btnSave.setMaximumSize(new Dimension(63, 23));
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.insets = new Insets(0, 0, 5, 0);
		gbc_btnSave.gridx = 0;
		gbc_btnSave.gridy = 7;
		panelListActions_1.add(btnSave, gbc_btnSave);

		Component verticalStrut_4 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_4 = new GridBagConstraints();
		gbc_verticalStrut_4.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_4.gridx = 0;
		gbc_verticalStrut_4.gridy = 8;
		panelListActions_1.add(verticalStrut_4, gbc_verticalStrut_4);

		btnDelete = new JButton("Delete");
		btnDelete.setName(BTN_DELETE);
		btnDelete.addActionListener(manageListsAdapter);
		btnDelete.setName("btnDelete");
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.gridx = 0;
		gbc_btnDelete.gridy = 9;
		panelListActions_1.add(btnDelete, gbc_btnDelete);

		JPanel panelListContents = new JPanel();
		GridBagConstraints gbc_panelListContents = new GridBagConstraints();
		gbc_panelListContents.insets = new Insets(0, 0, 5, 0);
		gbc_panelListContents.fill = GridBagConstraints.BOTH;
		gbc_panelListContents.gridx = 2;
		gbc_panelListContents.gridy = 0;
		tabTypes.add(panelListContents, gbc_panelListContents);
		GridBagLayout gbl_panelListContents = new GridBagLayout();
		gbl_panelListContents.columnWidths = new int[] { 0, 0 };
		gbl_panelListContents.rowHeights = new int[] { 0, 0 };
		gbl_panelListContents.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelListContents.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelListContents.setLayout(gbl_panelListContents);

		JScrollPane scrollListContents = new JScrollPane();
		GridBagConstraints gbc_scrollListContents = new GridBagConstraints();
		gbc_scrollListContents.fill = GridBagConstraints.BOTH;
		gbc_scrollListContents.gridx = 0;
		gbc_scrollListContents.gridy = 0;
		panelListContents.add(scrollListContents, gbc_scrollListContents);

		lblListEdit = new JLabel("<none>");
		lblListEdit.setHorizontalAlignment(SwingConstants.CENTER);
		lblListEdit.setForeground(new Color(139, 69, 19));
		lblListEdit.setFont(new Font("Arial", Font.BOLD, 14));
		scrollListContents.setColumnHeaderView(lblListEdit);

		listTypesEdit = new JList<String>();
		listTypesEdit.addListSelectionListener(manageListsAdapter);

		// listEdit.addMouseListener(manageListsAdapter);
		listTypesEdit.setName(LIST_TYPES_EDIT);

		scrollListContents.setViewportView(listTypesEdit);

		JPanel panelMakeListActive = new JPanel();
		GridBagConstraints gbc_panelMakeListActive = new GridBagConstraints();
		gbc_panelMakeListActive.insets = new Insets(0, 0, 0, 5);
		gbc_panelMakeListActive.fill = GridBagConstraints.BOTH;
		gbc_panelMakeListActive.gridx = 0;
		gbc_panelMakeListActive.gridy = 1;
		tabTypes.add(panelMakeListActive, gbc_panelMakeListActive);
		GridBagLayout gbl_panelMakeListActive = new GridBagLayout();
		gbl_panelMakeListActive.columnWidths = new int[] { 0, 0 };
		gbl_panelMakeListActive.rowHeights = new int[] { 0, 0 };
		gbl_panelMakeListActive.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelMakeListActive.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelMakeListActive.setLayout(gbl_panelMakeListActive);

		JButton btnMakeListActive = new JButton("Make List Active");
		btnMakeListActive.setName(BTN_MAKE_LIST_ACTIVE);
		btnMakeListActive.addActionListener(manageListsAdapter);
		GridBagConstraints gbc_btnMakeListActive = new GridBagConstraints();
		gbc_btnMakeListActive.gridx = 0;
		gbc_btnMakeListActive.gridy = 0;
		panelMakeListActive.add(btnMakeListActive, gbc_btnMakeListActive);

		JPanel panelEditList = new JPanel();
		panelEditList
				.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Edit", TitledBorder.CENTER,

						TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panelEditList = new GridBagConstraints();
		gbc_panelEditList.fill = GridBagConstraints.BOTH;
		gbc_panelEditList.gridx = 2;
		gbc_panelEditList.gridy = 1;
		tabTypes.add(panelEditList, gbc_panelEditList);
		GridBagLayout gbl_panelEditList = new GridBagLayout();
		gbl_panelEditList.columnWidths = new int[] { 0, 0 };
		gbl_panelEditList.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelEditList.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelEditList.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelEditList.setLayout(gbl_panelEditList);

		btnAddRemove = new JButton(EDIT_ADD_REMOVE);
		btnAddRemove.addActionListener(manageListsAdapter);
		btnAddRemove.setName(BTN_ADD_REMOVE);

		GridBagConstraints gbc_btnAddRemove = new GridBagConstraints();
		gbc_btnAddRemove.insets = new Insets(0, 0, 5, 0);
		gbc_btnAddRemove.gridx = 0;
		gbc_btnAddRemove.gridy = 0;
		panelEditList.add(btnAddRemove, gbc_btnAddRemove);

		txtEdit = new JTextField();
		txtEdit.setName(TXT_EDIT);
		txtEdit.addFocusListener(manageListsAdapter);
		txtEdit.setHorizontalAlignment(SwingConstants.CENTER);
		txtEdit.setColumns(10);
		GridBagConstraints gbc_txtEdit = new GridBagConstraints();
		gbc_txtEdit.anchor = GridBagConstraints.NORTH;
		gbc_txtEdit.gridx = 0;
		gbc_txtEdit.gridy = 1;
		panelEditList.add(txtEdit, gbc_txtEdit);

		JPanel tabCatalogs = new JPanel();
		tabCatalogs.setName(TAB_CATALOGS);
		tpMain.addTab("Catalogs", null, tabCatalogs, "View and manipulate the catalogs");
		GridBagLayout gbl_tabCatalogs = new GridBagLayout();
		gbl_tabCatalogs.columnWidths = new int[] { 0, 0 };
		gbl_tabCatalogs.rowHeights = new int[] { 0, 120, 0 };
		gbl_tabCatalogs.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_tabCatalogs.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		tabCatalogs.setLayout(gbl_tabCatalogs);

		JPanel panelCatalogAction = new JPanel();
		GridBagConstraints gbc_panelCatalogAction = new GridBagConstraints();
		gbc_panelCatalogAction.insets = new Insets(0, 0, 5, 0);
		gbc_panelCatalogAction.fill = GridBagConstraints.BOTH;
		gbc_panelCatalogAction.gridx = 0;
		gbc_panelCatalogAction.gridy = 0;
		tabCatalogs.add(panelCatalogAction, gbc_panelCatalogAction);
		GridBagLayout gbl_panelCatalogAction = new GridBagLayout();
		gbl_panelCatalogAction.columnWidths = new int[] { 80, 100, 0, 100, 0 };
		gbl_panelCatalogAction.rowHeights = new int[] { 0, 0 };
		gbl_panelCatalogAction.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelCatalogAction.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelCatalogAction.setLayout(gbl_panelCatalogAction);

		JPanel panelCatalogActions = new JPanel();
		GridBagConstraints gbc_panelCatalogActions = new GridBagConstraints();
		gbc_panelCatalogActions.insets = new Insets(0, 0, 0, 5);
		gbc_panelCatalogActions.fill = GridBagConstraints.BOTH;
		gbc_panelCatalogActions.gridx = 0;
		gbc_panelCatalogActions.gridy = 0;
		panelCatalogAction.add(panelCatalogActions, gbc_panelCatalogActions);
		GridBagLayout gbl_panelCatalogActions = new GridBagLayout();
		gbl_panelCatalogActions.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelCatalogActions.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelCatalogActions.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelCatalogActions.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panelCatalogActions.setLayout(gbl_panelCatalogActions);

		Component verticalStrut_5 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_5 = new GridBagConstraints();
		gbc_verticalStrut_5.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_5.gridx = 1;
		gbc_verticalStrut_5.gridy = 0;
		panelCatalogActions.add(verticalStrut_5, gbc_verticalStrut_5);

		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		GridBagConstraints gbc_horizontalStrut_1 = new GridBagConstraints();
		gbc_horizontalStrut_1.insets = new Insets(0, 0, 0, 5);
		gbc_horizontalStrut_1.gridx = 0;
		gbc_horizontalStrut_1.gridy = 1;
		panelCatalogActions.add(horizontalStrut_1, gbc_horizontalStrut_1);

		JPanel panelCatalogButtons = new JPanel();
		GridBagConstraints gbc_panelCatalogButtons = new GridBagConstraints();
		gbc_panelCatalogButtons.fill = GridBagConstraints.BOTH;
		gbc_panelCatalogButtons.gridx = 1;
		gbc_panelCatalogButtons.gridy = 1;
		panelCatalogActions.add(panelCatalogButtons, gbc_panelCatalogButtons);
		GridBagLayout gbl_panelCatalogButtons = new GridBagLayout();
		gbl_panelCatalogButtons.columnWidths = new int[] { 0, 70, 0 };
		gbl_panelCatalogButtons.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelCatalogButtons.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelCatalogButtons.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		panelCatalogButtons.setLayout(gbl_panelCatalogButtons);

		JButton btnCatalogReload = new JButton("Reload");
		btnCatalogReload.setName(BTN_CATALOG_RELOAD);
		btnCatalogReload.addActionListener(catalogAdapter);
		GridBagConstraints gbc_btnCatalogReload = new GridBagConstraints();
		gbc_btnCatalogReload.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCatalogReload.insets = new Insets(0, 0, 5, 0);
		gbc_btnCatalogReload.gridx = 1;
		gbc_btnCatalogReload.gridy = 1;
		panelCatalogButtons.add(btnCatalogReload, gbc_btnCatalogReload);

		JButton btnCatalogNew = new JButton("Save...");
		btnCatalogNew.setName(BTN_CATALOG_SAVE);
		btnCatalogNew.addActionListener(catalogAdapter);
		GridBagConstraints gbc_btnCatalogNew = new GridBagConstraints();
		gbc_btnCatalogNew.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCatalogNew.insets = new Insets(0, 0, 5, 0);
		gbc_btnCatalogNew.gridx = 1;
		gbc_btnCatalogNew.gridy = 3;
		panelCatalogButtons.add(btnCatalogNew, gbc_btnCatalogNew);

		JButton btnCatalogImport = new JButton("Import");
		btnCatalogImport.setName(BTN_CATALOG_IMPORT);
		btnCatalogImport.addActionListener(catalogAdapter);
		GridBagConstraints gbc_btnCatalogImport = new GridBagConstraints();
		gbc_btnCatalogImport.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCatalogImport.insets = new Insets(0, 0, 5, 0);
		gbc_btnCatalogImport.gridx = 1;
		gbc_btnCatalogImport.gridy = 7;
		panelCatalogButtons.add(btnCatalogImport, gbc_btnCatalogImport);

		JButton btnCatalogExport = new JButton("Export");
		btnCatalogExport.setName(BTN_CATALOG_EXPORT);
		btnCatalogExport.addActionListener(catalogAdapter);
		GridBagConstraints gbc_btnCatalogExport = new GridBagConstraints();
		gbc_btnCatalogExport.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCatalogExport.insets = new Insets(0, 0, 5, 0);
		gbc_btnCatalogExport.gridx = 1;
		gbc_btnCatalogExport.gridy = 9;
		panelCatalogButtons.add(btnCatalogExport, gbc_btnCatalogExport);

		JButton btnCatalogRemove = new JButton("Remove");
		btnCatalogRemove.setName(BTN_CATALOG_REMOVE);
		btnCatalogRemove.addActionListener(catalogAdapter);
		GridBagConstraints gbc_btnCatalogRemove = new GridBagConstraints();
		gbc_btnCatalogRemove.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCatalogRemove.gridx = 1;
		gbc_btnCatalogRemove.gridy = 11;
		panelCatalogButtons.add(btnCatalogRemove, gbc_btnCatalogRemove);

		JPanel panelCatalogInUse = new JPanel();
		panelCatalogInUse.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelCatalogInUse = new GridBagConstraints();
		gbc_panelCatalogInUse.insets = new Insets(0, 0, 0, 5);
		gbc_panelCatalogInUse.fill = GridBagConstraints.BOTH;
		gbc_panelCatalogInUse.gridx = 1;
		gbc_panelCatalogInUse.gridy = 0;
		panelCatalogAction.add(panelCatalogInUse, gbc_panelCatalogInUse);
		GridBagLayout gbl_panelCatalogInUse = new GridBagLayout();
		gbl_panelCatalogInUse.columnWidths = new int[] { 0, 0 };
		gbl_panelCatalogInUse.rowHeights = new int[] { 0, 0 };
		gbl_panelCatalogInUse.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelCatalogInUse.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelCatalogInUse.setLayout(gbl_panelCatalogInUse);

		JScrollPane scrollPaneCatalogInUse = new JScrollPane();
		GridBagConstraints gbc_scrollPaneCatalogInUse = new GridBagConstraints();
		gbc_scrollPaneCatalogInUse.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneCatalogInUse.gridx = 0;
		gbc_scrollPaneCatalogInUse.gridy = 0;
		panelCatalogInUse.add(scrollPaneCatalogInUse, gbc_scrollPaneCatalogInUse);

		// lstCatalogInUse = new JList();
		lstCatalogInUse = new JList<CatalogItem>(inUseCatalogItemModel);
		lstCatalogInUse.addListSelectionListener(catalogAdapter);

		scrollPaneCatalogInUse.setViewportView(lstCatalogInUse);

		JLabel label_2 = new JLabel("In Use");
		label_2.setMaximumSize(new Dimension(43, 14));
		label_2.setMinimumSize(new Dimension(43, 14));
		label_2.setPreferredSize(new Dimension(43, 14));
		label_2.setAlignmentX(Component.CENTER_ALIGNMENT);
		label_2.setHorizontalAlignment(SwingConstants.CENTER);
		label_2.setForeground(Color.BLUE);
		label_2.setFont(new Font("Tahoma", Font.BOLD, 14));
		scrollPaneCatalogInUse.setColumnHeaderView(label_2);

		Component horizontalGlue = Box.createHorizontalGlue();
		GridBagConstraints gbc_horizontalGlue = new GridBagConstraints();
		gbc_horizontalGlue.insets = new Insets(0, 0, 0, 5);
		gbc_horizontalGlue.gridx = 2;
		gbc_horizontalGlue.gridy = 0;
		panelCatalogAction.add(horizontalGlue, gbc_horizontalGlue);

		JPanel panelCatalogAvailable = new JPanel();
		panelCatalogAvailable.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelCatalogAvailable = new GridBagConstraints();
		gbc_panelCatalogAvailable.fill = GridBagConstraints.BOTH;
		gbc_panelCatalogAvailable.gridx = 3;
		gbc_panelCatalogAvailable.gridy = 0;
		panelCatalogAction.add(panelCatalogAvailable, gbc_panelCatalogAvailable);
		GridBagLayout gbl_panelCatalogAvailable = new GridBagLayout();
		gbl_panelCatalogAvailable.columnWidths = new int[] { 0, 0 };
		gbl_panelCatalogAvailable.rowHeights = new int[] { 0, 0 };
		gbl_panelCatalogAvailable.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelCatalogAvailable.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelCatalogAvailable.setLayout(gbl_panelCatalogAvailable);

		JScrollPane scrollPaneCatalogAvailable = new JScrollPane();
		GridBagConstraints gbc_scrollPaneCatalogAvailable = new GridBagConstraints();
		gbc_scrollPaneCatalogAvailable.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneCatalogAvailable.gridx = 0;
		gbc_scrollPaneCatalogAvailable.gridy = 0;
		panelCatalogAvailable.add(scrollPaneCatalogAvailable, gbc_scrollPaneCatalogAvailable);

		lstCatalogAvailable = new JList<CatalogItem>(availableCatalogItemModel);
		lstCatalogAvailable.addListSelectionListener(catalogAdapter);
		scrollPaneCatalogAvailable.setViewportView(lstCatalogAvailable);

		JLabel label_3 = new JLabel("Available");
		label_3.setAlignmentX(Component.CENTER_ALIGNMENT);
		label_3.setHorizontalAlignment(SwingConstants.CENTER);
		label_3.setForeground(Color.BLUE);
		label_3.setFont(new Font("Tahoma", Font.BOLD, 14));
		scrollPaneCatalogAvailable.setColumnHeaderView(label_3);

		JPanel panelCatalogInfo = new JPanel();
		GridBagConstraints gbc_panelCatalogInfo = new GridBagConstraints();
		gbc_panelCatalogInfo.fill = GridBagConstraints.BOTH;
		gbc_panelCatalogInfo.gridx = 0;
		gbc_panelCatalogInfo.gridy = 1;
		tabCatalogs.add(panelCatalogInfo, gbc_panelCatalogInfo);
		GridBagLayout gbl_panelCatalogInfo = new GridBagLayout();
		gbl_panelCatalogInfo.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelCatalogInfo.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_panelCatalogInfo.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelCatalogInfo.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelCatalogInfo.setLayout(gbl_panelCatalogInfo);

		lblCatalogName = new JLabel("Name");
		lblCatalogName.setHorizontalAlignment(SwingConstants.LEFT);
		lblCatalogName.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblCatalogName = new GridBagConstraints();
		gbc_lblCatalogName.insets = new Insets(0, 0, 5, 0);
		gbc_lblCatalogName.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblCatalogName.gridx = 1;
		gbc_lblCatalogName.gridy = 0;
		panelCatalogInfo.add(lblCatalogName, gbc_lblCatalogName);

		lblCatalogDescription = new JLabel("Description");
		lblCatalogDescription.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblCatalogDescription = new GridBagConstraints();
		gbc_lblCatalogDescription.anchor = GridBagConstraints.WEST;
		gbc_lblCatalogDescription.insets = new Insets(0, 0, 5, 0);
		gbc_lblCatalogDescription.gridx = 1;
		gbc_lblCatalogDescription.gridy = 1;
		panelCatalogInfo.add(lblCatalogDescription, gbc_lblCatalogDescription);

		lblCatalogCount = new JLabel("Count");
		lblCatalogCount.setHorizontalAlignment(SwingConstants.LEFT);
		lblCatalogCount.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblCatalogCount = new GridBagConstraints();
		gbc_lblCatalogCount.anchor = GridBagConstraints.WEST;
		gbc_lblCatalogCount.insets = new Insets(0, 0, 5, 0);
		gbc_lblCatalogCount.gridx = 1;
		gbc_lblCatalogCount.gridy = 2;
		panelCatalogInfo.add(lblCatalogCount, gbc_lblCatalogCount);

		lblCatalogDirectory = new JLabel("Directory");
		lblCatalogDirectory.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblCatalogDirectory = new GridBagConstraints();
		gbc_lblCatalogDirectory.anchor = GridBagConstraints.WEST;
		gbc_lblCatalogDirectory.gridx = 1;
		gbc_lblCatalogDirectory.gridy = 3;
		panelCatalogInfo.add(lblCatalogDirectory, gbc_lblCatalogDirectory);

		JPanel tabUtilities = new JPanel();
		tpMain.addTab("Utilities", null, tabUtilities, null);
		GridBagLayout gbl_tabUtilities = new GridBagLayout();
		gbl_tabUtilities.columnWidths = new int[] { 0, 0 };
		gbl_tabUtilities.rowHeights = new int[] { 0, 0 };
		gbl_tabUtilities.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_tabUtilities.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		tabUtilities.setLayout(gbl_tabUtilities);

		JSplitPane splitPaneUtilities = new JSplitPane();
		GridBagConstraints gbc_splitPaneUtilities = new GridBagConstraints();
		gbc_splitPaneUtilities.fill = GridBagConstraints.BOTH;
		gbc_splitPaneUtilities.gridx = 0;
		gbc_splitPaneUtilities.gridy = 0;
		tabUtilities.add(splitPaneUtilities, gbc_splitPaneUtilities);

		JPanel panelUtilityLeft = new JPanel();
		panelUtilityLeft.setBorder(null);
		splitPaneUtilities.setLeftComponent(panelUtilityLeft);
		GridBagLayout gbl_panelUtilityLeft = new GridBagLayout();
		gbl_panelUtilityLeft.columnWidths = new int[] { 0, 0 };
		gbl_panelUtilityLeft.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panelUtilityLeft.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelUtilityLeft.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		panelUtilityLeft.setLayout(gbl_panelUtilityLeft);

		Component verticalStrut_18 = Box.createVerticalStrut(20);
		verticalStrut_18.setPreferredSize(new Dimension(0, 50));
		GridBagConstraints gbc_verticalStrut_18 = new GridBagConstraints();
		gbc_verticalStrut_18.anchor = GridBagConstraints.WEST;
		gbc_verticalStrut_18.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_18.gridx = 0;
		gbc_verticalStrut_18.gridy = 0;
		panelUtilityLeft.add(verticalStrut_18, gbc_verticalStrut_18);

		JPanel panelLeftUtility1 = new JPanel();
		panelLeftUtility1.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null),
				new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "File Types", TitledBorder.CENTER,
						TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0))));
		GridBagConstraints gbc_panelLeftUtility1 = new GridBagConstraints();
		gbc_panelLeftUtility1.insets = new Insets(0, 0, 5, 0);
		gbc_panelLeftUtility1.fill = GridBagConstraints.BOTH;
		gbc_panelLeftUtility1.gridx = 0;
		gbc_panelLeftUtility1.gridy = 2;
		panelUtilityLeft.add(panelLeftUtility1, gbc_panelLeftUtility1);
		GridBagLayout gbl_panelLeftUtility1 = new GridBagLayout();
		gbl_panelLeftUtility1.columnWidths = new int[] { 0, 0 };
		gbl_panelLeftUtility1.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelLeftUtility1.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelLeftUtility1.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelLeftUtility1.setLayout(gbl_panelLeftUtility1);

		JButton btnFileTypeCensus = new JButton("Take Census");
		btnFileTypeCensus.setName(BTN_TAKE_CENSUS);
		btnFileTypeCensus.addActionListener(identicAdapter);

		Component verticalStrut_19 = Box.createVerticalStrut(20);
		verticalStrut_19.setPreferredSize(new Dimension(0, 40));
		GridBagConstraints gbc_verticalStrut_19 = new GridBagConstraints();
		gbc_verticalStrut_19.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_19.gridx = 0;
		gbc_verticalStrut_19.gridy = 0;
		panelLeftUtility1.add(verticalStrut_19, gbc_verticalStrut_19);
		GridBagConstraints gbc_btnFileTypeCensus = new GridBagConstraints();
		gbc_btnFileTypeCensus.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnFileTypeCensus.gridx = 0;
		gbc_btnFileTypeCensus.gridy = 1;
		panelLeftUtility1.add(btnFileTypeCensus, gbc_btnFileTypeCensus);

		Component verticalStrut_20 = Box.createVerticalStrut(20);
		verticalStrut_20.setPreferredSize(new Dimension(0, 100));
		GridBagConstraints gbc_verticalStrut_20 = new GridBagConstraints();
		gbc_verticalStrut_20.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_20.gridx = 0;
		gbc_verticalStrut_20.gridy = 3;
		panelUtilityLeft.add(verticalStrut_20, gbc_verticalStrut_20);

		JPanel panelLeftUtility2 = new JPanel();
		panelLeftUtility2.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null),
				new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Empty Folders", TitledBorder.CENTER,
						TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0))));
		GridBagConstraints gbc_panelLeftUtility2 = new GridBagConstraints();
		gbc_panelLeftUtility2.fill = GridBagConstraints.BOTH;
		gbc_panelLeftUtility2.gridx = 0;
		gbc_panelLeftUtility2.gridy = 4;
		panelUtilityLeft.add(panelLeftUtility2, gbc_panelLeftUtility2);
		GridBagLayout gbl_panelLeftUtility2 = new GridBagLayout();
		gbl_panelLeftUtility2.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelLeftUtility2.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelLeftUtility2.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelLeftUtility2.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelLeftUtility2.setLayout(gbl_panelLeftUtility2);

		Component verticalStrut_21 = Box.createVerticalStrut(20);
		verticalStrut_21.setPreferredSize(new Dimension(0, 40));
		GridBagConstraints gbc_verticalStrut_21 = new GridBagConstraints();
		gbc_verticalStrut_21.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_21.gridx = 0;
		gbc_verticalStrut_21.gridy = 0;
		panelLeftUtility2.add(verticalStrut_21, gbc_verticalStrut_21);

		JButton btnFindEmptyFolders = new JButton("Find");
		btnFindEmptyFolders.setName(BTN_FIND_EMPTY_FOLDERS);
		btnFindEmptyFolders.addActionListener(identicAdapter);
		GridBagConstraints gbc_btnFindEmptyFolders = new GridBagConstraints();
		gbc_btnFindEmptyFolders.insets = new Insets(0, 0, 5, 0);
		gbc_btnFindEmptyFolders.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnFindEmptyFolders.gridx = 1;
		gbc_btnFindEmptyFolders.gridy = 1;
		panelLeftUtility2.add(btnFindEmptyFolders, gbc_btnFindEmptyFolders);

		Component verticalStrut_22 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_22 = new GridBagConstraints();
		gbc_verticalStrut_22.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_22.gridx = 1;
		gbc_verticalStrut_22.gridy = 2;
		panelLeftUtility2.add(verticalStrut_22, gbc_verticalStrut_22);

		btnRemoveEmptyFolders = new JButton("Remove Folders");
		btnRemoveEmptyFolders.setName(BTN_REMOVE_EMPTY_FOLDERS);
		btnRemoveEmptyFolders.addActionListener(identicAdapter);
		btnRemoveEmptyFolders.setToolTipText("Remove only folders that are selected");
		GridBagConstraints gbc_btnRemoveEmptyFolders = new GridBagConstraints();
		gbc_btnRemoveEmptyFolders.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnRemoveEmptyFolders.insets = new Insets(0, 0, 5, 0);
		gbc_btnRemoveEmptyFolders.gridx = 1;
		gbc_btnRemoveEmptyFolders.gridy = 3;
		panelLeftUtility2.add(btnRemoveEmptyFolders, gbc_btnRemoveEmptyFolders);

		Component verticalStrut_23 = Box.createVerticalStrut(20);
		verticalStrut_23.setPreferredSize(new Dimension(0, 10));
		GridBagConstraints gbc_verticalStrut_23 = new GridBagConstraints();
		gbc_verticalStrut_23.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_23.gridx = 1;
		gbc_verticalStrut_23.gridy = 4;
		panelLeftUtility2.add(verticalStrut_23, gbc_verticalStrut_23);

		btnRemoveEmptyFoldersTree = new JButton("Remove Folders & Trees");
		btnRemoveEmptyFoldersTree.setName(BTN_REMOVE_EMPTY_FOLDERS_TREE);
		btnRemoveEmptyFoldersTree.addActionListener(identicAdapter);
		btnRemoveEmptyFoldersTree
				.setToolTipText("Remove selected folderd and any parents that become empty by this removal");
		GridBagConstraints gbc_btnRemoveEmptyFoldersTree = new GridBagConstraints();
		gbc_btnRemoveEmptyFoldersTree.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnRemoveEmptyFoldersTree.gridx = 1;
		gbc_btnRemoveEmptyFoldersTree.gridy = 5;
		panelLeftUtility2.add(btnRemoveEmptyFoldersTree, gbc_btnRemoveEmptyFoldersTree);

		JScrollPane scrollPaneUtilities = new JScrollPane();
		splitPaneUtilities.setRightComponent(scrollPaneUtilities);

		// tableUtility = new JTable();
		scrollPaneUtilities.setViewportView(utilityTable);
		splitPaneUtilities.setDividerLocation(200);

		popupUtility = new JPopupMenu();
		addPopup(utilityTable, popupUtility);

		JMenuItem popupUtilitySelect = new JMenuItem("Select");
		popupUtilitySelect.setName(PUM_UTILITY_SELECT);
		popupUtilitySelect.setActionCommand(PUM_UTILITY_SELECT);
		popupUtilitySelect.addActionListener(utilityAdapter);
		popupUtility.add(popupUtilitySelect);

		JMenuItem popupUtilityDeselect = new JMenuItem("Deselect");
		popupUtilityDeselect.setName(PUM_UTILITY_DESELECT);
		popupUtilityDeselect.setActionCommand(PUM_UTILITY_DESELECT);
		popupUtilityDeselect.addActionListener(utilityAdapter);
		popupUtility.add(popupUtilityDeselect);

		JPanel tabLog = new JPanel();
		tabLog.setName(TAB_LOG);
		tpMain.addTab("App Log", null, tabLog, null);
		GridBagLayout gbl_tabLog = new GridBagLayout();
		gbl_tabLog.columnWidths = new int[] { 0, 0 };
		gbl_tabLog.rowHeights = new int[] { 0, 0 };
		gbl_tabLog.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_tabLog.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		tabLog.setLayout(gbl_tabLog);

		JScrollPane scrollPaneAppLog = new JScrollPane();
		GridBagConstraints gbc_scrollPaneAppLog = new GridBagConstraints();
		gbc_scrollPaneAppLog.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneAppLog.gridx = 0;
		gbc_scrollPaneAppLog.gridy = 0;
		tabLog.add(scrollPaneAppLog, gbc_scrollPaneAppLog);

		JLabel label = new JLabel("Application Log");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setForeground(new Color(139, 69, 19));
		label.setFont(new Font("Courier New", Font.BOLD, 16));
		scrollPaneAppLog.setColumnHeaderView(label);

		txtLog = new JTextPane();
		txtLog.setText("");
		txtLog.setFont(new Font("Courier New", Font.PLAIN, 15));
		scrollPaneAppLog.setViewportView(txtLog);

		popupLog = new JPopupMenu();
		addPopup(txtLog, popupLog);

		JMenuItem popupLogClear = new JMenuItem("Clear Log");
		popupLogClear.setName(PUM_LOG_CLEAR);
		popupLogClear.addActionListener(logAdaper);
		popupLog.add(popupLogClear);

		JSeparator separator = new JSeparator();
		popupLog.add(separator);

		JMenuItem popupLogPrint = new JMenuItem("Print Log");
		popupLogPrint.setName(PUM_LOG_PRINT);
		popupLogPrint.addActionListener(logAdaper);
		popupLog.add(popupLogPrint);

		JPanel panelLeft = new JPanel();
		splitPaneMain.setLeftComponent(panelLeft);
		GridBagLayout gbl_panelLeft = new GridBagLayout();
		gbl_panelLeft.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panelLeft.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelLeft.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panelLeft.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		panelLeft.setLayout(gbl_panelLeft);

		Component verticalStrut_6 = Box.createVerticalStrut(60);
		GridBagConstraints gbc_verticalStrut_6 = new GridBagConstraints();
		gbc_verticalStrut_6.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_6.gridx = 1;
		gbc_verticalStrut_6.gridy = 1;
		panelLeft.add(verticalStrut_6, gbc_verticalStrut_6);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 5);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 1;
		gbc_panel_1.gridy = 2;
		panelLeft.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		rbNoCatalog = new JRadioButton("No Catalog(s)");
		rbNoCatalog.setName("rbNoCatalog");
		rbNoCatalog.setActionCommand("rbNoCatalog");
		GridBagConstraints gbc_rbNoCatalog = new GridBagConstraints();
		gbc_rbNoCatalog.anchor = GridBagConstraints.SOUTHWEST;
		gbc_rbNoCatalog.insets = new Insets(0, 0, 5, 0);
		gbc_rbNoCatalog.gridx = 0;
		gbc_rbNoCatalog.gridy = 0;
		panel_1.add(rbNoCatalog, gbc_rbNoCatalog);

		rbWithCatalog = new JRadioButton("With Catalog(s)");
		rbWithCatalog.setName("rbWithCatalog");
		rbWithCatalog.setActionCommand("rbWithCatalog");
		GridBagConstraints gbc_rbWithCatalog = new GridBagConstraints();
		gbc_rbWithCatalog.anchor = GridBagConstraints.WEST;
		gbc_rbWithCatalog.insets = new Insets(0, 0, 5, 0);
		gbc_rbWithCatalog.gridx = 0;
		gbc_rbWithCatalog.gridy = 1;
		panel_1.add(rbWithCatalog, gbc_rbWithCatalog);

		rbOnlyCatalogs = new JRadioButton("Only Catalogs");
		rbOnlyCatalogs.setName("rbOnlyCatalogs");
		rbOnlyCatalogs.setActionCommand("rbOnlyCatalogs");
		GridBagConstraints gbc_rbOnlyCatalogs = new GridBagConstraints();
		gbc_rbOnlyCatalogs.anchor = GridBagConstraints.WEST;
		gbc_rbOnlyCatalogs.gridx = 0;
		gbc_rbOnlyCatalogs.gridy = 2;
		panel_1.add(rbOnlyCatalogs, gbc_rbOnlyCatalogs);

		Component verticalStrut_7 = Box.createVerticalStrut(40);
		GridBagConstraints gbc_verticalStrut_7 = new GridBagConstraints();
		gbc_verticalStrut_7.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_7.gridx = 1;
		gbc_verticalStrut_7.gridy = 3;
		panelLeft.add(verticalStrut_7, gbc_verticalStrut_7);

		cbSaveExcludedFiles = new JCheckBox("Save Excluded Files");
		cbSaveExcludedFiles.setSelected(false);
		cbSaveExcludedFiles.setName("cbSaveExcludedFiles");
		GridBagConstraints gbc_cbSaveExcludedFiles = new GridBagConstraints();
		gbc_cbSaveExcludedFiles.anchor = GridBagConstraints.WEST;
		gbc_cbSaveExcludedFiles.insets = new Insets(0, 0, 5, 5);
		gbc_cbSaveExcludedFiles.gridx = 1;
		gbc_cbSaveExcludedFiles.gridy = 4;
		panelLeft.add(cbSaveExcludedFiles, gbc_cbSaveExcludedFiles);

		Component verticalStrut_8 = Box.createVerticalStrut(60);
		GridBagConstraints gbc_verticalStrut_8 = new GridBagConstraints();
		gbc_verticalStrut_8.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_8.gridx = 1;
		gbc_verticalStrut_8.gridy = 7;
		panelLeft.add(verticalStrut_8, gbc_verticalStrut_8);

		JButton btnStart = new JButton("Start");
		btnStart.setName(BTN_START);
		btnStart.addActionListener(identicAdapter);
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnStart.anchor = GridBagConstraints.NORTH;
		gbc_btnStart.insets = new Insets(0, 0, 5, 5);
		gbc_btnStart.gridx = 1;
		gbc_btnStart.gridy = 8;
		panelLeft.add(btnStart, gbc_btnStart);

		Component verticalStrut_17 = Box.createVerticalStrut(20);
		verticalStrut_17.setPreferredSize(new Dimension(0, 40));
		GridBagConstraints gbc_verticalStrut_17 = new GridBagConstraints();
		gbc_verticalStrut_17.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_17.gridx = 1;
		gbc_verticalStrut_17.gridy = 9;
		panelLeft.add(verticalStrut_17, gbc_verticalStrut_17);

		JButton btnTest = new JButton("Test");
		btnTest.setVisible(false);
		btnTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doTest();
			}
		});
		GridBagConstraints gbc_btnTest = new GridBagConstraints();
		gbc_btnTest.insets = new Insets(0, 0, 0, 5);
		gbc_btnTest.gridx = 1;
		gbc_btnTest.gridy = 10;
		panelLeft.add(btnTest, gbc_btnTest);

		splitPaneMain.setDividerLocation(150);

		JPanel panelStatus = new JPanel();
		panelStatus.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelStatus = new GridBagConstraints();
		gbc_panelStatus.ipady = 2;
		gbc_panelStatus.ipadx = 2;
		gbc_panelStatus.fill = GridBagConstraints.BOTH;
		gbc_panelStatus.gridx = 0;
		gbc_panelStatus.gridy = 1;
		frmIdentic.getContentPane().add(panelStatus, gbc_panelStatus);
		GridBagLayout gbl_panelStatus = new GridBagLayout();
		gbl_panelStatus.columnWidths = new int[] { 0, 0 };
		gbl_panelStatus.rowHeights = new int[] { 0, 0 };
		gbl_panelStatus.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelStatus.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelStatus.setLayout(gbl_panelStatus);

		lblStatusTypeList = new JLabel("");
		lblStatusTypeList.setHorizontalAlignment(SwingConstants.CENTER);
		lblStatusTypeList.setForeground(new Color(0, 0, 205));
		lblStatusTypeList.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblStatusTypeList = new GridBagConstraints();
		gbc_lblStatusTypeList.anchor = GridBagConstraints.NORTH;
		gbc_lblStatusTypeList.gridx = 0;
		gbc_lblStatusTypeList.gridy = 0;
		panelStatus.add(lblStatusTypeList, gbc_lblStatusTypeList);

		JMenuBar menuBar = new JMenuBar();
		frmIdentic.setJMenuBar(menuBar);

		JMenu mnuFile = new JMenu("File");
		menuBar.add(mnuFile);

		JMenuItem mnuFileExit = new JMenuItem("Exit");
		mnuFileExit.setName(MNU_FILE_EXIT);
		mnuFile.add(mnuFileExit);
	}// initialize

	// Constants

	public static final FileStat END_OF_SUBJECT = new FileStat("<END OF QUEUE>", -1L, "0000-00-00  00:00:00");
	public static final FileStatReject END_OF_REJECT = new FileStatReject("<END OF QUEUE>", -1L, "0000-00-00  00:00:00",
			"END OF QUEUE");
	private static final String EMPTY_STRING = "";
	// private static final String NONE = "<none>";

	// private static final int COLUMN_NAME_SUBJECT = 0;
	// private static final int COLUMN_DIRECTORY_SUBJECT = 1;
	private static final int COLUMN_DIRECTORY_DUP = 4;
	private static final int COLUMN_ID_SUBJECT = 5;

	// private static final int COLUMN_ACTION_ACTION = 0;
	// private static final int COLUMN_NAME_ACTION = 1;
	// private static final int COLUMN_DIRECTORY_ACTION = 2;

	private static final String TAB_SUMMARY = "tabSummary";
	private static final String TAB_CATALOGS = "tabCatalogs";
	private static final String TAB_TYPES = "tabTypes";
	private static final String TAB_LOG = "tabLog";

	private static final String MNU_FILE_EXIT = "mnuFileExit";

	private static final String BTN_SOURCE_FOLDER = "btnSourceFolder";

	private static final String RB_CATALOG_NO = "rbNoCatalog";
	private static final String RB_CATALOG_WITH = "rbWithCatalog";
	private static final String RB_CATALOG_ONLY = "rbOnlyCatalogs";

	// Lists Constants
	private static final String EDIT_ADD = "Add";
	private static final String EDIT_REMOVE = "Remove";
	private static final String EDIT_ADD_REMOVE = "Add/Remove";
	private static final String BTN_ADD_REMOVE = "btnAddRemove";

	private static final String BTN_LOAD = "btnLoad";
	private static final String BTN_NEW = "btnNew";
	private static final String BTN_SAVE = "btnSave";
	private static final String BTN_DELETE = "btnDelete";
	private static final String BTN_MAKE_LIST_ACTIVE = "btnMakeListActive";

	private static final String LIST_TYPES_AVAILABLE = "listTypesAvailable";
	private static final String LIST_TYPES_EDIT = "listEdit";

	private static final String TXT_ACTIVE = "txtActive";
	private static final String TXT_EDIT = "txtEdit";

	private static final String NEW_LIST = "<NEW>";
	private static final String LIST_SUFFIX = "typeList";
	private static final String LIST_SUFFIX_DOT = "." + LIST_SUFFIX;
	private static final String[] INITIAL_LISTFILES = new String[] { "/VB" + LIST_SUFFIX_DOT,
			"/Music" + LIST_SUFFIX_DOT, "/MusicAndPictures" + LIST_SUFFIX_DOT, "/Pictures" + LIST_SUFFIX_DOT };

	// Catalog Constants
	private static final String BTN_START = "btnStart";
	private static final String BTN_CATALOG_RELOAD = "btnCatalogReload";
	private static final String BTN_CATALOG_SAVE = "btnCatalogSave";
	// private static final String BTN_CATALOG_COMBINE = "btnCatalogCombine";
	private static final String BTN_CATALOG_IMPORT = "btnCatalogImport";
	private static final String BTN_CATALOG_EXPORT = "btnCatalogExport";
	private static final String BTN_CATALOG_REMOVE = "btnCatalogRemove";

	private static final String CATALOG_SUFFIX = "catalog";
	private static final String CATALOG_SUFFIX_DOT = "." + CATALOG_SUFFIX;

	private static final String BTN_SUMMARY_EXCLUDED = "btnSummaryExcluded";
	private static final String BTN_SUMMARY_TARGETS = "btnSummaryTargets";
	private static final String BTN_SUMMARY_DISTINCT = "btnSummaryDistinct";
	private static final String BTN_SUMMARY_UNIQUE = "btnSummaryUnique";
	private static final String BTN_SUMMARY_DUPLICATES = "btnSummaryDuplicates";

	private static final String BTN_AC_EXCLUDED = "Excluded Files";
	private static final String BTN_AC_TARGETS = "Target Files";
	private static final String BTN_AC_DISTINCT = "Distinct Files";
	private static final String BTN_AC_UNIQUE = "Unique Files";
	private static final String BTN_AC_DUPLICATES = "Duplicate Files";

	private static final String BTN_PRINT_RESULTS = "btnPrintResults";

	private static final String BTN_ACTION_LOAD_RESULTS = "btnActionLoadResults";
	private static final String BTN_ACTION_COPY = "btnActionCopy";
	private static final String BTN_ACTION_MOVE = "btnActionMove";
	private static final String BTN_ACTION_DELETE = "btnActionDelete";

	private static final String RB_AC_TARGETS = "Target Files";
	private static final String RB_AC_DISTINCT = "Distinct Files";
	private static final String RB_AC_UNIQUE = "Unique Files";
	private static final String RB_AC_DUPLICATES = "Duplicate Files";

	private static final String PUM_ACTION_SELECT = "popupActionsSelect";
	private static final String PUM_ACTION_DESELECT = "popupActionsDeselect";

	private static final String PUM_UTILITY_SELECT = "popupUtilitySelect";
	private static final String PUM_UTILITY_DESELECT = "popupUtilityDeselect";

	private static final String PUM_LOG_PRINT = "popupLogPrint";
	private static final String PUM_LOG_CLEAR = "popupLogClear";

	private static final String BTN_TAKE_CENSUS = "btnTakeCensus";

	private static final String BTN_FIND_EMPTY_FOLDERS = "btnFindEmptyFolders";
	private static final String BTN_REMOVE_EMPTY_FOLDERS = "btnRemoveEmptyFolders";
	private static final String BTN_REMOVE_EMPTY_FOLDERS_TREE = "btnRemoveEmptyFoldersTree";

	// members
	private JFrame frmIdentic;
	private JSplitPane splitPaneMain;
	private JTextPane txtLog;
	private JTabbedPane tpMain;
	private JTextField txtActive;
	private JList<String> listTypesAvailable;
	private JList<String> listTypesEdit;
	private JLabel lblStatusTypeList;
	private JTextField txtEdit;
	private JLabel lblListEdit;
	private JButton btnAddRemove;
	private JButton btnLoad;
	private JButton btnNew;
	private JButton btnSave;
	private JButton btnDelete;
	private JLabel lblCatalogName;
	private JLabel lblCatalogDirectory;
	private JLabel lblCatalogCount;
	private JLabel lblCatalogDescription;
	private JLabel lblSourceFolder;
	private JRadioButton rbNoCatalog;
	private JRadioButton rbWithCatalog;
	private JRadioButton rbOnlyCatalogs;
	private JCheckBox cbSaveExcludedFiles;
	private JToggleButton btnSummaryExcluded;
	private JToggleButton btnSummaryTargets;
	private JToggleButton btnSummaryDistinct;
	private JToggleButton btnSummaryUnique;
	private JToggleButton btnSummaryDuplicates;
	private JSplitPane splitPaneSummary;
	private JLabel lblSummaryLegend;
	private JLabel lblTotalFiles;
	private JButton btnPrintResults;
	private JRadioButton rbLoadTargetFiles;
	private JRadioButton rbLoadDistinctFiles;
	private JRadioButton rbLoadUniqueFiles;
	private JRadioButton rbLoadHaveDuplicates;
	private JPopupMenu popupActions;
	private JPopupMenu popupLog;
	private JPopupMenu popupUtility;
	// private JTable tableUtility;
	private JButton btnRemoveEmptyFolders;
	private JButton btnRemoveEmptyFoldersTree;
	// private JList lstCatalogInUse;
	// private JList lstCatalogAvailable;

	////////////////////// Included Classes ///////////////////////////////////////////

	/////////////////////////////////////////

	public class GatherFromCatalogs implements Runnable {

		// @Override
		public void run() {
			// SubjectTableModel subjectTableModel;
			CatalogItem catalogItem;
			for (int i = 0; i < inUseCatalogItemModel.getSize(); i++) {
				catalogItem = inUseCatalogItemModel.get(i);
				qHashes.addAll(catalogItem.getFileStats());
			} // for - each catalog Item
			qHashes.add(END_OF_SUBJECT);
		}// run
	}// class GatherFromCatalogs

	//////////////////////////////////

	// ---------------------------------------------------------

	static class ListFilter implements FilenameFilter {
		String suffix;

		public ListFilter(String suffix) {
			this.suffix = suffix;
		}// Constructor

		@Override
		public boolean accept(File arg0, String arg1) {
			if (arg1.endsWith(this.suffix)) {
				return true;
			} // if
			return false;
		}// accept
	}// ListFilter

	// -------------------------------------------------------------------

	//////////////////////////// [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[

	@SuppressWarnings("serial")
	static class ListTransferHandler extends TransferHandler {

		@Override
		public boolean canImport(TransferSupport support) {
			return (support.getComponent() instanceof JList
					&& support.isDataFlavorSupported(ListItemTransferable.LIST_ITEM_DATA_FLAVOR));
		}// canImport

		@SuppressWarnings("unchecked")
		@Override
		public boolean importData(TransferSupport support) {// support is target
			if (!canImport(support)) {
				return false;
			} // if support

			Transferable t = support.getTransferable();
			CatalogItem value = null;

			try {
				value = (CatalogItem) t.getTransferData(ListItemTransferable.LIST_ITEM_DATA_FLAVOR);
			} catch (Exception e) {
				e.printStackTrace();
			} // try

			JList<CatalogItem> targetList = (JList<CatalogItem>) support.getComponent();
			CatalogItemModel model = (CatalogItemModel) targetList.getModel();
			int index = targetList.getDropLocation().getIndex();
			model.add(index, value);
			targetList.updateUI();

			return true;
		}// inportData

		@Override
		public int getSourceActions(JComponent component) {
			return DnDConstants.ACTION_COPY_OR_MOVE;
		}// getSourceActions

		@Override
		protected Transferable createTransferable(JComponent component) {
			Transferable t = null;

			if (component instanceof JList) {
				@SuppressWarnings("unchecked")
				JList<CatalogItem> list = (JList<CatalogItem>) component;
				Object value = list.getSelectedValue();
				if (value instanceof CatalogItem) {
					CatalogItem catalogItem = (CatalogItem) value;
					t = new ListItemTransferable(catalogItem);
				} // inner if value
			} // outer if - JList

			return t;
		}// createTransferable

		@Override
		protected void exportDone(JComponent source, Transferable data, int action) {
			@SuppressWarnings("unchecked")
			int index = ((JList<CatalogItem>) source).getSelectedIndex();
			@SuppressWarnings("unchecked")
			CatalogItemModel model = (CatalogItemModel) ((JList<CatalogItem>) source).getModel();
			model.removeElementAt(index);
			source.updateUI();
		}// exportDone

	}// class ListTransferHandler

	//////////////////////////////////////////////////////////////////////////

	public static class ListItemTransferable implements Transferable {
		public static final DataFlavor LIST_ITEM_DATA_FLAVOR = new DataFlavor(CatalogItem.class, "identic/CatalogItem");
		private CatalogItem catalogItem;

		public ListItemTransferable(CatalogItem catalogItem) {
			this.catalogItem = catalogItem;
		}// Constructor

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { LIST_ITEM_DATA_FLAVOR };
		}// getTransferDataFlavors

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor.equals(LIST_ITEM_DATA_FLAVOR);
		}// isDataFlavorSupported

		@Override
		public Object getTransferData(DataFlavor arg0) throws UnsupportedFlavorException, IOException {
			return catalogItem;
		}// getTransferData

	}// class ListItemTransferable
		////////////////////////////////////////// ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]

	// Classes of Adapters ////////////////////////////////////////////////////////////////
	class AdapterIdentic implements ActionListener {// , ListSelectionListener , ChangeListener

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			// Menus
			case MNU_FILE_EXIT:
				doFileExit();
				break;

			case BTN_START:
				doStart();
				break;

			case BTN_SOURCE_FOLDER:
				doSourceFolder();
				break;

			case BTN_SUMMARY_EXCLUDED:
			case BTN_SUMMARY_TARGETS:
			case BTN_SUMMARY_DISTINCT:
			case BTN_SUMMARY_UNIQUE:
			case BTN_SUMMARY_DUPLICATES:
				doShowResults(name);
				break;

			case BTN_PRINT_RESULTS:
				doPrintResults();
				break;

			case BTN_ACTION_LOAD_RESULTS:
				doActionLoadResults();
				break;

			case BTN_ACTION_COPY:
			case BTN_ACTION_MOVE:
				doActionMoveCopy(name);
				break;

			case BTN_ACTION_DELETE:
				doActionDelete();
				break;

			case BTN_TAKE_CENSUS:
				doTakeCensus();
				break;

			case BTN_FIND_EMPTY_FOLDERS:
				doFindEmptyFolders();
				break;

			case BTN_REMOVE_EMPTY_FOLDERS_TREE:
				doRemoveEmptyFoldersTree();
				break;

			case BTN_REMOVE_EMPTY_FOLDERS:
				doRemoveEmptyFolders();
				break;

			}// switch - name

		}// actionPerformed

		// @Override
		// public void stateChanged(ChangeEvent changeEvent) {
		// JTabbedPane tabbedPane = (JTabbedPane) changeEvent.getSource();
		//
		// switch (tabbedPane.getSelectedComponent().getName()) {
		// case TAB_SUMMARY:
		// doTabSummary();
		// break;
		// case TAB_CATALOGS:
		// doTabCatalogs();
		// break;
		// case TAB_TYPES:
		// doTabTypes();
		// break;
		// case TAB_LOG:
		// doTabLog();
		// break;
		// }// switch - name
		//
		// }// stateChanged

		// ***** ListSelectionListener

		// @Override
		// public void valueChanged(ListSelectionEvent listSelectionEvent) {
		// doCatalogListSelected(listSelectionEvent);
		// }// valueChanged

	}// class IdenticAdapter

	////////////////////////////////////////////////////////////////

	class AdapterManageLists implements ActionListener, MouseListener, FocusListener, ListSelectionListener {

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			case BTN_LOAD:
				doLoadTargetEdit(); // loadTargetEdit();
				break;
			case BTN_NEW:
				doNewTargetEdit();// loadNewTargetEdit();
				break;
			case BTN_SAVE:
				doSaveList();
				break;
			case BTN_DELETE:
				doDeleteList();
				break;
			case BTN_ADD_REMOVE:
				doAddRemove();
				break;
			case BTN_MAKE_LIST_ACTIVE:
				changeTargetList();
				break;
			default:

			}// switch

		}// actionPerformed

		@Override
		public void mouseClicked(MouseEvent mouseEvent) {
			if (mouseEvent.getClickCount() > 1) {
				String name = ((Component) mouseEvent.getSource()).getName();
				if (name.equals(LIST_TYPES_AVAILABLE)) {
					doLoadTargetEdit(); // loadTargetEdit();
				} // if list
			} // if count > 1
		}// mouseClicked

		@Override
		public void mouseEntered(MouseEvent e) {
		}//

		@Override
		public void mouseExited(MouseEvent e) {
		}//

		@Override
		public void mousePressed(MouseEvent e) {
		}//

		@Override
		public void mouseReleased(MouseEvent e) {
		}//

		@Override
		public void focusGained(FocusEvent arg0) {
		}//

		@Override
		public void focusLost(FocusEvent focusEvent) {
			String name = ((Component) focusEvent.getSource()).getName();
			switch (name) {
			case TXT_EDIT:
				flag1 = true;
				doEditListMember();
				break;
			case TXT_ACTIVE:
				doNameChanged();
				break;
			}// switch

		}// focusLost

		private boolean flag1 = false; // control the echo of events

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!flag1) {
				txtEdit.setText(EMPTY_STRING);
				btnAddRemove.setText(EDIT_REMOVE);
			} // if flag
			flag1 = false;
		}// valueChanged

	}// class ManageListsAdapter

	////////////////////////////////////////////////////////////////

	class AdapterCatalog implements ActionListener, ListSelectionListener {
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			case BTN_CATALOG_RELOAD:
				doCatalogLoad();
				break;
			case BTN_CATALOG_SAVE:
				doCatalogSave();
				break;
			// case BTN_CATALOG_COMBINE:
			// doCatalogCombine();
			// break;
			case BTN_CATALOG_IMPORT:
				doCatalogImport();
				break;
			case BTN_CATALOG_EXPORT:
				doCatalogExport();
				break;
			case BTN_CATALOG_REMOVE:
				doCatalogRemove();
				break;
			}// switch - name
		}// actionPerformed

		@Override
		public void valueChanged(ListSelectionEvent listSelectionEvent) {
			doCatalogListSelected(listSelectionEvent);
		}// valueChanged

	}// class CatalogAdapter

	////////////////////////////////////////////////////////////////

	class AdapterAction implements ActionListener {// , ListSelectionListener
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			case PUM_ACTION_SELECT:
				doActionMultiSelect();
				break;
			case PUM_ACTION_DESELECT:
				doActionMultiDeselect();
				break;
			}// switch
		}// actionPerformed
	}// class AdapterAction

	class AdapterUtility implements ActionListener {// , ListSelectionListener
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			case PUM_UTILITY_SELECT:
				doUtilityMultiSelect();
				break;
			case PUM_UTILITY_DESELECT:
				doUtilityMultiDeselect();
				break;
			}// switch
		}// actionPerformed
	}// class AdapterAction

	class AdapterLog implements ActionListener {// , ListSelectionListener
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			case PUM_LOG_PRINT:
				doLogPrint();
				break;
			case PUM_LOG_CLEAR:
				doLogClear();
				break;
			}// switch
		}// actionPerformed
	}// class AdapterAction

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				} // if popup Trigger
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}// addPopup

}// class Identic
