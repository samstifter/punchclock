import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimePair {
	private long start;
	private long end;
	
	public TimePair(long start, long end) {
		this.start = start;
		this.end = end;
	}
	
	/**
	 * 
	 * @return Time between 2 times in milliseconds
	 */
	public long getElapsedTime() {
        return end - start;
    }
	
	/**
	 * Gets the start time in milliseconds format
	 * 
	 * @return start time
	 */
	public long getStartTime(){
		return this.start;
	}
	
	/**
	 * Gets the end time in milliseconds format
	 *  
	 * @return end time
	 */
	public long getEndTime(){
		return this.end;
	}
	
	public String toString() {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Date dateStart = new Date(this.start);
		
		// get duration and convert to seconds
		int durationSeconds = (int)this.getElapsedTime() / 1000;
		int durationMinutes = durationSeconds / 60;
		int durationHours = durationMinutes / 60;
		
		String durationString = String.format("%d:%02d:%02d", durationHours, durationMinutes, durationSeconds);
		
		return dateFormat.format(dateStart) + " - " + durationString;
	}
}
