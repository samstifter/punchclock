import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * The TimeModel class is the Model component of the MVC framework. It acts as a
 * storage class for all data pertaining to the time 'structure' Read more about
 * MVC architecture <a href=
 * 'https://www.tutorialspoint.com/design_pattern/mvc_pattern.htm'>here</a>
 * 
 * @author A7
 *
 */
public class TimeModel {
	private long lastStart;
	private boolean isStarted;
	private List<Session> sessions;

	private Session currSession;

	/**
	 * 
	 */
	public TimeModel() {
		this.lastStart = 0;
		this.sessions = new ArrayList<Session>();
		loadSavedSessions();
		this.currSession = new Session();
	}

	/**
	 *
	 * @return Total Elapsed milliseconds of current session
	 */
	public long getCurrentSessionTime() {
		long totalTime = currSession.getTotalTime();
		if (isStarted)
			totalTime += getTimeSinceLastStart();
		return totalTime;
	}

	/**
	 * Get the time of only the most recent session
	 * 
	 * @return the elapsed time of the most recent session
	 */
	private long getTimeSinceLastStart() {
		return System.currentTimeMillis() - this.lastStart;
	}

	/**
	 * Starts the timer by recording the current time
	 */
	public void startTime(String sessionName) {
		this.lastStart = System.currentTimeMillis();
		this.isStarted = true;
		currSession.setSessionName(sessionName);
	}

	/**
	 *
	 * @return True if the time was started (aka time was started previously),
	 *         false otherwise
	 */
	public boolean stopTime() {
		if (this.isStarted) {
			this.currSession.getTimePairList().add(new TimePair(lastStart, System.currentTimeMillis()));
			this.lastStart = 0;
			this.isStarted = false;
		} else {
			return false; // A Timer that is not started can not be stopped
		}
		return true;
	}

	/**
	 * Resets the TimeModel, removing any TimePairs and "stopping" the time
	 */
	public void resetTime() {
		this.isStarted = false;
		this.lastStart = 0;
		sessions.add(this.currSession);
		this.currSession = new Session();
	}

	/**
	 * Returns the list of time pairs
	 * 
	 * @return List of time pairs.
	 */
	public Session getCurrentSession() {
		return this.currSession;
	}

	/**
	 * @param session
	 */
	public void addSession(Session session) {
		this.sessions.add(session);
	}

	/**
	 * @return
	 */
	public ArrayList<String> getFormattedSessionList() {
		ArrayList<String> list = new ArrayList<String>();
		for (Session session : this.sessions) {
			list.add(session.toString());
		}
		return list;
	}

	/**
	 * @return
	 */
	private boolean loadSavedSessions() {
		File saveFile = new File("output/userdata.csv");

		Scanner key;
		try {
			key = new Scanner(saveFile);
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException");
			return false;
		}
		List<String> sessions = new ArrayList<String>();
		while (key.hasNextLine()) {
			sessions.add(key.nextLine());
		}
		key.close();

		// Checks if all lines have 2 times
		String[] times;
		for (String session : sessions) {
			times = session.split(",");
			// DEBUG System.out.println(times[0].isEmpty());
			if ((times.length % 3) != 0 && !times[0].isEmpty()) {
				System.err.println("File Data Incorrect");
				return false;
			}
		}

		// saves times as timepairs, and add to a session.
		for (String line : sessions) {
			Session session = new Session();
			times = line.split(",");
			if (!times[0].isEmpty()) {
				for (int i = 1; i < times.length - 1; i += 2) {
					TimePair tp = new TimePair(Long.parseLong(times[i]), Long.parseLong(times[i + 1]));
					session.addTimePair(tp);
				}
				session.setSessionName(times[0]);
				this.addSession(session);
			}
		}
		return true;
	}

	/**
	 * Writes the time pairs of the current session to a file in CSV format.
	 * 
	 * @return true if file is written, false otherwise.
	 */
	public boolean saveSessions() {
		File outDir = new File("output");
		File outFile = new File("output/userdata.csv");

		List<Session> sessionList = this.getSessions();

		// Make the directory if it doesn't exist.
		try {
			outDir.mkdir();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		PrintWriter out;
		try {
			out = new PrintWriter(outFile);
		} catch (IOException e) {
			// If an exception with opening the file happens, return false.
			System.err.println("Could not write file");
			e.printStackTrace();
			return false;
		}

		for (Session session : sessionList) {

			// Go through each pair of times
			for (TimePair tp : session.getTimePairList()) {
				// Print both times, each one followed by a comma.
				out.printf("%s,%d,%d,", session.getSessionName(), tp.getStartTime(), tp.getEndTime());
			}
			// Add a new line at the end of the session.
			out.print("\n");

		}
		out.close();
		return true;
	}
	
	public boolean writeToReadableFile() {
		File outDir = new File("output");
		File outFile = new File("output/UserLogs.csv");

		// Make the directory if it doesn't exist.
		try {
			outDir.mkdir();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return false;
		}

		PrintWriter pw;
		try {
			pw = new PrintWriter(outFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Session Name:");
		sb.append(",");
		sb.append("Start Time:");
		sb.append(",");
		sb.append("End Time");
		sb.append(",");
		sb.append("Duration");
		sb.append("\n");

		for (Session session : this.sessions) {
			for (TimePair pair : session.getTimePairList()) {
				Date timeBegin = new Date(pair.getStartTime());
				Date timeEnd = new Date(pair.getEndTime());
				DateFormat dateFormat = new SimpleDateFormat("EEEE MMMM dd yyyy hh:mm:ss a");
				String startTime = dateFormat.format(timeBegin);
				String endTime = dateFormat.format(timeEnd);
				List<Integer> duration = pair.getDuration();
				String durationTime = String.format("%d:%02d:%02d", duration.get(0), duration.get(1), duration.get(2));
				sb.append(session.getSessionName());
				sb.append(",");
				sb.append(startTime);
				sb.append(",");
				sb.append(endTime);
				sb.append(",");
				sb.append(durationTime);
				sb.append("\n");
			}
		}
		pw.write(sb.toString());
		pw.close();
		try {
			Runtime.getRuntime().exec("explorer.exe /select," + outFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean writeToReadableFile(LocalDate start, LocalDate end) {
		// Set up start Millis
		Instant startDate = Instant.from(start.atStartOfDay(ZoneId.of("UTC")));
		long startMillis = startDate.getEpochSecond() * 1000;

		// Set up end Millis
		end = end.plusDays(1);
		Instant endDate = Instant.from(end.atStartOfDay(ZoneId.of("UTC")));
		long endMillis = endDate.getEpochSecond() * 1000;

		List<Session> sessions = this.getSessions();
		StringBuilder sb = new StringBuilder();
		sb.append("Session Name:");
		sb.append(",");
		sb.append("Start Time:");
		sb.append(",");
		sb.append("End Time");
		sb.append(",");
		sb.append("Duration");
		sb.append("\n");//
		for (Session session : sessions) {
			List<TimePair> pairs = session.getTimePairList();
			for (TimePair pair : pairs) {
				if ((pair.getStartTime() >= startMillis && pair.getStartTime() <= endMillis)
						|| (pair.getEndTime() >= startMillis && pair.getEndTime() <= endMillis)) {
					Date timeBegin = new Date(pair.getStartTime());
					Date timeEnd = new Date(pair.getEndTime());
					DateFormat dateFormat = new SimpleDateFormat("EEEE MMMM dd yyyy hh:mm:ss a");
					String startTime = dateFormat.format(timeBegin);
					String endTime = dateFormat.format(timeEnd);
					List<Integer> duration = pair.getDuration();
					String durationTime = String.format("%d:%02d:%02d", duration.get(0), duration.get(1),
							duration.get(2));
					sb.append(session.getSessionName());
					sb.append(",");
					sb.append(startTime);
					sb.append(",");
					sb.append(endTime);
					sb.append(",");
					sb.append(durationTime);
					sb.append("\n");
				}
			}
		}
		String dir = "output/range" + getNumberOfExportedRangeFiles() + ".csv";
		File exportedFile = new File(dir);
		try {
			PrintWriter pw = new PrintWriter(exportedFile);
			pw.write(sb.toString());
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			Runtime.getRuntime().exec("explorer.exe /select," + exportedFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	/**
	 *
	 * @return number of range files
	 */
	public int getNumberOfExportedRangeFiles() {
		File out = new File("output");
		File[] filesInOutput = out.listFiles();
		int count = 0;
		for (File f : filesInOutput) {
			if (f.getName().contains("range")) {
				count++;
			}
		}
		return ++count;
	}

	public List<Session> getSessions() {
		return sessions;
	}
}