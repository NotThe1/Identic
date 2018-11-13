package identic;

import java.io.File;
import java.util.concurrent.RecursiveAction;

public class FindEmptyFolders extends RecursiveAction {
	private static final long serialVersionUID = 1L;

	private UtilityEmptyFolderTableModel utilityEmptyFolderTableModel;
	private File directory;

	public FindEmptyFolders(File directory, UtilityEmptyFolderTableModel utilityEmptyFolderTableModel) {
		this.directory = directory;
		this.utilityEmptyFolderTableModel = utilityEmptyFolderTableModel;
	}// constructor

	@Override
	 protected void compute() {//synchronized
		if(directory==null) {
			System.out.printf("[FindEmptyFolders.compute]Empty directory: %s%n", Thread.currentThread().getName());
		}
		File[] directories = directory.listFiles();
		if (directories.length == 0) {
				utilityEmptyFolderTableModel.addRow(new Object[] { true, directory.toPath() });
		} else {
			for (File directoryItem : directories) {
				if (directoryItem.isDirectory()) {
					FindEmptyFolders findEmptyFolders = new FindEmptyFolders(directoryItem, utilityEmptyFolderTableModel);
					findEmptyFolders.fork();
				} // if directory
			} // for
		} // if
	}// compute
}// FindEmptyFolders
