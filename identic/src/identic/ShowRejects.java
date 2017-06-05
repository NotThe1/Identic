package identic;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JTable;

public class ShowRejects implements Runnable {

	private HashMap<String, Integer> members = new HashMap<>();
	private LinkedBlockingQueue<FileStatReject> qRejects = new LinkedBlockingQueue<FileStatReject>();
	private RejectTableModel rejectTableModel;
	private Thread priorThread;
	private AppLogger appLogger = AppLogger.getInstance();

	public ShowRejects(Thread priorThread, LinkedBlockingQueue<FileStatReject> qRejects, JTable rejectTable) {
		this.priorThread = priorThread;
		this.qRejects = qRejects;
		rejectTableModel = (RejectTableModel) rejectTable.getModel();
	}// Constructor

	@Override
	public void run() {
		members.clear();
		String fileName = null;
		FileStatReject reject;
		while (true) {
			try {
				reject = qRejects.remove();
				rejectTableModel.addRow(reject);
				fileName = reject.getFileName();
			} catch (NoSuchElementException ex) {
				if (priorThread.getState().equals(Thread.State.TERMINATED)) {
					appLogger.addSpecial(fileName);
					return;
				} // if - done ?
			} // try
		} // while
	}// run

}// class ShowSubjects
