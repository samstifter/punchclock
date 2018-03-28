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
	private boolean started;
	private List<Session> sessions;
	private String newDirPath = "output";

	private Session currSession;

	/**
	 * Constructs a new TimeModel with an empty list, empty session, and a start
	 * time of 0.
	 */
	public TimeModel() {
		this.lastStart = 0;
		this.sessions = new ArrayList<Session>();
		this.currSession = new Session();
	}

	/**
	 * Get the current session time in milliseconds
	 *
	 * @return Total elapsed milliseconds of current session
	 */
	public long getCurrentSessionTime() {
		long totalTime = currSession.getTotalTime();
		if (started)
			totalTime += getTimeSinceLastStart();
		return totalTime;
	}

	/**
	 * Get the time of only the session currently used for timing
	 * 
	 * @return the elapsed time of the most recent session
	 */
	private long getTimeSinceLastStart() {
		return System.currentTimeMillis() - this.lastStart;
	}

	/**
	 * Starts the timer by recording the current time
	 */
	public boolean startTime() {
		if (!started) {
			this.lastStart = System.currentTimeMillis();
			this.started = true;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Stops the time and creates a time pair and adds it to the session
	 * 
	 * @return True if the time was stopped as a reslut of this call, false otherwise
	 */
	public boolean stopTime() {
		if (this.started) {
			try {
				this.currSession.getTimePairList().add(new TimePair(lastStart, System.currentTimeMillis()));
				this.lastStart = 0;
				this.started = false;
			} catch (IllegalArgumentException e) {
				return false; // If the end time is before the start time, a
								// timepair cannot be created.
			}
		} else {
			return false; // A Timer that is not started can not be stopped
		}
		return true;
	}

	/**
	 * Resets the TimeModel, removing any TimePairs and "stopping" the time
	 */
	public void resetTime() {
		this.started = false;
		this.lastStart = 0;
		this.currSession = new Session();
	}

	/**
	 * Returns the current session object
	 * 
	 * @return List of time pairs.
	 */
	public Session getCurrentSession() {
		return this.currSession;
	}

	/**
	 * Adds a session to the list if the time pair list is not empty
	 * 
	 * @param session
	 */
	public void addSession(Session session) {
		if (!session.getTimePairList().isEmpty()) {
			this.sessions.add(session);
		}
	}

	/**
	 * Sets the name of the current session
	 * 
	 * @param name
	 *            New name for the session
	 */
	public void setCurrentSessionName(String name) {
		this.getCurrentSession().setSessionName(name);
	}
	
	public boolean isStarted(){
		return started;
	}

	/**
	 * Returns a list of the toString representation of each session (Start date
	 * and time, duration, and name if it has one)
	 * 
	 * @return a List of the Sessions represented by their toString
	 */
	public ArrayList<String> getFormattedSessionList() {
		ArrayList<String> list = new ArrayList<String>();
		for (Session session : this.sessions) {
			list.add(session.toString());
		}
		return list;
	}

	/**
	 * Loads the saved userdata csv. If the file is not present, it aborts, and
	 * if the file is formatted incorrectly, it aborts
	 * 
	 * @return
	 */
	public boolean loadSavedSessions() {
		File saveFile = new File("output/userdata.csv");

		Scanner key;
		try {
			key = new Scanner(saveFile);
		} catch (FileNotFoundException e) {
			return false;
		}
		List<String> sessions = new ArrayList<String>();
		while (key.hasNextLine()) {
			sessions.add(key.nextLine());
		}
		key.close();

		// saves times as timepairs, and add to a session.
		String[] times;
		for (String line : sessions) {
			Session session = new Session();
			times = line.split(",");
			if (!times[0].isEmpty()) {
				for (int i = 0; i < times.length - 1; i += 2) {
					try {
						TimePair tp = new TimePair(Long.parseLong(times[i]), Long.parseLong(times[i + 1]));
						session.addTimePair(tp);
					} catch (Exception e) {
						sessions.clear();
						return false;
					}

				}
				if (times.length % 2 != 0) {
					session.setSessionName(times[times.length - 1]);
				} else {
					session.setSessionName("");
				}
				this.addSession(session);
			}
		}
		return true;
	}

	/**
	 * Writes the sessions to a file in CSV format.
	 * 
	 * @return true if file is written, false otherwise.
	 */
	public boolean saveSessions() {
		File outDirNonReadable = new File("output");
		File outFileNonReadable = new File("output/userdata.csv");

		List<Session> sessionList = this.getSessions();

		// Make the directory if it doesn't exist.
		try {
			outDirNonReadable.mkdir();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		PrintWriter out;
		try {
			out = new PrintWriter(outFileNonReadable);
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
				out.printf("%d,%d,", tp.getStartTime(), tp.getEndTime());
			}
			// Print the session name, if it exists, at the end of the list
			if (session.getSessionName() != null) {
				out.printf("%s,", session.getSessionName());
			}
			// Add a new line at the end of the session.
			out.print("\n");

		}
		out.close();
		return true;
	}

	/**
	 * Writes the sessions to a human readable format.
	 * 
	 * @return true if the write happens, false otherwise
	 */
	public boolean writeToReadableFile() {
		File outDir = new File(newDirPath);
		File outFile = new File(newDirPath + "/UserLogs.csv");

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

	/**
	 * Writes the sessions from a specified date range to a human readable
	 * format
	 * 
	 * @param start
	 *            Start Date
	 * @param end
	 *            End Date
	 * @return true if the file is written, false otherwise
	 */
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
		String dir = newDirPath + "/range" + getNumberOfExportedRangeFiles() + ".csv";
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
	 * Get the number of range files that already exist
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

	/**
	 * Get a list containing Session objects
	 * 
	 * @return The list containing the Session objects
	 */
	public List<Session> getSessions() {
		return sessions;
	}

	/**
	 * Sets the TimeModel's session to sessions
	 * @param sessions
	 */
	public void setSessions(List<Session> sessions) {
		this.sessions = sessions;
	}

	/**
	 * Sets the new directory to export to
	 * 
	 * @param dirName
	 *            New Directory
	 */
	public void setDirectory(String dirName) {
		System.out.println(dirName);
		newDirPath = dirName;
	}

	/**
	 * Returns the TimeModel's last start
	 *
	 * @return The TimeModel's last start
	 */
	public long getLastStart() {
		return lastStart;
	}
}