package net.paxcel.labs.mxexecutable.jmx;

import java.util.Calendar;

/**
 * MBean for the executor, this is used to manage all tasks as a whole.
 * 
 * @author Kuldeep
 * 
 */
public interface ExecutorMXBean {

	/**
	 * Stop all tasks (no force)
	 */
	public void stopAll();

	/**
	 * Pause all tasks (no force)
	 */
	public void pauseAll();

	/**
	 * resumes all tasks
	 */
	public void resumeAll();

	/**
	 * starts all tasks
	 */
	public void startAll();

	/**
	 * Add same schedule for all the processes, however if it fails to add to
	 * any process, it throws exception but add to processes it can add
	 * 
	 * @param schedule
	 *            - in format (HH?MM?SS?HH?MM?SS?DD,DD...) - where HH is 24 hr
	 *            format hr (<=24) MM is minutes (<= 60) SS is seconds (<=60) DD
	 *            is day (between sunday to saturday) - 1 = Sunday (same as java
	 *            {@link Calendar} and so on, you can provide multiple values
	 *            separated by (,) ? is any character First there combincation
	 *            HH?MM?SS is for start and next one for end time
	 * 
	 */
	public void addScheduleToAllTasks(String schedule) throws Exception;

	public String generateTasksReport();

}
