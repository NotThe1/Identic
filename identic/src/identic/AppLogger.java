package identic;

import java.awt.Color;
import java.time.LocalDateTime;

import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class AppLogger {
	private StyledDocument docLog; // keep from failing if not set by using app

	private static AppLogger instance = new AppLogger();

	public static AppLogger getInstance() {
		return instance;
	}// getInstance

	private AppLogger() {
		setAttributes();
	}// Constructor

	public void setDoc(StyledDocument docLog) {
		this.docLog = docLog;
	}// setTextPane
	// ---------------------------------------------------------------------

	public void clear() {
		try {
			docLog.remove(0, docLog.getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // try
	}// clear
	
	public void addNL(int linesToSkip){	
		int lines = Math.max(linesToSkip, 0);
		lines = Math.min(lines, 15);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lines;i++){
			sb.append(NL);
		}//for
		insertListing(sb.toString(),null);
	}//addNL
	
	public void addNL(){
		insertListing(NL,null);
	}//addNL
	private void addMeta(SimpleAttributeSet attr,String... message){
		StringBuilder sb = new StringBuilder();
		for ( int i = 0; i<message.length;i++){
			sb.append(message[i]);
			sb.append(NL);
		}//for
		insertListing(sb.toString(), attr);
	}//addMeta
	
	public void addInfo(String... message){
		addMeta(attrBlack,message);
	}//addInfo

	public void addWarning(String... message){
		addMeta(attrBlue,message);
	}//addInfo

	public void addError(String... message){
		addMeta(attrRed,message);
	}//addInfo

	public void addSpecial(String... message){
		addMeta(attrTeal,message);
	}//addInfo
	
	public void addTimeStamp(){
		addMeta(attrSilver,LocalDateTime.now().toString());
	}//addTimeStamp
	
	public void addTimeStamp(String... message){
		addMeta(attrSilver,message);
		addMeta(attrSilver,LocalDateTime.now().toString());
	}//addTimeStamp

	
	private void insertListing(String str, SimpleAttributeSet attr) {
		try {
			docLog.insertString(docLog.getLength(), str, attr);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} // try
	}// insertSource

	// ----------------------------------------------------------------------
	private void setAttributes() {
		StyleConstants.setForeground(attrNavy, new Color(0, 0, 128));
		StyleConstants.setForeground(attrBlack, new Color(0, 0, 0));
		StyleConstants.setForeground(attrBlue, new Color(0, 0, 255));
		StyleConstants.setForeground(attrGreen, new Color(0, 128, 0));
		StyleConstants.setForeground(attrTeal, new Color(0, 128, 128));
		StyleConstants.setForeground(attrGray, new Color(128, 128, 128));
		StyleConstants.setForeground(attrSilver, new Color(192, 192, 192));
		StyleConstants.setForeground(attrRed, new Color(255, 0, 0));
		StyleConstants.setForeground(attrMaroon, new Color(128, 0, 0));
	}// setAttributes

	private SimpleAttributeSet attrBlack = new SimpleAttributeSet();
	private SimpleAttributeSet attrBlue = new SimpleAttributeSet();
	private SimpleAttributeSet attrGray = new SimpleAttributeSet();
	private SimpleAttributeSet attrGreen = new SimpleAttributeSet();
	private SimpleAttributeSet attrRed = new SimpleAttributeSet();
	private SimpleAttributeSet attrSilver = new SimpleAttributeSet();
	private SimpleAttributeSet attrNavy = new SimpleAttributeSet();
	private SimpleAttributeSet attrMaroon = new SimpleAttributeSet();
	private SimpleAttributeSet attrTeal = new SimpleAttributeSet();

	public static final Integer INFO = 0;
	public static final Integer WARNING = 1;
	public static final Integer ERROR = 2;
	public static final Integer SPECIAL = 4;

	private static final String EMPTY_STRING = "";
	private static final String NL = System.lineSeparator();

}// class AppLogger
