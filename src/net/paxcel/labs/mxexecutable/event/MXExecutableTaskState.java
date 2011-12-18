package net.paxcel.labs.mxexecutable.event;

import net.paxcel.labs.mxexecutable.MXExecutor;

/**
 * Enum containg different states a task can be in
 * 
 * @author Kuldeep
 * 
 */
public enum MXExecutableTaskState {

	/**
	 * Initial
	 */
	INITIALIZED(100),
	/**
	 * started
	 */
	STARTED(101),
	/**
	 * waiting. See {@link MXExecutor#getWaitInterval(String)}
	 */
	WAITING(102),
	/**
	 * Paused
	 */
	PAUSED(103),
	/**
	 * Forcefully paused
	 */
	FORCE_PAUSED(104),
	/**
	 * stopped
	 */
	STOPPED(105),
	/**
	 * force stopped
	 */
	FORCE_STOPPED(106),
	/**
	 * resumed
	 */
	RESUMED(107),
	/**
	 * running (just before executing your task's run)
	 */
	RUNNING(108),
	/**
	 * service removed
	 */
	REMOVED(109),
	/**
	 * unknown
	 */
	UNKNOWN(110);

	private MXExecutableTaskState(int value) {

	}

	/*
	 * public String toString (){ if (this == INITIALIZED){ return
	 * "INITIALIZED"; } if (this == RUNNING){ return "RUNNING"; } return
	 * "UNImplemented"; }
	 */
}
