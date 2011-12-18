package net.paxcel.labs.mxexecutable.util;

/**
 * Class holding time related data
 * 
 * @author Kuldeep
 * 
 */
public class StaticDateData implements Cloneable {

	private int HH;
	private int MM;
	private int SS;

	public StaticDateData(int HH, int MM, int SS) {

		if (HH > 24 || MM > 60 || SS > 60) {
			throw new RuntimeException("Date values were invalid. HH (<=24) "
					+ HH + " MM (<=60) " + MM + " SS (<=60) " + SS);
		}
		this.HH = HH;
		this.MM = MM;
		this.SS = SS;
	}

	public Object clone() {
		try {
			StaticDateData cloned = (StaticDateData) super.clone();
			return cloned;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public int getHH() {
		return HH;
	}

	public void setHH(int hh) {
		if (hh > 24) {
			throw new RuntimeException("Invalid Hours. HH should be (<=24) "
					+ hh);
		}
		this.HH = hh;
	}

	public int getMM() {
		return MM;
	}

	public void setMM(int mm) {
		if (MM > 60) {
			throw new RuntimeException(
					"Invalid Minutes value. MMshould be (<=60) " + mm);
		}

		this.MM = mm;
	}

	public int getSS() {
		return SS;
	}

	public void setSS(int ss) {
		if (ss > 60) {
			throw new RuntimeException("Invalid Seconds. SS should be (<=60) "
					+ ss);
		}

		this.SS = ss;
	}

	public boolean before(StaticDateData another) {
		return Integer.parseInt(this.toString()) < Integer.parseInt(another
				.toString());

	}

	public String toString() {
		return appendZero(HH) + "" + appendZero(MM) + "" + appendZero(SS);
	}

	private String appendZero(int value) {
		if (value < 10) {
			return "0" + value;
		}
		return "" + value;
	}
}
