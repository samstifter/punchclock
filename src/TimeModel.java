import java.util.ArrayList;
import java.util.List;

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
	 * Returns the TimeModel's last start
	 *
	 * @return The TimeModel's last start
	 */
	public long getLastStart() {
		return lastStart;
	}

	/**
	 * Sets the last start. Used for testing
	 * @param lastStart the new last start time
	 */
	public void setLastStart(long lastStart) {
		this.lastStart = lastStart;
	}

	/**
	 * Sets the current session to currSession. Used for testing,
	 * but could be used in the future for convenience
	 * @param currSession
	 */
	public void setCurrentSession(Session currSession) {
		this.currSession = currSession;
	}
	
	  /**
     * Find and return a session by its name if it exists
     * @param sessionName
     * @return Session object with name matching sessionName if exists, null otherwise
     */
    public Session getSessionByName(String sessionName) {
    	for(Session session : this.sessions) {
    		if(session.getSessionName().equals(sessionName)) {
    			return session;
    		}
    	}
    	return null;
    }
}