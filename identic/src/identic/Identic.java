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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class Identic {

	private IdenticAdapter identicAdapter = new IdenticAdapter();
	private JButton[] sideMenuButtons;
	private JPanel[] sideMenuPanels;
	private String[] sideMenuPanelNames;
	private DefaultComboBoxModel typeListModel = new DefaultComboBoxModel();

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
		
		//set up cbo model
		
		typeListModel.removeAllElements();
		
		for (File f:files){
			typeListModel.addElement(f.getName().replace(LIST_SUFFIX_DOT, EMPTY_STRING));
		}//for
		
		cboTypeLists.setModel(typeListModel);
		
		Preferences myPrefs = Preferences.userNodeForPackage(Identic.class).node(this.getClass().getSimpleName());
		cboTypeLists.setSelectedItem(myPrefs.get("ActiveList","Pictures"));
		myPrefs = null;

		
//		cboTypeLists.
		

		// lblStatus.setText(url.getPath());
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
		myPrefs.put("ActiveList",(String) cboTypeLists.getSelectedItem());
		myPrefs = null;
	}// appClose

	private void appInit() {

		Preferences myPrefs = Preferences.userNodeForPackage(Identic.class).node(this.getClass().getSimpleName());
		frmIdentic.setSize(myPrefs.getInt("Width", 761), myPrefs.getInt("Height", 693));
		frmIdentic.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));
		splitPane1.setDividerLocation(myPrefs.getInt("Divider", 250));

		fileListDirectory = myPrefs.get("ListDirectory", EMPTY_STRING);
		cboTypeLists.setSelectedItem(myPrefs.get("ActiveList","Pictures"));
		myPrefs = null;

		// These two arrays are synchronized to control the button positions and the selection of the correct panels.
		sideMenuButtons = new JButton[] { btnFindDuplicates, btnFindDuplicatesByName, btnDisplayResults,
				btnCopyMoveRemove, btnFileTypes };
		sideMenuPanelNames = new String[] { panelFindDuplicates.getName(), panelFindDuplicatesByName.getName(),
				panelDisplayResults.getName(), panelCopyMoveRemove.getName(), panelFileTypes.getName() };

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

		Component verticalStrut = Box.createVerticalStrut(20);
		panelFindDuplicates.add(verticalStrut);

		JPanel panel1 = new JPanel();
		panel1.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		panel1.setBorder(new CompoundBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Active List",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)), null));
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

		Component verticalStrut_1 = Box.createVerticalStrut(20);
		panelFindDuplicates.add(verticalStrut_1);

		cboTypeLists = new JComboBox();
		cboTypeLists.addActionListener(identicAdapter);
		cboTypeLists.setName(CBO_FILE_TYPES);
		cboTypeLists.setMaximumSize(new Dimension(32767, 30));
//		cboTypeLists.setModel(new DefaultComboBoxModel(new String[] { "panelFindDuplicates.getName()",
//				"panelFindDuplicatesByName.getName()", "panelDisplayResults.getName()", "panelCopyMoveRemove.getName()",
//				"panelFileTypespanelFindDuplicates.getName()", "panelFindDuplicatesByName.getName()",
//				"panelDisplayResults.getName()", "panelCopyMoveRemove.getName()",
//				"panelFileTypespanelFindDuplicates.getName()", "panelFindDuplicatesByName.getName()",
//				"panelDisplayResults.getName()", "panelCopyMoveRemove.getName()", "panelFileTypes" }));
		panelFindDuplicates.add(cboTypeLists
				);

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
		panelDetails.add(panelFileTypes, PNL_FILE_TYPES);// "name_670049977688605"
		GridBagLayout gbl_panelFileTypes = new GridBagLayout();
		gbl_panelFileTypes.columnWidths = new int[] { 0, 0 };
		gbl_panelFileTypes.rowHeights = new int[] { 0, 0 };
		gbl_panelFileTypes.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelFileTypes.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelFileTypes.setLayout(gbl_panelFileTypes);

		JLabel lblNewLabel = new JLabel("File Types");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panelFileTypes.add(lblNewLabel, gbc_lblNewLabel);

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
		gbl_panelMainFileTypes.columnWidths = new int[] { 0, 0 };
		gbl_panelMainFileTypes.rowHeights = new int[] { 0, 0 };
		gbl_panelMainFileTypes.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelMainFileTypes.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelMainFileTypes.setLayout(gbl_panelMainFileTypes);

		JLabel lblNewLabel_1 = new JLabel("File Types");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 0;
		panelMainFileTypes.add(lblNewLabel_1, gbc_lblNewLabel_1);
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
				
				//Other
			case CBO_FILE_TYPES:
				System.out.println("CBO_FILE_TYPES");
				break;

			default:

			}// switch

		}// actionPerformed

	}// class IdenticAdapter

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
	
	private static final String CBO_FILE_TYPES = "cboFileTypes";

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
	private JComboBox cboTypeLists;

}// class GUItemplate