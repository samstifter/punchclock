
/**
 * The TimeView class is the View component of the MVC framework. 
 * It should only contain methods that pertain to the displaying of info
 * The model should be be a parameter of the methods that require it and a copy of the model should NOT be kept as an instance var
 * This may not be needed as the project progresses or may just be updated to display more tastefully
 * Read more about MVC archecture <a href='https://www.tutorialspoint.com/design_pattern/mvc_pattern.htm'>here</a>
 * @author A7
 *
 */
public class TimeView {

	/**
	 * Displays the total elapsed time to the console
	 * @param timeModel, the model you wish to display the time of
	 */
	public void displayElapsedTimeInSeconds(TimeModel timeModel) {
		double time = timeModel.getTotalElapsedTime() / 1000.0;
		//System.out.println(time);
		System.out.printf("Total Elapsed Time: %4.2f Seconds\n", time);
	}
}
