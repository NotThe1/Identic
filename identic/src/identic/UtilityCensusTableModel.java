package identic;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class UtilityCensusTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private Map<Point, Object> lookup = new HashMap<Point, Object>();
	private int rows = 0;

	public UtilityCensusTableModel() {
		// TODO Auto-generated constructor stub
	}// Constructor
	
	public void clear() {
		lookup.clear();
		rows = 0;
	}// clear()


	@Override
	public int getColumnCount() {
		return columns ;
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
	
	public synchronized void removeRow(int row) {
		if ( row > rows|| row < 0) {
			return;
		}// if row we have
		
		if (row ==rows-1) {
			rows--;
			return;
		}// if last row
		
		/* move data down a row */
		for (int r = row ;r < rows-1; r++) {
			for (int i = 0; i < columns ; i++) {
				lookup.put(new Point(r, i), lookup.get(new Point(r+1, i)));
			} // for
		}// for r
		
		/* remove the last row */
		for (int i = 0; i < columns ; i++) {
			 lookup.remove(rows,columns);
		} // for
		
		rows--;
	}//removeRow

	public void addRow(Object[] values) {
		rows++;
		for (int i = 0; i < columns ; i++) {
			lookup.put(new Point(rows - 1, i), values[i]);
		} // for
	}// addRow
	

	
	@Override
	public Class<?> getColumnClass(int columnIndex) {

		switch (columnIndex) {
		case 0:// "Count"
			return Integer.class;
		case 1:// "Directory"
			return super.getColumnClass(columnIndex);
			
		default:
			return super.getColumnClass(columnIndex);
		}// switch
	}// getColumnClass
	
	
	public static final String COUNT = "Count";
	public static final String TYPE = "File Type";

	private static final String[] headers = new String[] {  COUNT, TYPE};
	private static final int columns = headers.length;



}//class UtilityCensusTableModel
