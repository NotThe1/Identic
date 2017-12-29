package identic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;

@SuppressWarnings("serial")
public class DisplayActiveTypeList extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JList<String> lstTypeList;
	private JLabel lblActiveTypeList;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			DisplayActiveTypeList dialog = new DisplayActiveTypeList();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		} // try
	}// main

	public int show(String listName, DefaultListModel<String> listModel) {
		// dialogResultValue = JOptionPane.CANCEL_OPTION;
		this.setLocationRelativeTo(this.getOwner());
		lblActiveTypeList.setText(listName);
		lstTypeList.setModel(listModel);
		this.setVisible(true);
		// this.dispose();
		return JOptionPane.OK_OPTION;

	}// show

	private void appClose() {

		Preferences myPrefs = Preferences.userNodeForPackage(DisplayActiveTypeList.class)
				.node(this.getClass().getSimpleName());
		Dimension dim = this.getSize();
		myPrefs.putInt("Height", dim.height);
		myPrefs.putInt("Width", dim.width);
//		Point point = this.getLocation();
//		myPrefs.putInt("LocX", point.x);
//		myPrefs.putInt("LocY", point.y);
		myPrefs = null;
		dispose();
	}// appClose

	private void appInit() {
		Preferences myPrefs = Preferences.userNodeForPackage(DisplayActiveTypeList.class)
				.node(this.getClass().getSimpleName());
		this.setSize(myPrefs.getInt("Width", 600), myPrefs.getInt("Height", 600));
//		this.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));

		myPrefs = null;

	}// appInit

	/**
	 * Create the dialog.
	 */
	public DisplayActiveTypeList() {
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
		// setBounds(100, 100, 354, 665);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 10, 100, 10, 0 };
		gridBagLayout.rowHeights = new int[] { 228, 33, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);
		contentPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		GridBagConstraints gbc_contentPanel = new GridBagConstraints();
		gbc_contentPanel.fill = GridBagConstraints.BOTH;
		gbc_contentPanel.insets = new Insets(0, 0, 5, 5);
		gbc_contentPanel.gridx = 1;
		gbc_contentPanel.gridy = 0;
		getContentPane().add(contentPanel, gbc_contentPanel);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPanel.add(scrollPane, gbc_scrollPane);

		lblActiveTypeList = new JLabel("New label");
		lblActiveTypeList.setForeground(new Color(32, 178, 170));
		lblActiveTypeList.setHorizontalAlignment(SwingConstants.CENTER);
		lblActiveTypeList.setFont(new Font("Tahoma", Font.BOLD, 14));
		scrollPane.setColumnHeaderView(lblActiveTypeList);

		lstTypeList = new JList<String>();
		lstTypeList.setVisibleRowCount(15);
		scrollPane.setViewportView(lstTypeList);

		JPanel buttonPane = new JPanel();
		buttonPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_buttonPane = new GridBagConstraints();
		gbc_buttonPane.fill = GridBagConstraints.HORIZONTAL;
		gbc_buttonPane.insets = new Insets(0, 0, 0, 5);
		gbc_buttonPane.anchor = GridBagConstraints.NORTH;
		gbc_buttonPane.gridx = 1;
		gbc_buttonPane.gridy = 1;
		getContentPane().add(buttonPane, gbc_buttonPane);
		GridBagLayout gbl_buttonPane = new GridBagLayout();
		gbl_buttonPane.columnWidths = new int[] { 10, 100, 10, 0 };
		gbl_buttonPane.rowHeights = new int[] { 23, 0 };
		gbl_buttonPane.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_buttonPane.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		buttonPane.setLayout(gbl_buttonPane);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				appClose();
			}
		});
		okButton.setActionCommand("OK");
		GridBagConstraints gbc_okButton = new GridBagConstraints();
		gbc_okButton.anchor = GridBagConstraints.NORTHEAST;
		gbc_okButton.insets = new Insets(0, 0, 0, 5);
		gbc_okButton.gridx = 1;
		gbc_okButton.gridy = 0;
		buttonPane.add(okButton, gbc_okButton);
		getRootPane().setDefaultButton(okButton);

	}// initialize()

}// class DsiplayActiveTypeList
