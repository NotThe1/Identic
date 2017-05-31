package identic;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.NoSuchElementException;

import javax.swing.JTextArea;

public class MakeFileKey implements Runnable {

	private ArrayDeque<Path> subjects = new ArrayDeque<Path>();
	private JTextArea txtLog;
	private Thread priorThread;
	
	public MakeFileKey(Thread priorThread,ArrayDeque<Path> subjects,JTextArea txtLog){
		this.priorThread = priorThread;
		this.subjects= subjects;
		this.txtLog = txtLog;
	}//Constructor
	
	@Override
	public void run() {
		Path path = null;
		String name = null;
		while (true){
			try{
			path = subjects.remove();
			txtLog.append(path.toString() + System.lineSeparator());
			}catch (NoSuchElementException ex){
				if(priorThread.getState().equals(Thread.State.TERMINATED)){
					return;
				}//if - done ?
			}//try
		}//while

	}//run

}//class ShowSubjects
