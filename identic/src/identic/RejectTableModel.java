package identic;

import java.text.DateFormat;

public class RejectTableModel extends MyTableModel {

	private static final long serialVersionUID = 1L;

	public RejectTableModel() {
		super(new String[] { NAME, DIRECTORY, SIZE, DATE, REASON });
	}// Constructor

	public void addRow(FileStatReject reject) {
		/* @formatter:off */
		Object[] rowData = new Object[] { reject.getFileName(),
										reject.getDirectory(),
										reject.getFileSize(),
										reject.getFileTime(),
										reject.getReason()};
		/* @formatter:on  */
		addRow(rowData);
	}// addRow

	@Override
	public Class<?> getColumnClass(int columnIndex) {

		switch (columnIndex) {
		case 0: // "Name"
			return String.class;
		case 1:// "Directory"
			return String.class;
		case 2:// "Size"
			return Long.class;
		case 3:// "Modified Date"
			return DateFormat.class;
		case 4:// "Reason"
			return String.class;
		default:
			return super.getColumnClass(columnIndex);
		}// switch
	}// getColumnClass

	public static final String NAME = "Name";
	public static final String DIRECTORY = "Directory";
	public static final String SIZE = "Size";
	public static final String DATE = "ModifiedDate";
	public static final String REASON = "Reason";

}// class MySparseTableModel
