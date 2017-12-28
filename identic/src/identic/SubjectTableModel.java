package identic;

import java.awt.Point;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

/**
 * modified getColumnCount to return 1 less than actual, to hide last field in the tables
 * 
 *
 */
public class SubjectTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private Map<Point, Object> lookup = new HashMap<Point, Object>();
	private int rows = 0;

	public SubjectTableModel() {
	}// Constructor

	public void clear() {
		lookup.clear();
		rows = 0;
	}// clear()

	public Long getStorageSum() {
		Long ans = 0l;
		for (int i = 0; i < this.getRowCount(); i++) {
			ans = ans + (Long) getValueAt(i, 2);
		} // for
		return ans;
	}// sun

	@Override
	public int getColumnCount() {
		return columns - 1;
	}// getColumnCount

	public String getColumnName(int column) {
		return headers[column];
	}// getColumnName

	@Override
	public int getRowCount() {
		return rows;
	}// getRowCount

	@Override
	public Object getValueAt(int row, int column) {
		return lookup.get(new Point(row, column));
	}// getValueAt

	public void setValueAt(Object value, int row, int column) {
		if ((row < 0) || (column < 0)) {
			throw new IllegalArgumentException("Invalid row/column setting");
		} // if - negative
		if ((row < rows) && (column < columns)) {
			lookup.put(new Point(row, column), value);
		} //
	}// setValueAt

	public Object[] getRow(int rowNumber) {
		Object[] row = new Object[this.getColumnCount()];
		for (int i = 0; i < columns - 1; i++) {
			row[i] = lookup.get(new Point(rowNumber, i));
		} // for
		return row;
	}// getRow

	// omit the two columns Duplicate an File ID

	public Object[] getCatalogItem(int rowNumber) {
		Object[] row = new Object[this.getColumnCount()];
		for (int i = 0; i < 4; i++) {
			row[i] = lookup.get(new Point(rowNumber, i));
		} // for
		row[4] = lookup.get(new Point(rowNumber, 6));
		return row;
	}// getCatalogItem

	public FileStat getFileStat(int rowNumber) {
		String filePath = Paths
				.get((String) lookup.get(new Point(rowNumber, 1)), (String) lookup.get(new Point(rowNumber, 0)))
				.toString();
		
		FileStat fileStat = new FileStat(filePath, (long) lookup.get(new Point(rowNumber, 2)),
				(String) lookup.get(new Point(rowNumber, 3)), (String) lookup.get(new Point(rowNumber, 6)));
		
		return fileStat;
	}// getFileStat

	public void addRow(Object[] values) {
		rows++;
		for (int i = 0; i < columns - 1; i++) {
			lookup.put(new Point(rows - 1, i), values[i]);
		} // for
	}// addRow

	public void addRow(FileStat subject) {
		rows++;
		lookup.put(new Point(rows - 1, 0), subject.getFileName());
		lookup.put(new Point(rows - 1, 1), subject.getDirectory());
		lookup.put(new Point(rows - 1, 2), subject.getFileSize());
		lookup.put(new Point(rows - 1, 3), subject.getFileTime().toString());
		lookup.put(new Point(rows - 1, 4), false); // is duplicate
		lookup.put(new Point(rows - 1, 5), -1); // file ID
		lookup.put(new Point(rows - 1, 6), subject.getHashKey());
	}// addRow

	public void addRow(FileStat subject, Integer fileID) {
		rows++;
		lookup.put(new Point(rows - 1, 0), subject.getFileName());
		lookup.put(new Point(rows - 1, 1), subject.getDirectory());
		lookup.put(new Point(rows - 1, 2), subject.getFileSize());
		lookup.put(new Point(rows - 1, 3), subject.getFileTime().toString());
		lookup.put(new Point(rows - 1, 4), false); // is dup
		lookup.put(new Point(rows - 1, 5), fileID);
		lookup.put(new Point(rows - 1, 6), subject.getHashKey());
	}// addRow

	@Override
	public Class<?> getColumnClass(int columnIndex) {

		switch (columnIndex) {
		case 0: // "Name"
			return super.getColumnClass(columnIndex);
		case 1:// "Directory"
			return super.getColumnClass(columnIndex);
		case 2:// "Size"
			return Long.class;
		case 3:// "Modified Date"
			return super.getColumnClass(columnIndex);
		// return FileTime.class;
		case 4:// Duplicate
			return Boolean.class;
		case 5:// File ID
			return Integer.class;
		default:
			return super.getColumnClass(columnIndex);
		case 6:// "HashKey"
			return super.getColumnClass(columnIndex);
		}// switch
	}// getColumnClass

	public static final String NAME = "Name";
	public static final String DIRECTORY = "Directory";
	public static final String SIZE = "Size";
	public static final String DATE = "ModifiedDate";
	public static final String DUP = "isDup";
	public static final String ID = "FileID";
	public static final String HASH_KEY = "HashKey";

	private static final String[] headers = new String[] { NAME, DIRECTORY, SIZE, DATE, DUP, ID, HASH_KEY };
	private static final int columns = headers.length;

}// class SubjectTableModel
