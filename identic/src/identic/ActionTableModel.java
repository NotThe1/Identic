	package identic;

/**
	 * ??????? modified getColumnCount to return 1 less than actual, to hide last field in the tables
	 * 
	 *
	 */
	public class ActionTableModel extends MyTableModel {
		private static final long serialVersionUID = 1L;
		
		public ActionTableModel() {
			super(new String[] { ACTION,NAME, DIRECTORY, SIZE, DATE, DUP, ID });
		}// Constructor

		public void addRowForAction(Object[] values) {
			Object[] rowData = new Object[values.length+1];
			rowData[0] = false;
			for ( int i = 0; i < values.length;i++) {
				rowData[i+1] = values[i];
			}//for
			addRow(rowData);
		}//addRowForAction
		
		public boolean isCellEditable(int row, int column) {
			return column == 0;
		}// isCellEditable


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


	}// class SubjectTableModel

