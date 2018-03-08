import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class defines a Session. A session holds pairs of time pairs.
 * 
 */
public class Session {
	private String sessionName;
	private List<TimePair> timePairList;

	/**
	 * A session object holding a list of time pairs.
	 */
	public Session() {
		this.timePairList = new ArrayList<TimePair>();

	}

	/**
	 * A session object holding a list of time pairs with a given name.
	 * 
	 * @param name
	 *            The name given to the session
	 */
	public Session(String name) {
		this.sessionName = name;
		this.timePairList = new ArrayList<TimePair>();
	}

	/**
	 * Returns the name of the session.
	 * 
	 * @return Session name
	 */
	public String getSessionName() {
		return this.sessionName;
	}

	/**
	 * Sets the name of the session.
	 * 
	 * @param sessionName
	 *            name to set session
	 */
	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	/**
	 * Returns the sessions list of time pairs.
	 * 
	 * @return timePair
	 */
	public List<TimePair> getTimePairList() {
		return timePairList;
	}

	/**
	 * Sets the sessions time pair list.
	 * 
	 * @param timePairList
	 */
	public void setTimePairList(List<TimePair> timePairList) {
		this.timePairList = timePairList;
	}

	/**
	 * 
	 * @param tp
	 */
	public void addTimePair(TimePair tp) {
		if (tp != null)
			timePairList.add(tp);
	}

	/**
	 * Gets the total elapsed time of all time pairs in a session.
	 * 
	 * @return Total elapsed time in session.
	 */
	public long getTotalTime() {
		long time = 0;
		for (TimePair tp : timePairList) {
			time += tp.getElapsedTime();
		}
		return time;
	}

	public String toString() {
		if (this.getTimePairList().isEmpty()) {
			return "Empty session";
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dateStart = new Date(this.timePairList.get(0).getStartTime());

		// get duration and convert to seconds
		int seconds = (int) this.getTotalTime() / 1000;
		int durationSeconds = seconds % 60;
		int durationMinutes = (seconds / 60) % 60;
		int durationHours = (seconds / 60) / 60;

		String durationString = String.format("%d:%02d:%02d", durationHours, durationMinutes, durationSeconds);

		return dateFormat.format(dateStart) + " - " + durationString;
	}

	public List<Integer> getDuration() {
		List<Integer> durations = new ArrayList<Integer>();
		int totalSeconds = (int) this.getTotalTime() / 1000;
		int durationSeconds = totalSeconds % 60;
		int durationMinutes = (totalSeconds / 60) % 60;
		int durationHours = (totalSeconds / 60) / 60;
		durations.add(durationHours);
		durations.add(durationMinutes);
		durations.add(durationSeconds);
		return durations;
	}
}
