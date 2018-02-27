import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Session {
	private String sessionName;
	private List<TimePair> timePairList;
	
	public Session () {
		timePairList = new ArrayList<TimePair>();
	}
	
	public Session(String name) {
		sessionName = name;
		timePairList = new ArrayList<TimePair>();
	}
	
	public String getSessionName() {
		return sessionName;
	}
	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}
	public List<TimePair> getTimePairList() {
		return timePairList;
	}
	public void setTimePairList(List<TimePair> timePairList) {
		this.timePairList = timePairList;
	}
	
	public void addTimePair(TimePair tp) {
    	if(tp != null)
    		timePairList.add(tp);
    }

	public long getTotalTime() {
		long time = 0;
		for(TimePair tp : timePairList) {
			time += tp.getElapsedTime();
		}
		return time;
	}
	
	public String toString() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dateStart = new Date(this.timePairList.get(0).getStartTime());
	
		// get duration and convert to seconds
		int durationSeconds = (int)this.getTotalTime() / 1000;
		int durationMinutes = durationSeconds / 60;
		int durationHours = durationMinutes / 60;
		
		String durationString = String.format("%d:%02d:%02d", durationHours, durationMinutes, durationSeconds);
		
		return dateFormat.format(dateStart) + " - " + durationString;
		}
}
