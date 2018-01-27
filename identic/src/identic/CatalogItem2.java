package identic;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class CatalogItem2 implements Serializable{
	
	private static final long serialVersionUID = 1L;
	String entryName,entryDescription,entryStartDirectory;
	ArrayList<FileStat> fileStats;
	public static final String EMPTY_ITEM = "emptyItem";
	
	
	public CatalogItem2() {
		this.entryName = EMPTY_ITEM;
	}//CatalogItem
	
	public CatalogItem2(String entryName) {
		this.entryName = entryName;
	}//CatalogItem
	
	public CatalogItem2(String entryName,String entryDescription	) {
		this.entryName = entryName;
		this.entryDescription = entryDescription;
	}//CatalogItem

	
	public CatalogItem2(String entryName,String entryDescription,String entryStartDirectory,
			ArrayList<FileStat> fileStats	) {
		this.entryName = entryName;
		this.entryDescription = entryDescription;
		this.entryStartDirectory = entryStartDirectory;
		this.fileStats = fileStats;
	}//CatalogItem

	public String getEntryName() {
		return entryName;
	}//getEntryName

	public String getEntryDescription() {
		return entryDescription;
	}//getEntryDescription

	public void setEntryDescription(String entryDescription) {
		this.entryDescription = entryDescription;
	}//setEntryDescription

	public String getEntryStartDirectory() {
		return entryStartDirectory;
	}//getEntryStartDirectory
	
	public Path getEntryStartPath() {
		return Paths.get(entryStartDirectory);
	}

	public void setEntryStartDirectory(String entryStartDirectory) {
		this.entryStartDirectory = entryStartDirectory;
	}//setEntryStartDirectory

	public ArrayList<FileStat> getFileStats() {
		return fileStats;
	}//getSubjectTableModel

	public void setFileStats(ArrayList<FileStat> fileStats) {
		this.fileStats= fileStats;
	}//setSubjectTableModel
	
	public String toString() {
		return entryName;
	}//toString

}//CatalogItem
