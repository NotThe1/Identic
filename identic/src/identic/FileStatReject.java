package identic;

import java.nio.file.Path;

public class FileStatReject extends FileStat{

	String reason;

	public FileStatReject(Path filePath, long fileSize, String fileTime,String reason) {
		super(filePath, fileSize, fileTime);
		this.reason = reason;
		// TODO Auto-generated constructor stub
	}//Constructor
	
	public String getReason(){
		return this.reason;
	}//getHashKey

}//class FileStatReject 
