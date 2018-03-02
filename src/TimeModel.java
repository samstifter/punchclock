import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
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
        if(isStarted)
            totalTime += getTimeSinceLastStart();
        return totalTime;
    }

    /**
     * Get the time of only the most recent session
     * @return the elapsed time of the most recent session
     */
    private long getTimeSinceLastStart() {
        return System.currentTimeMillis() - this.lastStart;
    }

    /**
     * Starts the timer by recording the current time
     */
    public void startTime() {
    	this.lastStart = System.currentTimeMillis();
    	this.isStarted = true;
    }

    /**
     *
     * @return True if the time was started (aka time was started previously), false otherwise
     */
    public boolean stopTime() {
        if(this.isStarted) {
        	this.currSession.getTimePairList().add(new TimePair(lastStart,System.currentTimeMillis()));
            this.lastStart = 0;
            this.isStarted = false;
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
    public Session getCurrentSession(){
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
		for(Session session : this.sessions) {
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
			System.out.println(times[0].isEmpty());
			if ((times.length % 2) != 0 && !times[0].isEmpty()) {
				System.err.println("File Data Incorrect");
				return false;
			}
		}

		// saves times as timepairs, and add to a session.
		for (String line : sessions) {
			Session session = new Session();
			times = line.split(",");
			if (!times[0].isEmpty()) {
				for (int i = 0; i < times.length - 1; i += 2) {
					TimePair tp = new TimePair(Long.parseLong(times[i]), Long.parseLong(times[i + 1]));
					session.addTimePair(tp);
				}
				this.addSession(session);
			}
		}
		return true;
	}
	
	public boolean writeToReadableFile() throws FileNotFoundException {
		File outDir = new File("output");
		File outFile = new File("output/UserLogs.csv");

		// Make the directory if it doesn't exist.
		try {
			outDir.mkdir();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
		PrintWriter pw = new PrintWriter(outFile);
		StringBuilder sb = new StringBuilder();
		sb.append("Start Time:");
		sb.append(",");
		sb.append("End Time");
		sb.append(",");
		sb.append("Duration");
		sb.append("\n");

		for(Session session: this.sessions){
			for (TimePair pair : session.getTimePairList()) {
				Date timeBegin = new Date(pair.getStartTime());
				Date timeEnd = new Date(pair.getEndTime());
				DateFormat dateFormat = new SimpleDateFormat("EEEE MMMM dd yyyy hh:mm:ss a");
				String startTime = dateFormat.format(timeBegin);
				String endTime = dateFormat.format(timeEnd);
				List<Integer> duration = pair.getDuration();
				String durationTime = String.format("%d:%02d:%02d", duration.get(0), duration.get(1), duration.get(2));
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

		return true;
	}

    public List<Session> getSessions() {
		return sessions;
	}
}