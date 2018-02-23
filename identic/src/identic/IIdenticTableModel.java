package identic;

public interface IIdenticTableModel {

	void clear();// clear()

	int getColumnCount();// getColumnCount

	String getColumnName(int column);// getColumnName

	int getRowCount();// getRowCount

	Object getValueAt(int row, int column);// getValueAt

	void setValueAt(Object value, int row, int column);// setValueAt

	Object[] getRow(int rowNumber);// getRow

	void removeRow(int row);//removeRow

	void addRow(Object[] values);// addRow

	Class<?> getColumnClass(int columnIndex);// getColumnClass


}