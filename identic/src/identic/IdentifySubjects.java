package identic;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;


public class IdentifySubjects implements Runnable {
	private ArrayDeque<Path> subjects;
	private ArrayDeque<Path> rejects;
	private Path startPath;
	private ArrayList<String> targetSuffixes;
	
	@Override
	public void run() {
		MyWalker myWalker = new MyWalker();
		try {
			Files.walkFileTree(startPath, myWalker);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // try
		System.out.printf("myWalker %n");
	}//run
		
	public IdentifySubjects(ArrayDeque<Path> subjects,ArrayDeque<Path> rejects,Path startPath,ArrayList<String> targetSuffixes){
		this.subjects= subjects;
		subjects.clear();
		this.rejects=rejects;
		rejects.clear();
		this.startPath=startPath;
		this.targetSuffixes= targetSuffixes;
	}//constructor

	class MyWalker implements FileVisitor<Path> {

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			// TODO Auto-generated method stub
			return FileVisitResult.CONTINUE;
		}//FileVisitResult

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//			txtLog.append("                    - " + dir.toString() + System.lineSeparator());
			return FileVisitResult.CONTINUE;
		}//FileVisitResult

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			String fileName = file.getFileName().toString();
			int partsCount;
			String part = null;
			String[] parts = fileName.split("\\.");
			partsCount = parts.length;
			if (partsCount > 1) {
				part = parts[partsCount-1].toUpperCase();
				if ( targetSuffixes.contains(part)){
					subjects.add(file);
				}else{
					rejects.add(file);	
				}//if
			} // if
			return FileVisitResult.CONTINUE;
		}//FileVisitResult

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			rejects.add(file);	
			return FileVisitResult.CONTINUE;
		}//FileVisitResult
	}//class MyWalker
}//class IdentifySubjects
