
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
}
