package net.paxcel.labs.mxexecutable.event;

/**
 * Listener which process can register on executor to get notified on events
 * 
 * @author Kuldeep
 * 
 */
public interface MXExecutorListener {

	/**
	 * state of task changed. see {@link MXExecutableTaskState}
	 * 
	 * @param serviceId
	 *            - id
	 * @param newState
	 *            - new state
	 * @param executionCount
	 *            - execution count
	 */
	public void onStateChanged(String serviceId,
			MXExecutableTaskState newState, int executionCount);

}
