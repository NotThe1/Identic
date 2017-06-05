package identic;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.DefaultListModel;
import javax.swing.JList;

public class IdentifySubjects implements Runnable {
	private LinkedBlockingQueue<FileStatSubject> qSubjects;
	private LinkedBlockingQueue<FileStatReject> qRejects;
	private Path startPath;
	private ArrayList<String> targetSuffixes;
	private AppLogger appLogger = AppLogger.getInstance();

	private HashMap<String, Integer> members = new HashMap<>();
	private DefaultListModel<String> excludeModel;
	private int fileCount;
	private int folderCount;
	private int subjectCount;
	private int rejectCount;

	public IdentifySubjects(LinkedBlockingQueue<FileStatSubject> qSubjects, LinkedBlockingQueue<FileStatReject> qRejects,
			Path startPath, ArrayList<String> targetSuffixes) {
		this.qSubjects = qSubjects;
		qSubjects.clear();
		this.qRejects = qRejects;
		qRejects.clear();
		this.startPath = startPath;
		this.targetSuffixes = targetSuffixes;
	}// constructor

	public IdentifySubjects(LinkedBlockingQueue<FileStatSubject> qSubjects, LinkedBlockingQueue<FileStatReject> qRejects,
			Path startPath, ArrayList<String> targetSuffixes,JList<String> listExcluded) {
		this.qSubjects = qSubjects;
		qSubjects.clear();
		this.qRejects = qRejects;
		qRejects.clear();
		this.startPath = startPath;
		this.targetSuffixes = targetSuffixes;
		this.excludeModel = (DefaultListModel<String>) listExcluded.getModel();
	}// constructor

	@Override
	public void run() {
		fileCount = 0;
		folderCount = 0;
		subjectCount = 0;
		rejectCount = 0;
		this.excludeModel.clear();
		MyWalker myWalker = new MyWalker();
		try {
			Files.walkFileTree(startPath, myWalker);
		} catch (IOException e) {
			e.printStackTrace();
		} // try
		logSummary();
	}// run

	private void logSummary() {
		appLogger.addNL(2);
		appLogger.addSpecial("folderCount = " + folderCount);
		appLogger.addSpecial("fileCount  = " + fileCount);
		appLogger.addSpecial("subjectCount = " + subjectCount);
		appLogger.addSpecial("rejectCount  = " + rejectCount);
		appLogger.addNL();
		appLogger.addInfo(String.format("%,d File Types excluded", members.size()));
		Set<String> keys = members.keySet();
		appLogger.addNL();
		for (String key : keys) {
			appLogger.addInfo(String.format("%s - %,d occurances", key, members.get(key)));
		} // for
	}// logSummary

	class MyWalker implements FileVisitor<Path> {

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			// TODO Auto-generated method stub
			return FileVisitResult.CONTINUE;
		}// FileVisitResult

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			folderCount++;
			return FileVisitResult.CONTINUE;
		}// FileVisitResult

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			FileTime lastModifieTime;
			long fileSize;
			fileCount++;
			String fileName = file.getFileName().toString();
			lastModifieTime = Files.getLastModifiedTime(file);
			fileSize = Files.size(file);
			int partsCount;
			String part = null;
			String[] parts = fileName.split("\\.");
			partsCount = parts.length;
			if (partsCount > 1) {
				part = parts[partsCount - 1].toUpperCase();
				if (targetSuffixes.contains(part)) {
					subjectCount++;
					qSubjects.add(new FileStatSubject(file, fileSize, lastModifieTime));
				} else {
					rejectCount++;
					keepSuffixCount(part);
					qRejects.add(new FileStatReject(file, fileSize, lastModifieTime, FileStat.NOT_ON_LIST));
				} // if
			} // if - only process files with suffixes
			return FileVisitResult.CONTINUE;
		}// FileVisitResult

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			qRejects.add(new FileStatReject(file, 0, null, FileStat.IO_EXCEPTION));
			return FileVisitResult.CONTINUE;
		}// FileVisitResult

		private void keepSuffixCount(String suffix) {

			Integer occurances = members.get(suffix);
			if (occurances == null) {
				members.put(suffix, 1);
				 excludeModel.addElement(suffix);
			} else {
				members.put(suffix, occurances + 1);
			} // if unique
		}// keepSuffixCount
	}// class MyWalker
}// class IdentifySubjects
