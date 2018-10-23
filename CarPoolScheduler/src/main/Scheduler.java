package main;

import java.time.LocalTime;
import java.util.List;

public class Scheduler {
	public static ScheduleResult tryInsertInCurrentVehicleSchedule(Vehicle v, Passenger p) {
		List<Event> currScheduleEvents = v.getLatestSchedule().getEvents();
		List<Event> prevScheduleEvents = null;
		if (v.getScheduleCount() > 0) {
			prevScheduleEvents =  v.getSchedule(v.getScheduleCount() - 1).getEvents();
		}
		ScheduleResult continuousInsertion = tryInsertContinuously(prevScheduleEvents, currScheduleEvents, p);
		ScheduleResult nonContinuousInsertion = tryInsertNonContinuously(prevScheduleEvents, currScheduleEvents, p);
		if (continuousInsertion.wasSuccessful) {
			if (nonContinuousInsertion.wasSuccessful) {
				if (continuousInsertion.bestMaxDelay > nonContinuousInsertion.bestMaxDelay) {
					return continuousInsertion;
				} else {
					return nonContinuousInsertion;
				}
			} else {
				return continuousInsertion;
			}
		} else if (nonContinuousInsertion.wasSuccessful) {
			return nonContinuousInsertion;
		}
		return null;
	}

	private static ScheduleResult tryInsertContinuously(List<Event> prevScheduleEvents,
			List<Event> currScheduleEvents, Passenger p) {
		boolean wasSuccessful = false;
		int insertionIdx = -1;
		int bestMaxDelay = -1;
		Event pickup = null;
		Event dropoff = null;
		for (int i = 0; i <= currScheduleEvents.size(); i++) {
			if (i == 0) {
				LocalTime potentialPickupTime = p.earliestPickup;
				int timeFromPickupToDropoff = DistanceUtil.calculateTravelTime(
						p.x_destination, p.y_destination, p.x_origin, p.y_origin);
				if (prevScheduleEvents != null) {
					Event previousDropoff = prevScheduleEvents.get(prevScheduleEvents.size() -1);
					int timeFromPrevDropoffToPickup = DistanceUtil.calculateTravelTime(
							previousDropoff.x, previousDropoff.y, p.x_origin, p.y_origin);
					potentialPickupTime = previousDropoff.getActualTime().plusMinutes(timeFromPrevDropoffToPickup);
					if (potentialPickupTime.plusMinutes(timeFromPickupToDropoff).isAfter(p.latestDropOff)) {
						continue;
					}
				}
				
				int timeFromDropoffToPrev = DistanceUtil.calculateTravelTime(
						currScheduleEvents.get(i).x, currScheduleEvents.get(i).y, p.x_destination, p.y_destination);
				int timeDelayDueToLatePickup = LocalTimeUtil.minus(potentialPickupTime, currScheduleEvents.get(i).getActualTime());
				if ((timeFromPickupToDropoff + timeFromDropoffToPrev + timeDelayDueToLatePickup) < currScheduleEvents.get(i).getMaxDelay()) {
					insertionIdx = i;
					int newMaxDelayAtPrev = currScheduleEvents.get(i).getMaxDelay() - (timeFromPickupToDropoff + timeFromDropoffToPrev);
					int maxDelayAtDropoff = LocalTimeUtil.minus(p.latestDropOff,
							potentialPickupTime.plusMinutes(timeFromPickupToDropoff));
					bestMaxDelay = Math.min(newMaxDelayAtPrev, maxDelayAtDropoff);
					wasSuccessful = true;
					pickup = new Event(p.tripNumber, p.name, "pickup", p.x_origin, p.y_origin,
							potentialPickupTime, p.earliestPickup, 0, Integer.MAX_VALUE);
					dropoff = new Event(p.tripNumber, p.name, "dropoff", p.x_destination, p.y_destination,
							potentialPickupTime.plusMinutes(timeFromPickupToDropoff),
							p.latestDropOff, timeFromPickupToDropoff, maxDelayAtDropoff);
				}
			} else if (i == currScheduleEvents.size()) {
				int prev = i - 1;
				int timeFromPrevToPickup = DistanceUtil.calculateTravelTime(
						currScheduleEvents.get(prev).x, currScheduleEvents.get(prev).y, p.x_origin, p.y_origin);
				if (timeFromPrevToPickup < LocalTimeUtil.minus(p.earliestPickup, currScheduleEvents.get(i-1).getActualTime())) {
					timeFromPrevToPickup = LocalTimeUtil.minus(p.earliestPickup, currScheduleEvents.get(i-1).getActualTime());
				}
				int timeFromPickupToDropoff = DistanceUtil.calculateTravelTime(
						p.x_destination, p.y_destination, p.x_origin, p.y_origin);
				LocalTime actualDropoffTime = currScheduleEvents.get(prev).getActualTime()
						.plusMinutes(timeFromPrevToPickup + timeFromPickupToDropoff);
				int maxDelayAtDropoff = LocalTimeUtil.minus(p.latestDropOff, actualDropoffTime);
				if (actualDropoffTime.isBefore(p.latestDropOff)) {
					int effectiveMaxDelay = Math.min(currScheduleEvents.get(0).getMaxDelay(),
							maxDelayAtDropoff);
					if (effectiveMaxDelay > bestMaxDelay) {
						insertionIdx = i;
						wasSuccessful = true;
						pickup = new Event(p.tripNumber, p.name, "pickup", p.x_origin, p.y_origin,
								currScheduleEvents.get(prev).getActualTime().plusMinutes(timeFromPrevToPickup),
								p.earliestPickup, timeFromPrevToPickup, Integer.MAX_VALUE);
						dropoff = new Event(p.tripNumber, p.name, "dropoff", p.x_destination, p.y_destination,
								currScheduleEvents.get(prev).getActualTime().plusMinutes(timeFromPrevToPickup + timeFromPickupToDropoff),
								p.latestDropOff, timeFromPickupToDropoff, maxDelayAtDropoff);
					}
				}
			} else {
				int prev = i - 1;
				int next = i;
				int timeFromPrevToPickup = DistanceUtil.calculateTravelTime(
						currScheduleEvents.get(prev).x, currScheduleEvents.get(prev).y, p.x_origin, p.y_origin);
				if (timeFromPrevToPickup < LocalTimeUtil.minus(p.earliestPickup, currScheduleEvents.get(i-1).getActualTime())) {
					timeFromPrevToPickup = LocalTimeUtil.minus(p.earliestPickup, currScheduleEvents.get(i-1).getActualTime());
				}
				int timeFromPickupToDropoff = DistanceUtil.calculateTravelTime(
						p.x_destination, p.y_destination, p.x_origin, p.y_origin);
				LocalTime timeToDestination = currScheduleEvents.get(prev).getActualTime()
						.plusMinutes(timeFromPickupToDropoff)
						.plusMinutes(timeFromPrevToPickup);
				if (timeToDestination.isAfter(p.latestDropOff)) {
					break;
				}
				int timeFromDropoffToNext = DistanceUtil.calculateTravelTime(
						currScheduleEvents.get(next).x, currScheduleEvents.get(next).y, p.x_destination, p.y_destination);
				int newTotalTimeFromPrevToNext = LocalTimeUtil.minus(timeToDestination.plusMinutes(timeFromDropoffToNext),
						currScheduleEvents.get(prev).getActualTime());
				int newMaxDelayAtNext = currScheduleEvents.get(next).getMaxDelay() - 
						(newTotalTimeFromPrevToNext - currScheduleEvents.get(next).getTimeFromPreviousStop());
				int maxDelayAtDropoff = LocalTimeUtil.minus(p.latestDropOff, timeToDestination);
				int effectiveMaxDelay = Math.min(currScheduleEvents.get(0).getMaxDelay(),
						Math.min(newMaxDelayAtNext, maxDelayAtDropoff));
				if (effectiveMaxDelay > bestMaxDelay) {
					insertionIdx = i;
					bestMaxDelay = effectiveMaxDelay;
					wasSuccessful = true;
					pickup = new Event(p.tripNumber, p.name, "pickup", p.x_origin, p.y_origin,
							currScheduleEvents.get(prev).getActualTime().plusMinutes(timeFromPrevToPickup),
							p.earliestPickup, timeFromPrevToPickup, Integer.MAX_VALUE);
					dropoff = new Event(p.tripNumber, p.name, "dropoff", p.x_destination, p.y_destination,
							currScheduleEvents.get(prev).getActualTime().plusMinutes(timeFromPrevToPickup + timeFromPickupToDropoff),
							p.latestDropOff, timeFromPickupToDropoff, maxDelayAtDropoff);
				}
			}
		}
		if (insertionIdx != -1) {
			return new ScheduleResult(insertionIdx, insertionIdx + 1, wasSuccessful, bestMaxDelay, pickup, dropoff);
		}
		return new ScheduleResult(-1, -1, false, -1, null, null);
	}

	private static ScheduleResult tryInsertNonContinuously(List<Event> prevScheduleEvents,
			List<Event> currScheduleEvents, Passenger p) {
		boolean wasSuccessful = false;
		int pickupInsertionIdx = -1;
		int dropoffInsertionIdx = -1;
		int bestMaxDelay = -1;
		Event pickup = null;
		Event dropoff = null;
		for (int i = 0; i < currScheduleEvents.size(); i++) {
			if (i == 0) {
				LocalTime potentialPickupTime = p.earliestPickup;
				if (prevScheduleEvents != null) {
					Event previousDropoff = prevScheduleEvents.get(prevScheduleEvents.size() -1);
					int timeFromPrevDropoffToPickup = DistanceUtil.calculateTravelTime(
							previousDropoff.x, previousDropoff.y, p.x_origin, p.y_origin);
					potentialPickupTime = previousDropoff.getActualTime().plusMinutes(timeFromPrevDropoffToPickup);
					if (potentialPickupTime.isAfter(p.latestDropOff)) {
						break;
					}
				}
				int timeFromPickupToPrev = DistanceUtil.calculateTravelTime(
						currScheduleEvents.get(i).x, currScheduleEvents.get(i).y, p.x_origin, p.y_origin);
				int timeDelayDueToLatePickup = LocalTimeUtil.minus(potentialPickupTime, currScheduleEvents.get(i).getActualTime());
				if (timeFromPickupToPrev + timeDelayDueToLatePickup < currScheduleEvents.get(i).getMaxDelay()) {
					for (int prev = 0; prev < currScheduleEvents.size(); prev++) {
						if (prev != currScheduleEvents.size() - 1) {
							int next = prev + 1;
							int timeFromPrevToDropoff = DistanceUtil.calculateTravelTime(
									currScheduleEvents.get(prev).x, currScheduleEvents.get(prev).y, p.x_destination, p.y_destination);
							int timeFromDropoffToNext = DistanceUtil.calculateTravelTime(
									p.x_destination, p.y_destination, currScheduleEvents.get(next).x, currScheduleEvents.get(next).y);
							LocalTime timeToDestination = currScheduleEvents.get(prev).getActualTime()
									.plusMinutes(timeDelayDueToLatePickup)
									.plusMinutes(timeFromPickupToPrev)
									.plusMinutes(timeFromPrevToDropoff);
							if (timeToDestination.isAfter(p.latestDropOff)) {
								break;
							}
							int newTotalTimeFromPrevToNext = timeFromPrevToDropoff + timeFromPickupToPrev
									+ timeFromDropoffToNext + timeDelayDueToLatePickup;
							int newMaxDelayAtNext = currScheduleEvents.get(next).getMaxDelay() - 
									(newTotalTimeFromPrevToNext - currScheduleEvents.get(next).getTimeFromPreviousStop());
							int maxDelayAtDropoff = LocalTimeUtil.minus(p.latestDropOff, timeToDestination);
							int effectiveMaxDelay = Math.min(currScheduleEvents.get(0).getMaxDelay(),
									Math.min(newMaxDelayAtNext, maxDelayAtDropoff));
							if (effectiveMaxDelay > bestMaxDelay) {
								pickupInsertionIdx = 0;
								dropoffInsertionIdx = i+2;
								bestMaxDelay = effectiveMaxDelay;
								wasSuccessful = true;
								pickup = new Event(p.tripNumber, p.name, "pickup", p.x_origin, p.y_origin,
										potentialPickupTime, p.earliestPickup, 0, Integer.MAX_VALUE);
								dropoff = new Event(p.tripNumber, p.name, "dropoff", p.x_destination, p.y_destination,
										timeToDestination,
										p.latestDropOff, timeFromPrevToDropoff, maxDelayAtDropoff);
							}
						} else {
							int timeFromPrevToDropoff = DistanceUtil.calculateTravelTime(
									p.x_destination, p.y_destination, currScheduleEvents.get(prev).x, currScheduleEvents.get(prev).y);
							LocalTime actualDropoffTime = currScheduleEvents.get(prev).getActualTime()
									.plusMinutes(timeFromPickupToPrev + timeFromPrevToDropoff + timeDelayDueToLatePickup);
							int maxDelayAtDropoff = LocalTimeUtil.minus(p.latestDropOff, actualDropoffTime);
							if (actualDropoffTime.isBefore(p.latestDropOff)) {
								int effectiveMaxDelay = Math.min(currScheduleEvents.get(0).getMaxDelay(),
										maxDelayAtDropoff);
								if (effectiveMaxDelay > bestMaxDelay) {
									pickupInsertionIdx = 0;
									dropoffInsertionIdx = i+2;
									wasSuccessful = true;
									pickup = new Event(p.tripNumber, p.name, "pickup", p.x_origin, p.y_origin,
											potentialPickupTime, p.earliestPickup, 0, Integer.MAX_VALUE);
									dropoff = new Event(p.tripNumber, p.name, "dropoff", p.x_destination, p.y_destination,
											actualDropoffTime,
											p.latestDropOff, timeFromPrevToDropoff, maxDelayAtDropoff);
								}
							}
						}
					}
				}
			} else {
				int timeFromPickupToPrev = DistanceUtil.calculateTravelTime(
						currScheduleEvents.get(i-1).x, currScheduleEvents.get(i-1).y, p.x_origin, p.y_origin);
				if (timeFromPickupToPrev < LocalTimeUtil.minus(p.earliestPickup, currScheduleEvents.get(i-1).getActualTime())) {
					timeFromPickupToPrev = LocalTimeUtil.minus(p.earliestPickup, currScheduleEvents.get(i-1).getActualTime());
				}
				int timeFromPickupToNext = DistanceUtil.calculateTravelTime(
						currScheduleEvents.get(i).x, currScheduleEvents.get(i).y, p.x_origin, p.y_origin);
				if (p.earliestPickup.isAfter(currScheduleEvents.get(i).getActualTime())) {
					timeFromPickupToNext = LocalTimeUtil.minus(p.earliestPickup.plusMinutes(timeFromPickupToNext),
							currScheduleEvents.get(i).getActualTime());
				}
				int delta = (timeFromPickupToNext + timeFromPickupToPrev) - currScheduleEvents.get(i).getTimeFromPreviousStop();
				if (delta < currScheduleEvents.get(i).getMaxDelay()) {
					for (int prev = i; prev < currScheduleEvents.size(); prev++) {
						if (prev != currScheduleEvents.size() - 1) {
							int next = prev + 1;
							int timeFromPrevToDropoff = DistanceUtil.calculateTravelTime(
									currScheduleEvents.get(prev).x, currScheduleEvents.get(prev).y, p.x_destination, p.y_destination);
							int timeFromDropoffToNext = DistanceUtil.calculateTravelTime(
									p.x_destination, p.y_destination, currScheduleEvents.get(next).x, currScheduleEvents.get(next).y);
							LocalTime timeToDestination = currScheduleEvents.get(prev).getActualTime()
									.plusMinutes(timeFromPickupToPrev)
									.plusMinutes(timeFromPickupToNext)
									.plusMinutes(timeFromPrevToDropoff);
							if (timeToDestination.isAfter(p.latestDropOff)) {
								break;
							}
							int newTotalTimeFromPrevToNext = timeFromPrevToDropoff + timeFromPickupToPrev
									+ timeFromDropoffToNext + timeFromPickupToNext;
							int newMaxDelayAtNext = currScheduleEvents.get(next).getMaxDelay() - 
									(newTotalTimeFromPrevToNext - currScheduleEvents.get(next).getTimeFromPreviousStop());
							int maxDelayAtDropoff = LocalTimeUtil.minus(p.latestDropOff, timeToDestination);
							int effectiveMaxDelay = Math.min(currScheduleEvents.get(0).getMaxDelay(),
									Math.min(newMaxDelayAtNext, maxDelayAtDropoff));
							if (effectiveMaxDelay > bestMaxDelay) {
								pickupInsertionIdx = i;
								dropoffInsertionIdx = i+2;
								bestMaxDelay = effectiveMaxDelay;
								wasSuccessful = true;
								pickup = new Event(p.tripNumber, p.name, "pickup", p.x_origin, p.y_origin,
										currScheduleEvents.get(i -1).getActualTime().plusMinutes(timeFromPickupToPrev),
										p.earliestPickup, timeFromPickupToPrev, Integer.MAX_VALUE);
								dropoff = new Event(p.tripNumber, p.name, "dropoff", p.x_destination, p.y_destination,
										timeToDestination,
										p.latestDropOff, timeFromPrevToDropoff, maxDelayAtDropoff);
							}
						} else {
							int timeFromPrevToDropoff = DistanceUtil.calculateTravelTime(
									p.x_destination, p.y_destination, currScheduleEvents.get(prev).x, currScheduleEvents.get(prev).y);
							LocalTime actualDropoffTime = currScheduleEvents.get(prev).getActualTime()
									.plusMinutes(timeFromPickupToPrev + timeFromPickupToNext+ timeFromPrevToDropoff);
							int maxDelayAtDropoff = LocalTimeUtil.minus(p.latestDropOff, actualDropoffTime); 
							if (actualDropoffTime.isBefore(p.latestDropOff)) {
								int effectiveMaxDelay = Math.min(currScheduleEvents.get(0).getMaxDelay(),
										LocalTimeUtil.minus(p.latestDropOff, actualDropoffTime));
								if (effectiveMaxDelay > bestMaxDelay) {
									pickupInsertionIdx = i;
									dropoffInsertionIdx = i+2;
									wasSuccessful = true;
									pickup = new Event(p.tripNumber, p.name, "pickup", p.x_origin, p.y_origin,
											currScheduleEvents.get(i -1).getActualTime().plusMinutes(timeFromPickupToPrev),
											p.earliestPickup, 0, Integer.MAX_VALUE);
									dropoff = new Event(p.tripNumber, p.name, "dropoff", p.x_destination, p.y_destination,
											actualDropoffTime,
											p.latestDropOff, timeFromPrevToDropoff, maxDelayAtDropoff);
								}
							}
						}
					}
				}
			}
		}
		if (wasSuccessful) {
			return new ScheduleResult(pickupInsertionIdx, dropoffInsertionIdx, wasSuccessful, bestMaxDelay, pickup, dropoff);
		}
		return new ScheduleResult(-1, -1, false, -1, null, null);
	}
}
