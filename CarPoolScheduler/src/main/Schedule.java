package main;

import java.util.List;

public class Schedule {
	private List<Event> events;

	public Schedule(List<Event> events) {
		this.events = events;
	}

	public void print() {
		events.stream().forEach(Event::print);
	}

	public int size() {
		return events.size();
	}

	public void add(Event e) {
		if (e.eventType.equals("dropoff")) {
			updateMaxDelayOfPreviousEvents(e.getMaxDelay());
		}
		events.add(e);
	}

	public void add(int idx, Event e) {
		if (idx == events.size()) {
			updateMaxDelayOfPreviousEvents(e.getMaxDelay());
		} else {
			if (events.get(idx).getMaxDelay() < e.getMaxDelay()) {
				e.updateMaxDelay(events.get(idx).getMaxDelay());
			}
			if (e.eventType.equals("dropoff")) {
				updateMaxDelayOfPreviousEvents(e.getMaxDelay(), idx);
			}
			int timeToNextStop = DistanceUtil.calculateTravelTime(events.get(idx).x,
					events.get(idx).y, e.x, e.y);
			int maxDelayDelta = LocalTimeUtil.minus(e.getActualTime().plusMinutes(timeToNextStop),
					events.get(idx).getActualTime());
			reduceMaxDelayOfNextEventsByDelta(idx, maxDelayDelta);
			increaseActualTimeOfNextEventsByDelta(idx, maxDelayDelta);
		}
		events.add(idx, e);
	}

	public List<Event> getEvents() {
		return events;
	}

	private void updateMaxDelayOfPreviousEvents(int maxDelay) {
		updateMaxDelayOfPreviousEvents(maxDelay, events.size());
	}

	private void updateMaxDelayOfPreviousEvents(int maxDelay, int idx) {
		for (int i = 0; i < idx; i++) {
			if (maxDelay < events.get(i).getMaxDelay()) {
				events.get(i).updateMaxDelay(maxDelay);
			}
		}
	}
	
	private void reduceMaxDelayOfNextEventsByDelta(int idx, int maxDelayDelta) {
		for (int i = idx; i <= events.size() - 1; i++) {
			events.get(i).updateMaxDelay(events.get(i).getMaxDelay() - maxDelayDelta);
		}
	}

	private void increaseActualTimeOfNextEventsByDelta(int idx, int maxDelayDelta) {
		for (int i = idx; i <= events.size() - 1; i++) {
			events.get(i).updateActualTime(events.get(i).getActualTime().plusMinutes(maxDelayDelta));
		}
	}
}
