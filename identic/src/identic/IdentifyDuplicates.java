package identic;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JTable;

public class IdentifyDuplicates implements Runnable {
	private LinkedBlockingQueue<FileStatSubject> qHashes = new LinkedBlockingQueue<FileStatSubject>();
	private Thread priorThread;
	private Integer fileID;

	// private JTable table;
	private SubjectTableModel subjectTableModel;
	private HashMap<String, Integer> hashCounts;
	private HashMap<String, Integer> hashIDs = new HashMap<>();


	public IdentifyDuplicates(LinkedBlockingQueue<FileStatSubject> qHashes, Thread priorThread, JTable table,
			HashMap<String, Integer> hashCounts) {
		this.qHashes = qHashes;
		this.priorThread = priorThread;
		this.subjectTableModel= (SubjectTableModel) table.getModel();
		this.hashCounts = hashCounts;
	}// Constructor

	@Override
	public void run() {
		fileID = 0;
		this.hashIDs.clear();
		this.hashCounts.clear();
		FileStatSubject subject;
		while (true) {
			try {
				subject = qHashes.remove();
				subjectTableModel.addRow(subject,keepHashKeyCount(subject.getHashKey()));
			} catch (NoSuchElementException ex) {
				if (priorThread.getState().equals(Thread.State.TERMINATED)) {
					return;
				} // if - done ?
			} // try
		} // while
	}// run

//	public void setTable(JTable table) {
//		this.subjectTableModel = (SubjectTableModel) table.getModel();
//	}// setTable
//
//	public void setCounts(HashMap<String, Integer> fileCounts) {
//		this.hashCounts = fileCounts;
//	}// setMap

	private Integer keepHashKeyCount(String hashKey) {

		Integer occurances = hashCounts.get(hashKey);
		if (occurances == null) {
			hashCounts.put(hashKey, 1);
			hashIDs.put(hashKey, fileID++);
			// excludeModel.addElement(filePart);
		} else {
			hashCounts.put(hashKey, occurances + 1);
		} // if unique
		
		return hashIDs.get(hashKey);
	}// keepSuffixCount

}// class IdentifyDuplicates
