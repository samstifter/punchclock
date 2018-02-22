import java.util.ArrayList;
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
	
	
}
