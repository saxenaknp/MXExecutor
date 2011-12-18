package net.paxcel.labs.mxexecutable.test;

import net.paxcel.labs.mxexecutable.MXExecutor;
import net.paxcel.labs.mxexecutable.MXExecutors;
import net.paxcel.labs.mxexecutable.event.MXExecutableTaskState;
import net.paxcel.labs.mxexecutable.event.MXExecutorListener;
import net.paxcel.labs.mxexecutable.exception.MXTaskException;

/**
 * A test application
 * 
 * @author Kuldeep
 * 
 */
public class MXExecutableTest {

	public static void main(String args[]) {

		final MXExecutor executor = MXExecutors
				.newCachedScheduledServiceExecutor("Scheduled Service Test Executor");
		executor.addTaskStateListener(new MXExecutorListener() {

			@Override
			public void onStateChanged(String serviceId,
					MXExecutableTaskState newState, int executionCount) {
				System.out.println("New State for Service " + serviceId + " = "
						+ newState + " Count " + executionCount);
			}
		});

		try {
			executor.execute("Service 1", new HelloWorldExecutableTask(), -1,
					20000);
		} catch (MXTaskException e1) {
			e1.printStackTrace();
		}
		try {
			Thread.sleep(5000);
		} catch (Exception e) {

		}

		try {
			executor.resume("1");
		} catch (MXTaskException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
