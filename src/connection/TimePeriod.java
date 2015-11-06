package connection;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TimePeriod {
	private LocalDateTime from;
	private LocalDateTime to;
	private double distanceMax;
	private List<LegCollection> travelStats;
	
	public TimePeriod(){
		this.from = LocalDateTime.now().minusDays(1);
		this.to = LocalDateTime.now();
		this.travelStats = new ArrayList<LegCollection>();
		this.process();
	}
	public TimePeriod(LocalDateTime from, LocalDateTime to){
		this.from = from;
		this.to = to;
		this.travelStats = new ArrayList<LegCollection>();
		this.process();
	}
	public TimePeriod(String from, String to){
		this.from = LocalDateTime.parse(from);
		this.to= LocalDateTime.parse(to);
		this.travelStats = new ArrayList<LegCollection>();
		this.process();
	}
	public TimePeriod(int yFrom, int moFrom, int dFrom, int hFrom, int yTo, int moTo, int dTo, int hTo){
		this.from = LocalDateTime.of(yFrom, moFrom, dFrom, hFrom, 0);
		this.to = LocalDateTime.of(yTo, moTo, dTo, hTo, 0);
		this.travelStats = new ArrayList<LegCollection>();
		this.process();
	}
	
	private void process(){
		List<BusLocation> busPoints = (new BusLocationsQuery()).getAllBusLocation();
		List<String> busId = (new WayPointsQuery()).getBusIdByDate(this.from, this.to);
		this.distanceMax = 500.0;
		HashMap<Duration, Integer> statStop = new HashMap<Duration, Integer>();
		statStop.put(Duration.ofMinutes(1), 0);
		statStop.put(Duration.ofMinutes(5), 0);
		statStop.put(Duration.ofMinutes(10), 0);
		for (String id : busId){
			List<WayPoint> wayPoints = (new WayPointsQuery()).getWayPointsByDate(id, this.from, this.to, -1);
			int indexStopStart = -1;
			int indexStopEnd = -1;
			
			BusLocation blStart = null;
			WayPoint wpStart = null;
			boolean close = false;
			ArrayList<TravelLeg> legs = new ArrayList<TravelLeg>();
			for (int i=0; i<wayPoints.size(); i++){
				WayPoint wp = wayPoints.get(i);
				if (wp.getSpeed()<=2){
					for (int j=0; j<busPoints.size(); j++){
						BusLocation bl = busPoints.get(j);
						double dist = bl.calculateDistance(wp);
						if (dist < this.distanceMax && blStart != null && !close){
							close = true;
							legs.add(new TravelLeg(blStart, bl, wpStart.getLocalDateTime(), wp.getLocalDateTime(), statStop));
							statStop.forEach((dur, stat) ->{ stat=0; });
							blStart = bl;
							wpStart = wp;
						}
						else if(close){
							if(dist>this.distanceMax || (dist < this.distanceMax && bl!=blStart))
								close = false;
						}
					}
					if (!close){
						if (i == indexStopEnd+1)
							indexStopEnd++;
						else{
							int duration = wayPoints.get(indexStopEnd).getTime() - wayPoints.get(indexStopStart).getTime();
							if (duration < 60){}
							else if (duration < 300)
								statStop.put(Duration.ofMinutes(5), (statStop.get(Duration.ofMinutes(5))+1));
							else if (duration < 600)
								statStop.put(Duration.ofMinutes(5), (statStop.get(Duration.ofMinutes(5))+1));
						}
					}
				}
			}
			boolean exist = false;
			for (TravelLeg tl : legs){
				for (LegCollection lc : this.travelStats){
					if (tl.getPointA().getCode().equals(lc.getPointA().getCode()) && tl.getPointB().getCode().equals(lc.getPointB().getCode())){
						exist = true;
						lc.add(tl);
					}
				}
				if (!exist){
					this.travelStats.add(new LegCollection(tl));
				}
			}
		}
	}

	
	
	public LocalDateTime getFrom() {
		return from;
	}
	public void setFrom(LocalDateTime from) {
		this.from = from;
	}
	public LocalDateTime getTo() {
		return to;
	}
	public void setTo(LocalDateTime to) {
		this.to = to;
	}
	public List<LegCollection> getTravelStats() {
		return travelStats;
	}
	public void setTravelStats(List<LegCollection> travelStats) {
		this.travelStats = travelStats;
	}
	public double getDistanceMax() {
		return distanceMax;
	}
	public void setDistanceMax(double distanceMax) {
		this.distanceMax = distanceMax;
	}

}
