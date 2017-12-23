package identic;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileStatSubject extends FileStat {

	String hashKey;

	public FileStatSubject(Path filePath, long fileSize, String fileTime, String hashKey) {
		super(filePath, fileSize, fileTime);
		this.hashKey = hashKey;
	}// Constructor
	
	public FileStatSubject ( Object[] catalogItem) {
		super(Paths.get((String)catalogItem[1] , (String)catalogItem[0]) , (long)catalogItem[2] ,(String)catalogItem[3]) ;
		this.hashKey = (String)catalogItem[4] ;
	}//Constructor
	
	public FileStatSubject(Path filePath, long fileSize, String fileTime) {
		super(filePath, fileSize, fileTime);
	}// Constructor
	
	public void setHashKey(String hashKey){
		this.hashKey = hashKey;
	}//setHashKey

	public String getHashKey() {
		return this.hashKey;
	}// getHashKey

}// class FileStatSubject
