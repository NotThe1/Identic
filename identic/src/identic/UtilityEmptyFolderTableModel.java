package identic;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class UtilityEmptyFolderTableModel extends AbstractTableModel implements IIdenticTableModel {
	private static final long serialVersionUID = 1L;
	private Map<Point, Object> lookup = new HashMap<Point, Object>();
	private int rows = 0;

	public UtilityEmptyFolderTableModel() {
	}// Constructor
	
	/* (non-Javadoc)
	 * @see identic.iIdenticTableModel#clear()
	 */
	@Override
	public void clear() {
		lookup.clear();
		rows = 0;
	}// clear()


	/* (non-Javadoc)
	 * @see identic.iIdenticTableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return columns ;
	}// getColumnCount

	/* (non-Javadoc)
	 * @see identic.iIdenticTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return headers[column];
	}// getColumnName

	/* (non-Javadoc)
	 * @see identic.iIdenticTableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return rows;
	}// getRowCount

	/* (non-Javadoc)
	 * @see identic.iIdenticTableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int column) {
		return lookup.get(new Point(row, column));
	}// getValueAt
	
	/* (non-Javadoc)
	 * @see identic.iIdenticTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object value, int row, int column) {
		if ((row < 0) || (column < 0)) {
			throw new IllegalArgumentException("Invalid row/column setting");
		} // if - negative
		if ((row < rows) && (column < columns)) {
			lookup.put(new Point(row, column), value);
		} //
	}// setValueAt

	/* (non-Javadoc)
	 * @see identic.iIdenticTableModel#getRow(int)
	 */
	@Override
	public Object[] getRow(int rowNumber) {
		Object[] row = new Object[this.getColumnCount()];
		for (int i = 0; i < columns - 1; i++) {
			row[i] = lookup.get(new Point(rowNumber, i));
		} // for
		return row;
	}// getRow
	
	/* (non-Javadoc)
	 * @see identic.iIdenticTableModel#removeRow(int)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see identic.iIdenticTableModel#addRow(java.lang.Object[])
	 */
	@Override
	public void addRow(Object[] values) {
		rows++;
		for (int i = 0; i < columns ; i++) {
			lookup.put(new Point(rows - 1, i), values[i]);
		} // for
	}// addRow
	

	
	/* (non-Javadoc)
	 * @see identic.iIdenticTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {

		switch (columnIndex) {
		case 0:// "Action"
			return Boolean.class;
		case 1:// "Folder"
			return super.getColumnClass(columnIndex);
			
		default:
			return super.getColumnClass(columnIndex);
		}// switch
	}// getColumnClass
	
	public static final String ACTION = "Action";
	public static final String FOLDER = "Folder";
	
	private static final String[] headers = new String[] {ACTION  , FOLDER};
	private static final int columns = headers.length;



}//class EmptyFolderTableModel
