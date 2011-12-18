package net.paxcel.labs.mxexecutable.beans;

import net.paxcel.labs.mxexecutable.util.StaticDateData;

/**
 * Bean to hold scheduled information
 * 
 * @author Kuldeep
 * 
 */
public class ScheduledInformation implements Cloneable {
	private String serviceId;
	private StaticDateData start;
	private StaticDateData end;
	private int days[] = { -1, -1, -1, -1, -1, -1, -1 };
	private int startDayRan[] = { -1, -1, -1, -1, -1, -1, -1 };

	public Object clone() {
		try {
			ScheduledInformation cloned = (ScheduledInformation) super.clone();
			cloned.start = (StaticDateData) start.clone();
			cloned.end = (StaticDateData) end.clone();
			return cloned;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public int[] getStartDayRan() {
		return startDayRan;
	}

	public void setStartDayRan(int[] startDayRan) {
		this.startDayRan = startDayRan;
	}

	public int[] getEndDayRan() {
		return endDayRan;
	}

	public void setEndDayRan(int[] endDayRan) {
		this.endDayRan = endDayRan;
	}

	private int endDayRan[] = { -1, -1, -1, -1, -1, -1, -1 };
	private long id;
	private boolean active = true;

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void resetStartDayRan(int dayRanIndex) {
		startDayRan = new int[] { -1, -1, -1, -1, -1, -1, -1 };
		startDayRan[dayRanIndex] = 0;
	}

	public void resetEndDayRan(int dayRanIndex) {
		endDayRan = new int[] { -1, -1, -1, -1, -1, -1, -1 };
		endDayRan[dayRanIndex] = 0;
	}

	public ScheduledInformation(long id, String serviceId,
			StaticDateData start, StaticDateData end, int... days) {

		this.serviceId = serviceId;
		this.start = start;
		this.end = end;
		for (int i = 0; i < days.length; i++) {
			this.days[days[i] - 1] = 0; // set 0 at index provided
		}

		this.id = id;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public StaticDateData getStart() {
		return start;
	}

	public void setStart(StaticDateData start) {
		this.start = start;
	}

	public StaticDateData getEnd() {
		return end;
	}

	public void setEnd(StaticDateData end) {
		this.end = end;
	}

	public int[] getDays() {
		return days;
	}

	public void setDays(int[] days) {
		this.days = days;
	}

	public void resetStartStopDays() {
		endDayRan = new int[] { -1, -1, -1, -1, -1, -1, -1 };
		startDayRan = new int[] { -1, -1, -1, -1, -1, -1, -1 };
	}

	public String toString() {
		String returnValue = serviceId + "-" + start.toString() + "-"
				+ end.toString() + "-";
		int i = 0;
		for (i = 0; i < days.length - 1; i++) {
			if (days[i] == 0) {
				returnValue += (i + 1) + ",";
			}
		}

		if (days[i] == 0) {
			returnValue += (i + 1) + ",";
		}
		returnValue += " Schedule ID (" + id + ")";
		if (active) {
			returnValue += " (Active)";
		} else {
			returnValue += " (Inactive)";
		}
		return returnValue;
	}

	public boolean between(ScheduledInformation another) {

		StaticDateData anoStart = another.getStart();
		StaticDateData anoEnd = another.getEnd();
		int anoValueStart = Integer.parseInt(anoStart.toString());
		int anoValueEnd = Integer.parseInt(anoEnd.toString());
		int valueStart = Integer.parseInt(getStart().toString());
		int valueEnd = Integer.parseInt(getEnd().toString());

		boolean between = false;
		if (valueStart >= anoValueStart && valueStart <= anoValueEnd) {
			between = true;
		}
		if (!between) {
			if (valueEnd >= anoValueStart && valueEnd <= anoValueEnd) {
				between = true;
			}
		}

		return between;
	}

}
