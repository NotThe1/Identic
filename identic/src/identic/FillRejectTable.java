package identic;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FillRejectTable implements Runnable {
	private RejectTableModel rejectTableModel;
	private ConcurrentLinkedQueue<FileStatReject> qRejects;

	public FillRejectTable(RejectTableModel rejectTableModel, ConcurrentLinkedQueue<FileStatReject> qRejects) {
		this.rejectTableModel = rejectTableModel;
		this.qRejects = qRejects;
	}// Constructor

	@Override
	public void run() {
		FileStatReject reject;
		// boolean thereIsMore = true;
		while (true) { // while(thereIsMore) {
			try {
				reject = qRejects.remove();
				if (reject.equals(Identic.END_OF_REJECT)) {
					break;
				} // if
				rejectTableModel.addRow(reject);
			} catch (NoSuchElementException ex) {
				//
			} // try - done ?
		} // while
	}// run

}// class FillRejectTable
