package net.paxcel.labs.mxexecutable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

/**
 * Used to provide different executors, right now it has only cached
 * implementation to return.
 * 
 * Support JMX?
 * 
 * @author Kuldeep
 * 
 */
public class MXExecutors {

	private static Map<String, MXExecutor> executorMap = new HashMap<String, MXExecutor>();

	/**
	 * Creates new executor If executor was already created it return null;
	 * 
	 * @param executorName
	 *            - This is used as domain name
	 * 
	 */
	public static MXExecutor newCachedScheduledServiceExecutor(
			String executorName) {
		return newCachedScheduledServiceExecutor(executorName, null);
	}

	/**
	 * Creates new executor with threadfactory provided If executor was already
	 * created it return null;
	 * 
	 * @param executorName
	 *            this is used as domain name in mbean
	 * @param tf
	 *            custom thread factory
	 * @return created executor
	 */
	public static MXExecutor newCachedScheduledServiceExecutor(
			String executorName, ThreadFactory tf) {
		if (executorMap.containsKey(executorName)) {
			return null;
		}
		return new MXCachedExecutor(executorName, tf);
	}

	/**
	 * returns earlier created executor if any or null
	 * 
	 * @param executorName
	 * @return
	 */
	public static MXExecutor getExecutor(String executorName) {
		return executorMap.get(executorName);
	}
}
