package net.paxcel.labs.mxexecutable.jmx;

import java.util.List;

import net.paxcel.labs.mxexecutable.MXExecutor;
import net.paxcel.labs.mxexecutable.beans.ScheduledInformation;
import net.paxcel.labs.mxexecutable.event.MXExecutableTaskState;
import net.paxcel.labs.mxexecutable.event.MXExecutorListener;
import net.paxcel.labs.mxexecutable.exception.MXTaskException;
import net.paxcel.labs.mxexecutable.util.StaticDateData;

/**
 * Implementation for task MBean
 * 
 * @author Kuldeep
 * 
 */
public class MXExecutableTaskMBeanImpl implements MXExecutableTaskMXBean,
		MXExecutorListener {

	private MXExecutor executor;
	private String serviceId;
	private int executionCount = 0;
	private MXExecutableTaskState state = MXExecutableTaskState.UNKNOWN;

	public MXExecutableTaskMBeanImpl(String serviceId, MXExecutor executor) {
		this.executor = executor;
		this.serviceId = serviceId;
		this.executor.addTaskStateListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getExecutionCount() {
		return executionCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MXExecutableTaskState getState() {
		return state;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getWaitIntervalForIteration() throws Exception {
		try {
			return executor.getWaitInterval(serviceId);
		} catch (MXTaskException ex) {
			throw new Exception(ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWaitIntervalForIteration(long interval) throws Exception {
		try {
			executor.updateWaitInterval(serviceId, interval);
		} catch (MXTaskException ex) {
			throw new Exception(ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pause() throws Exception {

		try {
			executor.pause(serviceId, false, -1);
		} catch (MXTaskException ex) {
			throw new Exception(ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() throws Exception {
		try {
			executor.stop(serviceId, false);
		} catch (MXTaskException ex) {
			throw new Exception(ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resume() throws Exception {
		try {
			executor.resume(serviceId);
		} catch (MXTaskException ex) {
			throw new Exception(ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws Exception {
		try {
			executor.start(serviceId);
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStateChanged(String serviceId,
			MXExecutableTaskState newState, int executionCount) {
		if (serviceId.equals(this.serviceId)) {
			state = newState;
			this.executionCount = executionCount;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long addSchedule(String schedule) throws Exception {
		// this should be in format HH-MM-SS-HH-MM-SS-dd,dd
		if (schedule == null || schedule.length() < 19) {
			throw new Exception(
					"Invalid value for schedule, should be in format HH-MM-SS-HH-MM-SS-dd,dd ... min length of string is expected 19");
		}
		try {
			StaticDateData start = new StaticDateData(Integer.parseInt(schedule
					.substring(0, 2)), Integer.parseInt(schedule
					.substring(3, 5)), Integer.parseInt(schedule
					.substring(6, 8)));
			StaticDateData end = new StaticDateData(Integer.parseInt(schedule
					.substring(9, 11)), Integer.parseInt(schedule.substring(12,
					14)), Integer.parseInt(schedule.substring(15, 17)));
			String days[] = schedule.substring(18).split(",");

			int daysForSchedule[] = new int[days.length];
			for (int i = 0; i < days.length; i++) {
				daysForSchedule[i] = Integer.parseInt(days[i]);
			}
			return executor.addSchedule(serviceId, start, end, daysForSchedule);

		} catch (MXTaskException ex) {
			throw new Exception(ex.getMessage());
		} catch (Exception e) {
			throw new Exception(
					"Invalid value for schedule, should be in format HH-MM-SS-HH-MM-SS-dd,dd ... min length of string is expected 19 "
							+ e.getMessage());
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeSchedule(long scheduleId) throws Exception {
		try {
			executor.removeSchedule(serviceId, scheduleId);
		} catch (MXTaskException ex) {
			throw new Exception(ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getSchedules() throws Exception {
		List<ScheduledInformation> scheduledInformations = executor
				.getSchedules(serviceId);
		if (scheduledInformations != null) {
			String schedules[] = new String[scheduledInformations.size()];
			int i = 0;
			for (ScheduledInformation scheduledInformation : scheduledInformations) {
				schedules[i] = scheduledInformation.toString();
				i++;
			}
			return schedules;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getLastExecutionTimeTaken() throws Exception {
		try {
			return executor.getLastExecutionTimeTaken(serviceId);
		} catch (MXTaskException e) {
			throw new Exception(e.getMessage());
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeAllSchedules() throws Exception {
		executor.clearSchedules(serviceId);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void inactivateSchedule(long scheduleId) throws Exception {
		try {
			executor.inactivateSchedule(serviceId, scheduleId);
		} catch (MXTaskException ex) {
			throw new Exception(ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void inactivateAllSchedules() throws Exception {
		try {
			executor.inactivateAllSchedules(serviceId);
		} catch (MXTaskException ex) {
			throw new Exception(ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void activateSchedule(long scheduleId) throws Exception {
		try {
			executor.activateSchedule(serviceId, scheduleId);
		} catch (MXTaskException ex) {
			throw new Exception(ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void activateAllSchedules() throws Exception {
		try {
			executor.activateAllSchedules(serviceId);
		} catch (MXTaskException ex) {
			throw new Exception(ex.getMessage());
		}

	}

}
