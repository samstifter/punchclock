import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The TimeController class is the Controller component of the MVC framework. It
 * acts as a Read more about MVC archecture <a href=
 * 'https://www.tutorialspoint.com/design_pattern/mvc_pattern.htm'>here</a>
 * 
 * @author A7
 *
 */
public class TimeController {
	private TimeModel timeModel;
	private TimeView timeView;

	/**
	 * A controller for the PunchClock
	 * 
	 * @param timeModel
	 *            model to be used
	 * @param timeView
	 *            view to be used
	 */
	public TimeController(TimeModel timeModel, TimeView timeView) {
		this.timeModel = timeModel;
		this.timeView = timeView;
	}

	/**
	 * Starts the timer by recording the current time
	 */
	public boolean startTime() {
		return timeModel.startTime();
	}

	/**
	 * Stop the timer
	 * 
	 * @return True if the time was stopped as a result of this call, false otherwise
	 */
	public boolean stopTime() {
		return timeModel.stopTime();
	}

	/**
	 * Sets the name of the current session
	 * 
	 * @param sessionName
	 *            Session name
	 */
	public void setSessionName(String sessionName) {
		this.timeModel.setCurrentSessionName(sessionName);
	}

	/**
	 * Ends the current session by stopping the time, adding the current session
	 * to the list, and resetting time with a new session.
	 */
	public void endCurrentSession() {
		timeModel.stopTime();
		timeModel.addSession(timeModel.getCurrentSession());
		timeModel.resetTime();
	}

	/**
	 * Get the current session time in milliseconds
	 * 
	 * @return Total elapsed milliseconds of current session
	 */
	public long getCurrentSessionElapsedTime() {
		return timeModel.getCurrentSessionTime();
	}

	/**
	 * Displays the total elapsed time to the console
	 * 
	 * @param timeModel,
	 *            the model you wish to display the time of
	 */
	public void displayElapsedTimeInSeconds(TimeModel timeModel) {
		timeView.displayElapsedTimeInSeconds(timeModel);
	}

	/**
	 * Edits the session stored in the session list at the specified index to
	 * have the new duration by creating a new TimePair with the same start time
	 * and an end time at the new duration after the start time
	 * 
	 * @param index
	 *            Session index to edit
	 * @param newDurationMillis
	 *            new duration
	 * @return true if edit was successful, false otherwise.
	 */
	public boolean editSession(int index, long newDurationMillis) {
		try {
			timeModel.getSessions().get(index).getTimePairList();
			List<TimePair> list = new ArrayList<TimePair>();
			long startTime = timeModel.getSessions().get(index).getTimePairList().get(0).getStartTime();
			long endTime = startTime + newDurationMillis;
			try {
				list.add(new TimePair(startTime, endTime));
				timeModel.getSessions().get(index).setTimePairList(list);
			} catch (IllegalArgumentException e) {
				return false; // the end time is before the start time
			}
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
		return true;
	}

	/**
	 * Delete a session at the index
	 * 
	 * @param index
	 *            Index to delete
	 * @return true if successful, false otherwise
	 */
	public boolean deleteSession(int index) {
		try {
			timeModel.getSessions().remove(index);
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
		return true;
	}

	/**
	 * Writes the sessions to a file in CSV format.
	 * 
	 * @return true if file is written, false otherwise.
	 */
	public boolean saveSessions() {
		return timeModel.saveSessions();
	}

	/**
	 * Writes the sessions to a human readable format.
	 * 
	 * @return true if the write happens, false otherwise
	 */
	public boolean writeToReadableFile() {
		return timeModel.writeToReadableFile();
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
		return timeModel.writeToReadableFile(start, end);
	}
	
	/**
	 * Get a list containing Session objects
	 * 
	 * @return The list containing the Session objects
	 */
	public List<Session> getSessions() {
		return timeModel.getSessions();
	}
	
	/**
     * Find and return a session by its name if it exists
     * @param sessionName
     * @return Session object with name matching sessionName if exists, null otherwise
     */
	 public Session getSessionByName(String sessionName) { 
		 return timeModel.getSessionByName(sessionName);
	 }

}
