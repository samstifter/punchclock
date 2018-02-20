
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
}
