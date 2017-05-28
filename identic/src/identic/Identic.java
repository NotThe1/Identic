package identic;

import java.awt.CardLayout;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Identic {

	private IdenticAdapter identicAdapter = new IdenticAdapter();
	private JButton[] sideMenuButtons;
	private String[] sideMenuPanelNames; // SortedComboBoxModel
	// private SortedComboBoxModel typeListModel = new SortedComboBoxModel();
	// private SortedComboBoxModel activeListModel = new SortedComboBoxModel();
	private DefaultComboBoxModel<String> typeListModel = new DefaultComboBoxModel<>();
	private DefaultComboBoxModel<String> activeListModel = new DefaultComboBoxModel<>();
	private ArrayList<String> targetSuffixes = new ArrayList<>();

	private String fileListDirectory;

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
				} // try
			}// run
		});
	}// main
		// ---------------FileTypes--------------------------------

	private void loadTargetList() {
		if(cboTypeLists.getSelectedIndex()== -1){//Nothing selected
			return;
		}
		String listName = (String) cboTypeLists.getSelectedItem();
		lblActiveList.setText(listName);
		String listFile = fileListDirectory + listName + LIST_SUFFIX_DOT;
		lblStatus.setText(listFile);

		Path p = Paths.get(listFile);
		try {
			targetSuffixes = (ArrayList<String>) Files.readAllLines(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // try
		lblActiveListCount.setText(String.format("%d", targetSuffixes.size()));
	}// loadTargetList

	private void loadTargetEdit() {
		if (listFileTypes.isSelectionEmpty()) {
			listFileTypes.setSelectedIndex(0);
		} // ensure a selection
		String editName = (String) listFileTypes.getSelectedValue();
		lblHotList.setText(editName);
		txtEditList.setText(editName);
		lblStatus.setText((String) listFileTypes.getSelectedValue());

		String editFile = fileListDirectory + editName + LIST_SUFFIX_DOT;
		Path p = Paths.get(editFile);
		try {
			ArrayList<String> lines = (ArrayList<String>) Files.readAllLines(p);
			activeListModel.removeAllElements();
			for (String line : lines) {
				activeListModel.addElement(line);
			} // for
		} catch (IOException e) {
			// TODO Auto-generated catch block activeListModel
			e.printStackTrace();
		} // try

		manageEditButtons("Load");

	}// loadTargetEdit

	private void loadNewTargetEdit() {
		String editName = NEW_LIST;
		txtEditList.setText(editName);
		lblHotList.setText(editName);
		activeListModel.removeAllElements();
		manageEditButtons("New");
	}// loadNewTargetEdit

	private void doNameChanged() {
		String newName = txtEditList.getText().trim();
		if (!lblHotList.getText().equals(newName)) {
			lblHotList.setText(newName);
			manageEditButtons("NameChanged");
		} // if
		listActiveList.clearSelection();
	}// doNameChanged

	private void doEditListMember() {
		if (!txtEditListMember.getText().equals(EMPTY_STRING)) {
			listActiveList.clearSelection();
			btnAddRemove.setText(EDIT_ADD);
		} else {
			btnAddRemove.setText(EDIT_ADD_REMOVE);
		} // if
	}// doEditListMember

	private void manageEditButtons(String action) {
		switch (action) {
		case "Load":
			btnFileListLoad.setEnabled(true);
			btnFileListSave.setEnabled(true);
			btnFileListNew.setEnabled(true);
			btnFileListDelete.setEnabled(true);
			break;
		case "New":
			btnFileListLoad.setEnabled(true);
			btnFileListNew.setEnabled(false);
			btnFileListSave.setEnabled(false);
			btnFileListDelete.setEnabled(false);
			break;
		case "NameChanged":
			btnFileListLoad.setEnabled(true);
			btnFileListNew.setEnabled(true);
			btnFileListSave.setEnabled(true);
			btnFileListDelete.setEnabled(false);
			break;
		case "Save":
			btnFileListLoad.setEnabled(true);
			btnFileListNew.setEnabled(true);
			btnFileListSave.setEnabled(true);
			btnFileListDelete.setEnabled(true);
			break;
		}// switch

	}// manageEditButtons

	private void doAddRemove() {
		if (btnAddRemove.getText().equals(EDIT_REMOVE)) {
			int index = listActiveList.getSelectedIndex();
			activeListModel.removeElementAt(index);
		} else if (btnAddRemove.getText().equals(EDIT_ADD)) {
			activeListModel.insertElementAt(txtEditListMember.getText().toUpperCase(), 0);
			txtEditListMember.setText(EMPTY_STRING);
		} // if
		btnAddRemove.setText(EDIT_ADD_REMOVE);
	}// doAddRemove

	// ---------------------------------------------------------
	// ---------------------------------------------------------

	private void doSideMenu(JButton button) {
		String targetPanelName = null;
		panelSideMenu.removeAll();
		for (int i = 0; i < sideMenuButtons.length; i++) {
			panelSideMenu.add(sideMenuButtons[i]);
			if (sideMenuButtons[i] == button) {
				panelSideMenu.add(panelDetails);
				targetPanelName = sideMenuPanelNames[i];
			} // if
		} // for
		CardLayout cl = (CardLayout) (panelDetails.getLayout());
		cl.show(panelDetails, targetPanelName);

		cl = (CardLayout) (panelMain.getLayout());
		cl.show(panelMain, targetPanelName);

		panelSideMenu.validate();
	}// doSideMenu

	private void doFileExit() {
		appClose();
		System.exit(0);
	}// doFileExit

	private void doSaveList() {
		String listFile = fileListDirectory + txtEditList.getText().toUpperCase() + LIST_SUFFIX_DOT;
		Path listPath = Paths.get(listFile);
		if (Files.exists(listPath)) {
			int ans = JOptionPane.showConfirmDialog(frmIdentic, "List Exits, Do you want to overwrite?",
					"Save File Suffix List", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (ans == JOptionPane.NO_OPTION) {
				return;
			} // if

		} // if file exists
		ArrayList<String> lines = new ArrayList<>();
		for (int i = 0; i < activeListModel.getSize(); i++) {
			lines.add(activeListModel.getElementAt(i));
		} // for

		try {
			Path p = Files.write(listPath, lines, StandardOpenOption.CREATE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // try
		
		initFileTypes();
		manageEditButtons("Save");
	}// doSaveList
	
	private void doDeleteList(){
		String listFile = fileListDirectory + txtEditList.getText().toUpperCase() + LIST_SUFFIX_DOT;
		Path listPath = Paths.get(listFile);
		if (Files.exists(listPath)) {
			int ans = JOptionPane.showConfirmDialog(frmIdentic, "Do you want to delete the list?",
					"Delete Suffix List", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (ans == JOptionPane.NO_OPTION) {
				return;
			} // if			
		} // if file exists
		try {
			Files.deleteIfExists(listPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//try
		initFileTypes();
		loadNewTargetEdit();
	}//doDeleteList

	// ---------------------------------------------------------

	class ListFilter implements FilenameFilter {

		@Override
		public boolean accept(File arg0, String arg1) {
			if (arg1.endsWith(LIST_SUFFIX)) {
				return true;
			} // if
			return false;
		}// accept
	}// ListFilter

	private void initFileTypes() {

		// see if the directory has been set up already
		if (fileListDirectory.equals(EMPTY_STRING)) {
			fileListDirectory = System.getProperty("java.io.tmpdir");
			fileListDirectory = fileListDirectory.replace("Temp", "Identic");

			Path p = Paths.get(fileListDirectory);
			if (!Files.exists(p)) {
				JOptionPane.showMessageDialog(frmIdentic, "Initializing File Type lists in " + p.toString(),
						"Initialization", JOptionPane.INFORMATION_MESSAGE);
				System.err.println("Making new directory");
				try {
					Files.createDirectories(p);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // try
			} // if exits

		} // if not there

		// we have the directory, do we have lists ?

		File targetDirectory = new File(fileListDirectory);
		File[] files = targetDirectory.listFiles(new ListFilter());

		// if files empty - initialize the directory
		if (files.length == 0) {

			String[] initalListFiles = new String[] { "/VB.typeList", "/Music.typeList", "/MusicAndPictures.typeList",
					"/Pictures.typeList" };
			ArrayList<Path> sources = new ArrayList<>();
			Path newDir = Paths.get(fileListDirectory);
			Path source = null;
			for (int i = 0; i < initalListFiles.length; i++) {
				try {
					// sources.add(Paths.get(this.getClass().getResource(initalListFiles[i] ).toURI()));
					source = Paths.get(this.getClass().getResource(initalListFiles[i]).toURI());
					Files.move(source, newDir.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
				} catch (URISyntaxException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // try
			} // for
			files = targetDirectory.listFiles(new ListFilter());
		} // if no type list files in target directory

		// set up cbo model

		typeListModel.removeAllElements();

		for (File f : files) {
			typeListModel.addElement(f.getName().replace(LIST_SUFFIX_DOT, EMPTY_STRING));
		} // for

		cboTypeLists.setModel(typeListModel);

		Preferences myPrefs = Preferences.userNodeForPackage(Identic.class).node(this.getClass().getSimpleName());
		cboTypeLists.setSelectedItem(myPrefs.get("ActiveList", "Pictures"));
		listFileTypes.setModel(typeListModel);
		myPrefs = null;

		// cboTypeLists.

		// lblStatus.setText(url.getPath());
	}// initFileTypes
	
	//private void display

	private void appClose() {
		Preferences myPrefs = Preferences.userNodeForPackage(Identic.class).node(this.getClass().getSimpleName());
		Dimension dim = frmIdentic.getSize();
		myPrefs.putInt("Height", dim.height);
		myPrefs.putInt("Width", dim.width);
		Point point = frmIdentic.getLocation();
		myPrefs.putInt("LocX", point.x);
		myPrefs.putInt("LocY", point.y);
		myPrefs.putInt("Divider", splitPane1.getDividerLocation());
		myPrefs.put("ListDirectory", fileListDirectory);
		myPrefs.put("ActiveList", (String) cboTypeLists.getSelectedItem());
		myPrefs = null;
	}// appClose

	private void appInit() {

		Preferences myPrefs = Preferences.userNodeForPackage(Identic.class).node(this.getClass().getSimpleName());
		frmIdentic.setSize(myPrefs.getInt("Width", 761), myPrefs.getInt("Height", 693));
		frmIdentic.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));
		splitPane1.setDividerLocation(myPrefs.getInt("Divider", 250));

		fileListDirectory = myPrefs.get("ListDirectory", EMPTY_STRING);
		cboTypeLists.setSelectedItem(myPrefs.get("ActiveList", "Pictures"));
		myPrefs = null;

		// These two arrays are synchronized to control the button positions and the selection of the correct panels.
		sideMenuButtons = new JButton[] { btnFindDuplicates, btnFindDuplicatesByName, btnDisplayResults,
				btnCopyMoveRemove, btnFileTypes };
		sideMenuPanelNames = new String[] { panelFindDuplicates.getName(), panelFindDuplicatesByName.getName(),
				panelDisplayResults.getName(), panelCopyMoveRemove.getName(), panelFileTypes.getName() };
		listActiveList.setModel(activeListModel);
	}// appInit

	public Identic() {
		initialize();
		appInit();
		initFileTypes();
	}// Constructor

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmIdentic = new JFrame();
		frmIdentic.setTitle("Identic 1.0");
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
		gridBagLayout.rowHeights = new int[] { 0, 0, 25, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		frmIdentic.getContentPane().setLayout(gridBagLayout);

		JPanel panelForButtons = new JPanel();
		GridBagConstraints gbc_panelForButtons = new GridBagConstraints();
		gbc_panelForButtons.anchor = GridBagConstraints.NORTH;
		gbc_panelForButtons.insets = new Insets(0, 0, 5, 0);
		gbc_panelForButtons.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelForButtons.gridx = 0;
		gbc_panelForButtons.gridy = 0;
		frmIdentic.getContentPane().add(panelForButtons, gbc_panelForButtons);
		GridBagLayout gbl_panelForButtons = new GridBagLayout();
		gbl_panelForButtons.columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panelForButtons.rowHeights = new int[] { 0, 0 };
		gbl_panelForButtons.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelForButtons.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelForButtons.setLayout(gbl_panelForButtons);

		splitPane1 = new JSplitPane();
		GridBagConstraints gbc_splitPane1 = new GridBagConstraints();
		gbc_splitPane1.insets = new Insets(0, 0, 5, 0);
		gbc_splitPane1.fill = GridBagConstraints.BOTH;
		gbc_splitPane1.gridx = 0;
		gbc_splitPane1.gridy = 1;
		frmIdentic.getContentPane().add(splitPane1, gbc_splitPane1);

		panelSideMenu = new JPanel();
		splitPane1.setLeftComponent(panelSideMenu);
		panelSideMenu.setLayout(new BoxLayout(panelSideMenu, BoxLayout.Y_AXIS));

		btnFindDuplicates = new JButton("Find Duplicates");
		btnFindDuplicates.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnFindDuplicates.addActionListener(identicAdapter);
		btnFindDuplicates.setName(BTN_FIND_DUPS);
		btnFindDuplicates.setMaximumSize(new Dimension(1000, 23));
		panelSideMenu.add(btnFindDuplicates);

		btnFindDuplicatesByName = new JButton("Find Duplicates By Name");
		btnFindDuplicatesByName.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnFindDuplicatesByName.addActionListener(identicAdapter);
		btnFindDuplicatesByName.setName(BTN_FIND_DUPS_BY_NAME);
		btnFindDuplicatesByName.setMaximumSize(new Dimension(1000, 23));
		panelSideMenu.add(btnFindDuplicatesByName);

		btnDisplayResults = new JButton("Display Results");
		btnDisplayResults.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnDisplayResults.addActionListener(identicAdapter);
		btnDisplayResults.setName(BTN_DISPLAY_RESULTS);
		btnDisplayResults.setMaximumSize(new Dimension(1000, 23));
		panelSideMenu.add(btnDisplayResults);

		btnCopyMoveRemove = new JButton("Copy/Move/Remove");
		btnCopyMoveRemove.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnCopyMoveRemove.addActionListener(identicAdapter);
		btnCopyMoveRemove.setName(BTN_COPY_MOVE_REMOVE);
		btnCopyMoveRemove.setMaximumSize(new Dimension(1000, 23));
		panelSideMenu.add(btnCopyMoveRemove);

		btnFileTypes = new JButton("File Types");
		btnFileTypes.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnFileTypes.addActionListener(identicAdapter);
		btnFileTypes.setName(BTN_FILE_TYPES);
		btnFileTypes.setMaximumSize(new Dimension(1000, 23));
		panelSideMenu.add(btnFileTypes);

		panelDetails = new JPanel();
		panelSideMenu.add(panelDetails);
		panelDetails.setLayout(new CardLayout(0, 0));

		panelFindDuplicates = new JPanel();
		panelFindDuplicates.setName(PNL_FIND_DUPS);
		panelDetails.add(panelFindDuplicates, PNL_FIND_DUPS);
		panelFindDuplicates.setLayout(new BoxLayout(panelFindDuplicates, BoxLayout.Y_AXIS));

		JPanel panel1 = new JPanel();
		panel1.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		panel1.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true),
				"FInd Duplicates", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)), null));
		panel1.setMaximumSize(new Dimension(150, 100));
		panelFindDuplicates.add(panel1);
		GridBagLayout gbl_panel1 = new GridBagLayout();
		gbl_panel1.columnWidths = new int[] { 113, 0 };
		gbl_panel1.rowHeights = new int[] { 10, 0, 10, 0 };
		gbl_panel1.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel1.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel1.setLayout(gbl_panel1);

		JLabel lblNewLabel_2 = new JLabel("No Active List");
		lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 1;
		panel1.add(lblNewLabel_2, gbc_lblNewLabel_2);

		JLabel lblNewLabel_3 = new JLabel("New label");
		lblNewLabel_3.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 2;
		panel1.add(lblNewLabel_3, gbc_lblNewLabel_3);

		// cboTypeListsOld = new JComboBox();
		// cboTypeListsOld.addActionListener(identicAdapter);
		// cboTypeListsOld.setName(CBO_FILE_TYPES);
		// cboTypeListsOld.setMaximumSize(new Dimension(32767, 30));
		//// cboTypeLists.setModel(new DefaultComboBoxModel(new String[] { "panelFindDuplicates.getName()",
		//// "panelFindDuplicatesByName.getName()", "panelDisplayResults.getName()", "panelCopyMoveRemove.getName()",
		//// "panelFileTypespanelFindDuplicates.getName()", "panelFindDuplicatesByName.getName()",
		//// "panelDisplayResults.getName()", "panelCopyMoveRemove.getName()",
		//// "panelFileTypespanelFindDuplicates.getName()", "panelFindDuplicatesByName.getName()",
		//// "panelDisplayResults.getName()", "panelCopyMoveRemove.getName()", "panelFileTypes" }));
		// panelFindDuplicates.add(cboTypeListsOld
		// );

		panelFindDuplicatesByName = new JPanel();
		panelFindDuplicatesByName.setName(PNL_FIND_DUPS_BY_NAME);
		panelDetails.add(panelFindDuplicatesByName, PNL_FIND_DUPS_BY_NAME);// "name_669979253199403"
		GridBagLayout gbl_panelFindDuplicatesByName = new GridBagLayout();
		gbl_panelFindDuplicatesByName.columnWidths = new int[] { 0, 0 };
		gbl_panelFindDuplicatesByName.rowHeights = new int[] { 0, 0 };
		gbl_panelFindDuplicatesByName.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelFindDuplicatesByName.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelFindDuplicatesByName.setLayout(gbl_panelFindDuplicatesByName);

		JLabel lblFindDuplicatesBy = new JLabel("Find Duplicates By Name");
		GridBagConstraints gbc_lblFindDuplicatesBy = new GridBagConstraints();
		gbc_lblFindDuplicatesBy.gridx = 0;
		gbc_lblFindDuplicatesBy.gridy = 0;
		panelFindDuplicatesByName.add(lblFindDuplicatesBy, gbc_lblFindDuplicatesBy);

		panelDisplayResults = new JPanel();
		panelDisplayResults.setName(PNL_DISPLAY_RESULTS);
		panelDetails.add(panelDisplayResults, PNL_DISPLAY_RESULTS);// "name_670006781010300"
		GridBagLayout gbl_panelDisplayResults = new GridBagLayout();
		gbl_panelDisplayResults.columnWidths = new int[] { 0, 0 };
		gbl_panelDisplayResults.rowHeights = new int[] { 0, 0 };
		gbl_panelDisplayResults.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelDisplayResults.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelDisplayResults.setLayout(gbl_panelDisplayResults);

		JLabel lblDisplayResults = new JLabel("Display Results");
		GridBagConstraints gbc_lblDisplayResults = new GridBagConstraints();
		gbc_lblDisplayResults.gridx = 0;
		gbc_lblDisplayResults.gridy = 0;
		panelDisplayResults.add(lblDisplayResults, gbc_lblDisplayResults);

		panelCopyMoveRemove = new JPanel();
		panelCopyMoveRemove.setName(PNL_COPY_MOVE_REMOVE);
		panelDetails.add(panelCopyMoveRemove, PNL_COPY_MOVE_REMOVE);// "name_670030448605218"
		GridBagLayout gbl_panelCopyMoveRemove = new GridBagLayout();
		gbl_panelCopyMoveRemove.columnWidths = new int[] { 0, 0 };
		gbl_panelCopyMoveRemove.rowHeights = new int[] { 0, 0 };
		gbl_panelCopyMoveRemove.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelCopyMoveRemove.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelCopyMoveRemove.setLayout(gbl_panelCopyMoveRemove);

		JLabel lblCopymoveremove = new JLabel("Copy/Move/Remove");
		GridBagConstraints gbc_lblCopymoveremove = new GridBagConstraints();
		gbc_lblCopymoveremove.gridx = 0;
		gbc_lblCopymoveremove.gridy = 0;
		panelCopyMoveRemove.add(lblCopymoveremove, gbc_lblCopymoveremove);

		panelFileTypes = new JPanel();
		panelFileTypes.setName(PNL_FILE_TYPES);
		panelDetails.add(panelFileTypes, PNL_FILE_TYPES);
		panelFileTypes.setLayout(new BoxLayout(panelFileTypes, BoxLayout.Y_AXIS));

		JLabel lblNewLabel = new JLabel("File Types");
		panelFileTypes.add(lblNewLabel);

		Component verticalStrut_2 = Box.createVerticalStrut(20);
		panelFileTypes.add(verticalStrut_2);

		JPanel panel = new JPanel();
		panel.setMaximumSize(new Dimension(150, 100));
		panel.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Active List",

				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)), null));
		panel.setAlignmentY(1.0f);
		panelFileTypes.add(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 113, 0 };
		gbl_panel.rowHeights = new int[] { 10, 0, 10, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		lblActiveList = new JLabel("No Active List");
		lblActiveList.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblActiveList = new GridBagConstraints();
		gbc_lblActiveList.insets = new Insets(0, 0, 5, 0);
		gbc_lblActiveList.gridx = 0;
		gbc_lblActiveList.gridy = 1;
		panel.add(lblActiveList, gbc_lblActiveList);

		lblActiveListCount = new JLabel("New label");
		lblActiveListCount.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblActiveListCount = new GridBagConstraints();
		gbc_lblActiveListCount.gridx = 0;
		gbc_lblActiveListCount.gridy = 2;
		panel.add(lblActiveListCount, gbc_lblActiveListCount);

		Component verticalStrut_3 = Box.createVerticalStrut(20);
		panelFileTypes.add(verticalStrut_3);

		cboTypeLists = new JComboBox();
		cboTypeLists.setName(CBO_FILE_TYPES);
		cboTypeLists.addActionListener(identicAdapter);
		cboTypeLists.setMaximumSize(new Dimension(32767, 30));
		panelFileTypes.add(cboTypeLists);

		panelMain = new JPanel();
		splitPane1.setRightComponent(panelMain);
		panelMain.setLayout(new CardLayout(0, 0));

		JPanel panelMainFIndDuplicates = new JPanel();
		panelMain.add(panelMainFIndDuplicates, PNL_FIND_DUPS);
		GridBagLayout gbl_panelMainFIndDuplicates = new GridBagLayout();
		gbl_panelMainFIndDuplicates.columnWidths = new int[] { 0, 0 };
		gbl_panelMainFIndDuplicates.rowHeights = new int[] { 0, 0 };
		gbl_panelMainFIndDuplicates.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelMainFIndDuplicates.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelMainFIndDuplicates.setLayout(gbl_panelMainFIndDuplicates);

		JLabel lblFindDuplicates_1 = new JLabel("Find Duplicates");
		GridBagConstraints gbc_lblFindDuplicates_1 = new GridBagConstraints();
		gbc_lblFindDuplicates_1.gridx = 0;
		gbc_lblFindDuplicates_1.gridy = 0;
		panelMainFIndDuplicates.add(lblFindDuplicates_1, gbc_lblFindDuplicates_1);

		JPanel panelMainFIndDuplicatesByName = new JPanel();
		panelMain.add(panelMainFIndDuplicatesByName, PNL_FIND_DUPS_BY_NAME);
		GridBagLayout gbl_panelMainFIndDuplicatesByName = new GridBagLayout();
		gbl_panelMainFIndDuplicatesByName.columnWidths = new int[] { 0, 0 };
		gbl_panelMainFIndDuplicatesByName.rowHeights = new int[] { 0, 0 };
		gbl_panelMainFIndDuplicatesByName.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelMainFIndDuplicatesByName.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelMainFIndDuplicatesByName.setLayout(gbl_panelMainFIndDuplicatesByName);

		JLabel lblFindDuplicatesBy_1 = new JLabel("Find Duplicates by Name");
		GridBagConstraints gbc_lblFindDuplicatesBy_1 = new GridBagConstraints();
		gbc_lblFindDuplicatesBy_1.gridx = 0;
		gbc_lblFindDuplicatesBy_1.gridy = 0;
		panelMainFIndDuplicatesByName.add(lblFindDuplicatesBy_1, gbc_lblFindDuplicatesBy_1);

		JPanel panelMainDisplayResults = new JPanel();
		panelMain.add(panelMainDisplayResults, PNL_DISPLAY_RESULTS);
		GridBagLayout gbl_panelMainDisplayResults = new GridBagLayout();
		gbl_panelMainDisplayResults.columnWidths = new int[] { 0, 0 };
		gbl_panelMainDisplayResults.rowHeights = new int[] { 0, 0 };
		gbl_panelMainDisplayResults.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelMainDisplayResults.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelMainDisplayResults.setLayout(gbl_panelMainDisplayResults);

		JLabel lblDisplayResults_1 = new JLabel("Display Results");
		GridBagConstraints gbc_lblDisplayResults_1 = new GridBagConstraints();
		gbc_lblDisplayResults_1.gridx = 0;
		gbc_lblDisplayResults_1.gridy = 0;
		panelMainDisplayResults.add(lblDisplayResults_1, gbc_lblDisplayResults_1);

		JPanel panelMainCopyMoveRemove = new JPanel();
		panelMain.add(panelMainCopyMoveRemove, PNL_COPY_MOVE_REMOVE);
		GridBagLayout gbl_panelMainCopyMoveRemove = new GridBagLayout();
		gbl_panelMainCopyMoveRemove.columnWidths = new int[] { 0, 0 };
		gbl_panelMainCopyMoveRemove.rowHeights = new int[] { 0, 0 };
		gbl_panelMainCopyMoveRemove.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelMainCopyMoveRemove.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelMainCopyMoveRemove.setLayout(gbl_panelMainCopyMoveRemove);

		JLabel lblCopyMoveRemove = new JLabel("Copy Move Remove");
		GridBagConstraints gbc_lblCopyMoveRemove = new GridBagConstraints();
		gbc_lblCopyMoveRemove.gridx = 0;
		gbc_lblCopyMoveRemove.gridy = 0;
		panelMainCopyMoveRemove.add(lblCopyMoveRemove, gbc_lblCopyMoveRemove);

		JPanel panelMainFileTypes = new JPanel();
		panelMain.add(panelMainFileTypes, PNL_FILE_TYPES);
		GridBagLayout gbl_panelMainFileTypes = new GridBagLayout();
		gbl_panelMainFileTypes.columnWidths = new int[] { 0, 227, 161, 0, 0 };
		gbl_panelMainFileTypes.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panelMainFileTypes.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelMainFileTypes.rowWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panelMainFileTypes.setLayout(gbl_panelMainFileTypes);

		JPanel panelFileTypes1 = new JPanel();
		panelFileTypes1.setBorder(new CompoundBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Available Lists", TitledBorder.CENTER,
						TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0)),
				new BevelBorder(BevelBorder.LOWERED, null, null, null, null)));
		GridBagConstraints gbc_panelFileTypes1 = new GridBagConstraints();
		gbc_panelFileTypes1.anchor = GridBagConstraints.WEST;
		gbc_panelFileTypes1.insets = new Insets(0, 0, 0, 5);
		gbc_panelFileTypes1.fill = GridBagConstraints.VERTICAL;
		gbc_panelFileTypes1.gridx = 1;
		gbc_panelFileTypes1.gridy = 2;
		panelMainFileTypes.add(panelFileTypes1, gbc_panelFileTypes1);
		GridBagLayout gbl_panelFileTypes1 = new GridBagLayout();
		gbl_panelFileTypes1.columnWidths = new int[] { 0, 0 };
		gbl_panelFileTypes1.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panelFileTypes1.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelFileTypes1.rowWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
		panelFileTypes1.setLayout(gbl_panelFileTypes1);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setMinimumSize(new Dimension(200, 23));
		scrollPane.setPreferredSize(new Dimension(200, 2));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.VERTICAL;
		gbc_scrollPane.anchor = GridBagConstraints.WEST;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panelFileTypes1.add(scrollPane, gbc_scrollPane);

		listFileTypes = new JList();
		listFileTypes.addMouseListener(identicAdapter);
		listFileTypes.setName(LIST_FILE_TYPES);
		listFileTypes.setPreferredSize(new Dimension(150, 0));
		listFileTypes.setMinimumSize(new Dimension(150, 0));
		scrollPane.setViewportView(listFileTypes);

		// JButton btnSelectList = new JButton("Select List");
		// btnSelectList.addActionListener(identicAdapter);
		// btnSelectList.setName(BTN_SELECT_LIST);
		// GridBagConstraints gbc_btnSelectList = new GridBagConstraints();
		// gbc_btnSelectList.gridx = 0;
		// gbc_btnSelectList.gridy = 2;
		// panelFileTypes1.add(btnSelectList, gbc_btnSelectList);

		JPanel panelFileTypes2 = new JPanel();
		panelFileTypes2.setBorder(null);
		GridBagConstraints gbc_panelFileTypes2 = new GridBagConstraints();
		gbc_panelFileTypes2.insets = new Insets(0, 0, 0, 5);
		gbc_panelFileTypes2.fill = GridBagConstraints.BOTH;
		gbc_panelFileTypes2.gridx = 2;
		gbc_panelFileTypes2.gridy = 2;
		panelMainFileTypes.add(panelFileTypes2, gbc_panelFileTypes2);
		GridBagLayout gbl_panelFileTypes2 = new GridBagLayout();
		gbl_panelFileTypes2.columnWidths = new int[] { 0, 0 };
		gbl_panelFileTypes2.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panelFileTypes2.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelFileTypes2.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelFileTypes2.setLayout(gbl_panelFileTypes2);

		Component verticalStrut_1 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_1 = new GridBagConstraints();
		gbc_verticalStrut_1.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_1.gridx = 0;
		gbc_verticalStrut_1.gridy = 0;
		panelFileTypes2.add(verticalStrut_1, gbc_verticalStrut_1);

		JPanel panelFileType2A = new JPanel();
		panelFileType2A.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)),
				"Manage Lists", TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0)), null));
		GridBagConstraints gbc_panelFileType2A = new GridBagConstraints();
		gbc_panelFileType2A.insets = new Insets(0, 0, 5, 0);
		gbc_panelFileType2A.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelFileType2A.gridx = 0;
		gbc_panelFileType2A.gridy = 1;
		panelFileTypes2.add(panelFileType2A, gbc_panelFileType2A);
		GridBagLayout gbl_panelFileType2A = new GridBagLayout();
		gbl_panelFileType2A.columnWidths = new int[] { 0, 0 };
		gbl_panelFileType2A.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelFileType2A.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelFileType2A.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		panelFileType2A.setLayout(gbl_panelFileType2A);

		Component verticalStrut_5 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_5 = new GridBagConstraints();
		gbc_verticalStrut_5.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_5.gridx = 0;
		gbc_verticalStrut_5.gridy = 0;
		panelFileType2A.add(verticalStrut_5, gbc_verticalStrut_5);

		txtEditList = new JTextField();
		txtEditList.addFocusListener(identicAdapter);
		txtEditList.setName(TXT_EDIT_LIST);
		txtEditList.setHorizontalAlignment(SwingConstants.CENTER);
		txtEditList.setPreferredSize(new Dimension(300, 23));
		txtEditList.setMinimumSize(new Dimension(300, 23));
		txtEditList.setMaximumSize(new Dimension(300, 23));
		GridBagConstraints gbc_txtEditList = new GridBagConstraints();
		gbc_txtEditList.insets = new Insets(0, 0, 5, 0);
		gbc_txtEditList.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEditList.gridx = 0;
		gbc_txtEditList.gridy = 1;
		panelFileType2A.add(txtEditList, gbc_txtEditList);
		txtEditList.setColumns(10);

		Component verticalStrut_6 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_6 = new GridBagConstraints();
		gbc_verticalStrut_6.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_6.gridx = 0;
		gbc_verticalStrut_6.gridy = 2;
		panelFileType2A.add(verticalStrut_6, gbc_verticalStrut_6);

		btnFileListNew = new JButton("New");
		btnFileListNew.addActionListener(identicAdapter);

		btnFileListLoad = new JButton("Load");
		btnFileListLoad.addActionListener(identicAdapter);
		btnFileListLoad.setName(BTN_FILE_LIST_LOAD);
		btnFileListLoad.setToolTipText("Load a list from from selection on left");
		GridBagConstraints gbc_btnFileListLoad = new GridBagConstraints();
		gbc_btnFileListLoad.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnFileListLoad.insets = new Insets(0, 0, 5, 0);
		gbc_btnFileListLoad.gridx = 0;
		gbc_btnFileListLoad.gridy = 3;
		panelFileType2A.add(btnFileListLoad, gbc_btnFileListLoad);

		Component verticalStrut_13 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_13 = new GridBagConstraints();
		gbc_verticalStrut_13.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_13.gridx = 0;
		gbc_verticalStrut_13.gridy = 4;
		panelFileType2A.add(verticalStrut_13, gbc_verticalStrut_13);
		btnFileListNew.setName(BTN_FILE_LIST_NEW);
		btnFileListNew.setMaximumSize(new Dimension(75, 23));
		btnFileListNew.setMinimumSize(new Dimension(75, 23));
		btnFileListNew.setPreferredSize(new Dimension(75, 23));
		GridBagConstraints gbc_btnFileListNew = new GridBagConstraints();
		gbc_btnFileListNew.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnFileListNew.insets = new Insets(0, 0, 5, 0);
		gbc_btnFileListNew.gridx = 0;
		gbc_btnFileListNew.gridy = 5;
		panelFileType2A.add(btnFileListNew, gbc_btnFileListNew);

		Component verticalStrut_7 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_7 = new GridBagConstraints();
		gbc_verticalStrut_7.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_7.gridx = 0;
		gbc_verticalStrut_7.gridy = 6;
		panelFileType2A.add(verticalStrut_7, gbc_verticalStrut_7);

		btnFileListSave = new JButton("Save");
		btnFileListSave.setEnabled(false);
		btnFileListSave.addActionListener(identicAdapter);
		btnFileListSave.setName(BTN_FILE_LIST_SAVE);
		btnFileListSave.setMaximumSize(new Dimension(75, 23));
		btnFileListSave.setMinimumSize(new Dimension(75, 23));
		btnFileListSave.setPreferredSize(new Dimension(75, 23));
		GridBagConstraints gbc_btnFileListSave = new GridBagConstraints();
		gbc_btnFileListSave.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnFileListSave.insets = new Insets(0, 0, 5, 0);
		gbc_btnFileListSave.gridx = 0;
		gbc_btnFileListSave.gridy = 7;
		panelFileType2A.add(btnFileListSave, gbc_btnFileListSave);

		// btnFileListSaveAs = new JButton("Save As");
		// btnFileListSaveAs.setEnabled(false);
		// btnFileListSaveAs.addActionListener(identicAdapter);
		// btnFileListSaveAs.setName(BTN_FILE_LIST_SAVE_AS);
		// btnFileListSaveAs.setPreferredSize(new Dimension(75, 23));
		// btnFileListSaveAs.setMinimumSize(new Dimension(75, 23));
		// btnFileListSaveAs.setMaximumSize(new Dimension(75, 23));
		// GridBagConstraints gbc_btnFileListSaveAs = new GridBagConstraints();
		// gbc_btnFileListSaveAs.fill = GridBagConstraints.HORIZONTAL;
		// gbc_btnFileListSaveAs.insets = new Insets(0, 0, 5, 0);
		// gbc_btnFileListSaveAs.gridx = 0;
		// gbc_btnFileListSaveAs.gridy = 9;
		// panelFileType2A.add(btnFileListSaveAs, gbc_btnFileListSaveAs);

		Component verticalStrut_9 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_9 = new GridBagConstraints();
		gbc_verticalStrut_9.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_9.gridx = 0;
		gbc_verticalStrut_9.gridy = 10;
		panelFileType2A.add(verticalStrut_9, gbc_verticalStrut_9);

		btnFileListDelete = new JButton("Delete");
		btnFileListDelete.setEnabled(false);
		btnFileListDelete.addActionListener(identicAdapter);
		btnFileListDelete.setName(BTN_FILE_LIST_DELETE);
		btnFileListDelete.setMaximumSize(new Dimension(75, 23));
		btnFileListDelete.setMinimumSize(new Dimension(75, 23));
		btnFileListDelete.setPreferredSize(new Dimension(75, 23));
		GridBagConstraints gbc_btnFileListDelete = new GridBagConstraints();
		gbc_btnFileListDelete.insets = new Insets(0, 0, 5, 0);
		gbc_btnFileListDelete.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnFileListDelete.gridx = 0;
		gbc_btnFileListDelete.gridy = 11;
		panelFileType2A.add(btnFileListDelete, gbc_btnFileListDelete);

		Component verticalStrut_10 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_10 = new GridBagConstraints();
		gbc_verticalStrut_10.gridx = 0;
		gbc_verticalStrut_10.gridy = 12;
		panelFileType2A.add(verticalStrut_10, gbc_verticalStrut_10);

		Component verticalStrut_4 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_4 = new GridBagConstraints();
		gbc_verticalStrut_4.gridx = 0;
		gbc_verticalStrut_4.gridy = 2;
		panelFileTypes2.add(verticalStrut_4, gbc_verticalStrut_4);

		JPanel panelFileTypes3 = new JPanel();
		panelFileTypes3.setBorder(new CompoundBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), "List Being Edited", TitledBorder.CENTER,
						TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0)),
				new BevelBorder(BevelBorder.LOWERED, null, null, null, null)));
		GridBagConstraints gbc_panelFileTypes3 = new GridBagConstraints();
		gbc_panelFileTypes3.anchor = GridBagConstraints.WEST;
		gbc_panelFileTypes3.fill = GridBagConstraints.VERTICAL;
		gbc_panelFileTypes3.gridx = 3;
		gbc_panelFileTypes3.gridy = 2;
		panelMainFileTypes.add(panelFileTypes3, gbc_panelFileTypes3);
		GridBagLayout gbl_panelFileTypes3 = new GridBagLayout();
		gbl_panelFileTypes3.columnWidths = new int[] { 0, 0 };
		gbl_panelFileTypes3.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panelFileTypes3.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelFileTypes3.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelFileTypes3.setLayout(gbl_panelFileTypes3);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setPreferredSize(new Dimension(200, 2));
		scrollPane_1.setMinimumSize(new Dimension(200, 23));
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 0;
		panelFileTypes3.add(scrollPane_1, gbc_scrollPane_1);

		listActiveList = new JList();
		listActiveList.addListSelectionListener(identicAdapter);
		listActiveList.setName(LIST_ACTIVE_LIST);

		scrollPane_1.setViewportView(listActiveList);

		lblHotList = new JLabel("<none>");
		lblHotList.setForeground(Color.BLUE);
		lblHotList.setHorizontalAlignment(SwingConstants.CENTER);
		scrollPane_1.setColumnHeaderView(lblHotList);

		Component verticalStrut_12 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_12 = new GridBagConstraints();
		gbc_verticalStrut_12.fill = GridBagConstraints.HORIZONTAL;
		gbc_verticalStrut_12.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_12.gridx = 0;
		gbc_verticalStrut_12.gridy = 1;
		panelFileTypes3.add(verticalStrut_12, gbc_verticalStrut_12);

		txtEditListMember = new JTextField();
		txtEditListMember.addFocusListener(identicAdapter);
		txtEditListMember.setName(TXT_EDIT_LIST_MEMBER);
		GridBagConstraints gbc_txtEditListMember = new GridBagConstraints();
		gbc_txtEditListMember.insets = new Insets(0, 0, 5, 0);
		gbc_txtEditListMember.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEditListMember.gridx = 0;
		gbc_txtEditListMember.gridy = 2;
		panelFileTypes3.add(txtEditListMember, gbc_txtEditListMember);
		txtEditListMember.setColumns(10);

		Component verticalStrut_11 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_11 = new GridBagConstraints();
		gbc_verticalStrut_11.fill = GridBagConstraints.HORIZONTAL;
		gbc_verticalStrut_11.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_11.gridx = 0;
		gbc_verticalStrut_11.gridy = 3;
		panelFileTypes3.add(verticalStrut_11, gbc_verticalStrut_11);

		btnAddRemove = new JButton("Add/Remove");
		btnAddRemove.addActionListener(identicAdapter);
		btnAddRemove.setName(BTN_ADD_REMOVE);
		GridBagConstraints gbc_btnAddRemove = new GridBagConstraints();
		gbc_btnAddRemove.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAddRemove.gridx = 0;
		gbc_btnAddRemove.gridy = 4;
		panelFileTypes3.add(btnAddRemove, gbc_btnAddRemove);
		splitPane1.setDividerLocation(250);

		JPanel panelStatus = new JPanel();
		panelStatus.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelStatus = new GridBagConstraints();
		gbc_panelStatus.fill = GridBagConstraints.BOTH;
		gbc_panelStatus.gridx = 0;
		gbc_panelStatus.gridy = 2;
		frmIdentic.getContentPane().add(panelStatus, gbc_panelStatus);

		lblStatus = new JLabel("Status");
		panelStatus.add(lblStatus);

		JMenuBar menuBar = new JMenuBar();
		frmIdentic.setJMenuBar(menuBar);

		JMenu mnuFile = new JMenu("File");
		menuBar.add(mnuFile);

		JMenuItem mnuFileExit = new JMenuItem("Exit");
		mnuFileExit.setName(MNU_FILE_EXIT);
		mnuFileExit.addActionListener(identicAdapter);
		mnuFile.add(mnuFileExit);

		JMenu mnuReports = new JMenu("Reports");
		menuBar.add(mnuReports);

		JMenuItem mnuReportsViewLogFiles = new JMenuItem("View Log Files");
		mnuReportsViewLogFiles.setName(MNU_REPORTS_LOG_FILES);
		mnuReports.add(mnuReportsViewLogFiles);

		JMenuItem mnuReportsViewXmlDocuments = new JMenuItem("View XML Documents");
		mnuReportsViewXmlDocuments.setName(MNU_REPORTS_XML_DOC);
		mnuReports.add(mnuReportsViewXmlDocuments);

		JMenu mnuHelp = new JMenu("Help");
		menuBar.add(mnuHelp);

		JMenuItem mnuHelpAbout = new JMenuItem("About");
		mnuHelpAbout.setName(MNU_HELP_ABOUT);
		mnuHelp.add(mnuHelpAbout);

	}// initialize

	class SortedComboBoxModel extends DefaultComboBoxModel {
		public SortedComboBoxModel() {
			super();
		}

		public SortedComboBoxModel(Object[] items) {
			Arrays.sort(items);
			int size = items.length;
			for (int i = 0; i < size; i++) {
				super.addElement(items[i]);
			}
			setSelectedItem(items[0]);
		}

		public SortedComboBoxModel(Vector items) {
			Collections.sort(items);
			int size = items.size();
			for (int i = 0; i < size; i++) {
				super.addElement(items.elementAt(i));
			}
			setSelectedItem(items.elementAt(0));
		}

		@Override
		public void addElement(Object element) {
			insertElementAt(element, 0);
		}

		@Override
		public void insertElementAt(Object element, int index) {
			int size = getSize();
			for (index = 0; index < size; index++) {
				Comparable c = (Comparable) getElementAt(index);
				if (c.compareTo(element) > 0) {
					break;
				}
			}
			super.insertElementAt(element, index);
		}
	}// class SortedComboBoxModel

	class IdenticAdapter implements ActionListener, MouseListener, FocusListener, ListSelectionListener {

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			// Menus
			case MNU_FILE_EXIT:
				doFileExit();
				break;
			case MNU_REPORTS_LOG_FILES:
				break;
			case MNU_REPORTS_XML_DOC:
				break;
			case MNU_HELP_ABOUT:
				break;

			// Buttons

			case BTN_FIND_DUPS:
				// break;
			case BTN_FIND_DUPS_BY_NAME:
				// break;
			case BTN_DISPLAY_RESULTS:
				// break;
			case BTN_COPY_MOVE_REMOVE:
				// break;
			case BTN_FILE_TYPES:
				doSideMenu((JButton) actionEvent.getSource());
				break;

			case BTN_FILE_LIST_LOAD:
				loadTargetEdit();
				break;

			case BTN_FILE_LIST_NEW:
				loadNewTargetEdit();
				break;
			case BTN_FILE_LIST_SAVE:
				doSaveList();
				break;
			case BTN_FILE_LIST_DELETE:
				doDeleteList();
				break;
			case BTN_ADD_REMOVE:
				doAddRemove();
				break;

			// Other
			case CBO_FILE_TYPES:
				loadTargetList();
				break;

			// case LIST_FILE_TYPES:
			//
			// break;

			default:

			}// switch

		}// actionPerformed

		@Override
		public void mouseClicked(MouseEvent mouseEvent) {
			// TODO Auto-generated method stub
			if (mouseEvent.getClickCount() > 1) {
				String name = ((Component) mouseEvent.getSource()).getName();
				if (name.equals(LIST_FILE_TYPES)) {
					loadTargetEdit();
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
			case TXT_EDIT_LIST_MEMBER:
				flag1 = true;
				doEditListMember();
				break;
			case TXT_EDIT_LIST:
				doNameChanged();
				break;
			}// switch

		}// focusLost

		private boolean flag1 = false; // control the echo of events

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!flag1) {
				txtEditListMember.setText(EMPTY_STRING);
				btnAddRemove.setText(EDIT_REMOVE);
			} // if flag
			flag1 = false;
		}// valueChanged

	}// class IdenticAdapter

	private static final String EDIT_ADD = "Add";
	private static final String EDIT_REMOVE = "Remove";
	private static final String EDIT_ADD_REMOVE = "Add/Remove";

	private static final String NEW_LIST = "<NEW>";
	private static final String EMPTY_STRING = "";
	private static final String LIST_SUFFIX = "typeList";
	private static final String LIST_SUFFIX_DOT = ".typeList";

	private static final String MNU_FILE_EXIT = "mnuFileExit";
	private static final String MNU_REPORTS_LOG_FILES = "mnuReportsLogFiles";
	private static final String MNU_REPORTS_XML_DOC = "mnuReportsXMLdoc";
	private static final String MNU_HELP_ABOUT = "mnuHelpAbout";

	private static final String BTN_FIND_DUPS = "btnFindDuplicates";
	private static final String BTN_FIND_DUPS_BY_NAME = "btnFindDuplicatesByName";
	private static final String BTN_DISPLAY_RESULTS = "btnDisplayResults";
	private static final String BTN_COPY_MOVE_REMOVE = "btnCopyMoveRemove";
	private static final String BTN_FILE_TYPES = "btnFileTypes";

	private static final String BTN_FILE_LIST_LOAD = "btnFileListLoad";
	private static final String BTN_FILE_LIST_NEW = "btnFileListNew";
	private static final String BTN_FILE_LIST_SAVE = "btnFileListSave";
	private static final String BTN_FILE_LIST_SAVE_AS = "btnFileListSaveAs";
	private static final String BTN_FILE_LIST_DELETE = "btnFileListDelete";
	private static final String BTN_ADD_REMOVE = "btnAddRemove";

	private static final String CBO_FILE_TYPES = "cboFileTypes";
	private static final String LIST_FILE_TYPES = "listFileTypes";
	private static final String LIST_ACTIVE_LIST = "listActiveList";
	private static final String TXT_EDIT_LIST = "txtEditList";
	private static final String TXT_EDIT_LIST_MEMBER = "txtEditListMember";
	private static final String PNL_FIND_DUPS = "pnlFindDuplicates";
	private static final String PNL_FIND_DUPS_BY_NAME = "pnlFindDuplicatesByName";
	private static final String PNL_DISPLAY_RESULTS = "pnlDisplayResults";
	private static final String PNL_COPY_MOVE_REMOVE = "pnlCopyMoveRemove";
	private static final String PNL_FILE_TYPES = "pnlFileTypes";

	private JFrame frmIdentic;
	private JSplitPane splitPane1;

	private JButton btnFindDuplicates;
	private JButton btnFindDuplicatesByName;
	private JButton btnDisplayResults;
	private JButton btnCopyMoveRemove;
	private JButton btnFileTypes;

	private JPanel panelSideMenu;
	private JPanel panelDetails;
	private JPanel panelFindDuplicates;
	private JPanel panelFindDuplicatesByName;
	private JPanel panelDisplayResults;
	private JPanel panelCopyMoveRemove;
	private JPanel panelFileTypes;
	private JLabel lblStatus;
	private JPanel panelMain;
	private JComboBox cboTypeListsOld;
	private JComboBox cboTypeLists;
	private JList listFileTypes;
	private JTextField txtEditList;
	private JTextField txtEditListMember;
	private JList listActiveList;
	private JLabel lblActiveList;
	private JLabel lblActiveListCount;
	private JLabel lblHotList;
	private JButton btnFileListNew;
	private JButton btnFileListSave;
	private JButton btnFileListSaveAs;
	private JButton btnFileListDelete;
	private JButton btnAddRemove;
	private JButton btnFileListLoad;

}// class GUItemplate