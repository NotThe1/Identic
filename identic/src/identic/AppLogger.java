package identic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
			e.printStackTrace();
		} // try
	}// clear

	public void addNL(int linesToSkip) {
		int lines = Math.max(linesToSkip, 0);
		lines = Math.min(lines, 15);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lines; i++) {
			sb.append(NL);
		} // for
		insertListing(sb.toString(), null);
	}// addNL

	public void addNL() {
		insertListing(NL, null);
	}// addNL

	private void addMeta(SimpleAttributeSet attr, String... message) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < message.length; i++) {
			sb.append(message[i]);
			sb.append(NL);
		} // for
		insertListing(sb.toString(), attr);
	}// addMeta

	public void addInfo(String... message) {
		addMeta(attrBlack, message);
	}// addInfo

	public void addWarning(String... message) {
		addMeta(attrBlue, message);
	}// addInfo

	public void addError(String... message) {
		addMeta(attrRed, message);
	}// addInfo

	public void addSpecial(String... message) {
		addMeta(attrTeal, message);
	}// addInfo
	
//----------Time-------------------------------	
	
	public Date addTimeStamp() {
		Date now = new Date();
		addMeta(attrSilver,now.toString());
		return now;
	}//

//	public void addTimeStamp() {
//		addMeta(attrSilver, LocalDateTime.now().toString());
//	}// addTimeStamp

	public Date addTimeStamp(String message) {
		Date now = new Date();
		insertListing(message + " " + now.toString() + System.lineSeparator(), attrSilver);
		return now;
	}// addTimeStamp


//	public void addTimeStamp(String message) {
//		insertListing(message + " " + LocalDateTime.now().toString() + System.lineSeparator(), attrSilver);
//	}// addTimeStamp
	
	public Date addElapsedTime(Date startTime) {
		Date endTime = new Date();
		String message  = getElapsedTimeToString( startTime,  endTime);
		insertListing(message + " " + endTime.toString() + System.lineSeparator(), attrSilver);
		return endTime;
	}//addElapsedTime
	
	public Date addElapsedTime(Date startTime, String message) {
		Date endTime = new Date();
		insertListing(message + " " + endTime.toString() + System.lineSeparator(), attrSilver);
		String elapsedTime  = getElapsedTimeToString( startTime,  endTime);
		insertListing(elapsedTime +  System.lineSeparator(), attrSilver);
		return endTime;
	}//addElapsedTime
	
	private static String getElapsedTimeToString(Date startDate, Date endDate) {
		String result = "";
		Map<TimeUnit, Long> times = getElapsedTime(startDate,endDate);
		List<TimeUnit> timeUnits = new ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit.class));
		long duration;
		boolean nonZeroFlag = false;
		
		for(TimeUnit timeUnit:timeUnits) {
			duration = times.get(timeUnit);
			if (duration == 0) {
				if (!nonZeroFlag) {
					continue;
				}//if skip zero value
			} else {
				nonZeroFlag = true;
			}// outer if
			result = String.format("%s = %,d, %s ", timeUnit.toString(),times.get(timeUnit),result);					
		}// for time Unit

		return result;
	}//getElapsedTimeToString
	
	public static Map<TimeUnit, Long> getElapsedTime(Date startDate, Date endDate) {
		Map<TimeUnit, Long> result = new HashMap<TimeUnit, Long>();
		long diffInMilliseconds = endDate.getTime() - startDate.getTime();
		List<TimeUnit> timeUnits = new ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit.class));
		Collections.reverse(timeUnits); // Days to Nanoseconds
		long difference,milliSecondsLeftPerUnit;
		for (TimeUnit timeUnit : timeUnits) {
			difference = timeUnit.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
			result.put(timeUnit, difference);
			milliSecondsLeftPerUnit = timeUnit.toMillis(difference);
			diffInMilliseconds -= milliSecondsLeftPerUnit;
		} // for each time unit
		return result;
	}// getElapsedTime	
	
	//----------Time-------------------------------	
		
	
	
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

//	private static final String EMPTY_STRING = "";
	private static final String NL = System.lineSeparator();

}// class AppLogger
