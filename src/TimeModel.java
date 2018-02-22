import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * The TimeModel class is the Model component of the MVC framework. 
 * It acts as a storage class for all data pertaining to the time 'structure'
 * Read more about MVC architecture <a href='https://www.tutorialspoint.com/design_pattern/mvc_pattern.htm'>here</a>
 * @author A7
 *
 */
public class TimeModel {
    private long lastStart;
    private boolean isStarted;
    //private List<TimePair> timePairList;
    private List<Session> sessions;
    //int currentSession;
    private Session currSession;
    
    public TimeModel() {
        lastStart = 0;
        sessions = new ArrayList<Session>();
        currSession = new Session();
        sessions.add(currSession);
        
    }

    /**
     *
     * @return Total Elapsed milliseconds between all starts/stops
     */
    public long getCurrentSessionTime() {
        long totalTime = 0;
        for(TimePair tp : currSession.getTimePairList()) {//Gets last timepairlist of sessions
            totalTime += tp.getElapsedTime();
        }
        if(lastStart != 0)
            totalTime += getCurrentElapsedTime();
        return totalTime;
    }

    /**
     * Get the time of only the most recent session
     * @return the elapsed time of the most recent session
     */
    public long getCurrentElapsedTime() {
        return System.currentTimeMillis() - lastStart;
    }

    /**
     * Starts the timer by recording the current time
     */
    public void startTime() {
        lastStart = System.currentTimeMillis();
        isStarted = true;
    }

    /**
     *
     * @return True if the time was started (aka time was started previously), false otherwise
     */
    public boolean stopTime() {
        if(isStarted) {
            currSession.getTimePairList().add(new TimePair(lastStart,System.currentTimeMillis()));
            lastStart = 0;
            isStarted = false;
        }
        else {
            return false; // A Timer that is not started can not be stopped
        }
        return true;
    }

    /**
     * Resets the TimeModel, removing any TimePairs and "stopping" the time
     */
    public void resetTime() {
        isStarted = false;
        lastStart = 0;
        //timePairList.clear();
        currSession = new Session();
        sessions.add(currSession);
    }
    
    /**
     * Returns the list of time pairs
     * 
     * @return List of time pairs.
     */
    public List<TimePair> getCurrentTimePairList(){
    	return currSession.getTimePairList();
    }
    
    /**
     * Adds a specified timepair to the current list
     * @param tp The timepair list to add to the list
     */
    public void addTimePair(TimePair tp) {
    	if(tp != null)
    		currSession.getTimePairList().add(tp);
    }
    
    public void addSession(Session session) {
    	this.sessions.add(session);
    }

	public ArrayList<String> getFormattedTimePairList() {
		ArrayList<String> list = new ArrayList<String>();
		
		for(int i = 0; i < this.sessions.size(); i++) {
			//if(i != this.sessions.size() - 1) {//ignore current session
				for(TimePair time : this.sessions.get(i).getTimePairList()) {
					list.add(time.toString());
				}
			//}
		}
		
		/*
		for (TimePair time : currSession.getTimePairList()) {
			list.add(time.toString());
		}
		list.add("Old Sessions:");
		*/
		
		return list;
	}
}