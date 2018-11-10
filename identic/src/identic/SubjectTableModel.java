package identic;

import java.nio.file.Paths;

/**
 * modified getColumnCount to return 1 less than actual, to hide last field in the tables
 * 
 *
 */
public class SubjectTableModel extends MyTableModel {
	private static final long serialVersionUID = 1L;

	public SubjectTableModel() {
		super(new String[] { NAME, DIRECTORY, SIZE, DATE, DUP, ID, HASH_KEY });
	}// Constructor

	public Long getStorageSum() {
		Long ans = 0l;
		for (int i = 0; i < this.getRowCount(); i++) {
			ans = ans + (Long) getValueAt(i, 2);
		} // for
		return ans;
	}// sun

	@Override
	public int getColumnCount() { // want to ignore the HASH column
		return columnCount - 1;
	}// getColumnCount

	public FileStat getFileStat(int rowNumber) {
		String filePath = Paths.get((String) getValueAt(rowNumber, 1), (String) getValueAt(rowNumber, 0)).toString();

		FileStat fileStat = new FileStat(filePath, (long) getValueAt(rowNumber, 2), (String) getValueAt(rowNumber, 3),
				(String) getValueAt(rowNumber, 6));
		return fileStat;
	}// getFileStat

	public void addRow(FileStat subject) {
		addRow(subject, -1);
	}// addRow

	public void addRow(FileStat subject, Integer fileID) {
/* @formatter:off */
		Object[] rowData = new Object[] {subject.getFileName(),
				 subject.getDirectory(),
				 subject.getFileSize(),
				 subject.getFileTime().toString(),
				 false ,
				 fileID ,
				 subject.getHashKey() };
/* @formatter:on  */
		addRow(rowData);
	}// addRow

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex > this.getColumnCount() - 1) {
			String msg = String.format("[getColumnClass] Invalid column. columnIndex = %d, max Column = %d",
					columnIndex, this.getColumnCount());
			throw new IllegalArgumentException(msg);

		} // if
		Class<?> ans;
		switch (columnIndex) {
		case 0: // Name
			ans = String.class;
			break;
		case 1: // Directory
			ans = String.class;
			break;
		case 2: // Size
			ans = Long.class;
			break;
		case 3: // Modified Date
			ans = String.class;
			break;
		case 4: // Duplicate
			ans = Boolean.class;
			break;
		case 5:// File ID
			ans = Integer.class;
			break;
		case 6: // Hash Key
			ans = String.class;
			break;
		default:
			ans = String.class;
		}// switch
		return ans;
	}// getColumnClass

	public static final String NAME = "Name";
	public static final String DIRECTORY = "Directory";
	public static final String SIZE = "Size";
	public static final String DATE = "ModifiedDate";
	public static final String DUP = "Dup";
	public static final String ID = "ID";
	public static final String HASH_KEY = "HashKey";

}// class SubjectTableModel
