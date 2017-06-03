package identic;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

public class FileStatSubject extends FileStat {

	String hashKey;

//	public FileStatSubject(Path filePath, long fileSize, FileTime fileTime, String hashKey) {
//		super(filePath, fileSize, fileTime);
//		this.hashKey = hashKey;
//	}// Constructor
	public FileStatSubject(Path filePath, long fileSize, FileTime fileTime) {
		super(filePath, fileSize, fileTime);
	}// Constructor
	
	public void setHashKey(){
		this.hashKey = hashKey;
	}//setHashKey

	public String getHashKey() {
		return this.hashKey;
	}// getHashKey

}// class FileStatSubject
