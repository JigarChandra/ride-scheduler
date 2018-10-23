package main;

public class ScheduleResult {
	public final int pickupInsertionIdx;
	public final int dropoffInsertionIdx;
	public final boolean wasSuccessful;
	public final int bestMaxDelay;
	public final Event pickup;
	public final Event dropoff;
	
	public ScheduleResult(int insertionIdx, int dropoffInsertionIdx,
			boolean wasSuccessful, int maxDelayReduction, Event pickup, Event dropoff) {
		this.pickupInsertionIdx = insertionIdx;
		this.dropoffInsertionIdx = dropoffInsertionIdx;
		this.wasSuccessful = wasSuccessful;
		this.bestMaxDelay = maxDelayReduction;
		this.pickup = pickup;
		this.dropoff = dropoff;
	}
}
