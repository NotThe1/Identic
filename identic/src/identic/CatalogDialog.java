package identic;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class CatalogDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField txtName;
	private JTextField txtDescription;
	private int state = JOptionPane.CANCEL_OPTION;
	
	public static final String NEW = "new";
	public static final String EDIT = "edit";

	AppLogger log = AppLogger.getInstance();

	private String dialogType;

	/**
	 * Launch the application.
	 */
	
	// public static void main(String[] args) {
	// try {
	// CatalogDialog dialog = new CatalogDialog();
	// dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	// dialog.setVisible(true);
	// } catch (Exception e) {
	// e.printStackTrace();
	// } // try
	// }// main

	public int showDialog() {
		// dialogResultValue = JOptionPane.CANCEL_OPTION;
		this.setLocationRelativeTo(this.getOwner());


		switch (this.dialogType) {
		case NEW:
			break;
		case EDIT:
			break;
		default:
			String message = String.format("[CatalogDialog] showDialog()%n Bad dialogType: %s", this.dialogType);
			log.addError(message);
		}// switch

		this.setVisible(true);

//		this.dispose();
		return this.state;
	}// showDialog
	
	public String getName() {
		return txtName.getText();
	}//getName
	
	public String getDescription() {
		return txtDescription.getText();
	}//getDescription
	
	
	
	
	///////////////////////////////////////////////////////////
//	public void close() {
//		appClose();
//	}// close

	private void appClose() {

		Preferences myPrefs = Preferences.userNodeForPackage(ManageLists.class).node(this.getClass().getSimpleName());
		Dimension dim = this.getSize();
		myPrefs.putInt("Height", dim.height);
		myPrefs.putInt("Width", dim.width);
		Point point = this.getLocation();
		myPrefs.putInt("LocX", point.x);
		myPrefs.putInt("LocY", point.y);
		myPrefs = null;
		dispose();
	}// appClose

	private void appInit() {
		Preferences myPrefs = Preferences.userNodeForPackage(ManageLists.class).node(this.getClass().getSimpleName());
		this.setSize(myPrefs.getInt("Width", 578), myPrefs.getInt("Height", 250));
		this.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));
		myPrefs = null;
	}// appInit

	/**
	 * Create the dialog.
	 */

	public static CatalogDialog makeNewCatalogDialog() {
		return new CatalogDialog(CatalogDialog.NEW);

	}// Factory makeNewCatalogDialog

	public static CatalogDialog makeEditCatalogDialog() {
		return new CatalogDialog(CatalogDialog.EDIT);

	}// Factory makeEditCatalogDialog

	public CatalogDialog(String dialogType) {
		this.dialogType = dialogType;
		// this.subjectTableModel = subjectTableModel;

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose();
			}
		});
		initialize();
		appInit();
		// if(subjectTableModel ==null) {
		// JOptionPane.showMessageDialog(this, "No Catalog has been created, by FIND");
		// appClose();
		// }//if
	}// Constructor

	public void initialize() {
		setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		setTitle("Catalog Dialog");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 500, 30, 0 };
		gridBagLayout.rowHeights = new int[] { 50, 0, 0, 33, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagConstraints gbc_contentPanel = new GridBagConstraints();
		gbc_contentPanel.anchor = GridBagConstraints.WEST;
		gbc_contentPanel.fill = GridBagConstraints.VERTICAL;
		gbc_contentPanel.insets = new Insets(0, 0, 5, 5);
		gbc_contentPanel.gridx = 0;
		gbc_contentPanel.gridy = 0;
		getContentPane().add(contentPanel, gbc_contentPanel);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 800, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);

		JLabel lblName = new JLabel("Name:");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		contentPanel.add(lblName, gbc_lblName);

		txtName = new JTextField();
		txtName.setMinimumSize(new Dimension(400, 20));
		txtName.setPreferredSize(new Dimension(800, 20));
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.insets = new Insets(0, 0, 5, 0);
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 1;
		contentPanel.add(txtName, gbc_txtName);
		txtName.setColumns(10);

		JLabel lblDescription = new JLabel("Description");
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.EAST;
		gbc_lblDescription.insets = new Insets(0, 0, 0, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 3;
		contentPanel.add(lblDescription, gbc_lblDescription);

		txtDescription = new JTextField();
		txtDescription.setMinimumSize(new Dimension(400, 20));
		txtDescription.setPreferredSize(new Dimension(800, 20));
		GridBagConstraints gbc_txtDescription = new GridBagConstraints();
		gbc_txtDescription.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtDescription.gridx = 1;
		gbc_txtDescription.gridy = 3;
		contentPanel.add(txtDescription, gbc_txtDescription);
		txtDescription.setColumns(10);

		JPanel buttonPane = new JPanel();
		GridBagConstraints gbc_buttonPane = new GridBagConstraints();
		gbc_buttonPane.insets = new Insets(0, 0, 5, 5);
		gbc_buttonPane.anchor = GridBagConstraints.NORTHEAST;
		gbc_buttonPane.gridx = 0;
		gbc_buttonPane.gridy = 2;
		getContentPane().add(buttonPane, gbc_buttonPane);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				state = JOptionPane.OK_OPTION;
				appClose();
			}
		});
		buttonPane.setLayout(new GridLayout(0, 2, 0, 0));
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				state = JOptionPane.CANCEL_OPTION;
				appClose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);

	}// initialize
	
	////////////////////////////////////////////////////////////
//	public class CatalogNameVerifier extends InputVerifier {
//
//		@Override
//		public boolean verify(JComponent input) {
//			String fileListDirectory = System.getProperty("java.io.tmpdir");
//			fileListDirectory = fileListDirectory.replace("Temp", "Identic");
//			Path p = Paths.get(fileListDirectory);
//			if (!Files.exists(p)) {
//				JOptionPane.showMessageDialog(null, "Initializing File Type lists in " + p.toString(), "Initialization",
//						JOptionPane.INFORMATION_MESSAGE);
//				System.err.println("Making new directory");
//				try {
//		//			Files.createDirectories(p);
//				} catch (IOException e) {
//					e.printStackTrace();
//				} // try
//			} // if exits
//
////		} // if not there
//
//			return false;
//		}//verify
//
//	}//class CatalogNameVerifier

}// class CatalogDialog
