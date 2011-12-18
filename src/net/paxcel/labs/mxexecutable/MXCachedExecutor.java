package net.paxcel.labs.mxexecutable;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import net.paxcel.labs.mxexecutable.beans.MXExecutableTaskConfiguration;
import net.paxcel.labs.mxexecutable.beans.ScheduledInformation;
import net.paxcel.labs.mxexecutable.event.MXExecutableTaskState;
import net.paxcel.labs.mxexecutable.event.MXExecutorListener;
import net.paxcel.labs.mxexecutable.exception.MXTaskException;
import net.paxcel.labs.mxexecutable.jmx.ExecutorMXBean;
import net.paxcel.labs.mxexecutable.util.StaticDateData;

/**
 * 
 * 
 * This is an implementation of {@link MXExecutor}, using
 * {@link Executors#newCachedThreadPool()} This also provides MBean to manage
 * all task at once with name = Main
 * 
 * @author Kuldeep
 * 
 */
public class MXCachedExecutor implements MXExecutor, ExecutorMXBean {

	private Executor executor;
	private List<MXExecutorListener> scheduledServiceEventListener;
	private Map<String, MXExecutableTaskConfiguration> services = new HashMap<String, MXExecutableTaskConfiguration>();
	private Map<String, Runnable> scheduledServices = new HashMap<String, Runnable>();
	private Map<String, ServiceRunner> runningServices = new HashMap<String, MXCachedExecutor.ServiceRunner>();
	private Map<String, MXExecutableTaskState> serviceState = new HashMap<String, MXExecutableTaskState>();
	private Executor notificationUpdatorThreadPool = Executors
			.newFixedThreadPool(1);

	// single stater and stopper, future can have parallel starter and stopper
	private ServiceSchedulerStarter starter = new ServiceSchedulerStarter();
	private ServiceSchedulerStopper stopper = new ServiceSchedulerStopper();
	private Map<String, Long> lastExecutionTime = new HashMap<String, Long>();

	private long taskId = 0;

	private String name;

	private long scheduleId = 0;
	private Map<String, ObjectName> mBeanNames = new HashMap<String, ObjectName>();

	Map<String, List<ScheduledInformation>> scheduledInformationMap = new LinkedHashMap<String, List<ScheduledInformation>>();

	public String getName() {
		return name;

	}

	/**
	 * register mbean to manage this executor
	 */
	private void registerMbean() {
		// Init bean to manage this executor as whole
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		// Construct the ObjectName for the MBean we will register
		ObjectName name = null;
		try {
			name = new ObjectName(getName() + ":type=Main");
		} catch (MalformedObjectNameException e) {
		} catch (NullPointerException e) {
		}

		try {
			mbs.registerMBean(this, name);
		} catch (InstanceAlreadyExistsException e) {
		} catch (MBeanRegistrationException e) {
		} catch (NotCompliantMBeanException e) {
		}

	}

	public MXCachedExecutor(String executorName) {
		this(executorName, null);
	}

	/**
	 * 
	 * @param name
	 * @param tf
	 */
	public MXCachedExecutor(String name, ThreadFactory tf) {
		scheduledServiceEventListener = new ArrayList<MXExecutorListener>();
		if (tf == null) {
			tf = Executors.defaultThreadFactory();
		}
		executor = Executors.newCachedThreadPool(tf);
		// executor.execute(starter);
		// executor.execute(stopper);
		this.name = name;
		if (name == null) {
			name = this.toString(); // default is object memory
		}
		// starter and stopper thread
		new Thread(starter, name + ":Starter").start();
		new Thread(stopper, name + ":Stopper").start();

		registerMbean();
	}

	private void initializeService(String taskName, Runnable runnable,
			int maxIteration, long waitIntervalForIteration)
			throws MXTaskException {

		if (taskName == null || taskName.length() < 1) {
			throw new MXTaskException(
					"Service id can't be null or empty string");
		}
		if (runnable == null) {
			throw new MXTaskException("Runnable instance can't be null");
		}
		MXExecutableTaskConfiguration serviceConfiguration = new MXExecutableTaskConfiguration();
		serviceConfiguration.setRunnable(runnable);
		serviceConfiguration.setMaxIteration(maxIteration);
		serviceConfiguration.setServiceName(taskName);
		serviceConfiguration
				.setWaitIntervalForIteration(waitIntervalForIteration);
		serviceConfiguration.setServiceId(taskName);
		services.put(serviceConfiguration.getServiceId(), serviceConfiguration);
		scheduledServices.put(serviceConfiguration.getServiceId(), runnable);
		updateEvent(serviceConfiguration.getServiceId(),
				MXExecutableTaskState.INITIALIZED);

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		net.paxcel.labs.mxexecutable.jmx.MXExecutableTaskMBeanImpl jmxBean = new net.paxcel.labs.mxexecutable.jmx.MXExecutableTaskMBeanImpl(
				serviceConfiguration.getServiceId(), this);
		ObjectName name = null;
		try {
			String registerName = (taskName == null ? "" : taskName);
			if (registerName != null && registerName.length() > 0) {
				registerName += " - ";
			}
			name = new ObjectName(getName() + ":type=" + registerName);
			try {
				MBeanInfo mbean = mbs.getMBeanInfo(name);
				if (mbean != null) {
					// don't re-register
					return;
				}
			} catch (Exception e) {
			}
			mBeanNames.put(serviceConfiguration.getServiceId(), name);
		} catch (MalformedObjectNameException e) {
			throw new MXTaskException("Invalid name for MBean " + taskName);
		} catch (NullPointerException e) {
			throw new MXTaskException("Name can't be null for MBean "
					+ taskName);
		}

		try {
			mbs.registerMBean(jmxBean, name);
		} catch (InstanceAlreadyExistsException e) {
			throw new MXTaskException("Error creating MBean" + taskName
					+ " - Error :" + e.getMessage());
		} catch (MBeanRegistrationException e) {
			throw new MXTaskException("Error creating MBean" + taskName
					+ " - Error :" + e.getMessage());
		} catch (NotCompliantMBeanException e) {
			throw new MXTaskException("Error creating MBean" + taskName
					+ " - Error :" + e.getMessage());
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pause(String serviceId, boolean forcePause, long pauseTime)
			throws MXTaskException {
		ServiceRunner runner = runningServices.get(serviceId);
		MXExecutableTaskConfiguration config = services.get(serviceId);
		if (config == null) {
			throw new MXTaskException(
					"Can't pause a service which was not registered earlier. Service id "
							+ serviceId);
		}
		if (runner == null) {
			throw new MXTaskException(
					"Can't pause a service which is not running. Service id "
							+ serviceId);
		}
		if (runner.getServiceState() == ServiceState.STOP
				|| runner.getServiceState() == ServiceState.PAUSE) {
			throw new MXTaskException(
					"Can't pause a service which is already paused or stopped. Service id "
							+ serviceId);
		}

		runner.setPauseTime(pauseTime);
		runner.setServiceState(ServiceState.PAUSE);
		if (forcePause)
			runner.interrupt();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resume(String serviceId) throws MXTaskException {
		ServiceRunner runner = runningServices.get(serviceId);
		if (runner == null) {
			throw new MXTaskException(
					"Can't resume service, which was not paused earlier. Service id "
							+ serviceId);
		}
		if (runner.getServiceState() == ServiceState.PAUSE
				|| runner.getServiceState() == ServiceState.WAITING) {
			runner.setServiceState(ServiceState.RESUME);
		} else {
			throw new MXTaskException(
					"Can't resume a service which is not paused earlier");

		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop(String serviceId, boolean forceStop)
			throws MXTaskException {
		if (!services.containsKey(serviceId)) {
			throw new MXTaskException(
					"Can't stop a service which is not registered. Service id was "
							+ serviceId);
		}
		ServiceRunner runner = runningServices.get(serviceId);
		if (runner == null) {
			throw new MXTaskException(
					"Can't stop a service which is not running. Service id was "
							+ serviceId);
		} else {
			if (runner.getServiceState() == ServiceState.STOP) {
				throw new MXTaskException(
						"Can't stop a service which is already or stopped. Service id was "
								+ serviceId);
			} else if (runner.getServiceState() == ServiceState.PAUSE) {
				runner.setServiceState(ServiceState.STOP);
				runner.interrupt();
				return;
			}
		}

		runner.setServiceState(ServiceState.STOP);
		if (forceStop) {
			runner.interrupt();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(String taskName, Runnable runnable, int maxIteration,
			long waitInterval) throws MXTaskException {
		ServiceRunner runner = runningServices.get(taskName);
		if (runner != null) {
			throw new MXTaskException(
					"Service was Already running, can't start a running service. Service id was "
							+ taskName);
		}

		initializeService(taskName, runnable, maxIteration, waitInterval);
		MXExecutableTaskConfiguration config = services.get(taskName);

		// reset count
		config.setExecutionCount(0);

		runner = new ServiceRunner(scheduledServices.get(taskName),
				services.get(taskName));
		runningServices.put(taskName, runner);
		executor.execute(runner);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(String taskName, Runnable runnable)
			throws MXTaskException {
		execute(taskName, runnable, -1, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start(String serviceId) throws MXTaskException {
		startService(serviceId, false);
	}

	/**
	 * Start a service
	 * 
	 * @param serviceId
	 * @param stopOldRunning
	 * @throws MXTaskException
	 */
	private void startService(String serviceName, boolean stopOldRunning)
			throws MXTaskException {
		MXExecutableTaskConfiguration config = services.get(serviceName);
		if (config == null) {
			throw new MXTaskException(
					"Can't start a service which was not configured earlier, create a new one");
		}

		ServiceRunner runner = runningServices.get(serviceName);
		if (runner != null) {
			if (stopOldRunning) {
				stop(serviceName, false);
			}
		}

		if (config != null) {
			execute(config.getServiceName(), config.getRunnable(),
					config.getMaxIteration(),
					config.getWaitIntervalForIteration());
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long addSchedule(String serviceName, StaticDateData start,
			StaticDateData end, int... days) throws MXTaskException {
		if (!services.containsKey(serviceName)) {
			throw new MXTaskException(
					"Can't add schedule, Service was not a registered service to this executor. Service id "
							+ serviceName);
		}
		ScheduledInformation info = validateSchedule(serviceName, start, end,
				days);
		List<ScheduledInformation> schedules = scheduledInformationMap
				.get(serviceName);
		schedules.add(info);

		return info.getId();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ScheduledInformation> getSchedules(String taskName)
			throws MXTaskException {
		List<ScheduledInformation> schedules = scheduledInformationMap
				.get(taskName);
		List<ScheduledInformation> cloned = new LinkedList<ScheduledInformation>();
		for (ScheduledInformation schedule : schedules) {
			Object obj = schedule.clone();
			if (obj != null) {
				try {
					ScheduledInformation si = (ScheduledInformation) obj;
					cloned.add(si);
				} catch (Exception e) {

				}
			}

		}
		return schedules;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeSchedule(String taskName, long scheduleId)
			throws MXTaskException {
		if (!services.containsKey(taskName)) {
			throw new MXTaskException(
					"Can't remove schedule from a service which is not registered");
		}
		boolean found = false;
		List<ScheduledInformation> scheduledInformation = scheduledInformationMap
				.get(taskName);
		if (scheduledInformation != null) {
			for (ScheduledInformation info : scheduledInformation) {
				if (info.getId() == scheduleId) {
					scheduledInformation.remove(info);
					found = true;
					break;
				}
			}
		}
		if (!found) {
			throw new MXTaskException("Invalid schedule id for service "
					+ taskName);
		}
		return;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void stopAll(boolean forceFully) {
		Set<String> serviceIds = services.keySet();
		for (String serviceId : serviceIds) {
			try {
				stop(serviceId, forceFully);
			} catch (MXTaskException e) {
			}
		}
	}

	/**
	 * update listeners
	 * 
	 * @param serviceId
	 * @param state
	 */
	private void updateEvent(final String taskName,
			final MXExecutableTaskState state) {

		serviceState.put(taskName, state);

		if (state == MXExecutableTaskState.STOPPED
				|| state == MXExecutableTaskState.FORCE_STOPPED) {
			runningServices.remove(taskName);

		}
		final MXExecutableTaskConfiguration config = services.get(taskName);
		if (config == null) {
			return;
		}
		updateStats(config);
		final int executionCount = config.getExecutionCount();
		services.get(taskName).setState(state);
		notificationUpdatorThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				synchronized (scheduledServiceEventListener) {
					for (final MXExecutorListener listener : scheduledServiceEventListener) {
						// run one by one
						if (listener != null) {
							listener.onStateChanged(taskName, state,
									executionCount);
						}

					}
				}
			}

		});

	}

	private static final int MS_SECOND = 1000;

	private static final int MS_MINUTE = 60000;

	private static final int MS_HOUR = 3600000;

	private static final int NS_MILLISECOND = 1000000;

	private String getPrintableTime(long durationMS) {
		long tmpTime = durationMS;

		long hours = tmpTime / MS_HOUR;
		tmpTime -= hours * MS_HOUR;

		long minutes = tmpTime / MS_MINUTE;
		tmpTime -= minutes * MS_MINUTE;

		long seconds = tmpTime / MS_SECOND;
		tmpTime -= seconds * MS_SECOND;

		return hours + ":" + minutes + ":" + seconds + "." + tmpTime;
	}

	private void updateStats(MXExecutableTaskConfiguration config) {
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		long cpuTime = 0;
		if (threadMXBean.isThreadCpuTimeSupported()) {
			if (threadMXBean.isThreadCpuTimeEnabled()) {
				cpuTime = threadMXBean.getCurrentThreadCpuTime()
						/ NS_MILLISECOND;
				config.setCpuTime(cpuTime);
			}
		}
		Calendar c = config.getStartTime();
		if (c != null) {
			long startTime = c.getTimeInMillis();

			config.setIdleTime(System.currentTimeMillis() - startTime - cpuTime);
		}

	}

	/**
	 * validates schedule
	 * 
	 * @param serviceId
	 * @param start
	 * @param end
	 * @param days
	 * @return scheduled information created
	 * @throws MXTaskException
	 */
	private ScheduledInformation validateSchedule(String serviceId,
			StaticDateData start, StaticDateData end, int... days)
			throws MXTaskException {

		if (days == null || days.length < 1 || days.length > 7) {
			throw new MXTaskException(
					"Can't add schedule, days value can't be null or more than 7. Service id "
							+ serviceId);
		}
		if (end == null || start == null) {
			throw new MXTaskException(
					"Can't add schedule, start and end time value can't be null. Service id "
							+ serviceId);
		}
		if (serviceId == null) {
			throw new MXTaskException(
					"Can't add schedule, Service id was null.");
		}

		if (end.before(start)) {
			throw new MXTaskException(
					"Can't add schedule, End time can't be less than start time. Service id "
							+ serviceId);
		}

		for (int j = 0; j < days.length; j++) { // Basic. not optimal but ok
			int day = days[j];
			if (day > 7) { // value at any index is greater than 7?
				throw new MXTaskException(
						"Can't add schedule, day should be value between 1-7. day was "
								+ day + ". Service id " + serviceId);

			}
			for (int i = j + 1; i < days.length; i++) {
				if (day == days[i]) {
					throw new MXTaskException(
							"Can't add schedule, duplicate day found (" + day
									+ ") at index " + i + " and " + j
									+ ". Service id " + serviceId);
				}
			}

		}

		ScheduledInformation tempInfo = new ScheduledInformation(getNextId(),
				serviceId, start, end, days);
		List<ScheduledInformation> scheduleInfoStored = scheduledInformationMap
				.get(serviceId);
		if (scheduleInfoStored == null) {
			scheduleInfoStored = new LinkedList<ScheduledInformation>();
			scheduledInformationMap.put(serviceId, scheduleInfoStored);
		}
		for (ScheduledInformation aInfo : scheduleInfoStored) {

			if (aInfo.toString().equals(tempInfo.toString())) {
				throw new MXTaskException(
						"Can't add schedule, schedule with same configuration already exists for same service. Service Id "
								+ serviceId
								+ " Scheduled Information "
								+ aInfo.toString());
			}
			int oldDays[] = aInfo.getDays();
			int newDays[] = tempInfo.getDays();
			for (int j = 0; j < oldDays.length; j++) { // Basic. not optimal but
														// ok
				int day = oldDays[j];
				if (day > 7) { // value at any index is greater than 7?
					throw new MXTaskException(
							"Can't add schedule, day should be value between 1-7. day was "
									+ day + ". Service id " + serviceId);

				}

				for (int i = 0; i < newDays.length; i++) {
					if (day == newDays[i]) {
						if (tempInfo.between(aInfo))
							throw new MXTaskException(
									"Can't add schedule, duplicate schedule information found. Old match info  "
											+ aInfo.toString() + " New Info "
											+ tempInfo.toString());
					}
				}

			}
		}
		return tempInfo;

	}

	/**
	 * id for schedule
	 * 
	 * @return id for schedule
	 */
	private synchronized long getNextId() {
		return ++scheduleId;
	}

	/**
	 * local state for {@link ServiceRunner}
	 * 
	 * @author Kuldeep
	 * 
	 */
	private enum ServiceState {

		PAUSE(200), STOP(201), RESUME(202), RUNNING(203), WAITING(204);
		private ServiceState(int a) {

		}

	}

	/**
	 * Executable entity which run and monitors your {@link Runnable}
	 * 
	 * @author Kuldeep
	 * 
	 */
	private class ServiceRunner implements Runnable {

		private ServiceState serviceState;
		private transient Object mutex = new Object();

		public ServiceState getServiceState() {
			return serviceState;
		}

		public void setServiceState(ServiceState newState) {
			this.serviceState = newState;
			if (newState == ServiceState.RESUME) {

				updateEvent(config.getServiceId(),
						MXExecutableTaskState.RESUMED);
				synchronized (mutex) {
					mutex.notify();
				}
			}
		}

		private Runnable service;
		private MXExecutableTaskConfiguration config;
		private Thread t;

		private long pauseTime = -1;

		public void setPauseTime(long time) {
			this.pauseTime = time;
		}

		public ServiceRunner(Runnable service,
				MXExecutableTaskConfiguration config) {
			this.service = service;
			this.config = config;
		}

		public void run() {
			boolean force = false;
			boolean oneTime = false;
			t = Thread.currentThread();

			String threadName = "MX Service - [Id - " + config.getServiceId()
					+ " , Name " + config.getServiceName() + "]";
			t.setName(threadName);
			while (true) {

				try {

					if (!oneTime) {
						oneTime = true;
						config.setStartTime(Calendar.getInstance());
						updateEvent(config.getServiceId(),
								MXExecutableTaskState.STARTED);
					}
					if (serviceState == ServiceState.PAUSE) {
						convertAndUpdateEvent(ServiceState.PAUSE, force);
						if (pauseTime < 0) {
							synchronized (mutex) {
								mutex.wait();
							}
						} else {
							synchronized (mutex) {
								mutex.wait(pauseTime);
							}
						}
					}

					if (serviceState == ServiceState.STOP) {
						break;
					}
					serviceState = ServiceState.RUNNING;
					config.setExecutionCount(config.getExecutionCount() + 1);
					updateEvent(config.getServiceId(),
							MXExecutableTaskState.RUNNING);
					long startTime = System.currentTimeMillis();
					try {
						service.run();
					} catch (Exception e) {
						System.err.println("Error executing app "
								+ e.getMessage() + " Continuing");
					} catch (Error e) {
						System.err.println("Fatal Error executing app "
								+ e.getMessage());
					}
					lastExecutionTime.put(config.getServiceId(),
							System.currentTimeMillis() - startTime);

					if (serviceState == ServiceState.STOP) {
						break;
					}
					if (serviceState != ServiceState.PAUSE) {
						long waitInterval = config
								.getWaitIntervalForIteration();
						if (waitInterval > 0) {
							synchronized (mutex) {
								serviceState = ServiceState.WAITING;
								updateEvent(config.getServiceId(),
										MXExecutableTaskState.WAITING);
								mutex.wait(waitInterval);
							}
						}
					}

				} catch (InterruptedException ex) {
					force = true;
					if (serviceState == ServiceState.STOP) {
						break;
					}
					if (serviceState == ServiceState.PAUSE) {
						synchronized (mutex) {
							try {
								if (pauseTime < 0) {
									synchronized (mutex) {
										mutex.wait();
									}
								} else {
									synchronized (mutex) {
										mutex.wait(pauseTime);
									}
								}

							} catch (InterruptedException e) {
								System.out
										.println("Unexpected interruption of thread when it was already waiting after interruption");
							}
						}
					}
					convertAndUpdateEvent(serviceState, force);
				}
				if (config.getMaxIteration() != -1) {
					if (config.getExecutionCount() >= config.getMaxIteration()) {
						serviceState = ServiceState.STOP;
						break;
					}
				}
				if (serviceState == ServiceState.STOP) {
					break;
				}
			}
			convertAndUpdateEvent(serviceState, force);
		}

		/**
		 * convert and send event to listeners
		 * 
		 * @param state
		 * @param force
		 */
		private void convertAndUpdateEvent(ServiceState state, boolean force) {

			if (serviceState == ServiceState.PAUSE) {
				if (force) {
					updateEvent(config.getServiceId(),
							MXExecutableTaskState.FORCE_PAUSED);
				} else {
					updateEvent(config.getServiceId(),
							MXExecutableTaskState.PAUSED);
				}
			}
			if (serviceState == ServiceState.STOP) {
				if (force) {
					updateEvent(config.getServiceId(),
							MXExecutableTaskState.FORCE_STOPPED);
				} else {
					updateEvent(config.getServiceId(),
							MXExecutableTaskState.STOPPED);
				}
			}
			if (serviceState == ServiceState.RESUME) {
				updateEvent(config.getServiceId(),
						MXExecutableTaskState.RESUMED);
			}

		}

		/**
		 * interrupt thread
		 */
		public void interrupt() {
			t.interrupt();
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addTaskStateListener(MXExecutorListener listener) {

		if (scheduledServiceEventListener == null) {
			scheduledServiceEventListener = new LinkedList<MXExecutorListener>();
		}
		synchronized (scheduledServiceEventListener) {
			if (!scheduledServiceEventListener.contains(listener))
				scheduledServiceEventListener.add(listener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeScheduledServiceListener(MXExecutorListener listner) {
		if (scheduledServiceEventListener == null) {
			return;
		}
		synchronized (scheduledServiceEventListener) {
			scheduledServiceEventListener.remove(listner);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MXExecutableTaskState getCurrentState(String serviceId)
			throws MXTaskException {
		if (serviceState.containsKey(serviceId)) {
			return serviceState.get(serviceId);
		} else {
			throw new MXTaskException(
					"State for service can not be find, make sure it was registered earlier and not removed");
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(String serviceId) throws MXTaskException {
		if (!services.containsKey(serviceId)) {
			throw new MXTaskException("Service was not registered. Service Id"
					+ serviceId);
		}
		try {
			if (runningServices.containsKey(serviceId)) {
				stop(serviceId, false);
			}
		} catch (MXTaskException e) {
		}
		clean(serviceId);
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.unregisterMBean(mBeanNames.get(serviceId));
		} catch (InstanceNotFoundException e) {
		} catch (MBeanRegistrationException e) {
		}
		updateEvent(serviceId, MXExecutableTaskState.REMOVED);
	}

	/**
	 * clean all data for task
	 * 
	 * @param serviceId
	 */
	private void clean(String serviceId) {
		services.remove(serviceId);
		scheduledInformationMap.remove(serviceId);
		scheduledServices.remove(serviceId);
		runningServices.remove(serviceId);
		serviceState.remove(serviceId);
	}

	/**
	 * Thread manages schedules, start task on base of schedule. it wait for 5
	 * seconds in each iteration. So your schedule may not immediately
	 * executable at same time
	 */
	private class ServiceSchedulerStopper implements Runnable {

		Object mutex = new Object();

		@Override
		public void run() {
			Calendar c = Calendar.getInstance();
			int oldDay = c.get(Calendar.DAY_OF_WEEK);
			while (true) {
				try {
					c.setTimeInMillis(System.currentTimeMillis()); // don't
																	// create
																	// new
																	// calendar,
																	// it takes
																	// time
					Set<String> serviceIds = scheduledInformationMap.keySet();
					synchronized (scheduledInformationMap) { // block no
																// addition
																// at time of
																// checking
																// values
																// for startup
						for (String id : serviceIds) {

							List<ScheduledInformation> scheduledInfos = scheduledInformationMap
									.get(id);
							for (ScheduledInformation info : scheduledInfos) {
								if (!info.isActive()) {
									continue;
								}
								int day = c.get(Calendar.DAY_OF_WEEK);

								if (day != oldDay) {
									oldDay = day;
									info.resetStartStopDays();
								}

								int days[] = info.getDays();
								int dayRan[] = info.getEndDayRan();
								boolean set = (days[day - 1] == 0)
										&& (dayRan[day - 1] == -1);

								if (set) {

									StaticDateData end = info.getEnd();

									ServiceRunner runner = runningServices
											.get(id);
									boolean stopService = false;
									if (runner != null) {
										if (end.getHH() < c
												.get(Calendar.HOUR_OF_DAY)) {
											stopService = true;
										} else if (end.getHH() == c
												.get(Calendar.HOUR_OF_DAY)) {
											if (end.getMM() < c
													.get(Calendar.MINUTE)) {
												stopService = true;
											} else if (end.getMM() == c
													.get(Calendar.MINUTE)) {
												if (end.getSS() < c
														.get(Calendar.SECOND)) {
													stopService = true;
												}
											}

										}

										if (stopService) {
											info.resetEndDayRan(day - 1); // checked
																			// on
																			// this
																			// day
											info.resetStartDayRan(day - 1); // checked
																			// on
																			// this
																			// day,
																			// don't
																			// let
																			// start
																			// work
																			// if
																			// process
																			// started
																			// not
																			// because
																			// of
																			// schedule
																			// event
											if (runner.getServiceState() == ServiceState.PAUSE) { // interrupt
												stop(id, true);
											} else {
												stop(id, false);
											}
										}
									}

								}
							}
						}
						try {
							synchronized (mutex) {
								mutex.wait(5000); // sleep for 5 second for
													// next check
							}
						} catch (Exception e) {

						}
					}

				} catch (Exception e) {

				}

			}
		}

	}

	/**
	 * Thread manages schedules, stops task on base of schedule. it wait for 5
	 * seconds in each iteration. So your schedule may not immediately stop at
	 * same time
	 * 
	 * @author Kuldeep
	 */
	private class ServiceSchedulerStarter implements Runnable {

		Object mutex = new Object();

		@Override
		public void run() {
			Calendar c = Calendar.getInstance();
			int oldDay = c.get(Calendar.DAY_OF_WEEK);
			while (true) {
				try {
					c.setTimeInMillis(System.currentTimeMillis()); // don't
																	// create
																	// new
																	// calendar,
																	// it takes
																	// time
					Set<String> serviceIds = scheduledInformationMap.keySet();
					synchronized (scheduledInformationMap) { // block no
																// addition
																// at time of
																// checking
																// values
																// for startup
						for (String id : serviceIds) {

							List<ScheduledInformation> scheduledInfos = scheduledInformationMap
									.get(id);
							for (ScheduledInformation info : scheduledInfos) {
								if (!info.isActive()) {
									continue;
								}
								int day = c.get(Calendar.DAY_OF_WEEK);
								if (day != oldDay) {
									oldDay = day;
									info.resetStartStopDays();
								}
								int days[] = info.getDays();
								int dayRan[] = info.getStartDayRan();
								boolean set = (days[day - 1] == 0)
										&& (dayRan[day - 1] == -1); // once
																	// checked

								if (set) {

									StaticDateData start = info.getStart();

									ServiceRunner runner = runningServices
											.get(id);
									boolean startService = false;
									if (runner == null) {
										if (start.getHH() < c
												.get(Calendar.HOUR_OF_DAY)) {
											startService = true;
										} else if (start.getHH() == c
												.get(Calendar.HOUR_OF_DAY)) {
											if (start.getMM() < c
													.get(Calendar.MINUTE)) {
												startService = true;
											} else if (start.getMM() == c
													.get(Calendar.MINUTE)) {
												if (start.getSS() < c
														.get(Calendar.SECOND)) {
													startService = true;
												}
											}

										}
										if (startService) {
											info.resetStartDayRan(day - 1); // checked
																			// on
																			// this
																			// day
											startService(id, false);
										}
									} else {
										if (runner.getServiceState() == ServiceState.PAUSE) {
											info.resetStartDayRan(day - 1); // checked
																			// on
																			// this
																			// day
											// resume it
											runner.setServiceState(ServiceState.RESUME);
										}
									}

								}
							}
						}
						try {
							synchronized (mutex) {
								mutex.wait(5000); // sleep for 10 second for
													// next check
							}
						} catch (Exception e) {

						}
					}

				} catch (Exception e) {

				}

			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void stopAll() {
		stopAll(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void pauseAll() {
		Set<String> serviceIds = services.keySet();
		if (serviceIds != null && serviceIds.size() > 0) {
			for (String serviceId : serviceIds) {
				try {
					pause(serviceId, false, -1);
				} catch (MXTaskException e) {

				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void resumeAll() {
		Set<String> serviceIds = services.keySet();
		if (serviceIds != null && serviceIds.size() > 0) {
			for (String serviceId : serviceIds) {
				try {
					resume(serviceId);
				} catch (MXTaskException e) {

				}
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startAll() {
		Object[] serviceIds = services.keySet().toArray();
		if (serviceIds != null && serviceIds.length > 0) {
			for (Object serviceId : serviceIds) {
				try {
					start(serviceId.toString());
				} catch (MXTaskException e) {

				}
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addScheduleToAllTasks(String schedule) throws Exception {

		if (schedule == null || schedule.length() < 19) {
			throw new Exception(
					"Invalid value for schedule, should be in format HH-MM-SS-HH-MM-SS-dd,dd ... min length of string is expected 19");
		}
		StaticDateData start = new StaticDateData(Integer.parseInt(schedule
				.substring(0, 2)), Integer.parseInt(schedule.substring(3, 5)),
				Integer.parseInt(schedule.substring(6, 8)));
		StaticDateData end = new StaticDateData(Integer.parseInt(schedule
				.substring(9, 11)),
				Integer.parseInt(schedule.substring(12, 14)),
				Integer.parseInt(schedule.substring(15, 17)));
		String days[] = schedule.substring(18).split(",");

		int daysForSchedule[] = new int[days.length];
		for (int i = 0; i < days.length; i++) {
			daysForSchedule[i] = Integer.parseInt(days[i]);
		}

		Object[] serviceIds = services.keySet().toArray();
		List<ScheduledInformation> schedules = new ArrayList<ScheduledInformation>();
		if (serviceIds != null && serviceIds.length > 0) {
			for (Object serviceId : serviceIds) {
				try {
					schedules.add(validateSchedule(serviceId.toString(), start,
							end, daysForSchedule)); // all ok?
				} catch (MXTaskException e) {
					// throw e;
				}
			}
		}

		for (ScheduledInformation info : schedules) {
			List<ScheduledInformation> oldSchedules = scheduledInformationMap
					.get(info.getServiceId());
			oldSchedules.add(info);
			// addSchedule(serviceId.toString(), start, end, daysForSchedule);
		}
		if (schedules.size() != serviceIds.length) {
			throw new Exception(
					"Schedules not added to all services. may be there are some clashing schedules for some process or no process at all");
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateWaitInterval(String serviceId, long waitInterval)
			throws MXTaskException {
		if (!services.containsKey(serviceId)) {
			throw new MXTaskException(
					"Can't update wait interval for process. Service "
							+ serviceId + " is not registered service");
		}
		services.get(serviceId).setWaitIntervalForIteration(waitInterval);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getWaitInterval(String serviceId) throws MXTaskException {
		if (!services.containsKey(serviceId)) {
			throw new MXTaskException(
					"Can't update wait interval for process. Service "
							+ serviceId + " is not registered service");
		}
		return services.get(serviceId).getWaitIntervalForIteration();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getLastExecutionTimeTaken(String serviceId)
			throws MXTaskException {
		if (!lastExecutionTime.containsKey(serviceId)) {
			throw new MXTaskException("Service is not register. Service id "
					+ serviceId);
		}
		return lastExecutionTime.get(serviceId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void clearSchedules(String serviceId)
			throws MXTaskException {
		if (!this.scheduledInformationMap.containsKey(serviceId)) {
			throw new MXTaskException("Service is not register. Service id "
					+ serviceId);
		}
		scheduledInformationMap.remove(serviceId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void activateAllSchedules(String serviceId) throws MXTaskException {
		if (!scheduledInformationMap.containsKey(serviceId)) {
			throw new MXTaskException("No schedules to activate for service "
					+ serviceId);
		}
		List<ScheduledInformation> schedules = scheduledInformationMap
				.get(serviceId);
		for (ScheduledInformation schedule : schedules) {
			schedule.setActive(true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void inactivateAllSchedules(String serviceId) throws MXTaskException {
		if (!scheduledInformationMap.containsKey(serviceId)) {
			throw new MXTaskException("No schedules to activate for service "
					+ serviceId);
		}
		List<ScheduledInformation> schedules = scheduledInformationMap
				.get(serviceId);
		for (ScheduledInformation schedule : schedules) {
			schedule.setActive(false);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void activateSchedule(String serviceId, long scheduleId)
			throws MXTaskException {
		if (!scheduledInformationMap.containsKey(serviceId)) {
			throw new MXTaskException("No schedules for service " + serviceId);
		}

		List<ScheduledInformation> schedules = scheduledInformationMap
				.get(serviceId);
		boolean found = false;
		for (ScheduledInformation schedule : schedules) {
			if (schedule.getId() == scheduleId) {
				schedule.setActive(true);
				found = true;
			}
		}
		if (!found) {
			throw new MXTaskException("Schedule ID " + scheduleId
					+ " is invalid for " + serviceId);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void inactivateSchedule(String serviceId, long scheduleId)
			throws MXTaskException {
		if (!scheduledInformationMap.containsKey(serviceId)) {
			throw new MXTaskException("No schedules for service " + serviceId);
		}

		List<ScheduledInformation> schedules = scheduledInformationMap
				.get(serviceId);
		boolean found = false;
		for (ScheduledInformation schedule : schedules) {
			if (schedule.getId() == scheduleId) {
				schedule.setActive(false);
				found = true;
			}
		}
		if (!found) {
			throw new MXTaskException("Schedule ID " + scheduleId
					+ " is invalid for " + serviceId);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(Runnable command) {
		try {
			execute("Thread-" + createIdForTask(), command);
		} catch (MXTaskException e) {
			throw new RuntimeException(e.getMessage());
		}

	}

	private long createIdForTask() {
		return ++taskId;
	}

	@Override
	public String generateTasksReport() {
		StringBuffer buffer = new StringBuffer();
		Set<String> ids = services.keySet();
		long cpuTime = 0;
		long idleTime = 0;
		for (String id : ids) {
			MXExecutableTaskConfiguration config = services.get(id);
			buffer.append("Task Id=" + id + ";");
			buffer.append("State=" + config.getState() + ";");
			buffer.append("Execution Count=" + config.getExecutionCount() + ";");
			cpuTime += config.getCpuTime();
			idleTime += config.getIdleTime();

			buffer.append("CPU Time=" + getPrintableTime(config.getCpuTime())
					+ ";");
			buffer.append("Idle Time=" + getPrintableTime(config.getIdleTime())
					+ ";");
			buffer.append("Wait Interval="
					+ config.getWaitIntervalForIteration() + ";");
			buffer.append("Last Execution Duration (mills)="
					+ lastExecutionTime.get(id) + ";");
			buffer.append("Task Start Time " + config.getStartTime().toString()
					+ ";\n");

		}
		buffer.append("Total CPU time (all thread in current executor)="
				+ getPrintableTime(cpuTime) + "\n");
		buffer.append("Total Idle (all thread in current executor)="
				+ getPrintableTime(idleTime) + "\n");

		return buffer.toString();
	}

}
