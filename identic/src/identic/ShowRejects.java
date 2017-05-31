package identic;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.NoSuchElementException;

import javax.swing.DefaultListModel;

public class ShowRejects implements Runnable {

	private HashMap<String,Integer> members = new HashMap<>();
	private ArrayDeque<Path> rejects = new ArrayDeque<Path>();
	private DefaultListModel<String> excludeModel = new DefaultListModel<>();
	private Thread priorThread;

	public ShowRejects(Thread priorThread, ArrayDeque<Path> subjects, DefaultListModel<String> excludeModel) {
		this.priorThread = priorThread;
		this.rejects = subjects;
		this.excludeModel = excludeModel;
	}// Constructor

	@Override
	public void run() {
		excludeModel.clear();
		Path path = null;
		String fileName = null;
		String filePart = null;
		int partsCount;
		Integer occurances;
		while (true) {
			try {
				path = rejects.remove();
				fileName = path.getFileName().toString();
				String[] parts = fileName.split("\\.");
				partsCount = parts.length;

				if (partsCount > 1) {
					filePart = parts[partsCount - 1].toUpperCase();
				} else {
					filePart = "<none>";
				} // if
				
				occurances = members.get(filePart);
				if (occurances == null){
					members.put(filePart, 1);
					excludeModel.addElement(filePart);
				}else{
					members.put(filePart, occurances++);
				}//if unique
				

			} catch (NoSuchElementException ex) {
				if (priorThread.getState().equals(Thread.State.TERMINATED)) {
					return;
				} // if - done ?
			} // try
		} // while
	}// run


}// class ShowSubjects
