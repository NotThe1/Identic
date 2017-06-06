package identic;

import java.awt.Point;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class RejectTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	
	private Map<Point, Object> lookup;
	private int rows;
	private final int columns;
	private final String[] headers;

	public RejectTableModel(int rows, String columnHeaders[]) {
		if ((rows < 0) || (columnHeaders == null)) {
			throw new IllegalArgumentException("Invalid row count/columnHeaders");
		}
		this.rows = rows;
		this.columns = columnHeaders.length;
		headers = columnHeaders;
		lookup = new HashMap<Point, Object>();
	}// Constructor

	public RejectTableModel(String columnHeaders[]) {
		this(0, columnHeaders);
	}// Constructor
	
	public void clear(){
		lookup.clear();
		rows = 0;
	}//clear()
	
	public RejectTableModel(){
		this(0,new String[] { "Name", "Directory", "Size", "Modified Date", "Reason" });
	}//Constructor

	@Override
	public int getColumnCount() {
		return columns;
	}// getColumnCount

	@Override
	public int getRowCount() {
		return rows;
	}// getRowCount

	public String getColumnName(int column) {
		return headers[column];
	}// getColumnName

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

	public void addRow(Object[] values) {
		rows++;
		for (int i = 0; i < columns; i++) {
			lookup.put(new Point(rows - 1, i), values[i]);
		} // for
	}// addRow
	
	public void addRow(FileStatReject reject){
		rows++;
		lookup.put(new Point(rows - 1, 0), reject.getFileName());
		lookup.put(new Point(rows - 1, 1), reject.getDirectory());
		lookup.put(new Point(rows - 1, 2), reject.getFileSize());
		lookup.put(new Point(rows - 1, 3), reject.getFileTime());
		lookup.put(new Point(rows - 1, 4), reject.getReason());
	}//addRow

	@Override
	public Class<?> getColumnClass(int columnIndex) {

		switch (columnIndex) {
		case 0: //"Name"
			return super.getColumnClass(columnIndex);
		case 1://"Directory"
			return super.getColumnClass(columnIndex);
		case 2://"Size"
			return Number.class;
		case 3://"Modified Date"
			return DateFormat.class;
		case 4:// "Reason"
			return super.getColumnClass(columnIndex);
		default:
			return super.getColumnClass(columnIndex);
		}// switch
	}// getColumnClass

}// class MySparseTableModel
