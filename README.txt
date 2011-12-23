Summery - This is designed to provide monitor and management capability to Java runnable tasks/Threads. This wraps normal java executor to provide various features listed below -
=============================================

Features –
=====================

It provides many features which can be controlled by any jmx client or directly by components using it –
	1. Execute a task (Java Runnable) as with old executors
	2. Force/Normal Pause/resume a registered task at runtime
	3. Force/Normal stop/restart a registered task at runtime
	4. Schedule a registered task at runtime, remove schedule, activate/in-activate schedule at runtime
	5. Provide Thread (Task) state (INITIALIZED, STARTED, WAITING, PAUSED, FORCE_PAUSED,
	STOPPED, FORCE_STOPPED, RESUMED, RUNNING, REMOVED, UNKNOWN) by providing Notification API
	6. Execution Count (how many times) task has been executed since start
	7. Wait Interval configuration for task at runtime (Time to wait for next execute of your task)
	8. Time spend in last execution of task (number of milliseconds spend by your task on a cycle of execution)
	9. CPU utilization report for individual thread.
	10. Overall monitoring/control for all tasks executed using this executor, one click pause/stop/start of all threads within the executor

===========================================================
Installation -

Add mxexecutor.jar to your project and start using it. 

See net.paxcel.labs.mxexecutable.test package for sample code

See Attached MX Executor User Guide.pdf to go through basic functionality provided

===========================================================




