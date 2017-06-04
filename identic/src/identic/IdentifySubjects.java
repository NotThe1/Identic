package identic;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class IdentifySubjects implements Runnable {
	private LinkedBlockingQueue<Path> qSubjects;
//	private ArrayDeque<FileStatReject> qRejects;
	private LinkedBlockingQueue<FileStatReject> qRejects;
	private Path startPath;
	private ArrayList<String> targetSuffixes;
	private AppLogger appLogger = AppLogger.getInstance();

	private int fileCount;
	private int folderCount;
	private int subjectCount;
	private int rejectCount;

	@Override
	public void run() {
		fileCount = 0;
		folderCount = 0;
		subjectCount = 0;
		rejectCount = 0;
		MyWalker myWalker = new MyWalker();
		try {
			Files.walkFileTree(startPath, myWalker);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // try
		appLogger.addSpecial("folderCount = " + folderCount);
		appLogger.addSpecial("fileCount  = " + fileCount);
		appLogger.addSpecial("subjectCount = " + subjectCount);
		appLogger.addSpecial("rejectCount  = " + rejectCount);
		appLogger.addNL();
//		System.out.printf("myWalker %n");
	}// run

	public IdentifySubjects(LinkedBlockingQueue<Path> subjects, LinkedBlockingQueue<FileStatReject> qRejects, Path startPath,
			ArrayList<String> targetSuffixes) {
		this.qSubjects = subjects;
		subjects.clear();
		this.qRejects = qRejects;
		qRejects.clear();
		this.startPath = startPath;
		this.targetSuffixes = targetSuffixes;
	}// constructor

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
			FileTime  lastModifieTime;
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
					qSubjects.add(file);
				} else {
					rejectCount++;
					qRejects.add(new FileStatReject(file,fileSize,lastModifieTime,FileStat.NOT_ON_LIST));
				} // if
			} // if - only process files with suffixes
			return FileVisitResult.CONTINUE;
		}// FileVisitResult

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			qRejects.add(new FileStatReject(file,0,null,FileStat.IO_EXCEPTION));
			return FileVisitResult.CONTINUE;
		}// FileVisitResult
	}// class MyWalker
}// class IdentifySubjects
