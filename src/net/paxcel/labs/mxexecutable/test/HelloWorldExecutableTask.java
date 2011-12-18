package net.paxcel.labs.mxexecutable.test;

/**
 * Test scheduled service which will be called by scheduled service
 * 
 * @author Kuldeep
 * 
 */
public class HelloWorldExecutableTask implements Runnable {

	@Override
	public void run() {
		// don't put infinite loop, that is done by executor
		try {
			for (int i = 0; i < 1000; i++) {
				System.out.println("Hello World");
			}
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
