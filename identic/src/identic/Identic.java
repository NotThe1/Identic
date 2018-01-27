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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
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
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
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
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableRowSorter;

public class Identic {

	private IdenticAdapter identicAdapter = new IdenticAdapter();
	private ManageListsAdapter manageListsAdapter = new ManageListsAdapter();
	private CatalogAdapter catalogAdapter = new CatalogAdapter();

	private AppLogger log = AppLogger.getInstance();

	private ButtonGroup bgFindType = new ButtonGroup();
	private ButtonGroup bgSummary = new ButtonGroup();

	// Find
	private LinkedBlockingQueue<FileStat> qSubjects = new LinkedBlockingQueue<FileStat>();
	private LinkedBlockingQueue<FileStatReject> qRejects = new LinkedBlockingQueue<FileStatReject>();
	private LinkedBlockingQueue<FileStat> qHashes = new LinkedBlockingQueue<FileStat>();
	private HashMap<String, Integer> excludedFileTypes = new HashMap<>();

	private SubjectTableModel subjectTableModel = new SubjectTableModel();
	private RejectTableModel rejectTableModel = new RejectTableModel();
	private DefaultListModel<String> excludeModel = new DefaultListModel<>();

	private JTable tableResults = new JTable();

	private HashMap<String, Integer> hashCounts = new HashMap<String, Integer>();;
	private HashMap<String, Integer> hashIDs = new HashMap<String, Integer>();

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

//	private JList<CatalogItem2> lstCatalogInUse1;// = new JList<CatalogItem>(inUseCatalogItemModel);

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

	// private void doTabSummary() {
	// log.addInfo("doTabSummary()");
	// }//doTabSummary
	//
	// private void doTabCatalogs() {
	// log.addInfo("doTabCatalogs()");
	// }// doTabCatalogs
	//
	// private void doTabTypes() {
	// log.addInfo("doTabTypes()");
	// }// doTabTypes
	//
	// private void doTabLog() {
	// log.addInfo("doTabLog()");
	// }// doTabLog

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
				// System.out.printf("Name: %s, Desc:
				// %s%n",catalogItem.getEntryName(),catalogItem.getEntryDescription());
				// System.out.printf("Directory %s, %n",catalogItem.getEntryStartDirectory());
				// System.out.printf("RowCount %s, %n",catalogItem.getSubjectTableModel().getRowCount());
			} catch (Exception e) {
				log.addError("[doCatalogLoad()] unable to load Catalog");
			} // try
		} // for file
		lstCatalogAvailable.updateUI();
		lstCatalogInUse.updateUI();
	}// doCatalogLoad

	private void doCatalogNew() {
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
	}

	private void doCatalogCombine() {
		List<CatalogItem> catalogItems = new ArrayList<CatalogItem>();
		try {
			catalogItems.addAll(lstCatalogInUse.getSelectedValuesList());
			catalogItems.addAll(lstCatalogAvailable.getSelectedValuesList());
		} catch (Exception e) {
			log.addError("[doCatalogCombine] failed to combine catalogs");
		}
		if (catalogItems.size() < 2) {
			JOptionPane.showMessageDialog(frmIdentic, "At least Two Catalog Items need to be selected",
					"Combine Catalog Items", JOptionPane.ERROR_MESSAGE);
			return;
		} // if less than two

		CatalogDialog catalogDialog = CatalogDialog.makeNewCatalogDialog();
		if (catalogDialog.showDialog() != JOptionPane.OK_OPTION) {
			return;
		} // if dialog OK

		log.addInfo(String.format("[doCatalogCombine()] Name: %s", catalogDialog.getName()));
		log.addInfo(String.format("Description: %s", catalogDialog.getDescription()));

		ArrayList<FileStat> newCombinedFileStats = new ArrayList<FileStat>();

		List<String> startingDirectorys = new ArrayList<String>(); // possibble future use

		for (CatalogItem catalogItem : catalogItems) {
			startingDirectorys.add(catalogItem.getEntryStartDirectory());// possibble future use
			newCombinedFileStats.addAll(catalogItem.getFileStats());
		} // for each catalogItem

		CatalogItem combinedCatalogItem = new CatalogItem(catalogDialog.getName(), catalogDialog.getDescription(),
				makeStartingDirectory(startingDirectorys), newCombinedFileStats);

		try {
			FileOutputStream fos = new FileOutputStream(
					getApplcationWorkingDirectory() + catalogDialog.getName() + CATALOG_SUFFIX_DOT);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(combinedCatalogItem);
			oos.close();
			fos.close();
		} catch (IOException e) {
			String message = String.format(
					"[Identic] doCatalogNew() failed writing catalog object%n" + "Name: %s%n Description : %n",
					catalogDialog.getName(), catalogDialog.getDescription());
			log.addError(message);
			e.printStackTrace();
		} // try

		catalogDialog = null;
		doCatalogLoad();

	}// doCatalogCombine

	private String makeStartingDirectory(List<String> startingDirectorys) {// possibble future use
		String result = "";
		result = startingDirectorys.get(0);

		return result;
	}// makeStartingDirectory

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

		// System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
		// System.out.println("I chose to open this file: " + absolutePath);
		// System.out.printf("lstCatAvailable.getSelectedValue() =
		// %s%n",lstCatAvailable.getSelectedValue().getEntryName());

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
		if (files.length == 0) {

			String[] initalListFiles = new String[] { "/VB.typeList", "/Music.typeList", "/MusicAndPictures.typeList",
					"/Pictures.typeList" };
			Path newDir = Paths.get(workingDirectory);
			Path source = null;
			for (int i = 0; i < initalListFiles.length; i++) {
				try {
					// sources.add(Paths.get(this.getClass().getResource(initalListFiles[i] ).toURI()));
					source = Paths.get(this.getClass().getResource(initalListFiles[i]).toURI());
					Files.move(source, newDir.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
				} catch (URISyntaxException | IOException e) {
					e.printStackTrace();
				} // try
			} // for
			files = targetDirectory.listFiles(new ListFilter(LIST_SUFFIX_DOT));
		} // if no type list files in target directory

		availableListsModel.removeAllElements();
		for (File file : files) {
			availableListsModel.addElement(file.getName().replace(LIST_SUFFIX_DOT, EMPTY_STRING));
		} // for - file

		// lblStatusTypeList.setText(arg0);

		// cboTypeLists.setModel(typeListModel);

		// Preferences myPrefs = Preferences.userNodeForPackage(Identic.class).node(this.getClass().getSimpleName());
		// cboTypeLists.setSelectedItem(myPrefs.get("ActiveList", "Pictures"));
		// listFileTypes.setModel(typeListModel);
		// myPrefs = null;

		// cboTypeLists.

		// lblStatus.setText(url.getPath());
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
		initialiseFind();
		btnSummaryExcluded.setVisible(cbSaveExcludedFiles.isSelected());
		// btnSummaryExcluded.setVisble(cbSaveExcludedFiles.isSelected());

		Date startTime = log.addTimeStamp("Start :");

		if (rbNoCatalog.isSelected()) {
			log.addInfo(" doStartNoCatalogs()");
			log.addInfo(lblSourceFolder.getText());
			doStartNoCatalog();
		} else if (rbWithCatalog.isSelected()) {
			if (!isCatalogSelected()) {
				return;
			} // if catalog selected
			log.addInfo(" doStartWithCatalogs()");
			log.addInfo(lblSourceFolder.getText());
			doStartOnlyCatalogs();
			doStartNoCatalog();
		} else if (rbOnlyCatalogs.isSelected()) {
			if (!isCatalogSelected()) {
				return;
			} // if catalog selected
			log.addInfo(" doStartNoCatalogs()");
			doStartOnlyCatalogs();
		} // if start type
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

		IdentifySubjects identifySubjects = new IdentifySubjects();
		Thread threadIdentify = new Thread(identifySubjects);
		threadIdentify.start();

		MakeFileKey makeFileKey = new MakeFileKey(threadIdentify);
		Thread threadMakeFileKey = new Thread(makeFileKey);
		threadMakeFileKey.start();

		ShowRejects showRejects = new ShowRejects(threadIdentify);
		Thread threadRejects = new Thread(showRejects);
		if (cbSaveExcludedFiles.isSelected()) {
			threadRejects.start();
		} // if

		IdentifyDuplicates identifyDuplicates = new IdentifyDuplicates(threadMakeFileKey);
		Thread threadIdentifyDuplicates = new Thread(identifyDuplicates);
		threadIdentifyDuplicates.start();

		try {
			threadIdentify.join();
			threadMakeFileKey.join();
			threadRejects.join();
			threadIdentifyDuplicates.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} // try

	}// doStartNoCatalog

	private void doStartOnlyCatalogs() {
		GatherFromCatalogs gatherFromCatalogs = new GatherFromCatalogs();
		Thread threadGather = new Thread(gatherFromCatalogs);
		threadGather.start();

		IdentifyDuplicates identifyDuplicates = new IdentifyDuplicates(threadGather);
		Thread threadIdentifyDuplicates = new Thread(identifyDuplicates);
		threadIdentifyDuplicates.start();

		try {
			threadGather.join();
			threadIdentifyDuplicates.join();
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

		// fileCount = 0;
		// folderCount = 0;
		// subjectCount = 0;
		// rejectCount = 0;
		excludeModel.clear();
		hashCounts.clear();
		subjectTableModel.clear();
		rejectTableModel.clear();
		hashIDs.clear();
		hashCounts.clear();

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

		Set<Entry<String, Integer>> excludedFileTypesSet = excludedFileTypes.entrySet();
		int totalCountOfExcludedFiles = 0;
		for (Entry<String, Integer> entry : excludedFileTypesSet) {
			Integer value = (Integer) entry.getValue();
			totalCountOfExcludedFiles += value;
		} // for - entry

		int totalFileCount = totalCountOfExcludedFiles + subjectTableModel.getRowCount();
		// setButtonLabel(btnSummaryTotal, totalFileCount);
		lblTotalFiles.setText(String.format("%,d  Total Files", totalFileCount));
		setButtonLabel(btnSummaryExcluded, totalCountOfExcludedFiles);
		// setButtonLabel(btnSummaryExcludedTypes, excludedFileTypes.size());
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
				tableResults.setModel(rejectTableModel);
				tableResults.setRowSorter(new TableRowSorter(rejectTableModel));
			} // if

			break;

		case BTN_SUMMARY_TARGETS:
			if (subjectTableModel.getRowCount() > 0) {
				tableResults.setModel(subjectTableModel);
				tableResults.setRowSorter(new TableRowSorter(subjectTableModel));
			} // if
			break;
			
		case BTN_SUMMARY_DISTINCT:
			if (subjectTableModel.getRowCount() > 0) {
				// -----------------------------------------
				RowFilter<Object, Object> dupFilter = new RowFilter<Object, Object>() {
					AbstractSet<String> hashIDs = new HashSet();

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
				TableRowSorter tableRowSorter = new TableRowSorter(subjectTableModel);
				tableRowSorter.setRowFilter(dupFilter);
				tableResults.setModel(subjectTableModel);
				tableResults.setRowSorter(tableRowSorter);
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
				TableRowSorter tableRowSorter = new TableRowSorter(subjectTableModel);
				tableRowSorter.setRowFilter(dupFilter);
				tableResults.setModel(subjectTableModel);
				tableResults.setRowSorter(tableRowSorter);

			} // if
			break;
		case BTN_SUMMARY_DUPLICATES:
			if (subjectTableModel.getRowCount() > 0) {

				RowFilter<Object, Object> dupFilter = new RowFilter<Object, Object>() {
					public boolean include(Entry<? extends Object, ? extends Object> entry) {
						return (boolean) entry.getValue(4);
					}// include
				};

				TableRowSorter tableRowSorter = new TableRowSorter(subjectTableModel);
				tableRowSorter.setRowFilter(dupFilter);
				tableResults.setRowSorter(tableRowSorter);

				tableResults.setModel(subjectTableModel);
			} // if

			break;
		default:

			log.addError("[doShowResults] Bad button name - " + buttonName);
		}// switch - button name

	}// doShowResults


	// Swing code ///////////////////////////////////////////////////////////////

	private void doFileExit() {
		appClose();
		System.exit(0);
	}// doFileExit

	private void appClose() {
		Preferences myPrefs = Preferences.userNodeForPackage(Identic0.class).node(this.getClass().getSimpleName());
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

		workingDirectory = getApplcationWorkingDirectory();

		Preferences myPrefs = Preferences.userNodeForPackage(Identic.class).node(this.getClass().getSimpleName());
		frmIdentic.setSize(916, 749);
		// frmIdentic.setSize(myPrefs.getInt("Width", 886), myPrefs.getInt("Height", 779));

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
		//
		// // These two arrays are synchronized to control the button positions and the selection of the correct panels.
		// catalogPanelIndex = 1; // index of btnFindDuplicatesWithCatalogs
		// sideMenuButtons = new JButton[] { btnFindDuplicates, btnListsAndCatalogs, btnDisplayResults,
		// btnCopyMoveRemove,
		// btnApplicationLog };
		// sideMenuPanelNames = new String[] { panelFindDuplicates.getName(), panelFindDuplicatesWithCatalogs.getName(),
		// panelDisplayResults.getName(), panelCopyMoveRemove.getName(), paneApplicationlLog.getName() };
		//
		//// cboTypeLists1.setModel(typeListModel);
		//
		//// listFindDuplicatesActive.setModel(targetModel);
		// listExcluded.setModel(excludeModel);
		txtLog.setText(EMPTY_STRING);

		log.setDoc(txtLog.getStyledDocument());

		// cbSaveExcludedFiles.setSelected(false);
		//
		// bgShowResults.add(rbAllTheFiles);
		// bgShowResults.add(rbDuplicateFiles);
		// bgShowResults.add(rbUniqueFiles);
		// bgShowResults.add(rbFilesNotProcessed);
		// bgShowResults.clearSelection();
		//
		bgFindType.add(rbNoCatalog);
		bgFindType.add(rbWithCatalog);
		bgFindType.add(rbOnlyCatalogs);

		bgSummary.add(btnSummaryExcluded);
		bgSummary.add(btnSummaryTargets);
		bgSummary.add(btnSummaryDistinct);
		bgSummary.add(btnSummaryUnique);
		bgSummary.add(btnSummaryDuplicates);

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
		// tpMain.addChangeListener(identicAdapter);
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
		frmIdentic.setTitle("Identic 2.0");
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

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		panelForTabbedPane.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 120, 72, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JButton btnSourceFolder = new JButton("Source Folder...");
		btnSourceFolder.setName(BTN_SOURCE_FOLDER);
		btnSourceFolder.addActionListener(identicAdapter);
		btnSourceFolder.setHorizontalAlignment(SwingConstants.LEFT);
		btnSourceFolder.setForeground(Color.BLACK);
		btnSourceFolder.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_btnSourceFolder = new GridBagConstraints();
		gbc_btnSourceFolder.anchor = GridBagConstraints.WEST;
		gbc_btnSourceFolder.insets = new Insets(0, 0, 0, 5);
		gbc_btnSourceFolder.gridx = 0;
		gbc_btnSourceFolder.gridy = 0;
		panel.add(btnSourceFolder, gbc_btnSourceFolder);

		lblSourceFolder = new JLabel("");
		GridBagConstraints gbc_lblSourceFolder = new GridBagConstraints();
		gbc_lblSourceFolder.anchor = GridBagConstraints.WEST;
		gbc_lblSourceFolder.insets = new Insets(0, 0, 0, 5);
		gbc_lblSourceFolder.gridx = 1;
		gbc_lblSourceFolder.gridy = 0;
		panel.add(lblSourceFolder, gbc_lblSourceFolder);
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
		splitPaneSummary.setLeftComponent(panelLeftSummary);
		GridBagLayout gbl_panelLeftSummary = new GridBagLayout();
		gbl_panelLeftSummary.columnWidths = new int[] { 0, 70, 0 };
		gbl_panelLeftSummary.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelLeftSummary.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelLeftSummary.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelLeftSummary.setLayout(gbl_panelLeftSummary);

		Component verticalStrut_9 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_9 = new GridBagConstraints();
		gbc_verticalStrut_9.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_9.gridx = 1;
		gbc_verticalStrut_9.gridy = 0;
		panelLeftSummary.add(verticalStrut_9, gbc_verticalStrut_9);

		btnSummaryTargets = new JToggleButton("Target Files");
		btnSummaryTargets.setHorizontalAlignment(SwingConstants.LEFT);
		btnSummaryTargets.setName(BTN_SUMMARY_TARGETS);
		btnSummaryTargets.addActionListener(identicAdapter);
		GridBagConstraints gbc_btnSummaryTargets = new GridBagConstraints();
		gbc_btnSummaryTargets.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSummaryTargets.anchor = GridBagConstraints.NORTH;
		gbc_btnSummaryTargets.insets = new Insets(0, 0, 5, 0);
		gbc_btnSummaryTargets.gridx = 1;
		gbc_btnSummaryTargets.gridy = 1;
		panelLeftSummary.add(btnSummaryTargets, gbc_btnSummaryTargets);

		btnSummaryDistinct = new JToggleButton("Distinct Files");
		btnSummaryDistinct.setHorizontalAlignment(SwingConstants.LEFT);
		btnSummaryDistinct.setName(BTN_SUMMARY_DISTINCT);
		btnSummaryDistinct.addActionListener(identicAdapter);
		GridBagConstraints gbc_btnSummaryDistinct = new GridBagConstraints();
		gbc_btnSummaryDistinct.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSummaryDistinct.anchor = GridBagConstraints.NORTH;
		gbc_btnSummaryDistinct.insets = new Insets(0, 0, 5, 0);
		gbc_btnSummaryDistinct.gridx = 1;
		gbc_btnSummaryDistinct.gridy = 3;
		panelLeftSummary.add(btnSummaryDistinct, gbc_btnSummaryDistinct);

		btnSummaryUnique = new JToggleButton("Unique Files");
		btnSummaryUnique.setHorizontalAlignment(SwingConstants.LEFT);
		btnSummaryUnique.setName(BTN_SUMMARY_UNIQUE);
		btnSummaryUnique.addActionListener(identicAdapter);
		GridBagConstraints gbc_btnSummaryUnique = new GridBagConstraints();
		gbc_btnSummaryUnique.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSummaryUnique.insets = new Insets(0, 0, 5, 0);
		gbc_btnSummaryUnique.gridx = 1;
		gbc_btnSummaryUnique.gridy = 5;
		panelLeftSummary.add(btnSummaryUnique, gbc_btnSummaryUnique);

		btnSummaryDuplicates = new JToggleButton("Have Duplicates");
		btnSummaryDuplicates.setHorizontalAlignment(SwingConstants.LEFT);
		btnSummaryDuplicates.setName(BTN_SUMMARY_DUPLICATES);
		btnSummaryDuplicates.addActionListener(identicAdapter);
		GridBagConstraints gbc_btnSummaryDuplicates = new GridBagConstraints();
		gbc_btnSummaryDuplicates.insets = new Insets(0, 0, 5, 0);
		gbc_btnSummaryDuplicates.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSummaryDuplicates.gridx = 1;
		gbc_btnSummaryDuplicates.gridy = 7;
		panelLeftSummary.add(btnSummaryDuplicates, gbc_btnSummaryDuplicates);

		btnSummaryExcluded = new JToggleButton("Total Excluded");
		btnSummaryExcluded.setHorizontalAlignment(SwingConstants.LEFT);
		btnSummaryExcluded.setName(BTN_SUMMARY_EXCLUDED);
		btnSummaryExcluded.addActionListener(identicAdapter);

		Component verticalStrut_10 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_10 = new GridBagConstraints();
		gbc_verticalStrut_10.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_10.gridx = 1;
		gbc_verticalStrut_10.gridy = 8;
		panelLeftSummary.add(verticalStrut_10, gbc_verticalStrut_10);
		GridBagConstraints gbc_btnSummaryExcluded = new GridBagConstraints();
		gbc_btnSummaryExcluded.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSummaryExcluded.anchor = GridBagConstraints.NORTH;
		gbc_btnSummaryExcluded.insets = new Insets(0, 0, 5, 0);
		gbc_btnSummaryExcluded.gridx = 1;
		gbc_btnSummaryExcluded.gridy = 9;
		panelLeftSummary.add(btnSummaryExcluded, gbc_btnSummaryExcluded);

		Component verticalStrut_11 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_11 = new GridBagConstraints();
		gbc_verticalStrut_11.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_11.gridx = 1;
		gbc_verticalStrut_11.gridy = 10;
		panelLeftSummary.add(verticalStrut_11, gbc_verticalStrut_11);

		JPanel panel_2 = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.VERTICAL;
		gbc_panel_2.gridx = 1;
		gbc_panel_2.gridy = 11;
		panelLeftSummary.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 0, 0 };
		gbl_panel_2.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);

		lblTotalFiles = new JLabel("Total Files");
		GridBagConstraints gbc_lblTotalFiles = new GridBagConstraints();
		gbc_lblTotalFiles.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblTotalFiles.gridx = 0;
		gbc_lblTotalFiles.gridy = 0;
		panel_2.add(lblTotalFiles, gbc_lblTotalFiles);

		JPanel panelRightSummary = new JPanel();
		splitPaneSummary.setRightComponent(panelRightSummary);
		GridBagLayout gbl_panelRightSummary = new GridBagLayout();
		gbl_panelRightSummary.columnWidths = new int[] { 0, 0 };
		gbl_panelRightSummary.rowHeights = new int[] { 0, 0 };
		gbl_panelRightSummary.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelRightSummary.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelRightSummary.setLayout(gbl_panelRightSummary);

		JScrollPane scrollPaneSummary = new JScrollPane();
		scrollPaneSummary.setViewportView(tableResults);

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

		listTypesAvailable = new JList();
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

		listTypesEdit = new JList();
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

		JButton btnCatalogNew = new JButton("New");
		btnCatalogNew.setName(BTN_CATALOG_NEW);
		btnCatalogNew.addActionListener(catalogAdapter);
		GridBagConstraints gbc_btnCatalogNew = new GridBagConstraints();
		gbc_btnCatalogNew.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCatalogNew.insets = new Insets(0, 0, 5, 0);
		gbc_btnCatalogNew.gridx = 1;
		gbc_btnCatalogNew.gridy = 3;
		panelCatalogButtons.add(btnCatalogNew, gbc_btnCatalogNew);

		JButton btnCatalogCombine = new JButton("Combine");
		btnCatalogCombine.setName(BTN_CATALOG_COMBINE);
		btnCatalogCombine.addActionListener(catalogAdapter);
		GridBagConstraints gbc_btnCatalogCombine = new GridBagConstraints();
		gbc_btnCatalogCombine.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCatalogCombine.insets = new Insets(0, 0, 5, 0);
		gbc_btnCatalogCombine.gridx = 1;
		gbc_btnCatalogCombine.gridy = 5;
		panelCatalogButtons.add(btnCatalogCombine, gbc_btnCatalogCombine);

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

		JPanel panelLeft = new JPanel();
		splitPaneMain.setLeftComponent(panelLeft);
		GridBagLayout gbl_panelLeft = new GridBagLayout();
		gbl_panelLeft.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panelLeft.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelLeft.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panelLeft.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
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
		gbc_btnStart.insets = new Insets(0, 0, 0, 5);
		gbc_btnStart.gridx = 1;
		gbc_btnStart.gridy = 8;
		panelLeft.add(btnStart, gbc_btnStart);
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
	private static final String EMPTY_STRING = "";
	private static final String NONE = "<none>";

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

	// Catalog Constants
	private static final String BTN_START = "btnStart";
	private static final String BTN_CATALOG_RELOAD = "btnCatalogReload";
	private static final String BTN_CATALOG_NEW = "btnCatalogNew";
	private static final String BTN_CATALOG_COMBINE = "btnCatalogCombine";
	private static final String BTN_CATALOG_IMPORT = "btnCatalogImport";
	private static final String BTN_CATALOG_EXPORT = "btnCatalogExport";
	private static final String BTN_CATALOG_REMOVE = "btnCatalogRemove";

	private static final String CATALOG_SUFFIX = "catalog";
	private static final String CATALOG_SUFFIX_DOT = "." + CATALOG_SUFFIX;

//	private static final String BTN_SUMMARY_TOTAL = "btnSummaryTotal";
	private static final String BTN_SUMMARY_EXCLUDED = "btnSummaryExcluded";
//	private static final String BTN_SUMMARY_EXCLUDED_TYPES = "btnSummaryExcludedTypes";
	private static final String BTN_SUMMARY_TARGETS = "btnSummaryTargets";
	private static final String BTN_SUMMARY_DISTINCT = "btnSummaryDistinct";
	private static final String BTN_SUMMARY_UNIQUE = "btnSummaryUnique";
	private static final String BTN_SUMMARY_DUPLICATES = "btnSummaryDuplicates";

	// members
	private JFrame frmIdentic;
	private JSplitPane splitPaneMain;
	private JTextPane txtLog;
	private JTabbedPane tpMain;
	private JTextField txtActive;
	private JList listTypesAvailable;
	private JList listTypesEdit;
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
	// private JList lstCatalogInUse;
	// private JList lstCatalogAvailable;

	////////////////////// Included Classes ///////////////////////////////////////////

	/*
	 * Class Identify Subjects - is the first pass at the targeted file. It populates two LinkedBlockingQueues. The
	 * first queue,qSubjects contains FileStatSubject which captures file full name, file size, and last date Modified
	 * for those files that are to be processed by this pass. The second queue, qRejects, contains FileStatSubject,
	 * which extends FileStatSubject with 'reason' for reject. it also tracks the number of occurrences of each file
	 * suffix
	 */
	public class IdentifySubjects implements Runnable {

		public IdentifySubjects() {
		}// Constructor

		@Override
		public void run() {
			excludedFileTypes.clear();
			MyWalker myWalker = new MyWalker();
			Path startPath = Paths.get(lblSourceFolder.getText());
			try {
				Files.walkFileTree(startPath, myWalker);
			} catch (IOException e) {
				e.printStackTrace();
			} // try
				// logSummary();
		}// run

		class MyWalker implements FileVisitor<Path> {
			Pattern patternSubjects = Pattern.compile(targetListRegex);
			Pattern patternFileType = Pattern.compile("\\.(.+$)");
			Matcher matcher;

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}// FileVisitResult

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				// folderCount++;
				return FileVisitResult.CONTINUE;
			}// FileVisitResult

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				String fileName = file.toString();
				String lastModifieTime = Files.getLastModifiedTime(file).toString();
				long fileSize = Files.size(file);

				matcher = patternFileType.matcher(fileName);
				String fileType = matcher.find() ? matcher.group(1).toLowerCase() : NONE;

				matcher = patternSubjects.matcher(fileType);
				if (matcher.find()) {
					qSubjects.add(new FileStat(fileName, fileSize, lastModifieTime));
				} else {
					keepSuffixCount(fileType);
					qRejects.add(new FileStatReject(fileName, fileSize, lastModifieTime, FileStat.NOT_ON_LIST));
				} // if - match

				return FileVisitResult.CONTINUE;
			}// FileVisitResult

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				qRejects.add(new FileStatReject(file.toString(), 0, null, FileStat.IO_EXCEPTION));
				return FileVisitResult.CONTINUE;
			}// FileVisitResult

			private void keepSuffixCount(String suffix) {

				Integer occurances = excludedFileTypes.get(suffix);
				if (occurances == null) {
					excludedFileTypes.put(suffix, 1);
					excludeModel.addElement(suffix);
				} else {
					excludedFileTypes.put(suffix, occurances + 1);
				} // if unique
			}// keepSuffixCount
		}// class MyWalker
	}// class IdentifySubjects
		//////////////////////

	public class MakeFileKey implements Runnable {
		private static final int bufSize = 1024;
		String algorithm = "SHA-1"; // 160 bits
		// String algorithm = "MD5"; // 128 bits
		// String algorithm = "SHA-256"; // 256 bits
		private Thread priorThread;
		// private AppLogger appLogger = AppLogger.getInstance();

		public MakeFileKey(Thread priorThread) {
			this.priorThread = priorThread;
		}// Constructor

		@Override
		public void run() {
			FileStat fileStat;

			int count = 0;
			String fileName = null;
			String key = null;
			while (true) {
				try {
					fileStat = qSubjects.remove();
					fileName = fileStat.getFileName();
					count++;
					try {
						key = hashFile(fileStat.getFilePath(), algorithm);
						fileStat.setHashKey(key);
						qHashes.add(fileStat);
						// appLogger.addInfo(key + " - " + fileName);
					} catch (HashGenerationException e) {
						log.addError("HashGenerationError", fileName);
						e.printStackTrace();
					} //
				} catch (NoSuchElementException ex) {
					if (priorThread.getState().equals(Thread.State.TERMINATED)) {
						// log.addSpecial("From MakeFileKey count = " + count);
						return;
					} // if - done ?
				} // try
			} // while
		}// run

		private String hashFile(String file, String algorithm) throws HashGenerationException {
			try (FileInputStream inputStream = new FileInputStream(file)) {
				MessageDigest digest = MessageDigest.getInstance(algorithm);

				byte[] bytesBuffer = new byte[bufSize];
				int bytesRead = -1;

				while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
					digest.update(bytesBuffer, 0, bytesRead);
				} // while

				byte[] hashedBytes = digest.digest();

				return convertByteArrayToHexString(hashedBytes);
			} catch (NoSuchAlgorithmException | IOException ex) {
				throw new HashGenerationException("Could not generate hash from file", ex);
			} // try
		}// hashFile

		private String convertByteArrayToHexString(byte[] arrayBytes) {
			StringBuffer stringBuffer = new StringBuffer();
			for (int i = 0; i < arrayBytes.length; i++) {
				stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16).substring(1));
			} // for
			return stringBuffer.toString();
		}// convertByteArrayToHexString

	}// class MakeFileKey

	//////////////////////////////////////////

	///////////////////////////

	public class ShowRejects implements Runnable {

		private Thread priorThread;
		// private AppLogger appLogger = AppLogger.getInstance();

		public ShowRejects(Thread priorThread) {
			this.priorThread = priorThread;
		}// Constructor

		@Override
		public void run() {
			String fileName = null;
			FileStatReject reject;
			while (true) {
				try {
					reject = qRejects.remove();
					rejectTableModel.addRow(reject);
					fileName = reject.getFileName();
				} catch (NoSuchElementException ex) {
					if (priorThread.getState().equals(Thread.State.TERMINATED)) {
						log.addSpecial(fileName);
						return;
					} // if - done ?
				} // try
			} // while
		}// run

	}// class ShowRejects
		///////////////////

	////////////////////////////////////////////////

	public class IdentifyDuplicates implements Runnable {
		private Thread priorThread;
		private Integer fileID;

		public IdentifyDuplicates(Thread priorThread) {
			this.priorThread = priorThread;
		}// Constructor

		@Override
		public void run() {
			fileID = 0;
			// hashIDs.clear();
			// hashCounts.clear();
			FileStat subject;
			while (true) {
				try {
					subject = qHashes.remove();
					subjectTableModel.addRow(subject, keepHashKeyCount(subject.getHashKey()));
				} catch (NoSuchElementException ex) {
					if (priorThread.getState().equals(Thread.State.TERMINATED)) {
						return;
					} // if - done ?
				} // try
			} // while
		}// run

		private Integer keepHashKeyCount(String hashKey) {

			Integer occurances = hashCounts.get(hashKey);
			if (occurances == null) {
				hashCounts.put(hashKey, 1);
				hashIDs.put(hashKey, fileID++);
				// excludeModel.addElement(filePart);
			} else {
				hashCounts.put(hashKey, occurances + 1);
			} // if unique

			return hashIDs.get(hashKey);
		}// keepSuffixCount

	}// class IdentifyDuplicates

	/////////////////////////////////////////

	public class GatherFromCatalogs implements Runnable {

		// @Override
		public void run() {
			// SubjectTableModel subjectTableModel;
			CatalogItem catalogItem;
			for (int i = 0; i < inUseCatalogItemModel.getSize(); i++) {
				catalogItem = inUseCatalogItemModel.get(i);
				qHashes.addAll(catalogItem.getFileStats());
				System.out.println(catalogItem.getEntryName());
			} // for - each catalog Item
		}// run
	}// class GatherFromCatalogs

	//////////////////////////// [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[

	//////////////////////////////////

	// ---------------------------------------------------------

	class ListFilter implements FilenameFilter {
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
	public class ListTransferHandler extends TransferHandler {

		@Override
		public boolean canImport(TransferSupport support) {
			return (support.getComponent() instanceof JList
					&& support.isDataFlavorSupported(ListItemTransferable.LIST_ITEM_DATA_FLAVOR));
		}// canImport

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
				// TODO: handle exception
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
			int index = ((JList<CatalogItem>) source).getSelectedIndex();
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
	class IdenticAdapter implements ActionListener {// , ListSelectionListener , ChangeListener

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

	class ManageListsAdapter implements ActionListener, MouseListener, FocusListener, ListSelectionListener {

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

	class CatalogAdapter implements ActionListener, ListSelectionListener {
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			case BTN_CATALOG_RELOAD:
				doCatalogLoad();
				break;
			case BTN_CATALOG_NEW:
				doCatalogNew();
				break;
			case BTN_CATALOG_COMBINE:
				doCatalogCombine();
				break;
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

}// class Identic
