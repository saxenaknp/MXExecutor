package net.paxcel.labs.mxexecutable;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executor;

import net.paxcel.labs.mxexecutable.beans.ScheduledInformation;
import net.paxcel.labs.mxexecutable.event.MXExecutableTaskState;
import net.paxcel.labs.mxexecutable.event.MXExecutorListener;
import net.paxcel.labs.mxexecutable.exception.MXTaskException;
import net.paxcel.labs.mxexecutable.util.StaticDateData;

/**
 * This interface wraps {@link Executor} to provide monitoring and control on
 * threads/task running. It provides many feature which can be controlled by any
 * jmx client or directly by component using it Features are - 1) Execute a task
 * ({@link Runnable}) as with old executors 2) Force/Normal Pause/resume a
 * registered task ({@link Runnable}) at runtime 3) Force/Normal stop/restart a
 * registered task ({@link Runnable}) at runtime 4) schedule a registered task (
 * {@link Runnable}) at runtime, remove schedule, activate/in-activate schedule
 * at runtime 5) task state (provided using {@link MXExecutableTaskState}) 6)
 * Execution Count (how many times) task has been executed 7) Wait Interval
 * configuration for task at runtime 8) Time spend in last execution of task 9)
 * overall monitoring/control for all tasks executed using this executor
 * 
 * Note - It is highly recommended to not to use infinite while loop in you
 * runnable run method. such things can be done using properties
 * {@link MXExecutor#updateWaitInterval(String, long)}
 * 
 * @author Kuldeep
 * 
 */
public interface MXExecutor extends Executor {

	/**
	 * 
	 * Pause a running service. Note - if there is some schedule which activates
	 * after time it is paused, start schedule will resume this service and if
	 * only stop time of schedule matches it will stop that service
	 * 
	 * @param taskName
	 * @param forceFullyPause
	 *            If false pauses service after current execute call finishes.
	 *            otherwise it will interrupt the service It is recommended to
	 *            use false for this parameter, as it can result in unstable
	 *            state. Also even forceFullyPause is set to true it should try
	 *            to pause normally then it forces pause Also force operation
	 *            does not mean it will stop current work instantly, however it
	 *            will try by interrupting thread running that service
	 * @param pauseTime
	 *            time till this service should be paused, negative or 0 value
	 *            will pause it for ever till it is not resumed again Positive
	 *            value indicates number of milliseconds to pause this service
	 *            from the current (last) execution finish
	 * @throws MXTaskException
	 *             if service is not registered or already stopped/paused
	 */
	public void pause(String taskName, boolean forceFullyPause, long pauseTime)
			throws MXTaskException;

	/**
	 * Resume previously paused task
	 * 
	 * @param taskName
	 * @throws MXTaskException
	 */
	public void resume(String taskName) throws MXTaskException;

	/**
	 * Stops a running task, if the service was early pause, it will be
	 * interrupted and stopped
	 * 
	 * @param taskName
	 * @param forceFullyStop
	 *            If false stops service after current execute call finishes.
	 *            otherwise it will interrupt the service It is recommended to
	 *            use false as parameter, as it can result in unstable state for
	 *            your task
	 * 
	 * @throws MXTaskException
	 *             if service is not registered or already stopped, or forceStop
	 *             is true but it unable to interrupt the service
	 */
	public void stop(String taskName, boolean forceFullyStop)
			throws MXTaskException;

	/**
	 * executes task {@link Runnable}
	 * 
	 * @param runnable
	 *            - your runnable task
	 * @param taskName
	 *            - A unique name for task in this executor, this is used to
	 *            differentiate between different task submitted to the executor
	 * @param maxIteration
	 *            - max number of times this task should run, once this reaches
	 *            task will stop automatically
	 * @param waitInterval
	 *            interval after which subsequent call will be made on your
	 *            runnable task
	 * @throws MXTaskException
	 */
	public void execute(String taskName, Runnable runnable, int maxIteration,
			long waitInterval) throws MXTaskException;

	/**
	 * executes task {@link Runnable}
	 * 
	 * @param taskName
	 *            - A unique name for task in this executor, this is used to
	 *            differentiate between different task submitted to the executor
	 * @param runnable
	 *            - your runnable task
	 * @throws MXTaskException
	 */
	public void execute(String taskName, Runnable runnable)
			throws MXTaskException;

	/**
	 * restarts task
	 * 
	 * @param taskName
	 *            id of service to be restarted
	 */
	public void start(String taskName) throws MXTaskException;

	/**
	 * Registers Service state listener which will be notified on service state
	 * change
	 * 
	 * @param listener
	 */
	public void addTaskStateListener(MXExecutorListener listener);

	/**
	 * Removes already registered listener
	 * 
	 * @param listner
	 */
	public void removeScheduledServiceListener(MXExecutorListener listner);

	/**
	 * It is used to stop all tasks running in this executor
	 * 
	 * @param forceFully
	 *            - if true all tasks will be interuppted
	 */
	public void stopAll(boolean forceFully);

	/**
	 * Adds a new schedule to service.
	 * 
	 * @param taskName
	 *            - service id
	 * @param start
	 *            start time for schedule refer format for time in (
	 *            {@link StaticDateData}
	 * @param end
	 *            end time for schedule refer format for time in (
	 *            {@link StaticDateData}
	 * @param days
	 *            days for schedule, day start with Sunday as in
	 *            {@link Calendar}
	 * @return schedule id created by executor
	 * @throws MXTaskException
	 */
	public long addSchedule(String taskName, StaticDateData start,
			StaticDateData end, int... days) throws MXTaskException;

	/**
	 * Returns current status for a service
	 * 
	 * @param taskName
	 * @return {@link MXExecutableTaskState}
	 * @throws MXTaskException
	 *             if service is not registered
	 */
	public MXExecutableTaskState getCurrentState(String taskName)
			throws MXTaskException;

	/**
	 * Stops (if running) and removes task from the registration. Once removed
	 * service can not be restarted. To run again need to use
	 * {@link MXExecutor#execute(String, String, Runnable, int, long)}
	 * 
	 * @param taskName
	 * @throws MXTaskException
	 */
	public void remove(String taskName) throws MXTaskException;

	/**
	 * Returns schedules for the service (task), this is returned as cloned
	 * object
	 * 
	 * @param taskName
	 * @return list of schedules
	 * @throws MXTaskException
	 */
	public List<ScheduledInformation> getSchedules(String taskName)
			throws MXTaskException;

	/**
	 * Removes specific schedule from specific service
	 * 
	 * @param taskName
	 * @param scheduleId
	 * @throws MXTaskException
	 */
	public void removeSchedule(String taskName, long scheduleId)
			throws MXTaskException;

	/**
	 * updates wait interval for service
	 * 
	 * @param taskName
	 * @param waitInterval
	 * @throws MXTaskException
	 */
	public void updateWaitInterval(String taskName, long waitInterval)
			throws MXTaskException;

	/**
	 * Returns current wait interval for service
	 * 
	 * @param taskName
	 * @return wait interval
	 */
	public long getWaitInterval(String taskName) throws MXTaskException;

	/**
	 * Returns time spend in last execution cycle
	 * 
	 * @param taskName
	 * @return time taken in last execution
	 * @throws MXTaskException
	 */
	public long getLastExecutionTimeTaken(String taskName)
			throws MXTaskException;

	/**
	 * removes all schedules for a task
	 * 
	 * @param taskName
	 * @throws MXTaskException
	 */
	public void clearSchedules(String taskName) throws MXTaskException;

	/**
	 * activates all schedules for a task
	 * 
	 * @param taskName
	 * @throws MXTaskException
	 */
	public void activateAllSchedules(String taskName) throws MXTaskException;

	/**
	 * inactivate all schedules for a task
	 * 
	 * @param taskName
	 * @throws MXTaskException
	 */
	public void inactivateAllSchedules(String taskName) throws MXTaskException;

	/**
	 * Activates specific schedule for specific task
	 * 
	 * @param taskName
	 * @param scheduleId
	 * @throws MXTaskException
	 */
	public void activateSchedule(String taskName, long scheduleId)
			throws MXTaskException;

	/**
	 * inactivate specific schedule for specific task
	 * 
	 * @param taskName
	 * @param scheduleId
	 * @throws MXTaskException
	 */
	public void inactivateSchedule(String taskName, long scheduleId)
			throws MXTaskException;

	/**
	 * This method is not recommended with this executor, use
	 * {@link MXExecutor#execute(String, String, Runnable)} instead. Here it is
	 * using {@link MXExecutor#execute(String, String, Runnable)} call by
	 * generating id and name as "Task". It can produce runtime error, if id for
	 * task created is already registered by another call of
	 * {@link MXExecutor#execute(String, String, Runnable)} It is here to
	 * provide compatibility to application which takes {@link Executor} as
	 * input in their application and wishes to use monitoring and management
	 * capability provided by this executor
	 */
	@Override
	public void execute(Runnable runnable);
}
