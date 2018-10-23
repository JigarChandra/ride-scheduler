package main;

import java.time.LocalTime;

public class Event {
	public final int tripNumber;
	public final String passenger;
	public final String eventType;
	public final int x;
	public final int y;
	private LocalTime actualTime;
	public final LocalTime desiredTime;
	private int timeFromPreviousStop;
	private int maxDelay;
	
	public Event(int tripNumber, String passenger, String event, int x, int y, LocalTime actualTime,
			LocalTime desiredTime, int timeFromPreviousStop, int maxDelay) {
		this.tripNumber = tripNumber;
		this.passenger = passenger;
		this.eventType = event;
		this.x = x;
		this.y = y;
		this.actualTime = actualTime;
		this.desiredTime = desiredTime;
		this.timeFromPreviousStop = timeFromPreviousStop;
		this.maxDelay = maxDelay;
	}
	
	public void updateActualTime(LocalTime actualTime) {
		this.actualTime = actualTime;
	}
	
	public void updateTimeFromPreviousStop(int timeFromPreviousStop) {
		this.timeFromPreviousStop = timeFromPreviousStop;
	}
	
	public void updateMaxDelay(int maxDelay) {
		this.maxDelay = maxDelay;
	}
	
	public LocalTime getActualTime() {
		return actualTime;
	}
	
	public int getTimeFromPreviousStop() {
		return timeFromPreviousStop;
	}
	
	public int getMaxDelay() {
		return maxDelay;
	}
	
	public void print() {
		System.out.println("Trip# " + tripNumber + " " + eventType + " " + passenger
				+ " at time: " + actualTime + " at coordinates: " + x + " , " + y);
	}
}
