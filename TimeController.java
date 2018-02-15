/**
 * The TimeController class is the Controller component of the MVC framework. 
 * It acts as a 
 * Read more about MVC archecture <a href='https://www.tutorialspoint.com/design_pattern/mvc_pattern.htm'>here</a>
 * @author A7
 *
 */
public class TimeController {
	private TimeModel timeModel;
	private TimeView timeView;
	
	public TimeController(TimeModel timeModel, TimeView timeView) {
		this.timeModel = timeModel;
		this.timeView = timeView;
	}
	
	/**
	 * Starts the timer by recording the current time
	 */
	public void startTime() {
		timeModel.startTime();
	}
	
	/**
	 * 
	 * @return True if the time was started (aka time was started previously), false otherwise
	 */
	public boolean stopTime() {
		return timeModel.stopTime();
	}
	
	/**
	 * Resets the TimeModel, removing any TimePairs and "stopping" the time
	 */
	public void resetTime() {
		timeModel.resetTime();
	}
	
	/**
	 * Get the total elapsed time over all sessions of this TimeModel
	 * @return Total Elapsed milliseconds between all starts/stops
	 */
	public long getTotalElapsedTime() {
		return timeModel.getTotalElapsedTime();
	}
	
	/**
	 * Get the time of only the most recent session
	 * @return the elapsed time of the most recent session
	 */
	public long getCurrentElapsedTime() {
		return timeModel.getCurrentElapsedTime();
	}
	
	/**
	 * Displays the total elapsed time to the console
	 * @param timeModel, the model you wish to display the time of
	 */
	public void displayElapsedTimeInSeconds(TimeModel timeModel) {
		timeView.displayElapsedTimeInSeconds(timeModel);
	}
}
