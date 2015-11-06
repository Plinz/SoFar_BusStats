package connection;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LegCollection {
	private BusLocation pointA;
	private BusLocation pointB;
	private Duration avgTravelTime;
	private HashMap<Duration, Integer> avgStopsByTime;
	private double avgDistance;
	List<TravelLeg> logedLegs;
	
	
	public LegCollection(TravelLeg tl) {
		this.pointA = tl.getPointA();
		this.pointB = tl.getPointB();
		this.avgTravelTime = tl.getTravelTime();
		this.avgStopsByTime = tl.getStopsByTime();
		this.avgDistance = tl.getDistance();
		this.logedLegs = new ArrayList<TravelLeg>();
		this.logedLegs.add(tl);
	}
	public BusLocation getPointA() {
		return pointA;
	}
	public void setPointA(BusLocation pointA) {
		this.pointA = pointA;
	}
	public BusLocation getPointB() {
		return pointB;
	}
	public void setPointB(BusLocation pointB) {
		this.pointB = pointB;
	}
	public Duration getAvgTravelTime() {
		return avgTravelTime;
	}
	public void setAvgTravelTime(Duration avgTravelTime) {
		this.avgTravelTime = avgTravelTime;
	}
	public HashMap<Duration, Integer> getAvgStopsByTime() {
		return avgStopsByTime;
	}
	public void setAvgStopsByTime(HashMap<Duration, Integer> avgStopsByTime) {
		this.avgStopsByTime = avgStopsByTime;
	}
	public double getAvgDistance() {
		return avgDistance;
	}
	public void setAvgDistance(double avgDistance) {
		this.avgDistance = avgDistance;
	}
	public List<TravelLeg> getLogedLegs() {
		return logedLegs;
	}
	public void setLogedLegs(List<TravelLeg> logedLegs) {
		this.logedLegs = logedLegs;
	}
	public void add(TravelLeg tl) {
		Duration tmpTravelTime = tl.getTravelTime();
		double tmpDistance = tl.getDistance();
		HashMap<Duration, Integer> tmpStopsByTime = tl.getStopsByTime();
		for (TravelLeg tmpTL : logedLegs){
			tmpTravelTime.plus(tmpTL.getTravelTime());
			tmpDistance+=tmpTL.getDistance();
			tmpStopsByTime.put(Duration.ofMinutes(1), tmpStopsByTime.get(Duration.ofMinutes(1))+tmpTL.getStopsByTime().get(Duration.ofMinutes(1)));
			tmpStopsByTime.put(Duration.ofMinutes(5), tmpStopsByTime.get(Duration.ofMinutes(5))+tmpTL.getStopsByTime().get(Duration.ofMinutes(5)));
			tmpStopsByTime.put(Duration.ofMinutes(10), tmpStopsByTime.get(Duration.ofMinutes(10))+tmpTL.getStopsByTime().get(Duration.ofMinutes(10)));
		}
		this.avgTravelTime = tmpTravelTime.dividedBy(this.logedLegs.size());
		this.avgDistance = tmpDistance/this.logedLegs.size();
		tmpStopsByTime.put(Duration.ofMinutes(1), tmpStopsByTime.get(Duration.ofMinutes(1))/this.logedLegs.size());
		tmpStopsByTime.put(Duration.ofMinutes(5), tmpStopsByTime.get(Duration.ofMinutes(5))/this.logedLegs.size());
		tmpStopsByTime.put(Duration.ofMinutes(10), tmpStopsByTime.get(Duration.ofMinutes(10))/this.logedLegs.size());
	}
	
	
	

}
