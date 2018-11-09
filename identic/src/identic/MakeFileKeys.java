package identic;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MakeFileKeys implements Runnable {
	private static final Object subjectsLock = new Object();
	private ConcurrentLinkedQueue<FileStat> qSubjects;
	private ConcurrentLinkedQueue<FileStat> qHashes;
	private static final int BUFF_SIZE = 1024;
	String algorithm = "SHA-1"; // 160 bits
	// String algorithm = "MD5"; // 128 bits
	// String algorithm = "SHA-256"; // 256 bits

	public MakeFileKeys(ConcurrentLinkedQueue<FileStat> qSubjects, ConcurrentLinkedQueue<FileStat> qHashes) {
		this.qSubjects = qSubjects;
		this.qHashes = qHashes;
	}// Constructor

	@Override
	public void run() {
		FileStat fileStat = null;
		String key = null;
		while (true) {
			synchronized (subjectsLock) {
				FileStat fs = qSubjects.peek();
				if (fs == null) {
					continue;
				} // if
				if (fs.equals(Identic.END_OF_SUBJECT)) {
					break;
				} // if
				fileStat = qSubjects.poll();
			} // Synchronized
			try {
				key = hashFile(fileStat.getFilePath(), algorithm);
				fileStat.setHashKey(key);
				qHashes.add(fileStat);
			} catch (HashGenerationException hashGenerationException) {
				hashGenerationException.printStackTrace();
			} // try

		} // while

	}// run

	private String hashFile(String filePath, String algorithm) throws HashGenerationException {
		try (FileInputStream inputStream = new FileInputStream(filePath)) {
			MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
			byte[] bytesBuffer = new byte[BUFF_SIZE];
			int bytesRead = -1;
			while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
				messageDigest.update(bytesBuffer, 0, bytesRead);
			} // while
			inputStream.close();
			byte[] hashedBytes = messageDigest.digest();
			StringBuilder sb = new StringBuilder();
			for (byte b : hashedBytes) {
				sb.append(String.format("%02X", b));
			} // for
			return sb.toString();
		} catch (NoSuchAlgorithmException | IOException interruptedException) {
			interruptedException.printStackTrace();
		} // try
		return null;
	}// hashFile

}// class MakeFileKeys
