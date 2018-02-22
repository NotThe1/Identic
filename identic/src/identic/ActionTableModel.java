	package identic;

	import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

	/**
	 * modified getColumnCount to return 1 less than actual, to hide last field in the tables
	 * 
	 *
	 */
	public class ActionTableModel extends AbstractTableModel{ //DefaultTableModel 
		private static final long serialVersionUID = 1L;
		private Map<Point, Object> lookup = new HashMap<Point, Object>();
		private int rows = 0;

		public ActionTableModel() {
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


		public void addRowForAction(Object[] values) {
			rows++;
			lookup.put(new Point(rows - 1, 0), false);
			for (int i = 0; i < values.length; i++) {
				lookup.put(new Point(rows - 1, i+1), values[i]);
			} // for
	
		}//addRowForAction

		@Override
		public Class<?> getColumnClass(int columnIndex) {

			switch (columnIndex) {
			case 0:// Action
				return Boolean.class;
			case 1: // "Name"
				return super.getColumnClass(columnIndex);
			case 2:// "Directory"
				return super.getColumnClass(columnIndex);
			case 3:// "Size"
				return Long.class;
			case 4:// "Modified Date"
				return super.getColumnClass(columnIndex);
			// return FileTime.class;
			case 5:// Duplicate
				return Boolean.class;
			case 6:// File ID
				return Integer.class;
			default:
				return super.getColumnClass(columnIndex);
			}// switch
		}// getColumnClass
		
		public static final String ACTION = "Action";
		public static final String NAME = "Name";
		public static final String DIRECTORY = "Directory";
		public static final String SIZE = "Size";
		public static final String DATE = "ModifiedDate";
		public static final String DUP = "Dup";
		public static final String ID = "ID";

		private static final String[] headers = new String[] { ACTION,NAME, DIRECTORY, SIZE, DATE, DUP, ID};
		private static final int columns = headers.length;

	}// class SubjectTableModel

