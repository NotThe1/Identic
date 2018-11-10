package identic;

import java.io.File;
import java.io.FileFilter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdentifyFiles extends RecursiveAction {
	
	private static final long serialVersionUID = 1L;

	private File directory;

	private   ConcurrentLinkedQueue<FileStat> qSubjects;
	private   ConcurrentLinkedQueue<FileStatReject> qRejects;
	
	Pattern patternSubjects;
	Pattern patternFileType = Pattern.compile("\\.([^.]+$)");
//	Matcher matcher;


//	private FileFilter getDirectories = new FileFilter() {
//		@Override
//		public boolean accept(File fileContent) {
//			return fileContent.isDirectory();
//		}// accept
//	};

//	xFileFilter getFiles = new FileFilter() {
//		@Override
//		public boolean accept(File fileContent) {
//			return fileContent.isFile();
//		}// accept
//	};

	public IdentifyFiles(File directory,Pattern patternSubjects, ConcurrentLinkedQueue<FileStat> qSubjects,
			ConcurrentLinkedQueue<FileStatReject> qRejects) {
		this.directory = directory;
		this.patternSubjects=patternSubjects;
		this.qSubjects=qSubjects;
		this.qRejects=qRejects;
	}// Constructor

	@Override
	protected void compute() {
//		System.out.printf("[IdentifyFiles.compute]%s: %s%n", Thread.currentThread().getName(), directory);
		File[] directories = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File fileContent) {
				return fileContent.isDirectory();
			}// accept
		});
		
		
		if (directories != null) {
			for (File directory : directories) {
				IdentifyFiles identifyFiles = new IdentifyFiles(directory,patternSubjects,qSubjects,qRejects);
				identifyFiles.fork();
			} // for each
		} // if
		File[] files = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File fileContent) {
				return fileContent.isFile();
			}// accept
		});
		if (files != null) {
			for (File file : files) {
				processFile(file);
			} // for each
		} // if
			// TODO Auto-generated method stub
	}// compute
	
	private void processFile(File file) {
//				System.out.printf("[IdentifyFiles.compute]file: %s: %s%n", Thread.currentThread().getName(), file);
		Date date = new Date(file.lastModified());
		Format myFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
		String lastModifieTime = myFormat.format(date);
		long fileSize = file.length();
		String fileName = file.getName();
		String fileFullPath = file.getAbsolutePath();
		Matcher matcher = patternFileType.matcher(fileName);
		String fileType = matcher.find() ? matcher.group(1).toLowerCase() : NONE;
		
		matcher = patternSubjects.matcher(fileType);
		if (matcher.matches()) {// find
			qSubjects.add(new FileStat(fileFullPath, fileSize, lastModifieTime));
		} else {
			qRejects.add(new FileStatReject(fileFullPath, fileSize, lastModifieTime, FileStat.NOT_ON_LIST));
		} // if - match	
	}//processFile
	
	private static final String NONE = "<none>";

}// class IdentifyFiles
