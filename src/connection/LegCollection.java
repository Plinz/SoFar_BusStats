package connection;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LegCollection {
	private BusLocation pointA;
	private BusLocation pointB;
	private Duration avgTravelTime;
	private Duration sqrtAvgTravelTime;
	private HashMap<Duration, Integer> avgStopsByTime;
	private int avgDistance;
	private Duration minTravelTime;
	private Duration maxTravelTime;
	List<TravelLeg> logedLegs;
	
	
	public LegCollection(TravelLeg tl) {
		this.pointA = tl.getPointA();
		this.pointB = tl.getPointB();
		this.avgTravelTime = Duration.ofSeconds(0);
		this.avgStopsByTime = new HashMap<Duration, Integer>();
		this.avgStopsByTime.put(Duration.ofMinutes(1), 0);
		this.avgStopsByTime.put(Duration.ofMinutes(5), 0);
		this.avgStopsByTime.put(Duration.ofMinutes(10), 0);
		this.avgDistance = 0;
		this.minTravelTime = Duration.ofDays(99);
		this.maxTravelTime = Duration.ofSeconds(0);
		this.sqrtAvgTravelTime = Duration.ofSeconds(0);
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
	public int getAvgDistance() {
		return avgDistance;
	}
	public void setAvgDistance(int avgDistance) {
		this.avgDistance = avgDistance;
	}
	public List<TravelLeg> getLogedLegs() {
		return logedLegs;
	}
	public void setLogedLegs(List<TravelLeg> logedLegs) {
		this.logedLegs = logedLegs;
	}
	public Duration getMinTravelTime() {
		return minTravelTime;
	}
	public void setMinTravelTime(Duration minTravelTime) {
		this.minTravelTime = minTravelTime;
	}
	public Duration getMaxTravelTime() {
		return maxTravelTime;
	}
	public void setMaxTravelTime(Duration maxTravelTime) {
		this.maxTravelTime = maxTravelTime;
	}
	public Duration getSqrtAvgTravelTime() {
		return sqrtAvgTravelTime;
	}
	public void setSqrtAvgTravelTime(Duration sqrtAvgTravelTime) {
		this.sqrtAvgTravelTime = sqrtAvgTravelTime;
	}
	
	public void statProcess(){
		for (TravelLeg tmpTl : this.logedLegs){
			if(tmpTl.getTravelTime().compareTo(this.minTravelTime)<0)
				this.minTravelTime = tmpTl.getTravelTime();
			if(tmpTl.getTravelTime().compareTo(this.maxTravelTime)>0)
				this.maxTravelTime = tmpTl.getTravelTime();
			
			this.avgDistance += tmpTl.getDistance();
			this.avgStopsByTime.put(Duration.ofMinutes(1), this.avgStopsByTime.get(Duration.ofMinutes(1))+tmpTl.getStopsByTime().get(Duration.ofMinutes(1)));
			this.avgStopsByTime.put(Duration.ofMinutes(5), this.avgStopsByTime.get(Duration.ofMinutes(5))+tmpTl.getStopsByTime().get(Duration.ofMinutes(5)));
			this.avgStopsByTime.put(Duration.ofMinutes(10), this.avgStopsByTime.get(Duration.ofMinutes(10))+tmpTl.getStopsByTime().get(Duration.ofMinutes(10)));
			this.avgTravelTime = this.avgTravelTime.plus(tmpTl.getTravelTime());
			this.sqrtAvgTravelTime = this.sqrtAvgTravelTime.plus(Duration.ofSeconds((long)Math.pow(((double)tmpTl.getTravelTime().getSeconds()),2)));
		}
		this.avgDistance /= this.logedLegs.size();
		this.avgTravelTime = this.avgTravelTime.dividedBy(this.logedLegs.size());
		this.sqrtAvgTravelTime = this.sqrtAvgTravelTime.dividedBy((long)Math.pow(this.logedLegs.size(), 2));
		this.sqrtAvgTravelTime = Duration.ofSeconds((long)Math.sqrt((double)this.sqrtAvgTravelTime.getSeconds()));
	}
	public void add(TravelLeg tl) {
		//if (tl.getDistance()>(this.avgDistance/2) && tl.getDistance()<(this.avgDistance*2) && tl.getTravelTime().getSeconds()>(this.avgTravelTime.getSeconds()/2) && tl.getTravelTime().getSeconds()<(this.avgTravelTime.getSeconds()*2)){
		//if(tl.getTravelTime().isZero())
		System.out.println("name: "+tl.getPointA().getName()+"/"+tl.getPointB().getName()+" distance :"+tl.getDistance()+" temps :"+tl.getTravelTime().toString());
		
//		Duration tmpTravelTime = tl.getTravelTime();
//			int tmpDistance = tl.getDistance();
//			HashMap<Duration, Integer> tmpStopsByTime = tl.getStopsByTime();
//			for (TravelLeg tmpTL : logedLegs){
//				tmpTravelTime.plus(tmpTL.getTravelTime());
//				tmpDistance+=tmpTL.getDistance();
//				tmpStopsByTime.put(Duration.ofMinutes(1), tmpStopsByTime.get(Duration.ofMinutes(1))+tmpTL.getStopsByTime().get(Duration.ofMinutes(1)));
//				tmpStopsByTime.put(Duration.ofMinutes(5), tmpStopsByTime.get(Duration.ofMinutes(5))+tmpTL.getStopsByTime().get(Duration.ofMinutes(5)));
//				tmpStopsByTime.put(Duration.ofMinutes(10), tmpStopsByTime.get(Duration.ofMinutes(10))+tmpTL.getStopsByTime().get(Duration.ofMinutes(10)));
//			}
//			this.avgTravelTime = tmpTravelTime.dividedBy(this.logedLegs.size());
//			this.avgDistance = tmpDistance/this.logedLegs.size();
//			tmpStopsByTime.put(Duration.ofMinutes(1), tmpStopsByTime.get(Duration.ofMinutes(1))/this.logedLegs.size());
//			tmpStopsByTime.put(Duration.ofMinutes(5), tmpStopsByTime.get(Duration.ofMinutes(5))/this.logedLegs.size());
//			tmpStopsByTime.put(Duration.ofMinutes(10), tmpStopsByTime.get(Duration.ofMinutes(10))/this.logedLegs.size());

		this.logedLegs.add(tl);
		//}
	}
}
