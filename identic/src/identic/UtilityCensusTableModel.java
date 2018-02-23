package identic;

//import java.awt.Point;
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.swing.table.AbstractTableModel;

public class UtilityCensusTableModel extends MyTableModel {
	private static final long serialVersionUID = 1L;

	UtilityCensusTableModel() {
		super(new String[] { COUNT, TYPE });
	}// Constructor

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex > this.getColumnCount() - 1) {
			String msg = String.format("[getColumnClass] Invalid column. columnIndex = %d, max Column = %d",
					columnIndex, this.getColumnCount());
			throw new IllegalArgumentException(msg);
		}//if
		
		Class<?> ans = String.class;
		switch (columnIndex) {
		case 0:// "Count"
			ans = Integer.class;
		case 1:// "Directory"
			ans = String.class;
		default:
			return super.getColumnClass(columnIndex);
		}// switch
	}// getColumnClass

	private static final String COUNT = "Count";
	private static final String TYPE = "File Type";

	// private static final String[] headers = new String[] { COUNT, TYPE};
	// private static final int columns = headers.length;

}// class UtilityCensusTableModel
