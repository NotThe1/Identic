package identic;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

public class FileStat {
	private Path filePath;
	private long fileSize;
	private FileTime fileTime;
	private String	hashKey;
	
	public FileStat(Path filePath, long fileSize, FileTime fileTime){
		this.filePath = filePath;
		this.fileSize = fileSize;
		this.fileTime = fileTime;
	}//Constructor
	
	public Path getFilePath(){
		return filePath;
	}//getFileName
	
	public String getFilePathString(){
		return filePath.toString();
	}//getFileName
	
	public long getFileSize(){
		return fileSize;
	}//getFileName
	
	public FileTime getFileTime(){
		return fileTime;
	}//getFileName
	
	public String getHashKey(){
		return hashKey;
	}//getFileName
	
	public String getFileName(){
		return filePath.getFileName().toString();
	}//getFileName
	
	public String getDirectory(){
		return filePath.getParent().toString();
	}//getDirectory
	
	public Path getParent(){
		return filePath.getParent();
	}//getParent
	
	public String getModifiedDate(){
		return fileTime.toString();
	}//String
	
	public static final String NOT_ON_LIST = "Not on list";
	public static final String HIDDEN= "Hidden File";
	public static final String IO_EXCEPTION= "IO Exception";

}//class FileStat
