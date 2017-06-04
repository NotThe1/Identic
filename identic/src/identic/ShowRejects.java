package identic;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JTable;

public class ShowRejects implements Runnable {

	private HashMap<String, Integer> members = new HashMap<>();
//	private ArrayDeque<FileStatReject> qRejects = new ArrayDeque<FileStatReject>(4096);
	private LinkedBlockingQueue<FileStatReject> qRejects = new LinkedBlockingQueue<FileStatReject>();
	private JTable rejectTable;
	// private String[] columnHeaders = new String[]{"Name","Directory","Size","Modified Date","Reason"};
	private RejectTableModel rejectTableModel = new RejectTableModel(
			new String[] { "Name", "Directory", "Size", "Modified Date", "Reason" });

	private Thread priorThread;
	private AppLogger appLogger = AppLogger.getInstance();

	public ShowRejects(Thread priorThread, LinkedBlockingQueue<FileStatReject> qRejects, JTable rejectTable) {
		this.priorThread = priorThread;
		this.qRejects = qRejects;
		this.rejectTable = rejectTable;
		this.rejectTable.setModel(rejectTableModel);
	}// Constructor

	@Override
	public void run() {
		members.clear();
		String fileName = null;
		FileStatReject reject;
		while (true) {
			try {
				reject = qRejects.remove();
				keepSuffixCount(reject);
				rejectTableModel.addRow(reject);
				fileName = reject.getFileName();
			} catch (NoSuchElementException ex) {
				if (priorThread.getState().equals(Thread.State.TERMINATED)) {
					System.err.println(ex.getMessage());
					appLogger.addSpecial(fileName);
					logSummary();
					return;
				} // if - done ?
			} // try
		} // while
	}// run

	private void logSummary() {
		appLogger.addInfo(String.format("%,d File Types excluded", members.size()));
		Set<String> keys = members.keySet();

		for (String key : keys) {
			appLogger.addInfo(String.format("%s - %,d occurances", key, members.get(key)));
		} // for
	}// logSummary

	private void keepSuffixCount(FileStatReject reject) {
		String fileName = reject.getFileName();
		String suffix = null;
		String[] parts = fileName.split("\\.");
		int partsCount = parts.length;

		if (partsCount > 1) {
			suffix = parts[partsCount - 1].toUpperCase();
		} else {
			return; // - should not happen
		} // if

		Integer occurances = members.get(suffix);
		if (occurances == null) {
			members.put(suffix, 1);
			// excludeModel.addElement(filePart);
		} else {
			members.put(suffix, occurances + 1);
		} // if unique
	}// keepSuffixCount

}// class ShowSubjects
