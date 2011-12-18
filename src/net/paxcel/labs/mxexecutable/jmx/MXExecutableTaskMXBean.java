package net.paxcel.labs.mxexecutable.jmx;

import java.util.Calendar;

import net.paxcel.labs.mxexecutable.event.MXExecutableTaskState;

/**
 * MBean providing functionality for your {@link Runnable} task monitor and
 * control
 * 
 * @author Kuldeep
 * 
 */
public interface MXExecutableTaskMXBean {

	/**
	 * returns current execution count
	 */
	public int getExecutionCount();

	/**
	 * returns current state
	 */
	public MXExecutableTaskState getState();

	/**
	 * 
	 * @return wait interval
	 * @throws Exception
	 */
	public long getWaitIntervalForIteration() throws Exception;

	/**
	 * Sets wait interval
	 * 
	 * @param interval
	 * @throws Exception
	 */
	public void setWaitIntervalForIteration(long interval) throws Exception;

	/**
	 * pause this service
	 */
	public void pause() throws Exception;

	/**
	 * stop this service
	 * 
	 * @throws Exception
	 */
	public void stop() throws Exception;

	/**
	 * resume paused service
	 * 
	 * @throws Exception
	 */
	public void resume() throws Exception;

	/**
	 * starts stopped service
	 */
	public void start() throws Exception;

	/**
	 * @param schedule
	 *            - in format (HH?MM?SS?HH?MM?SS?DD,DD...) - where HH is 24 hr
	 *            format hr (<=24) MM is minutes (<= 60) SS is seconds (<=60) DD
	 *            is day (between sunday to saturday) - 1 = Sunday (same as java
	 *            {@link Calendar} and so on, you can provide multiple values
	 *            separated by (,) ? is any character First there combincation
	 *            HH?MM?SS is for start and next one for end time
	 */
	public long addSchedule(String schedule) throws Exception;

	/**
	 * removes schedule from service
	 * 
	 * @param scheduleId
	 * @throws Exception
	 */
	public void removeSchedule(long scheduleId) throws Exception;

	/**
	 * retuns all schedules
	 * 
	 * @return schedules list as string
	 * @throws Exception
	 */
	public String[] getSchedules() throws Exception;

	/**
	 * 
	 * @return time taken in last execution
	 * @throws Exception
	 */
	public long getLastExecutionTimeTaken() throws Exception;

	/**
	 * removes all schedules
	 * 
	 * @throws Exception
	 */
	public void removeAllSchedules() throws Exception;

	/**
	 * inactivate schedule
	 * 
	 * @param id
	 * @throws Exception
	 */
	public void inactivateSchedule(long id) throws Exception;

	/**
	 * inactivate all schedules
	 * 
	 * @throws Exception
	 */
	public void inactivateAllSchedules() throws Exception;

	/**
	 * activate schedule
	 * 
	 * @param id
	 * @throws Exception
	 */
	public void activateSchedule(long id) throws Exception;

	/**
	 * activate all schedules
	 * 
	 * @throws Exception
	 */
	public void activateAllSchedules() throws Exception;
}
