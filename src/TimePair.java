import java.text.DateFormat;
import java.text.ParseException;
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
	 * @return Time between 2 times in seconds
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
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		
		Date dateStart = new Date(this.start);
		Date dateEnd = new Date(this.end);
		
		/*
		try {
			dateStart = dateFormat.parse(Long.toString(this.start));
			dateEnd = dateFormat.parse(Long.toString(this.end));
		} catch (ParseException e) {
			System.err.println("Failure to parse a timepair");
			e.printStackTrace();
		}
		*/
		
		
		return dateFormat.format(dateStart) + " - " + dateFormat.format(dateEnd);
	}
}
