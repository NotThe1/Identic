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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;



public class Identic {

	private IdenticAdapter identicAdapter = new IdenticAdapter();
	private JButton[] sideMenuButtons;
	private String[] sideMenuPanelNames; // SortedComboBoxModel
	private DefaultComboBoxModel<String> typeListModel = new DefaultComboBoxModel<>();
	private DefaultListModel<String> targetModel = new DefaultListModel<>();
	
	private DefaultListModel<String> excludeModel = new DefaultListModel<>();
	private ArrayList<String> targetSuffixes = new ArrayList<>();
	
	private LinkedBlockingQueue<FileStatSubject> qSubjects = new LinkedBlockingQueue<FileStatSubject>();
	
	private LinkedBlockingQueue<FileStatReject> qRejects = new LinkedBlockingQueue<FileStatReject>();
	private RejectTableModel rejectTableModel  = new RejectTableModel();
	private JTable rejectTable = new JTable(rejectTableModel);
	
	private LinkedBlockingQueue<FileStatSubject> qHashes = new LinkedBlockingQueue<FileStatSubject>();
	private SubjectTableModel subjectTableModel = new SubjectTableModel();
	private JTable subjectTable = new JTable(subjectTableModel);
	private HashMap<String,Integer> hashCounts = new HashMap<String,Integer>();;
	
	private HashMap<String, Integer> hashIDs = new HashMap<String, Integer>();
	
	private ButtonGroup bgShowResults = new ButtonGroup();; 

	
	private Path startPath;
	private int fileCount;
	private int folderCount;
	private int subjectCount;
	private int rejectCount;
	

	
	private  AppLogger appLogger= AppLogger.getInstance();

	private String fileListDirectory;
	private int sideButtonIndex;

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
		// ---------------Find Duplicates--------------------------------

	private void doSourceDirectory() {
		JFileChooser fc = new JFileChooser(lblSourceFolder.getText());
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		fc.setDialogTitle("Select Starting Directory");
		fc.setApproveButtonText("Select");

		if (fc.showOpenDialog(frmIdentic) == JFileChooser.APPROVE_OPTION) {
			lblSourceFolder.setText(fc.getSelectedFile().getAbsolutePath());
		} // if
	}// doSourceDirectory

	private void doManageTypeList() {
		ManageLists ml = new ManageLists();
		ml.showDialog();
		ml = null;
	}// doManageTypeList

	private void doStart() {
		startPath = Paths.get(lblSourceFolder.getText());
		if (!Files.exists(startPath)) {
			JOptionPane.showConfirmDialog(frmIdentic, "Starting Folder NOT Valid!", "Find Duplicates - Start",
					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		} // if
		qSubjects.clear();
		qRejects.clear();
		
		fileCount = 0;
		folderCount = 0;
		subjectCount = 0;
		rejectCount = 0;
		excludeModel.clear();
		hashCounts.clear();
		subjectTableModel.clear();
		rejectTableModel.clear();
		lblFilesNotProcessed.setText(String.format("%,d",0));

		
		appLogger.addTimeStamp("Start :");
		appLogger.addInfo(lblSourceFolder.getText());
		
//--------------------------
		IdentifySubjects identifySubjects = new IdentifySubjects();
		Thread threadIdentify = new Thread(identifySubjects);
		threadIdentify.start();
		
		MakeFileKey  makeFileKey= new MakeFileKey(threadIdentify);
		Thread threadMakeFileKey = new Thread(makeFileKey);
		threadMakeFileKey.start();
		

		ShowRejects showRejects =new ShowRejects(threadIdentify);
		Thread threadRejects = new Thread(showRejects);
		if(cbSaveExcludedFiles.isSelected()){
			threadRejects.start();
		}//if
		
		IdentifyDuplicates identifyDuplicates = new IdentifyDuplicates(threadMakeFileKey);
		Thread threadIdentifyDuplicates = new Thread(identifyDuplicates);
		threadIdentifyDuplicates.start();
		
		try{
			threadIdentify.join();
			threadMakeFileKey.join();
			threadRejects.join();
			threadIdentifyDuplicates.join();
		}catch (InterruptedException e){
			e.printStackTrace();
		}//try
		appLogger.addTimeStamp("End :");
//		if(cbSaveExcludedFiles.isSelected()){
//			scrollPaneResults.setViewportView(rejectTable);
//		}//if

		Set<String> keys = hashCounts.keySet();
		appLogger.addNL();
		for (String key : keys) {
			appLogger.addInfo(String.format("%s - %,d occurances", key, hashCounts.get(key)));
		} // for
		markTheDuplicates();
//		scrollPaneResults.setViewportView(subjectTable);
	}// doStart
	
	// ---------------Find Duplicates--------------------------
	
	private void markTheDuplicates(){
		int rows = subjectTableModel.getRowCount();
		String hashKey;
		for (int row = 0 ; row < rows;row++){
			hashKey = (String) subjectTableModel.getValueAt(row, 4);
			try{
			if (hashCounts.get(hashKey) >1){
				subjectTableModel.setValueAt(true, row, 5);
			}// set dup flag if > 1
				
			}catch (Exception e){
				System.out.println("HashKey = " + hashKey);
			}
			
		}//for
	}//markTheDuplicates
	// ---------------FileTypes--------------------------------
	

	private void loadTargetList() {
		if (cboTypeLists.getSelectedIndex() == -1) {// Nothing selected
			return;
		}
		String listName = (String) cboTypeLists.getSelectedItem();
		lblActiveListFind.setText(listName);
		String listFile = fileListDirectory + listName + LIST_SUFFIX_DOT;
		lblStatus.setText(listFile);

		Path pathTypeList	 = Paths.get(listFile);
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

	private void doSideMenu(JButton button) {
		String targetPanelName = null;
		panelSideMenu.removeAll();
		for (int i = 0; i < sideMenuButtons.length; i++) {
			panelSideMenu.add(sideMenuButtons[i]);
			if (sideMenuButtons[i] == button) {
				panelSideMenu.add(panelDetails);
				targetPanelName = sideMenuPanelNames[i];
				sideButtonIndex = i;
			} // if
		} // for
		CardLayout cl = (CardLayout) (panelDetails.getLayout());
		cl.show(panelDetails, targetPanelName);

		cl = (CardLayout) (panelMain.getLayout());
		cl.show(panelMain, targetPanelName);

		panelSideMenu.validate();
	}// doSideMenu
	
	private void doClearLog(){
		try {
			txtLog.getDocument().remove(0, txtLog.getDocument().getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//try
	}//doClearLog
	
	private void doShowResults(){
		switch(bgShowResults.getSelection().getActionCommand()){
		case RB_ALL_THE_FILES:
			if (subjectTableModel.getRowCount()>0){
				tableResults.setModel(subjectTableModel);
			}//
			break;
		case RB_DUPLICATE_FILES:
			if (false){
				tableResults.setModel(subjectTableModel);
			}//
			break;
		case RB_UNIQUE_FILES:
			if (false){
				tableResults.setModel(subjectTableModel);
			}//
			break;
		case RB_FILES_NOT_PROCESSED:
			tableResults.setModel(rejectTableModel);
			break;
		}//switch
	}//doShowResults
	
	private void doFileExit() {
		appClose();
		System.exit(0);
	}// doFileExit
	
	private void doSaveExcludedFiles(){
		rbFilesNotProcessed.setEnabled(cbSaveExcludedFiles.isSelected());
	}//doSaveExcludedFiles

	class ListFilter implements FilenameFilter {
		@Override
		public boolean accept(File arg0, String arg1) {
			if (arg1.endsWith(LIST_SUFFIX)) {
				return true;
			} // if
			return false;
		}// accept
	}// ListFilter

	private void initPanels() {
		doSideMenu(sideMenuButtons[sideButtonIndex]);
	}// initPanels

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
			// ArrayList<Path> sources = new ArrayList<>();
			Path newDir = Paths.get(fileListDirectory);
			Path source = null;
			for (int i = 0; i < initalListFiles.length; i++) {
				try {
					// sources.add(Paths.get(this.getClass().getResource(initalListFiles[i] ).toURI()));
					source = Paths.get(this.getClass().getResource(initalListFiles[i]).toURI());
					Files.move(source, newDir.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException | URISyntaxException e) {
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
		Preferences myPrefs = Preferences.userNodeForPackage(Identic.class).node(this.getClass().getSimpleName());
		cboTypeLists.setSelectedItem(myPrefs.get("ActiveList", "Pictures"));
		myPrefs = null;
	}// initFileTypes

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
		myPrefs.putInt("SideButtonIndex", sideButtonIndex);
		myPrefs.put("ActiveList", (String) cboTypeLists.getSelectedItem());
		myPrefs.put("SourceDirectory", lblSourceFolder.getText());
		myPrefs = null;
	}// appClose

	private void appInit() {

		Preferences myPrefs = Preferences.userNodeForPackage(Identic.class).node(this.getClass().getSimpleName());
		frmIdentic.setSize(886, 779);
		frmIdentic.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));
		splitPane1.setDividerLocation(174);

		fileListDirectory = myPrefs.get("ListDirectory", EMPTY_STRING);
		// cboTypeLists.setSelectedItem(myPrefs.get("ActiveList", "Pictures"));
		sideButtonIndex = myPrefs.getInt("SideButtonIndex", 0);
		lblSourceFolder.setText(myPrefs.get("SourceDirectory", NOT_SET));
		
		myPrefs = null;

		// These two arrays are synchronized to control the button positions and the selection of the correct panels.
		sideMenuButtons = new JButton[] { btnFindDuplicates, btnFindDuplicatesByName, btnDisplayResults,
				btnCopyMoveRemove, btnApplicationLog };
		sideMenuPanelNames = new String[] { panelFindDuplicates.getName(), panelFindDuplicatesByName.getName(),
				panelDisplayResults.getName(), panelCopyMoveRemove.getName(), paneApplicationlLog.getName() };
		
		cboTypeLists.setModel(typeListModel);

		listFindDuplicatesActive.setModel(targetModel);
		listExcluded.setModel(excludeModel);
		txtLog.setText(EMPTY_STRING);
		appLogger.setDoc(txtLog.getStyledDocument());
		cbSaveExcludedFiles.setSelected(false);
		
		bgShowResults.add(rbAllTheFiles);
		bgShowResults.add(rbDuplicateFiles);
		bgShowResults.add(rbUniqueFiles);
		bgShowResults.add(rbFilesNotProcessed);
		bgShowResults.clearSelection();
	}// appInit

	public Identic() {
		initialize();
		appInit();
		initFileTypes();
		initPanels();
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

		btnApplicationLog = new JButton("Application Log");
		btnApplicationLog.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnApplicationLog.addActionListener(identicAdapter);
		btnApplicationLog.setName(BTN_APPLICATION_LOG);
		btnApplicationLog.setMaximumSize(new Dimension(1000, 23));
		panelSideMenu.add(btnApplicationLog);

		panelDetails = new JPanel();
		panelSideMenu.add(panelDetails);
		panelDetails.setLayout(new CardLayout(0, 0));

		panelFindDuplicates = new JPanel();
		panelFindDuplicates.setName(PNL_FIND_DUPS);
		panelDetails.add(panelFindDuplicates, PNL_FIND_DUPS);
		GridBagLayout gbl_panelFindDuplicates = new GridBagLayout();
		gbl_panelFindDuplicates.columnWidths = new int[] { 150, 0 };
		gbl_panelFindDuplicates.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 50, 0, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelFindDuplicates.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelFindDuplicates.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 1.0, Double.MIN_VALUE };
		panelFindDuplicates.setLayout(gbl_panelFindDuplicates);

		Component verticalStrut = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut.gridx = 0;
		gbc_verticalStrut.gridy = 0;
		panelFindDuplicates.add(verticalStrut, gbc_verticalStrut);
		
				JButton btnStart = new JButton("Start");
				btnStart.addActionListener(identicAdapter);
				btnStart.setName(BTN_START);
				GridBagConstraints gbc_btnStart = new GridBagConstraints();
				gbc_btnStart.insets = new Insets(0, 0, 5, 0);
				gbc_btnStart.gridx = 0;
				gbc_btnStart.gridy = 2;
				panelFindDuplicates.add(btnStart, gbc_btnStart);

		Component verticalStrut_8 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_8 = new GridBagConstraints();
		gbc_verticalStrut_8.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_8.gridx = 0;
		gbc_verticalStrut_8.gridy = 5;
		panelFindDuplicates.add(verticalStrut_8, gbc_verticalStrut_8);

		JPanel panelFD1 = new JPanel();
		panelFD1.setPreferredSize(new Dimension(150, 40));
		panelFD1.setMinimumSize(new Dimension(150, 150));
		panelFD1.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		panelFD1.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true),
				"File Count", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), null));
		panelFD1.setMaximumSize(new Dimension(150, 100));
		GridBagConstraints gbc_panelFD1 = new GridBagConstraints();
		gbc_panelFD1.insets = new Insets(0, 0, 5, 0);
		gbc_panelFD1.gridx = 0;
		gbc_panelFD1.gridy = 6;
		panelFindDuplicates.add(panelFD1, gbc_panelFD1);
		GridBagLayout gbl_panelFD1 = new GridBagLayout();
		gbl_panelFD1.columnWidths = new int[] { 113, 0 };
		gbl_panelFD1.rowHeights = new int[] { 10, 0 };
		gbl_panelFD1.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelFD1.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelFD1.setLayout(gbl_panelFD1);

		lblFileCount = new JLabel("0");
		lblFileCount.setHorizontalAlignment(SwingConstants.CENTER);
		lblFileCount.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblFileCount = new GridBagConstraints();
		gbc_lblFileCount.gridx = 0;
		gbc_lblFileCount.gridy = 0;
		panelFD1.add(lblFileCount, gbc_lblFileCount);

		Component verticalStrut_14 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_14 = new GridBagConstraints();
		gbc_verticalStrut_14.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_14.gridx = 0;
		gbc_verticalStrut_14.gridy = 7;
		panelFindDuplicates.add(verticalStrut_14, gbc_verticalStrut_14);

		JPanel panelFD2 = new JPanel();
		panelFD2.setPreferredSize(new Dimension(150, 40));
		panelFD2.setMinimumSize(new Dimension(150, 40));
		panelFD2.setMaximumSize(new Dimension(150, 40));
		panelFD2.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true),
				"Folder Count", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), null));
		panelFD2.setAlignmentY(1.0f);
		GridBagConstraints gbc_panelFD2 = new GridBagConstraints();
		gbc_panelFD2.anchor = GridBagConstraints.NORTH;
		gbc_panelFD2.insets = new Insets(0, 0, 5, 0);
		gbc_panelFD2.gridx = 0;
		gbc_panelFD2.gridy = 8;
		panelFindDuplicates.add(panelFD2, gbc_panelFD2);
		GridBagLayout gbl_panelFD2 = new GridBagLayout();
		gbl_panelFD2.columnWidths = new int[] { 113, 0 };
		gbl_panelFD2.rowHeights = new int[] { 10, 0 };
		gbl_panelFD2.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelFD2.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelFD2.setLayout(gbl_panelFD2);

		lblFolderCount = new JLabel("0");
		lblFolderCount.setHorizontalAlignment(SwingConstants.CENTER);
		lblFolderCount.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblFolderCount = new GridBagConstraints();
		gbc_lblFolderCount.gridx = 0;
		gbc_lblFolderCount.gridy = 0;
		panelFD2.add(lblFolderCount, gbc_lblFolderCount);

		Component verticalStrut_15 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_15 = new GridBagConstraints();
		gbc_verticalStrut_15.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_15.gridx = 0;
		gbc_verticalStrut_15.gridy = 9;
		panelFindDuplicates.add(verticalStrut_15, gbc_verticalStrut_15);
		
				JButton btnSourceFolder = new JButton("Source Folder");
				btnSourceFolder.addActionListener(identicAdapter);
				btnSourceFolder.setName(BTN_SOURCE_FOLDER);
				GridBagConstraints gbc_btnSourceFolder = new GridBagConstraints();
				gbc_btnSourceFolder.fill = GridBagConstraints.VERTICAL;
				gbc_btnSourceFolder.insets = new Insets(0, 0, 5, 0);
				gbc_btnSourceFolder.gridx = 0;
				gbc_btnSourceFolder.gridy = 10;
				panelFindDuplicates.add(btnSourceFolder, gbc_btnSourceFolder);

		Component verticalStrut_18 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_18 = new GridBagConstraints();
		gbc_verticalStrut_18.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_18.gridx = 0;
		gbc_verticalStrut_18.gridy = 11;
		panelFindDuplicates.add(verticalStrut_18, gbc_verticalStrut_18);

		Component verticalStrut_19 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_19 = new GridBagConstraints();
		gbc_verticalStrut_19.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_19.gridx = 0;
		gbc_verticalStrut_19.gridy = 12;
		panelFindDuplicates.add(verticalStrut_19, gbc_verticalStrut_19);

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Active List",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 0);
		gbc_panel_3.anchor = GridBagConstraints.NORTH;
		gbc_panel_3.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 13;
		panelFindDuplicates.add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[] { 0, 0 };
		gbl_panel_3.rowHeights = new int[] { 0, 0 };
		gbl_panel_3.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_3.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_3.setLayout(gbl_panel_3);

		cboTypeLists = new JComboBox<String>();
		cboTypeLists.addActionListener(identicAdapter);
		cboTypeLists.setName(CBO_TYPES_LIST);
		GridBagConstraints gbc_cboTypeLists = new GridBagConstraints();
		gbc_cboTypeLists.anchor = GridBagConstraints.NORTH;
		gbc_cboTypeLists.fill = GridBagConstraints.HORIZONTAL;
		gbc_cboTypeLists.gridx = 0;
		gbc_cboTypeLists.gridy = 0;
		panel_3.add(cboTypeLists, gbc_cboTypeLists);

		Component verticalStrut_20 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_20 = new GridBagConstraints();
		gbc_verticalStrut_20.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_20.gridx = 0;
		gbc_verticalStrut_20.gridy = 14;
		panelFindDuplicates.add(verticalStrut_20, gbc_verticalStrut_20);

		JButton btnManageTypeList = new JButton("Manage Type Lists");
		btnManageTypeList.addActionListener(identicAdapter);
		btnManageTypeList.setName(BTN_MANAGE_TYPE_LIST);
		GridBagConstraints gbc_btnManageTypeList = new GridBagConstraints();
		gbc_btnManageTypeList.insets = new Insets(0, 0, 5, 0);
		gbc_btnManageTypeList.gridx = 0;
		gbc_btnManageTypeList.gridy = 15;
		panelFindDuplicates.add(btnManageTypeList, gbc_btnManageTypeList);
		
		cbSaveExcludedFiles = new JCheckBox("Save Excluded Files");
		cbSaveExcludedFiles.addActionListener(identicAdapter);
		cbSaveExcludedFiles.setName(CB_SAVE_EXCLUDED_FILES);
		GridBagConstraints gbc_cbSaveExcludedFiles = new GridBagConstraints();
		gbc_cbSaveExcludedFiles.anchor = GridBagConstraints.SOUTH;
		gbc_cbSaveExcludedFiles.gridx = 0;
		gbc_cbSaveExcludedFiles.gridy = 16;
		panelFindDuplicates.add(cbSaveExcludedFiles, gbc_cbSaveExcludedFiles);

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
		gbl_panelDisplayResults.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panelDisplayResults.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelDisplayResults.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelDisplayResults.setLayout(gbl_panelDisplayResults);
		
		Component verticalStrut_2 = Box.createVerticalStrut(20);
		verticalStrut_2.setPreferredSize(new Dimension(0, 50));
		GridBagConstraints gbc_verticalStrut_2 = new GridBagConstraints();
		gbc_verticalStrut_2.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_2.gridx = 0;
		gbc_verticalStrut_2.gridy = 1;
		panelDisplayResults.add(verticalStrut_2, gbc_verticalStrut_2);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Show Results", TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, null));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		panelDisplayResults.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		Component verticalStrut_3 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_3 = new GridBagConstraints();
		gbc_verticalStrut_3.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_3.gridx = 0;
		gbc_verticalStrut_3.gridy = 0;
		panel.add(verticalStrut_3, gbc_verticalStrut_3);
		
		rbAllTheFiles = new JRadioButton("All The Files");
		rbAllTheFiles.addActionListener(identicAdapter);
		rbAllTheFiles.setName(RB_ALL_THE_FILES);
		rbAllTheFiles.setActionCommand(RB_ALL_THE_FILES);
		GridBagConstraints gbc_rbAllTheFiles = new GridBagConstraints();
		gbc_rbAllTheFiles.fill = GridBagConstraints.HORIZONTAL;
		gbc_rbAllTheFiles.insets = new Insets(0, 0, 5, 0);
		gbc_rbAllTheFiles.anchor = GridBagConstraints.ABOVE_BASELINE;
		gbc_rbAllTheFiles.gridx = 0;
		gbc_rbAllTheFiles.gridy = 1;
		panel.add(rbAllTheFiles, gbc_rbAllTheFiles);
		
		Component verticalStrut_4 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_4 = new GridBagConstraints();
		gbc_verticalStrut_4.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_4.gridx = 0;
		gbc_verticalStrut_4.gridy = 2;
		panel.add(verticalStrut_4, gbc_verticalStrut_4);
		
		rbDuplicateFiles = new JRadioButton("Duplicate Files");
		rbDuplicateFiles.addActionListener(identicAdapter);
		rbDuplicateFiles.setName(RB_DUPLICATE_FILES);
		rbDuplicateFiles.setActionCommand(RB_DUPLICATE_FILES);
		GridBagConstraints gbc_rbDuplicateFiles = new GridBagConstraints();
		gbc_rbDuplicateFiles.fill = GridBagConstraints.HORIZONTAL;
		gbc_rbDuplicateFiles.insets = new Insets(0, 0, 5, 0);
		gbc_rbDuplicateFiles.gridx = 0;
		gbc_rbDuplicateFiles.gridy = 3;
		panel.add(rbDuplicateFiles, gbc_rbDuplicateFiles);
		
		Component verticalStrut_5 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_5 = new GridBagConstraints();
		gbc_verticalStrut_5.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_5.gridx = 0;
		gbc_verticalStrut_5.gridy = 4;
		panel.add(verticalStrut_5, gbc_verticalStrut_5);
		
		rbUniqueFiles = new JRadioButton("Unique Files");
		rbUniqueFiles.addActionListener(identicAdapter);
		rbUniqueFiles.setName(RB_UNIQUE_FILES);
		rbUniqueFiles.setActionCommand(RB_UNIQUE_FILES);
		GridBagConstraints gbc_rbUniqueFiles = new GridBagConstraints();
		gbc_rbUniqueFiles.fill = GridBagConstraints.HORIZONTAL;
		gbc_rbUniqueFiles.insets = new Insets(0, 0, 5, 0);
		gbc_rbUniqueFiles.gridx = 0;
		gbc_rbUniqueFiles.gridy = 5;
		panel.add(rbUniqueFiles, gbc_rbUniqueFiles);
		
		Component verticalStrut_6 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_6 = new GridBagConstraints();
		gbc_verticalStrut_6.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_6.gridx = 0;
		gbc_verticalStrut_6.gridy = 6;
		panel.add(verticalStrut_6, gbc_verticalStrut_6);
		
		rbFilesNotProcessed = new JRadioButton("Files Not Processed");
		rbFilesNotProcessed.addActionListener(identicAdapter);
		rbFilesNotProcessed.setName(RB_FILES_NOT_PROCESSED);
		rbFilesNotProcessed.setActionCommand(RB_FILES_NOT_PROCESSED);
		rbFilesNotProcessed.setEnabled(false);
		GridBagConstraints gbc_rbFilesNotProcessed = new GridBagConstraints();
		gbc_rbFilesNotProcessed.insets = new Insets(0, 0, 5, 0);
		gbc_rbFilesNotProcessed.fill = GridBagConstraints.HORIZONTAL;
		gbc_rbFilesNotProcessed.gridx = 0;
		gbc_rbFilesNotProcessed.gridy = 7;
		panel.add(rbFilesNotProcessed, gbc_rbFilesNotProcessed);
		
		Component verticalStrut_7 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_7 = new GridBagConstraints();
		gbc_verticalStrut_7.gridx = 0;
		gbc_verticalStrut_7.gridy = 8;
		panel.add(verticalStrut_7, gbc_verticalStrut_7);

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

		paneApplicationlLog = new JPanel();
		paneApplicationlLog.setName(PNL_APPLICATION_LOG);
		panelDetails.add(paneApplicationlLog, PNL_APPLICATION_LOG);
		paneApplicationlLog.setLayout(new BoxLayout(paneApplicationlLog, BoxLayout.Y_AXIS));

		JLabel lblNewLabel = new JLabel("Application Log");
		lblNewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		paneApplicationlLog.add(lblNewLabel);
		
		Component verticalStrut_1 = Box.createVerticalStrut(20);
		paneApplicationlLog.add(verticalStrut_1);
		
		JButton btnClearLog = new JButton("Clear Log");
		btnClearLog.addActionListener(identicAdapter);
		btnClearLog.setName(BTN_CLEAR_LOG);
		paneApplicationlLog.add(btnClearLog);

		panelMain = new JPanel();
		splitPane1.setRightComponent(panelMain);
		panelMain.setLayout(new CardLayout(0, 0));

		JPanel panelMainFIndDuplicates = new JPanel();
		panelMain.add(panelMainFIndDuplicates, PNL_FIND_DUPS);
		GridBagLayout gbl_panelMainFIndDuplicates = new GridBagLayout();
		gbl_panelMainFIndDuplicates.columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panelMainFIndDuplicates.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panelMainFIndDuplicates.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelMainFIndDuplicates.rowWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panelMainFIndDuplicates.setLayout(gbl_panelMainFIndDuplicates);

		JLabel lblNewLabel_1 = new JLabel("Source Folder:   ");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 0;
		panelMainFIndDuplicates.add(lblNewLabel_1, gbc_lblNewLabel_1);

		lblSourceFolder = new JLabel("<Not Set>");
		lblSourceFolder.setForeground(new Color(128, 0, 0));
		lblSourceFolder.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblSourceFolder = new GridBagConstraints();
		gbc_lblSourceFolder.anchor = GridBagConstraints.WEST;
		gbc_lblSourceFolder.insets = new Insets(0, 0, 5, 5);
		gbc_lblSourceFolder.gridx = 1;
		gbc_lblSourceFolder.gridy = 0;
		panelMainFIndDuplicates.add(lblSourceFolder, gbc_lblSourceFolder);

		panelActiveList = new JPanel();
		panelActiveList.setAlignmentX(Component.LEFT_ALIGNMENT);
		panelActiveList.setMinimumSize(new Dimension(150, 0));
		panelActiveList.setPreferredSize(new Dimension(165, 0));
		panelActiveList.setMaximumSize(new Dimension(165, 0));
		panelActiveList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelActiveList = new GridBagConstraints();
		gbc_panelActiveList.insets = new Insets(0, 0, 0, 5);
		gbc_panelActiveList.fill = GridBagConstraints.BOTH;
		gbc_panelActiveList.gridx = 0;
		gbc_panelActiveList.gridy = 2;
		panelMainFIndDuplicates.add(panelActiveList, gbc_panelActiveList);
		GridBagLayout gbl_panelActiveList = new GridBagLayout();
		gbl_panelActiveList.columnWidths = new int[] { 0, 0 };
		gbl_panelActiveList.rowHeights = new int[] { 0, 0 };
		gbl_panelActiveList.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelActiveList.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelActiveList.setLayout(gbl_panelActiveList);

		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane_2.setPreferredSize(new Dimension(160, 0));
		scrollPane_2.setMinimumSize(new Dimension(160, 0));
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridx = 0;
		gbc_scrollPane_2.gridy = 0;
		panelActiveList.add(scrollPane_2, gbc_scrollPane_2);

		listFindDuplicatesActive = new JList<String>();
		listFindDuplicatesActive.setEnabled(false);
		listFindDuplicatesActive.setMinimumSize(new Dimension(145, 0));
		listFindDuplicatesActive.setPreferredSize(new Dimension(145, 500));
		scrollPane_2.setViewportView(listFindDuplicatesActive);

		lblActiveListFind = new JLabel("New label");
		lblActiveListFind.setForeground(new Color(0, 128, 128));
		lblActiveListFind.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblActiveListFind.setHorizontalAlignment(SwingConstants.CENTER);
		scrollPane_2.setColumnHeaderView(lblActiveListFind);

		JPanel panelSummary = new JPanel();
		panelSummary.setMinimumSize(new Dimension(255, 0));
		panelSummary.setPreferredSize(new Dimension(255, 0));
		panelSummary.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		GridBagConstraints gbc_panelSummary = new GridBagConstraints();
		gbc_panelSummary.insets = new Insets(0, 0, 0, 5);
		gbc_panelSummary.fill = GridBagConstraints.BOTH;
		gbc_panelSummary.gridx = 1;
		gbc_panelSummary.gridy = 2;
		panelMainFIndDuplicates.add(panelSummary, gbc_panelSummary);
		GridBagLayout gbl_panelSummary = new GridBagLayout();
		gbl_panelSummary.columnWidths = new int[] { 0, 0 };
		gbl_panelSummary.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelSummary.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelSummary.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		panelSummary.setLayout(gbl_panelSummary);
		
		lblComplete = new JLabel("New label");
		GridBagConstraints gbc_lblComplete = new GridBagConstraints();
		gbc_lblComplete.insets = new Insets(0, 0, 5, 0);
		gbc_lblComplete.gridx = 0;
		gbc_lblComplete.gridy = 1;
		panelSummary.add(lblComplete, gbc_lblComplete);
		
		Component verticalStrut_9 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_9 = new GridBagConstraints();
		gbc_verticalStrut_9.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_9.gridx = 0;
		gbc_verticalStrut_9.gridy = 2;
		panelSummary.add(verticalStrut_9, gbc_verticalStrut_9);
		
		JPanel panelSummary1 = new JPanel();
		panelSummary1.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panelSummary1 = new GridBagConstraints();
		gbc_panelSummary1.insets = new Insets(0, 0, 5, 0);
		gbc_panelSummary1.fill = GridBagConstraints.BOTH;
		gbc_panelSummary1.gridx = 0;
		gbc_panelSummary1.gridy = 3;
		panelSummary.add(panelSummary1, gbc_panelSummary1);
		GridBagLayout gbl_panelSummary1 = new GridBagLayout();
		gbl_panelSummary1.columnWidths = new int[]{0, 0};
		gbl_panelSummary1.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_panelSummary1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelSummary1.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panelSummary1.setLayout(gbl_panelSummary1);
		
		Component verticalStrut_11 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_11 = new GridBagConstraints();
		gbc_verticalStrut_11.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_11.gridx = 0;
		gbc_verticalStrut_11.gridy = 0;
		panelSummary1.add(verticalStrut_11, gbc_verticalStrut_11);
		
		JPanel panelPanelSummary1A = new JPanel();
		GridBagConstraints gbc_panelPanelSummary1A = new GridBagConstraints();
		gbc_panelPanelSummary1A.anchor = GridBagConstraints.NORTH;
		gbc_panelPanelSummary1A.insets = new Insets(0, 0, 5, 0);
		gbc_panelPanelSummary1A.gridx = 0;
		gbc_panelPanelSummary1A.gridy = 1;
		panelSummary1.add(panelPanelSummary1A, gbc_panelPanelSummary1A);
		GridBagLayout gbl_panelPanelSummary1A = new GridBagLayout();
		gbl_panelPanelSummary1A.columnWidths = new int[]{0, 0, 0};
		gbl_panelPanelSummary1A.rowHeights = new int[]{0, 0};
		gbl_panelPanelSummary1A.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panelPanelSummary1A.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelPanelSummary1A.setLayout(gbl_panelPanelSummary1A);
		
		txtTotalFilesProcessed = new JTextField();
		txtTotalFilesProcessed.setMaximumSize(new Dimension(80, 20));
		txtTotalFilesProcessed.setPreferredSize(new Dimension(80, 20));
		txtTotalFilesProcessed.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_txtTotalFilesProcessed = new GridBagConstraints();
		gbc_txtTotalFilesProcessed.insets = new Insets(0, 0, 0, 5);
		gbc_txtTotalFilesProcessed.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTotalFilesProcessed.gridx = 0;
		gbc_txtTotalFilesProcessed.gridy = 0;
		panelPanelSummary1A.add(txtTotalFilesProcessed, gbc_txtTotalFilesProcessed);
		txtTotalFilesProcessed.setColumns(10);
		
		JLabel lblNewLabel_3 = new JLabel("Total Files Processed");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.gridx = 1;
		gbc_lblNewLabel_3.gridy = 0;
		panelPanelSummary1A.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		Component verticalStrut_12 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_12 = new GridBagConstraints();
		gbc_verticalStrut_12.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_12.gridx = 0;
		gbc_verticalStrut_12.gridy = 2;
		panelSummary1.add(verticalStrut_12, gbc_verticalStrut_12);
		
		JPanel panelUniqueFiles = new JPanel();
		panelUniqueFiles.setBorder(new TitledBorder(null, "Unique Files", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panelUniqueFiles = new GridBagConstraints();
		gbc_panelUniqueFiles.insets = new Insets(0, 0, 5, 0);
		gbc_panelUniqueFiles.fill = GridBagConstraints.BOTH;
		gbc_panelUniqueFiles.gridx = 0;
		gbc_panelUniqueFiles.gridy = 3;
		panelSummary1.add(panelUniqueFiles, gbc_panelUniqueFiles);
		GridBagLayout gbl_panelUniqueFiles = new GridBagLayout();
		gbl_panelUniqueFiles.columnWidths = new int[]{0, 0, 0};
		gbl_panelUniqueFiles.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_panelUniqueFiles.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panelUniqueFiles.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panelUniqueFiles.setLayout(gbl_panelUniqueFiles);
		
		Component verticalStrut_13 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_13 = new GridBagConstraints();
		gbc_verticalStrut_13.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_13.gridx = 0;
		gbc_verticalStrut_13.gridy = 0;
		panelUniqueFiles.add(verticalStrut_13, gbc_verticalStrut_13);
		
		txtFilesWithNoDuplicates = new JTextField();
		txtFilesWithNoDuplicates.setMaximumSize(new Dimension(80, 20));
		txtFilesWithNoDuplicates.setPreferredSize(new Dimension(80, 20));
		txtFilesWithNoDuplicates.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_txtFilesWithNoDuplicates = new GridBagConstraints();
		gbc_txtFilesWithNoDuplicates.anchor = GridBagConstraints.EAST;
		gbc_txtFilesWithNoDuplicates.insets = new Insets(0, 0, 5, 5);
		gbc_txtFilesWithNoDuplicates.gridx = 0;
		gbc_txtFilesWithNoDuplicates.gridy = 1;
		panelUniqueFiles.add(txtFilesWithNoDuplicates, gbc_txtFilesWithNoDuplicates);
		txtFilesWithNoDuplicates.setColumns(10);
		
		JLabel lblNewLabel_4 = new JLabel("Files with No Duplicates");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_4.gridx = 1;
		gbc_lblNewLabel_4.gridy = 1;
		panelUniqueFiles.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		Component verticalStrut_16 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_16 = new GridBagConstraints();
		gbc_verticalStrut_16.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_16.gridx = 0;
		gbc_verticalStrut_16.gridy = 2;
		panelUniqueFiles.add(verticalStrut_16, gbc_verticalStrut_16);
		
		txtUniqueFilesWithDuplicates = new JTextField();
		txtUniqueFilesWithDuplicates.setMaximumSize(new Dimension(80, 20));
		txtUniqueFilesWithDuplicates.setPreferredSize(new Dimension(80, 20));
		txtUniqueFilesWithDuplicates.setMinimumSize(new Dimension(80, 20));
		txtUniqueFilesWithDuplicates.setColumns(10);
		GridBagConstraints gbc_txtUniqueFilesWithDuplicates = new GridBagConstraints();
		gbc_txtUniqueFilesWithDuplicates.anchor = GridBagConstraints.EAST;
		gbc_txtUniqueFilesWithDuplicates.insets = new Insets(0, 0, 5, 5);
		gbc_txtUniqueFilesWithDuplicates.gridx = 0;
		gbc_txtUniqueFilesWithDuplicates.gridy = 3;
		panelUniqueFiles.add(txtUniqueFilesWithDuplicates, gbc_txtUniqueFilesWithDuplicates);
		
		JLabel lblUniqueFilesWith = new JLabel("Unique Files with Duplicates");
		GridBagConstraints gbc_lblUniqueFilesWith = new GridBagConstraints();
		gbc_lblUniqueFilesWith.anchor = GridBagConstraints.WEST;
		gbc_lblUniqueFilesWith.insets = new Insets(0, 0, 5, 0);
		gbc_lblUniqueFilesWith.gridx = 1;
		gbc_lblUniqueFilesWith.gridy = 3;
		panelUniqueFiles.add(lblUniqueFilesWith, gbc_lblUniqueFilesWith);
		
		Component verticalStrut_17 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_17 = new GridBagConstraints();
		gbc_verticalStrut_17.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_17.gridx = 0;
		gbc_verticalStrut_17.gridy = 4;
		panelUniqueFiles.add(verticalStrut_17, gbc_verticalStrut_17);
		
		txtTotalNumberOfUniqueFiles = new JTextField();
		txtTotalNumberOfUniqueFiles.setMaximumSize(new Dimension(80, 20));
		txtTotalNumberOfUniqueFiles.setPreferredSize(new Dimension(80, 20));
		txtTotalNumberOfUniqueFiles.setMinimumSize(new Dimension(80, 20));
		txtTotalNumberOfUniqueFiles.setColumns(10);
		GridBagConstraints gbc_txtTotalNumberOfUniqueFiles = new GridBagConstraints();
		gbc_txtTotalNumberOfUniqueFiles.anchor = GridBagConstraints.EAST;
		gbc_txtTotalNumberOfUniqueFiles.insets = new Insets(0, 0, 0, 5);
		gbc_txtTotalNumberOfUniqueFiles.gridx = 0;
		gbc_txtTotalNumberOfUniqueFiles.gridy = 5;
		panelUniqueFiles.add(txtTotalNumberOfUniqueFiles, gbc_txtTotalNumberOfUniqueFiles);
		
		JLabel lblTotalNumberOf = new JLabel("Total Number of Unique Files");
		GridBagConstraints gbc_lblTotalNumberOf = new GridBagConstraints();
		gbc_lblTotalNumberOf.anchor = GridBagConstraints.WEST;
		gbc_lblTotalNumberOf.gridx = 1;
		gbc_lblTotalNumberOf.gridy = 5;
		panelUniqueFiles.add(lblTotalNumberOf, gbc_lblTotalNumberOf);
		
		Component verticalStrut_24 = Box.createVerticalStrut(20);
		verticalStrut_24.setMinimumSize(new Dimension(0, 80));
		GridBagConstraints gbc_verticalStrut_24 = new GridBagConstraints();
		gbc_verticalStrut_24.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_24.gridx = 0;
		gbc_verticalStrut_24.gridy = 4;
		panelSummary1.add(verticalStrut_24, gbc_verticalStrut_24);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Duplicate Files", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 5;
		panelSummary1.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_panel_2.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
		
		Component verticalStrut_21 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_21 = new GridBagConstraints();
		gbc_verticalStrut_21.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_21.gridx = 0;
		gbc_verticalStrut_21.gridy = 0;
		panel_2.add(verticalStrut_21, gbc_verticalStrut_21);
		
		txtFilesWithDuplicates = new JTextField();
		txtFilesWithDuplicates.setMaximumSize(new Dimension(80, 20));
		txtFilesWithDuplicates.setPreferredSize(new Dimension(80, 20));
		txtFilesWithDuplicates.setMinimumSize(new Dimension(80, 20));
		txtFilesWithDuplicates.setColumns(10);
		GridBagConstraints gbc_txtFilesWithDuplicates = new GridBagConstraints();
		gbc_txtFilesWithDuplicates.anchor = GridBagConstraints.EAST;
		gbc_txtFilesWithDuplicates.insets = new Insets(0, 0, 5, 5);
		gbc_txtFilesWithDuplicates.gridx = 0;
		gbc_txtFilesWithDuplicates.gridy = 1;
		panel_2.add(txtFilesWithDuplicates, gbc_txtFilesWithDuplicates);
		
		JLabel lblFilesWithDuplicates = new JLabel("Files with Duplicates");
		GridBagConstraints gbc_lblFilesWithDuplicates = new GridBagConstraints();
		gbc_lblFilesWithDuplicates.anchor = GridBagConstraints.WEST;
		gbc_lblFilesWithDuplicates.insets = new Insets(0, 0, 5, 0);
		gbc_lblFilesWithDuplicates.gridx = 1;
		gbc_lblFilesWithDuplicates.gridy = 1;
		panel_2.add(lblFilesWithDuplicates, gbc_lblFilesWithDuplicates);
		
		Component verticalStrut_22 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_22 = new GridBagConstraints();
		gbc_verticalStrut_22.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_22.gridx = 0;
		gbc_verticalStrut_22.gridy = 2;
		panel_2.add(verticalStrut_22, gbc_verticalStrut_22);
		
		txtRedundantFiles = new JTextField();
		txtRedundantFiles.setMaximumSize(new Dimension(80, 20));
		txtRedundantFiles.setPreferredSize(new Dimension(80, 20));
		txtRedundantFiles.setMinimumSize(new Dimension(80, 20));
		txtRedundantFiles.setColumns(10);
		GridBagConstraints gbc_txtRedundantFiles = new GridBagConstraints();
		gbc_txtRedundantFiles.anchor = GridBagConstraints.EAST;
		gbc_txtRedundantFiles.insets = new Insets(0, 0, 5, 5);
		gbc_txtRedundantFiles.gridx = 0;
		gbc_txtRedundantFiles.gridy = 3;
		panel_2.add(txtRedundantFiles, gbc_txtRedundantFiles);
		
		JLabel lblRedundantFiles = new JLabel("Redundant Files");
		GridBagConstraints gbc_lblRedundantFiles = new GridBagConstraints();
		gbc_lblRedundantFiles.anchor = GridBagConstraints.WEST;
		gbc_lblRedundantFiles.insets = new Insets(0, 0, 5, 0);
		gbc_lblRedundantFiles.gridx = 1;
		gbc_lblRedundantFiles.gridy = 3;
		panel_2.add(lblRedundantFiles, gbc_lblRedundantFiles);
		
		Component verticalStrut_23 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_23 = new GridBagConstraints();
		gbc_verticalStrut_23.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_23.gridx = 0;
		gbc_verticalStrut_23.gridy = 4;
		panel_2.add(verticalStrut_23, gbc_verticalStrut_23);
		
		txtExcessStorage = new JTextField();
		txtExcessStorage.setMaximumSize(new Dimension(80, 20));
		txtExcessStorage.setPreferredSize(new Dimension(80, 20));
		txtExcessStorage.setMinimumSize(new Dimension(80, 20));
		txtExcessStorage.setColumns(10);
		GridBagConstraints gbc_txtExcessStorage = new GridBagConstraints();
		gbc_txtExcessStorage.anchor = GridBagConstraints.EAST;
		gbc_txtExcessStorage.insets = new Insets(0, 0, 0, 5);
		gbc_txtExcessStorage.gridx = 0;
		gbc_txtExcessStorage.gridy = 5;
		panel_2.add(txtExcessStorage, gbc_txtExcessStorage);
		
		JLabel lblTotalExcessStorage = new JLabel("Total Excess Storage");
		GridBagConstraints gbc_lblTotalExcessStorage = new GridBagConstraints();
		gbc_lblTotalExcessStorage.anchor = GridBagConstraints.WEST;
		gbc_lblTotalExcessStorage.gridx = 1;
		gbc_lblTotalExcessStorage.gridy = 5;
		panel_2.add(lblTotalExcessStorage, gbc_lblTotalExcessStorage);
		
		Component verticalStrut_10 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_10 = new GridBagConstraints();
		gbc_verticalStrut_10.gridx = 0;
		gbc_verticalStrut_10.gridy = 5;
		panelSummary.add(verticalStrut_10, gbc_verticalStrut_10);

		JPanel panelExclusions = new JPanel();
		panelExclusions.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Excluded File Types", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panelExclusions = new GridBagConstraints();
		gbc_panelExclusions.insets = new Insets(0, 0, 0, 5);
		gbc_panelExclusions.fill = GridBagConstraints.VERTICAL;
		gbc_panelExclusions.gridx = 2;
		gbc_panelExclusions.gridy = 2;
		panelMainFIndDuplicates.add(panelExclusions, gbc_panelExclusions);
		GridBagLayout gbl_panelExclusions = new GridBagLayout();
		gbl_panelExclusions.columnWidths = new int[] { 0, 0 };
		gbl_panelExclusions.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_panelExclusions.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelExclusions.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		panelExclusions.setLayout(gbl_panelExclusions);

		JScrollPane scrollPane_3 = new JScrollPane();
		scrollPane_3.setPreferredSize(new Dimension(150, 0));
		scrollPane_3.setMinimumSize(new Dimension(150, 23));
		GridBagConstraints gbc_scrollPane_3 = new GridBagConstraints();
		gbc_scrollPane_3.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_3.fill = GridBagConstraints.VERTICAL;
		gbc_scrollPane_3.gridx = 0;
		gbc_scrollPane_3.gridy = 2;
		panelExclusions.add(scrollPane_3, gbc_scrollPane_3);

		listExcluded = new JList<String>();
		scrollPane_3.setViewportView(listExcluded);

		JLabel label = new JLabel("Found & Excluded");
		label.setFont(new Font("Tahoma", Font.BOLD, 13));
		label.setForeground(new Color(0, 128, 128));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		scrollPane_3.setColumnHeaderView(label);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Files Not Processed",
				TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 3;
		panelExclusions.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		lblFilesNotProcessed = new JLabel("0");
		GridBagConstraints gbc_lblFilesNotProcessed = new GridBagConstraints();
		gbc_lblFilesNotProcessed.gridx = 0;
		gbc_lblFilesNotProcessed.gridy = 0;
		panel_1.add(lblFilesNotProcessed, gbc_lblFilesNotProcessed);

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
		gbl_panelMainDisplayResults.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelMainDisplayResults.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelMainDisplayResults.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panelMainDisplayResults.setLayout(gbl_panelMainDisplayResults);

		lblDisplayResults = new JLabel("Display Results");
		GridBagConstraints gbc_lblDisplayResults = new GridBagConstraints();
		gbc_lblDisplayResults.insets = new Insets(0, 0, 5, 0);
		gbc_lblDisplayResults.gridx = 0;
		gbc_lblDisplayResults.gridy = 0;
		panelMainDisplayResults.add(lblDisplayResults, gbc_lblDisplayResults);
		
		scrollPaneResults = new JScrollPane();
		GridBagConstraints gbc_scrollPaneResults = new GridBagConstraints();
		gbc_scrollPaneResults.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneResults.gridx = 0;
		gbc_scrollPaneResults.gridy = 1;
		panelMainDisplayResults.add(scrollPaneResults, gbc_scrollPaneResults);
		
		tableResults = new JTable();
		scrollPaneResults.setViewportView(tableResults);

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

		JPanel panelMainApplicationLog = new JPanel();
		panelMainApplicationLog.setPreferredSize(new Dimension(0, 0));
		panelMainApplicationLog.setMaximumSize(new Dimension(0, 0));
		panelMainApplicationLog.setMinimumSize(new Dimension(0, 0));
		panelMain.add(panelMainApplicationLog, PNL_APPLICATION_LOG);
		GridBagLayout gbl_panelMainApplicationLog = new GridBagLayout();
		gbl_panelMainApplicationLog.columnWidths = new int[] { 0, 0 };
		gbl_panelMainApplicationLog.rowHeights = new int[] { 0, 0 };
		gbl_panelMainApplicationLog.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelMainApplicationLog.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelMainApplicationLog.setLayout(gbl_panelMainApplicationLog);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panelMainApplicationLog.add(scrollPane, gbc_scrollPane);
		
		txtLog = new JTextPane();
		txtLog.setFont(new Font("Courier New", Font.PLAIN, 15));
		scrollPane.setViewportView(txtLog);
		
		JLabel lblNewLabel_2 = new JLabel("Application Log");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setForeground(new Color(139, 69, 19));
		lblNewLabel_2.setFont(new Font("Courier New", Font.BOLD, 16));
		scrollPane.setColumnHeaderView(lblNewLabel_2);

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

	class IdenticAdapter implements ActionListener {

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

			// Buttons---------------------------------------------
			// Side Menu Buttons
			case BTN_FIND_DUPS:
				// break;
			case BTN_FIND_DUPS_BY_NAME:
				// break;
			case BTN_DISPLAY_RESULTS:
				// break;
			case BTN_COPY_MOVE_REMOVE:
				// break;
			case BTN_APPLICATION_LOG:
				doSideMenu((JButton) actionEvent.getSource());
				break;

			// Find Duplicate Buttons
			case BTN_SOURCE_FOLDER:
				doSourceDirectory();
				break;
			case BTN_START:
				doStart();
				break;
			case BTN_MANAGE_TYPE_LIST:
				doManageTypeList();
				break;

				// Find Show Results
				case RB_ALL_THE_FILES:
				case RB_DUPLICATE_FILES:
				case RB_UNIQUE_FILES:
				case RB_FILES_NOT_PROCESSED:
					doShowResults();
					break;
					

			// Other
			case BTN_CLEAR_LOG:
				doClearLog();
				break;
				
			case CBO_TYPES_LIST:
				loadTargetList();
				break;

			case CB_SAVE_EXCLUDED_FILES:
				doSaveExcludedFiles();
				break;

			default:

			}// switch 

		}// actionPerformed

	}// class IdenticAdapter

	// private static final String NEW_LIST = "<NEW>";
	private static final String NOT_SET = "<Not Set>";
	private static final String EMPTY_STRING = "";
	private static final String LIST_SUFFIX = "typeList";
	private static final String LIST_SUFFIX_DOT = ".typeList";

	private static final String MNU_FILE_EXIT = "mnuFileExit";
	private static final String MNU_REPORTS_LOG_FILES = "mnuReportsLogFiles";
	private static final String MNU_REPORTS_XML_DOC = "mnuReportsXMLdoc";
	private static final String MNU_HELP_ABOUT = "mnuHelpAbout";
	// Side Menu Buttons
	private static final String BTN_FIND_DUPS = "btnFindDuplicates";
	private static final String BTN_FIND_DUPS_BY_NAME = "btnFindDuplicatesByName";
	private static final String BTN_DISPLAY_RESULTS = "btnDisplayResults";
	private static final String BTN_COPY_MOVE_REMOVE = "btnCopyMoveRemove";
	private static final String BTN_APPLICATION_LOG = "btnApplicationLog";
	// Side Find Duplicates Buttons
	private static final String BTN_SOURCE_FOLDER = "btnSourceFolder";
	private static final String BTN_START = "btnStart";
	private static final String BTN_MANAGE_TYPE_LIST = "btnManageTypeList";
	// ApplicationLogButtons
	private static final String BTN_CLEAR_LOG = "btnClearLog";
	private static final String CB_SAVE_EXCLUDED_FILES = "cbSaveExcludedFiles";
	// Radio Buttons
	private static final String RB_ALL_THE_FILES = "rbAllTheFiles";
	private static final String RB_DUPLICATE_FILES = "rbDuplicateFiles";
	private static final String RB_UNIQUE_FILES = "rbUniqueFiles";
	private static final String RB_FILES_NOT_PROCESSED = "rbFilesNotProcessed";


	private static final String CBO_TYPES_LIST = "cboTypeLists";
	private static final String PNL_FIND_DUPS = "pnlFindDuplicates";
	private static final String PNL_FIND_DUPS_BY_NAME = "pnlFindDuplicatesByName";
	private static final String PNL_DISPLAY_RESULTS = "pnlDisplayResults";
	private static final String PNL_COPY_MOVE_REMOVE = "pnlCopyMoveRemove";
	private static final String PNL_APPLICATION_LOG = "pnlApplicationLog";

	private JFrame frmIdentic;
	private JSplitPane splitPane1;

	private JButton btnFindDuplicates;
	private JButton btnFindDuplicatesByName;
	private JButton btnDisplayResults;
	private JButton btnCopyMoveRemove;
	private JButton btnApplicationLog;

	private JPanel panelSideMenu;
	private JPanel panelDetails;
	private JPanel panelFindDuplicates;
	private JPanel panelFindDuplicatesByName;
	private JPanel panelDisplayResults;
	private JPanel panelCopyMoveRemove;
	private JPanel paneApplicationlLog;
	private JLabel lblStatus;
	private JPanel panelMain;

	private JLabel lblFileCount;
	private JLabel lblFolderCount;
	private JLabel lblSourceFolder;
	private JPanel panelActiveList;
	private JLabel lblActiveListFind;
	private JList<String> listFindDuplicatesActive;
	private JList<String> listExcluded;
	private JComboBox<String> cboTypeLists;
	private JTextPane txtLog;
	private JScrollPane scrollPaneResults;
	private JCheckBox cbSaveExcludedFiles;
	private JRadioButton rbAllTheFiles;
	private JRadioButton rbDuplicateFiles;
	private JRadioButton rbFilesNotProcessed;
	private JRadioButton rbUniqueFiles;
	private JLabel lblFilesNotProcessed;
	private JLabel lblComplete;
	private JTextField txtTotalFilesProcessed;
	private JTextField txtFilesWithNoDuplicates;
	private JTextField txtUniqueFilesWithDuplicates;
	private JTextField txtTotalNumberOfUniqueFiles;
	private JTextField txtFilesWithDuplicates;
	private JTextField txtRedundantFiles;
	private JTextField txtExcessStorage;
	private JLabel lblDisplayResults;
	private JTable tableResults;
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	public class IdentifySubjects implements Runnable {
//		private AppLogger appLogger = AppLogger.getInstance();

		private HashMap<String, Integer> members = new HashMap<>();

		public IdentifySubjects(){	
		}//Constructor
		
		@Override
		public void run() {
			members.clear();
			MyWalker myWalker = new MyWalker();
			try {
				Files.walkFileTree(startPath, myWalker);
			} catch (IOException e) {
				e.printStackTrace();
			} // try
			logSummary();
		}// run

		private void logSummary() {
			appLogger.addNL(2);
			appLogger.addSpecial("folderCount = " + folderCount);
			appLogger.addSpecial("fileCount  = " + fileCount);
			appLogger.addSpecial("subjectCount = " + subjectCount);
			appLogger.addSpecial("rejectCount  = " + rejectCount);
			appLogger.addNL();
			appLogger.addInfo(String.format("%,d File Types excluded", members.size()));
			Set<String> keys = members.keySet();
			appLogger.addNL();
			for (String key : keys) {
				appLogger.addInfo(String.format("%s - %,d occurances", key, members.get(key)));
			} // for
		}// logSummary

		class MyWalker implements FileVisitor<Path> {

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}// FileVisitResult

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				folderCount++;
				return FileVisitResult.CONTINUE;
			}// FileVisitResult

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				FileTime lastModifieTime;
				long fileSize;
				fileCount++;
				String fileName = file.getFileName().toString();
				lastModifieTime = Files.getLastModifiedTime(file);
				fileSize = Files.size(file);
				int partsCount;
				String part = null;
				String[] parts = fileName.split("\\.");
				partsCount = parts.length;
				if (partsCount > 1) {
					part = parts[partsCount - 1].toUpperCase();
					if (targetSuffixes.contains(part)) {
						subjectCount++;
						qSubjects.add(new FileStatSubject(file, fileSize, lastModifieTime));
					} else {
						rejectCount++;
						lblFilesNotProcessed.setText(String.format("%,d",rejectCount));
						keepSuffixCount(part);
						qRejects.add(new FileStatReject(file, fileSize, lastModifieTime, FileStat.NOT_ON_LIST));
					} // if
				} // if - only process files with suffixes
				return FileVisitResult.CONTINUE;
			}// FileVisitResult

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				qRejects.add(new FileStatReject(file, 0, null, FileStat.IO_EXCEPTION));
				return FileVisitResult.CONTINUE;
			}// FileVisitResult

			private void keepSuffixCount(String suffix) {

				Integer occurances = members.get(suffix);
				if (occurances == null) {
					members.put(suffix, 1);
					 excludeModel.addElement(suffix);
				} else {
					members.put(suffix, occurances + 1);
				} // if unique
			}// keepSuffixCount
		}// class MyWalker
	}// class IdentifySubjects
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	public class ShowRejects implements Runnable {

		private Thread priorThread;
//		private AppLogger appLogger = AppLogger.getInstance();

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
						appLogger.addSpecial(fileName);
						return;
					} // if - done ?
				} // try
			} // while
		}// run

	}// class ShowSubjects
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	public class MakeFileKey implements Runnable {
		private static final int bufSize = 1024;
		String algorithm = "SHA-1";		//160 bits
//		String algorithm = "MD5";		// 128 bits
//		String algorithm = "SHA-256";	// 256 bits
		private Thread priorThread;
//		private AppLogger appLogger = AppLogger.getInstance();

		public MakeFileKey(Thread priorThread) {
			this.priorThread = priorThread;
		}// Constructor

		@Override
		public void run() {
			FileStatSubject fileStatSubject;
			
			int count = 0;
			String fileName = null;
			String key = null;
			while (true) {
				try {
					fileStatSubject = qSubjects.remove();
					fileName = fileStatSubject.getFileName();			
					count++;
					try {
						key = hashFile(fileStatSubject.getFilePathString(), algorithm);
						fileStatSubject.setHashKey(key);
						qHashes.add(fileStatSubject);
						appLogger.addInfo(key + " - " + fileName);
					} catch (HashGenerationException e) {
						appLogger.addError("HashGenerationError",fileName);
						e.printStackTrace();
					}//
				} catch (NoSuchElementException ex) {
					if (priorThread.getState().equals(Thread.State.TERMINATED)) {
						appLogger.addSpecial("From MakeFileKey count = " + count);
						return;
					} // if - done ?
				} // try
			} // while
		}// run

		private  String hashFile(String file, String algorithm) throws HashGenerationException {
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

		private  String convertByteArrayToHexString(byte[] arrayBytes) {
			StringBuffer stringBuffer = new StringBuffer();
			for (int i = 0; i < arrayBytes.length; i++) {
				stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16).substring(1));
			} // for
			return stringBuffer.toString();
		}// convertByteArrayToHexString

	}// class ShowSubjects
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	public class IdentifyDuplicates implements Runnable {
		private Thread priorThread;
		private Integer fileID;

		public IdentifyDuplicates( Thread priorThread) {
			this.priorThread = priorThread;
		}// Constructor

		@Override
		public void run() {
			fileID = 0;
			hashIDs.clear();
			hashCounts.clear();
			FileStatSubject subject;
			while (true) {
				try {
					subject = qHashes.remove();
					subjectTableModel.addRow(subject,keepHashKeyCount(subject.getHashKey()));
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

	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

}// class GUItemplate