package identic;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileStat {
	private Path filePath;
	private long fileSize;
	private String fileTime;
	private String	hashKey;
	
	public FileStat(Path filePath, long fileSize, String fileTime){
		this.filePath = filePath;
		this.fileSize = fileSize;
		this.fileTime = fileTime;
	}//Constructor
	
	public FileStat(Path filePath, long fileSize, String fileTime, String hashKey){
		this.filePath = filePath;
		this.fileSize = fileSize;
		this.fileTime = fileTime;
		this.hashKey= hashKey;
	}//Constructor
	
	public FileStat ( Object[] catalogItem) {
		this.filePath = Paths.get((String)catalogItem[1] , (String)catalogItem[0]);
		this.fileSize = (long)catalogItem[2];
		this.fileTime = (String)catalogItem[3];
		this.hashKey= (String)catalogItem[4] ;

//		super(Paths.get((String)catalogItem[1] , (String)catalogItem[0]) , (long)catalogItem[2] ,(String)catalogItem[3]) ;
//		this.hashKey = (String)catalogItem[4] ;
	}//Constructor
	

	
	public Path getFilePath(){
		return filePath;
	}//getFilePath
	
	public String getFilePathString(){
		return filePath.toString();
	}//getFilePathString
	
	public long getFileSize(){
		return fileSize;
	}//getFileSize
	
	public String getFileTime(){
		return fileTime;
	}//getFileTime
	
	public String getHashKey(){
		return hashKey;
	}//getHashKey
	
	public void setHashKey(String hashKey){
		this.hashKey = hashKey;
	}//setHashKey


	public String getFileName(){
		return filePath== null?"":filePath.getFileName().toString();
	}//getFileName
	
	public String getDirectory(){
		return filePath== null?"":filePath.getParent().toString();
	}//getDirectory
	
	public Path getParent(){
		return filePath.getParent();
	}//getParent
	
//	public String getModifiedDate(){
//		return fileTime.toString();
//	}//getModifiedDate
	
	public static final String NOT_ON_LIST = "Not on list";
	public static final String HIDDEN= "Hidden File";
	public static final String IO_EXCEPTION= "IO Exception";

}//class FileStat
