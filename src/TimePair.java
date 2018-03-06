import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 *
 */
public class TimePair {
	private long start;
	private long end;
	
	/**
	 * Constructs a time pair with the given start and end times.
	 * @param start
	 * 			start time
	 * @param end
	 * 			end time
	 */
	public TimePair(long start, long end) {
		this.start = start;
		this.end = end;
	}
	
	/**
	 * Calculates the elapsed time of a time pair by
	 * finding the difference between its end and start time
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

	public List<Integer> getDuration() {
		int durationSeconds = (int) this.getElapsedTime() / 1000;
		int durationMinutes = durationSeconds / 60;
		int durationHours = durationMinutes / 60;
		List<Integer> durations = new ArrayList<Integer>();
		durations.add(durationHours);
		durations.add(durationMinutes);
		durations.add(durationSeconds);
		return durations;
	}
	
	public String toString() {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Date dateStart = new Date(this.start);
		
		// get duration and convert to seconds
		List<Integer> durations = getDuration();
		
		String durationString = String.format("%d:%02d:%02d", durations.get(0), durations.get(1), durations.get(2));
		
		return dateFormat.format(dateStart) + " - " + durationString;
	}
}
