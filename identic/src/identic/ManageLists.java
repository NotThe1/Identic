package identic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ManageLists extends JDialog {
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();

	private String fileListDirectory;

	private DefaultListModel<String> availableListsModel = new DefaultListModel<>();
	private DefaultListModel<String> editListModel = new DefaultListModel<>();

	private ManageListsAdapter mla = new ManageListsAdapter();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ManageLists dialog = new ManageLists();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		} // try
	}// main

	public int showDialog() {
		// dialogResultValue = JOptionPane.CANCEL_OPTION;
		this.setLocationRelativeTo(this.getOwner());

		this.setVisible(true);
		this.dispose();
		return JOptionPane.OK_OPTION;
	}// showDialog
		// ----------------------------------------------------------

	private void loadTargetEdit() {
		if (listAvailable.isSelectionEmpty()) {
			listAvailable.setSelectedIndex(0);
		} // ensure a selection
		String editName = (String) listAvailable.getSelectedValue();
		lblEdit.setText(editName);
		txtActive.setText(editName);
		txtEdit.setText(EMPTY_STRING);
		// lblStatus.setText((String) listFileTypes.getSelectedValue());

		String editFile = fileListDirectory + editName + LIST_SUFFIX_DOT;
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

		manageEditButtons("Load");
		// validate();
	}// loadTargetEdit

	private void loadNewTargetEdit() {
		String editName = NEW_LIST;
		txtActive.setText(editName);
		lblEdit.setText(editName);
		txtEdit.setText(EMPTY_STRING);
		editListModel.removeAllElements();
		manageEditButtons("New");
	}// loadNewTargetEdit

	private void doNameChanged() {
		String newName = txtActive.getText().trim();
		if (!lblEdit.getText().equals(newName)) {
			lblEdit.setText(newName);
			manageEditButtons("NameChanged");
		} // if
		listEdit.clearSelection();
	}// doNameChanged

	private void doSaveList() {
		String listFile = fileListDirectory + txtActive.getText().toUpperCase() + LIST_SUFFIX_DOT;
		Path listPath = Paths.get(listFile);
		if (Files.exists(listPath)) {

			int ans = JOptionPane.showConfirmDialog(this, "List Exits, Do you want to overwrite?",
					"Save File Suffix List", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (ans == JOptionPane.NO_OPTION) {
				return;
			} // inner if
		} // if file exists

		try {
			Files.deleteIfExists(listPath);
			Files.createFile(listPath);
		} catch (IOException e) {
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
		String listFile = fileListDirectory + txtActive.getText().toUpperCase() + LIST_SUFFIX_DOT;
		Path listPath = Paths.get(listFile);
		if (Files.exists(listPath)) {
			int ans = JOptionPane.showConfirmDialog(this, "Do you want to delete the list?", "Delete Suffix List",
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
		loadNewTargetEdit();
	}// doDeleteList

	private void doEditListMember() {
		if (!txtEdit.getText().equals(EMPTY_STRING)) {
			listEdit.clearSelection();
			btnAddRemove.setText(EDIT_ADD);
		} else {
			btnAddRemove.setText(EDIT_ADD_REMOVE);
		} // if
	}// doEditListMember

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

	private void initFileTypes() {

		// see if the directory has been set up already
		if (fileListDirectory.equals(EMPTY_STRING)) {
			fileListDirectory = System.getProperty("java.io.tmpdir");
			fileListDirectory = fileListDirectory.replace("Temp", "Identic");
		} // if no established fileListDirectory

		File targetDirectory = new File(fileListDirectory);

		// Path p = Paths.get(fileListDirectory);
		if (!targetDirectory.exists()) {
			JOptionPane.showMessageDialog(this, "Initializing File Type lists in " + fileListDirectory,
					"Initialization", JOptionPane.INFORMATION_MESSAGE);
			System.err.println("Making new directory");

			if (!targetDirectory.mkdirs()) {
				System.out.printf("[ManageLists.initFileTypes]Did not create the directory %n");
			} // if

		} // if exits

		// we have the directory, do we have lists ?

		File[] files = targetDirectory.listFiles(new ListFilter(LIST_SUFFIX_DOT));

		// if files empty - initialize the directory

		if (files == null || files.length == 0) {

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
				} catch (URISyntaxException | IOException e) {
					e.printStackTrace();
				} // try
			} // for
			files = targetDirectory.listFiles(new ListFilter(LIST_SUFFIX_DOT));
		} // if no type list files in target directory

		// set up cbo model

		availableListsModel.removeAllElements();

		// if (files.length > 0) {
		if (files != null) {
			for (File f : files) {
				availableListsModel.addElement(f.getName().replace(LIST_SUFFIX_DOT, EMPTY_STRING));
			} // for
		} // if
	}// initFileTypes

	private void appClose() {

		Preferences myPrefs = Preferences.userNodeForPackage(ManageLists.class).node(this.getClass().getSimpleName());
		Dimension dim = this.getSize();
		myPrefs.putInt("Height", dim.height);
		myPrefs.putInt("Width", dim.width);
		Point point = this.getLocation();
		myPrefs.putInt("LocX", point.x);
		myPrefs.putInt("LocY", point.y);

		// myPrefs.put("ListDirectory", fileListDirectory);
		myPrefs = null;
		dispose();
	}// appClose

	private void appInit() {
		Preferences myPrefs = Preferences.userNodeForPackage(ManageLists.class).node(this.getClass().getSimpleName());
		this.setSize(myPrefs.getInt("Width", 600), myPrefs.getInt("Height", 600));
		this.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));

		fileListDirectory = myPrefs.get("ListDirectory", EMPTY_STRING);
		myPrefs = null;
		listAvailable.setModel(availableListsModel);
		listEdit.setModel(editListModel);
		initFileTypes();

	}// appInit

	/**
	 * Create the dialog.
	 */
	public ManageLists() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose();
			}
		});
		initialize();
		appInit();
	}// Constructor

	private void initialize() {
		setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		setTitle("File Suffix Lists Manager");
		// setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);

		JPanel panelAvailable = new JPanel();
		panelAvailable.setPreferredSize(new Dimension(150, 0));
		panelAvailable.setMinimumSize(new Dimension(150, 0));
		panelAvailable.setMaximumSize(new Dimension(150, 0));
		panelAvailable.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelAvailable = new GridBagConstraints();
		gbc_panelAvailable.insets = new Insets(0, 0, 0, 5);
		gbc_panelAvailable.fill = GridBagConstraints.BOTH;
		gbc_panelAvailable.gridx = 0;
		gbc_panelAvailable.gridy = 0;
		contentPanel.add(panelAvailable, gbc_panelAvailable);
		GridBagLayout gbl_panelAvailable = new GridBagLayout();
		gbl_panelAvailable.columnWidths = new int[] { 0, 0 };
		gbl_panelAvailable.rowHeights = new int[] { 0, 0 };
		gbl_panelAvailable.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelAvailable.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelAvailable.setLayout(gbl_panelAvailable);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panelAvailable.add(scrollPane, gbc_scrollPane);

		listAvailable = new JList<String>();
		listAvailable.addMouseListener(mla);
		listAvailable.setName(LIST_AVAILABLE);
		scrollPane.setViewportView(listAvailable);

		JLabel lblNewLabel = new JLabel("Available Lists");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setForeground(new Color(139, 69, 19));
		lblNewLabel.setFont(new Font("Arial", Font.BOLD, 14));
		scrollPane.setColumnHeaderView(lblNewLabel);

		JPanel panelSelection = new JPanel();
		panelSelection.setPreferredSize(new Dimension(140, 0));
		panelSelection.setMinimumSize(new Dimension(140, 0));
		panelSelection.setMaximumSize(new Dimension(140, 0));
		GridBagConstraints gbc_panelSelection = new GridBagConstraints();
		gbc_panelSelection.insets = new Insets(0, 0, 0, 5);
		gbc_panelSelection.fill = GridBagConstraints.BOTH;
		gbc_panelSelection.gridx = 1;
		gbc_panelSelection.gridy = 0;
		contentPanel.add(panelSelection, gbc_panelSelection);
		GridBagLayout gbl_panelSelection = new GridBagLayout();
		gbl_panelSelection.columnWidths = new int[] { 0, 0 };
		gbl_panelSelection.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelSelection.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelSelection.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panelSelection.setLayout(gbl_panelSelection);

		Component verticalStrut = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut.gridx = 0;
		gbc_verticalStrut.gridy = 0;
		panelSelection.add(verticalStrut, gbc_verticalStrut);

		JPanel panelSelection1 = new JPanel();
		GridBagConstraints gbc_panelSelection1 = new GridBagConstraints();
		gbc_panelSelection1.fill = GridBagConstraints.BOTH;
		gbc_panelSelection1.gridx = 0;
		gbc_panelSelection1.gridy = 1;
		panelSelection.add(panelSelection1, gbc_panelSelection1);
		GridBagLayout gbl_panelSelection1 = new GridBagLayout();
		gbl_panelSelection1.columnWidths = new int[] { 0, 0 };
		gbl_panelSelection1.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelSelection1.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelSelection1.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		panelSelection1.setLayout(gbl_panelSelection1);

		txtActive = new JTextField();
		txtActive.setHorizontalAlignment(SwingConstants.CENTER);
		txtActive.addFocusListener(mla);
		txtActive.setName(TXT_ACTIVE);
		GridBagConstraints gbc_txtActive = new GridBagConstraints();
		gbc_txtActive.insets = new Insets(0, 0, 5, 0);
		gbc_txtActive.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtActive.gridx = 0;
		gbc_txtActive.gridy = 1;
		panelSelection1.add(txtActive, gbc_txtActive);
		txtActive.setColumns(10);

		Component verticalStrut_1 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_1 = new GridBagConstraints();
		gbc_verticalStrut_1.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_1.gridx = 0;
		gbc_verticalStrut_1.gridy = 2;
		panelSelection1.add(verticalStrut_1, gbc_verticalStrut_1);

		btnLoad = new JButton("Load");
		btnLoad.addActionListener(mla);
		btnLoad.setName(BTN_LOAD);
		btnLoad.setMaximumSize(new Dimension(63, 23));
		btnLoad.setMinimumSize(new Dimension(63, 23));
		btnLoad.setPreferredSize(new Dimension(63, 23));
		GridBagConstraints gbc_btnLoad = new GridBagConstraints();
		gbc_btnLoad.insets = new Insets(0, 0, 5, 0);
		gbc_btnLoad.gridx = 0;
		gbc_btnLoad.gridy = 3;
		panelSelection1.add(btnLoad, gbc_btnLoad);

		Component verticalStrut_2 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_2 = new GridBagConstraints();
		gbc_verticalStrut_2.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_2.gridx = 0;
		gbc_verticalStrut_2.gridy = 4;
		panelSelection1.add(verticalStrut_2, gbc_verticalStrut_2);

		btnNew = new JButton("New");
		btnNew.addActionListener(mla);
		btnNew.setName(BTN_NEW);
		btnNew.setMaximumSize(new Dimension(63, 23));
		btnNew.setMinimumSize(new Dimension(63, 23));
		btnNew.setPreferredSize(new Dimension(63, 23));
		GridBagConstraints gbc_btnNew = new GridBagConstraints();
		gbc_btnNew.insets = new Insets(0, 0, 5, 0);
		gbc_btnNew.gridx = 0;
		gbc_btnNew.gridy = 5;
		panelSelection1.add(btnNew, gbc_btnNew);

		Component verticalStrut_3 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_3 = new GridBagConstraints();
		gbc_verticalStrut_3.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_3.gridx = 0;
		gbc_verticalStrut_3.gridy = 6;
		panelSelection1.add(verticalStrut_3, gbc_verticalStrut_3);

		btnSave = new JButton("Save");
		btnSave.addActionListener(mla);
		btnSave.setName(BTN_SAVE);
		btnSave.setMaximumSize(new Dimension(63, 23));
		btnSave.setMinimumSize(new Dimension(63, 23));
		btnSave.setPreferredSize(new Dimension(63, 23));
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.insets = new Insets(0, 0, 5, 0);
		gbc_btnSave.gridx = 0;
		gbc_btnSave.gridy = 7;
		panelSelection1.add(btnSave, gbc_btnSave);

		Component verticalStrut_4 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_4 = new GridBagConstraints();
		gbc_verticalStrut_4.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_4.gridx = 0;
		gbc_verticalStrut_4.gridy = 8;
		panelSelection1.add(verticalStrut_4, gbc_verticalStrut_4);

		btnDelete = new JButton("Delete");
		btnDelete.addActionListener(mla);
		btnDelete.setName(BTN_DELETE);
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.gridx = 0;
		gbc_btnDelete.gridy = 9;
		panelSelection1.add(btnDelete, gbc_btnDelete);

		JPanel panelEdit = new JPanel();
		GridBagConstraints gbc_panelEdit = new GridBagConstraints();
		gbc_panelEdit.fill = GridBagConstraints.BOTH;
		gbc_panelEdit.gridx = 2;
		gbc_panelEdit.gridy = 0;
		contentPanel.add(panelEdit, gbc_panelEdit);
		GridBagLayout gbl_panelEdit = new GridBagLayout();
		gbl_panelEdit.columnWidths = new int[] { 0, 0 };
		gbl_panelEdit.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelEdit.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelEdit.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		panelEdit.setLayout(gbl_panelEdit);

		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 0;
		panelEdit.add(scrollPane_1, gbc_scrollPane_1);

		lblEdit = new JLabel("<none>");
		lblEdit.setHorizontalAlignment(SwingConstants.CENTER);
		lblEdit.setForeground(new Color(139, 69, 19));
		lblEdit.setFont(new Font("Arial", Font.BOLD, 14));
		scrollPane_1.setColumnHeaderView(lblEdit);

		listEdit = new JList<String>();
		listEdit.addListSelectionListener(mla);
		listEdit.setName(LIST_EDIT);
		scrollPane_1.setViewportView(listEdit);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Edit", TitledBorder.CENTER,
				TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		panelEdit.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		btnAddRemove = new JButton("Add/Remove");
		GridBagConstraints gbc_btnAddRemove = new GridBagConstraints();
		gbc_btnAddRemove.insets = new Insets(0, 0, 5, 0);
		gbc_btnAddRemove.gridx = 0;
		gbc_btnAddRemove.gridy = 0;
		panel.add(btnAddRemove, gbc_btnAddRemove);
		btnAddRemove.addActionListener(mla);
		btnAddRemove.setName(BTN_ADD_REMOVE);

		txtEdit = new JTextField();
		txtEdit.addFocusListener(mla);
		txtEdit.setName(TXT_EDIT);
		txtEdit.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_txtEdit = new GridBagConstraints();
		gbc_txtEdit.anchor = GridBagConstraints.NORTH;
		gbc_txtEdit.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEdit.gridx = 0;
		gbc_txtEdit.gridy = 1;
		panel.add(txtEdit, gbc_txtEdit);
		txtEdit.setColumns(10);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton btnOK = new JButton("OK");
		btnOK.addActionListener(mla);
		btnOK.setName(BTN_OK);
		buttonPane.add(btnOK);
		getRootPane().setDefaultButton(btnOK);

	}// initialize

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
	class ManageListsAdapter implements ActionListener, MouseListener, FocusListener, ListSelectionListener {

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			case BTN_LOAD:
				loadTargetEdit();
				break;
			case BTN_NEW:
				loadNewTargetEdit();
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
			case BTN_OK:
				appClose();
			default:

			}// switch

		}// actionPerformed

		@Override
		public void mouseClicked(MouseEvent mouseEvent) {
			if (mouseEvent.getClickCount() > 1) {
				String name = ((Component) mouseEvent.getSource()).getName();
				if (name.equals(LIST_AVAILABLE)) {
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
		// --------------------------------------------------------------------

	private static final String NEW_LIST = "<NEW>";
	// private static final String NOT_SET = "<Not Set>";
	private static final String EMPTY_STRING = "";
	// private static final String LIST_SUFFIX = "typeList";
	private static final String LIST_SUFFIX_DOT = ".typeList";

	private static final String EDIT_ADD = "Add";
	private static final String EDIT_REMOVE = "Remove";
	private static final String EDIT_ADD_REMOVE = "Add/Remove";
	private static final String BTN_ADD_REMOVE = "btnAddRemove";

	private static final String BTN_LOAD = "btnLoad";
	private static final String BTN_NEW = "btnNew";
	private static final String BTN_SAVE = "btnSave";
	private static final String BTN_DELETE = "btnDelete";

	private static final String BTN_OK = "btnOK";

	private static final String LIST_EDIT = "listEdit";
	private static final String LIST_AVAILABLE = "listAvailable";
	private static final String TXT_ACTIVE = "txtActive";
	private static final String TXT_EDIT = "txtEdit";

	private JTextField txtActive;
	private JTextField txtEdit;
	private JList<String> listEdit;
	private JList<String> listAvailable;
	private JLabel lblEdit;
	private JButton btnAddRemove;
	private JButton btnLoad;
	private JButton btnNew;
	private JButton btnSave;
	private JButton btnDelete;

}// class ManageLists
