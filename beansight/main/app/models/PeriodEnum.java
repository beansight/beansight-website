package models;

public enum PeriodEnum {
	
	INFINITE(0l), THREE_MONTHS(90l*24l*60l*60l*1000l);

	private long timePeriod;
	
	private PeriodEnum(long time) {
		this.timePeriod = time;
	}
	
	public long getTimePeriod() {
		return timePeriod;
	}
	
}
