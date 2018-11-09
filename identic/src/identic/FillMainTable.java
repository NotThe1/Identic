package identic;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class FillMainTable implements Runnable {
	private static final Object hashLock = new Object();
	private static final Object idLock = new Object();

	private static final AtomicInteger fileID = new AtomicInteger(0);
	private static final ConcurrentHashMap<String, Integer> hashIDs = new ConcurrentHashMap<String, Integer>();
	private ConcurrentHashMap<String, Integer> hashCounts;

	private SubjectTableModel subjectTableModel;
	private ConcurrentLinkedQueue<FileStat> qHashes;

	public FillMainTable(SubjectTableModel subjectTableModel, ConcurrentLinkedQueue<FileStat> qHashes,
			ConcurrentHashMap<String, Integer> hashCounts) {
		this.subjectTableModel = subjectTableModel;
		this.qHashes = qHashes;
		this.hashCounts = hashCounts;
	}// Constructor

	@Override
	public void run() {
		FileStat fileStat = null;
		while (true) {
			synchronized (hashLock) {
				FileStat fs = qHashes.peek();
				if (fs == null) {
					continue;
				} // if
				if (fs.equals(Identic.END_OF_SUBJECT)) {
					break;
				} // if
				fileStat = qHashes.poll();
			} // Synchronized (hashLock)
			
			synchronized (idLock) {
				String hashKey = fileStat.getHashKey();
				if (!hashCounts.containsKey(hashKey)) {
					hashCounts.put(hashKey, 0);
					hashIDs.put(hashKey, fileID.getAndIncrement());
				} // if - new
				hashCounts.put(hashKey, hashCounts.get(hashKey) + 1);
				subjectTableModel.addRow(fileStat, hashIDs.get(hashKey));
			} // Synchronized (idLock)
		} // while
	}// run

	private Integer keepHashKeyCount(String hashKey) {

		return null;
	}// keepHashKeyCount

}// class FillMainTable
