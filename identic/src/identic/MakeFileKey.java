package identic;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingQueue;

public class MakeFileKey implements Runnable {
	private static int bufSize = 1024;
	String algorithm = "SHA-256";
	private LinkedBlockingQueue<Path> qSubjects = new LinkedBlockingQueue<Path>();
	private Thread priorThread;
	private AppLogger appLogger = AppLogger.getInstance();

	public MakeFileKey(Thread priorThread, LinkedBlockingQueue<Path> subjects) {
		this.priorThread = priorThread;
		this.qSubjects = subjects;

	}// Constructor

	@Override
	public void run() {
		PathKey pathKey;
		
		int count = 0;
		Path path = null;
		String fileName = null;
		String key = null;
		while (true) {
			try {
				path = qSubjects.remove();
				fileName = path.toString();
				count++;
				try {
					key = hashFile(fileName, algorithm);
					pathKey = new PathKey(key,fileName);
					appLogger.addInfo(pathKey.key + " - " + pathKey.filePath);
				} catch (HashGenerationException e) {
					appLogger.addError("HashGenerationError",fileName);
					e.printStackTrace();
				}//
			} catch (NoSuchElementException ex) {
				if (priorThread.getState().equals(Thread.State.TERMINATED)) {
					appLogger.addSpecial("From MakeFileKey count = " + count);
					return;
				} // if - done ?
			} // try
		} // while
	}// run

	private static String hashFile(String file, String algorithm) throws HashGenerationException {
		try (FileInputStream inputStream = new FileInputStream(file)) {
			MessageDigest digest = MessageDigest.getInstance(algorithm);

			byte[] bytesBuffer = new byte[bufSize];
			int bytesRead = -1;

			while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
				digest.update(bytesBuffer, 0, bytesRead);
			} // while

			byte[] hashedBytes = digest.digest();

			return convertByteArrayToHexString(hashedBytes);
		} catch (NoSuchAlgorithmException | IOException ex) {
			throw new HashGenerationException("Could not generate hash from file", ex);
		} // try
	}// hashFile

	private static String convertByteArrayToHexString(byte[] arrayBytes) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < arrayBytes.length; i++) {
			stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16).substring(1));
		} // for
		return stringBuffer.toString();
	}// convertByteArrayToHexString

}// class ShowSubjects
