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

		switch (columnIndex) {
		case 0:// "Action"
			return Boolean.class;
		case 1:// "Folder"
			return Path.class;

		default:
			return super.getColumnClass(columnIndex);
		}// switch
	}// getColumnClass

	public static final String ACTION = "Action";
	public static final String FOLDER = "Folder";

	private static final String[] headers = new String[] { ACTION, FOLDER };
	private static final int columns = headers.length;

}// class EmptyFolderTableModel
