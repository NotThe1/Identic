package identic;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CatalogItem implements Serializable{
	
	private static final long serialVersionUID = 1L;
	String entryName,entryDescription,entryStartDirectory;
	SubjectTableModel subjectTableModel;
	public static final String EMPTY_ITEM = "emptyItem";
	
	
	public CatalogItem() {
		this.entryName = EMPTY_ITEM;
	}//CatalogItem
	
	public CatalogItem(String entryName) {
		this.entryName = entryName;
	}//CatalogItem
	
	public CatalogItem(String entryName,String entryDescription	) {
		this.entryName = entryName;
		this.entryDescription = entryDescription;
	}//CatalogItem

	
	public CatalogItem(String entryName,String entryDescription,String entryStartDirectory,
			SubjectTableModel subjectTableModel	) {
		this.entryName = entryName;
		this.entryDescription = entryDescription;
		this.entryStartDirectory = entryStartDirectory;
		this.subjectTableModel = subjectTableModel;
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

	public SubjectTableModel getSubjectTableModel() {
		return subjectTableModel;
	}//getSubjectTableModel

	public void setSubjectTableModel(SubjectTableModel subjectTableModel) {
		this.subjectTableModel = subjectTableModel;
	}//setSubjectTableModel
	
	public String toString() {
		return entryName;
	}//toString

}//CatalogItem
