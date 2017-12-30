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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Identic {

	private IdenticAdapter identicAdapter = new IdenticAdapter();
	private ManageListsAdapter manageListsAdapter = new ManageListsAdapter();
	private AppLogger log = AppLogger.getInstance();

	private DefaultListModel<String> availableListsModel = new DefaultListModel<>();
	private DefaultListModel<String> editListModel = new DefaultListModel<>();
	private ArrayList<String> targetSuffixes = new ArrayList<>();
	private DefaultListModel<String> targetModel = new DefaultListModel<>();

	private String workingDirectory;
	private String activeTypeList;

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

	private void doTabCatalogs() {
		log.addInfo("doTabCatalogs()");
	}// doTabCatalogs

	private void doTabTypes() {
		log.addInfo("doTabTypes()");
	}// doTabTypes

	private void doTabLog() {
		log.addInfo("doTabLog()");
	}// doTabLog

	// Tab Lists Code ///////////////////////////////////////////////////////////

	private void changeTargetList() {
		String selectedValue = (String) listAvailable.getSelectedValue();
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
		for (String line : targetSuffixes) {
			targetModel.addElement(line);
		} // for

	}// loadTargetList

	private void doLoadTargetEdit() {
		log.addInfo("doLoadTargetEdit()");

		if (listAvailable.isSelectionEmpty()) {
			listAvailable.setSelectedIndex(0);
		} // ensure a selection
		String editName = (String) listAvailable.getSelectedValue();
		lblListEdit.setText(editName);
		txtActive.setText(editName);
		txtEdit.setText(EMPTY_STRING);
		// lblStatus.setText((String) listFileTypes.getSelectedValue());

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

		// manageEditButtons("Load");
		// validate();
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
			}//inner if
		}// if file exists
		
		try {
			Files.deleteIfExists(listPath);
			Files.createFile(listPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} //try

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
		doNewTargetEdit();	//loadNewTargetEdit();

	}// doDeleteList

	private void doAddRemove() {
		if (btnAddRemove.getText().equals(EDIT_REMOVE)) {
			int index = listEdit.getSelectedIndex();
			editListModel.removeElementAt(index);
		} else if (btnAddRemove.getText().equals(EDIT_ADD)) {
			editListModel.insertElementAt(txtEdit.getText().toUpperCase(), 0);
			txtEdit.setText(EMPTY_STRING);
		} // if
		btnAddRemove.setText(EDIT_ADD_REMOVE);
	}// doAddRemove

	private void doEditListMember() {
		if (!txtEdit.getText().equals(EMPTY_STRING)) {
			listEdit.clearSelection();
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
		listEdit.clearSelection();
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
		myPrefs.putInt("Divider", splitPane1.getDividerLocation());
		myPrefs.put("ActiveList", activeTypeList);
		// // myPrefs.put("ListDirectory", fileListDirectory);
		// myPrefs.putInt("SideButtonIndex", sideButtonIndex);
		// myPrefs.put("SourceDirectory", lblSourceFolder.getText());
		// String findTypeButton = bgFindType.getSelection().getActionCommand();
		// myPrefs.put("findTypeButton", findTypeButton);
		myPrefs = null;
	}// appClose

	private void appInit() {

		workingDirectory = getApplcationWorkingDirectory();

		Preferences myPrefs = Preferences.userNodeForPackage(Identic.class).node(this.getClass().getSimpleName());
		frmIdentic.setSize(916, 749);
		// frmIdentic.setSize(myPrefs.getInt("Width", 886), myPrefs.getInt("Height", 779));

		frmIdentic.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));
		splitPane1.setDividerLocation(myPrefs.getInt("Divider", 174));
		//
		// // fileListDirectory = myPrefs.get("ListDirectory", EMPTY_STRING);
		// sideButtonIndex = myPrefs.getInt("SideButtonIndex", 0);
		// lblSourceFolder.setText(myPrefs.get("SourceDirectory", NOT_SET));
		//
		// String findTypeButton = myPrefs.get("findTypeButton", RB_CATALOG_NO);
		// switch (findTypeButton) {
		// case RB_CATALOG_NO:
		// rbNoCatalog.setSelected(true);
		// break;
		// case RB_CATALOG_WITH:
		// rbWithCatalog.setSelected(true);
		// break;
		// case RB_CATALOG_ONLY:
		// rbOnlyCatalogs.setSelected(true);
		// break;
		// }// switch find Type
		//
		activeTypeList = myPrefs.get("ActiveList", "Pictures");
		loadTargetList();
		//
		myPrefs = null;

		// TypeLists //////
		listAvailable.setModel(availableListsModel);
		listEdit.setModel(editListModel);
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
		// bgFindType.add(rbNoCatalog);
		// bgFindType.add(rbWithCatalog);
		// bgFindType.add(rbOnlyCatalogs);
		//
		// availableCatalogItemModel.clear();
		// lstCatAvailable.setDragEnabled(true);
		// lstCatAvailable.setDropMode(DropMode.INSERT);
		// lstCatAvailable.setTransferHandler(new ListTransferHandler());
		//
		// inUseCatalogItemModel.clear();
		// lstInUse.setDragEnabled(true);
		// lstInUse.setDropMode(DropMode.INSERT);
		// lstInUse.setTransferHandler(new ListTransferHandler());
		tpMain.addChangeListener(identicAdapter);

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

		splitPane1 = new JSplitPane();
		GridBagConstraints gbc_splitPane1 = new GridBagConstraints();
		gbc_splitPane1.insets = new Insets(0, 0, 5, 0);
		gbc_splitPane1.fill = GridBagConstraints.BOTH;
		gbc_splitPane1.gridx = 0;
		gbc_splitPane1.gridy = 0;
		frmIdentic.getContentPane().add(splitPane1, gbc_splitPane1);

		tpMain = new JTabbedPane(JTabbedPane.TOP);
		splitPane1.setRightComponent(tpMain);

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

		listAvailable = new JList();
		listAvailable.addMouseListener(manageListsAdapter);
		listAvailable.setName(LIST_AVAILABLE);

		scrollListsAvailable.setViewportView(listAvailable);

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
//		txtActive.setName("txtActive");
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

		listEdit = new JList();
		listEdit.addListSelectionListener(manageListsAdapter);

		// listEdit.addMouseListener(manageListsAdapter);
		listEdit.setName(LIST_EDIT);

		scrollListContents.setViewportView(listEdit);

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
		gbl_tabCatalogs.columnWidths = new int[] { 0 };
		gbl_tabCatalogs.rowHeights = new int[] { 0 };
		gbl_tabCatalogs.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_tabCatalogs.rowWeights = new double[] { Double.MIN_VALUE };
		tabCatalogs.setLayout(gbl_tabCatalogs);

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

	private static final String TAB_CATALOGS = "tabCatalogs";
	private static final String TAB_TYPES = "tabTypes";
	private static final String TAB_LOG = "tabLog";

	private static final String MNU_FILE_EXIT = "mnuFileExit";

	private static final String EMPTY_STRING = "";

	// Tba Lists Constants
	private static final String EDIT_ADD = "Add";
	private static final String EDIT_REMOVE = "Remove";
	private static final String EDIT_ADD_REMOVE = "Add/Remove";
	private static final String BTN_ADD_REMOVE = "btnAddRemove";

	private static final String BTN_LOAD = "btnLoad";
	private static final String BTN_NEW = "btnNew";
	private static final String BTN_SAVE = "btnSave";
	private static final String BTN_DELETE = "btnDelete";
	private static final String BTN_MAKE_LIST_ACTIVE = "btnMakeListActive";

	private static final String LIST_AVAILABLE = "listAvailable";
	private static final String LIST_EDIT = "listEdit";

	private static final String TXT_ACTIVE = "txtActive";
	private static final String TXT_EDIT = "txtEdit";

	private static final String NEW_LIST = "<NEW>";
	private static final String LIST_SUFFIX = "typeList";
	private static final String LIST_SUFFIX_DOT = ".typeList";

	// members
	private JFrame frmIdentic;
	private JSplitPane splitPane1;
	private JTextPane txtLog;
	private JTabbedPane tpMain;
	private JTextField txtActive;
	private JList listAvailable;
	private JList listEdit;
	private JLabel lblStatusTypeList;
	private JTextField txtEdit;
	private JLabel lblListEdit;
	private JButton btnAddRemove;
	private JButton btnLoad;
	private JButton btnNew;
	private JButton btnSave;
	private JButton btnDelete;

	////////////////////// Included Classes ///////////////////////////////////////////

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

	class IdenticAdapter implements ActionListener, ListSelectionListener, ChangeListener {

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			// TODO Auto-generated method stub
		}// valueChanged

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			// Menus
			case MNU_FILE_EXIT:
				doFileExit();
				break;
			}// switch - name
		}// actionPerformed

		@Override
		public void stateChanged(ChangeEvent changeEvent) {
			JTabbedPane tabbedPane = (JTabbedPane) changeEvent.getSource();

			switch (tabbedPane.getSelectedComponent().getName()) {
			case TAB_CATALOGS:
				doTabCatalogs();
				break;
			case TAB_TYPES:
				doTabTypes();
				break;
			case TAB_LOG:
				doTabLog();
				break;
			}// switch - name

		}// stateChanged

	}// class IdenticAdapter

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
				if (name.equals(LIST_AVAILABLE)) {
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

}// class Identic
