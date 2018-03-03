package identic;

import java.nio.file.Path;

public class UtilityEmptyFolderTableModel extends MyTableModel {
	private static final long serialVersionUID = 1L;

	UtilityEmptyFolderTableModel() {
		super(new String[] { ACTION, FOLDER });
	}// Constructor

	public boolean isCellEditable(int row, int column) {
		return column == 0;
	}// isCellEditable

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		Class<?> ans;
		switch (columnIndex) {
		case 0:// "Action"
			ans = Boolean.class;
			break;
		case 1:// "Folder"
			ans = Path.class;
			break;
		default:
			ans = super.getColumnClass(columnIndex);
		}// switch
		return ans;
	}// getColumnClass

	public static final String ACTION = "Action";
	public static final String FOLDER = "Folder";

}// class EmptyFolderTableModel
