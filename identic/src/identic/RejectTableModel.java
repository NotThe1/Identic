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
		if ((row < rows) && (columns < columns)) {
			lookup.put(new Point(row, column), value);
		} //
	}// setValueAt

	public void addRow(Object[] values) {
		rows++;
		for (int i = 0; i < columns; i++) {
			lookup.put(new Point(rows - 1, i), values[i]);
		} // for
	}// addRow

	@Override
	public Class<?> getColumnClass(int columnIndex) {

		switch (columnIndex) {
		case 0:
			return super.getColumnClass(columnIndex);
		case 1:
			return DateFormat.class;
//			return Date.class;
		case 2:
			return Boolean.class;
		case 3:
			return Number.class;
		default:
			return super.getColumnClass(columnIndex);
		}// switch
	}// getColumnClass

}// class MySparseTableModel
