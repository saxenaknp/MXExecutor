package net.paxcel.labs.mxexecutable.beans;

import java.util.Calendar;

import net.paxcel.labs.mxexecutable.event.MXExecutableTaskState;

/**
 * Internal bean used by Executor to hold data
 * 
 * @author Kuldeep
 * 
 */
public class MXExecutableTaskConfiguration implements Cloneable {

	public MXExecutableTaskConfiguration() {

	}

	public Object clone() {
		try {
			MXExecutableTaskConfiguration cloned = (MXExecutableTaskConfiguration) super
					.clone();
			return cloned;
		} catch (CloneNotSupportedException e) {
			return this;
		}
	}

	/**
	 * service id
	 */
	private String serviceId;

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public long getWaitIntervalForIteration() {
		return waitIntervalForIteration;
	}

	public void setWaitIntervalForIteration(long waitIntervalForIteration) {
		this.waitIntervalForIteration = waitIntervalForIteration;
	}

	public int getMaxIteration() {
		return maxIteration;
	}

	public void setMaxIteration(int maxIteration) {
		this.maxIteration = maxIteration;
	}

	public int getExecutionCount() {
		return executionCount;
	}

	public void setExecutionCount(int executionCount) {
		this.executionCount = executionCount;
	}

	/**
	 * name for service
	 * 
	 */

	private String serviceName;

	/**
	 * pause between current and next execution, setting 0 or negative will not
	 * wait
	 */
	private long waitIntervalForIteration;
	/**
	 * Max number of iteration can happen
	 */
	private int maxIteration = -1;
	/**
	 * Current execution count, set by executor
	 */
	private int executionCount;

	private MXExecutableTaskState state;

	public MXExecutableTaskState getState() {
		return state;
	}

	public void setState(MXExecutableTaskState state) {
		this.state = state;
	}

	private Runnable runnable;

	public Runnable getRunnable() {
		return runnable;
	}

	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}

	private Calendar startTime;

	public Calendar getStartTime() {
		return startTime;
	}

	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}

	public long getCpuTime() {
		return cpuTime;
	}

	public void setCpuTime(long cpuTime) {
		this.cpuTime = cpuTime;
	}

	public long getIdleTime() {
		return idleTime;
	}

	public void setIdleTime(long idleTime) {
		this.idleTime = idleTime;
	}

	private long cpuTime;
	private long idleTime;
}
