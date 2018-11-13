package identic;

import java.io.File;
import java.io.FileFilter;
import java.util.AbstractMap;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileTypeCensus extends RecursiveAction {
	private static final long serialVersionUID = 1L;
	
	private File directory;
	private AbstractMap<String, Integer> fileTypes;

	
	
	public FileTypeCensus(File directory,AbstractMap<String, Integer> fileTypes) {
		this.directory=directory;
		this.fileTypes=fileTypes;
	}//Constructor

	@Override
	protected void compute() {
		File[] directories = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File directoryContent) {
				return directoryContent.isDirectory();
			}//accept
		});
		
		if (directories != null) {
			for(File directory:directories) {
				FileTypeCensus fileTypeCensus = new FileTypeCensus(directory,fileTypes);
				fileTypeCensus.fork();
			}//for
		}//if
		
		File[] files = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File directoryContent) {
				return directoryContent.isFile();
			}//accept
		});
		if(files!=null) {
			for(File file:files) {
				Matcher matcherFiletype = patternFileType.matcher(file.getName());
				if (matcherFiletype.find()) {
					String fileType = matcherFiletype.group(1).toLowerCase();
					synchronized(this){
						Integer oldValue = fileTypes.put(fileType, 1);
						if (oldValue !=null) {
							fileTypes.put(fileType, oldValue+1);
						}//if
					}//synchronized(this)
				}// if file type
			}//for each file
		}//if

	}//compute
	private static final Pattern patternFileType = Pattern.compile("\\.([^.]+$)");

}//class FileTypeCensus
