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
	List<BusLocation> busPoints;
	
	public TimePeriod(){
		this.from = LocalDateTime.now().minusDays(1);
		this.to = LocalDateTime.now();
		this.travelStats = new ArrayList<LegCollection>();
		BusLocationsQuery queryLB = new BusLocationsQuery();
		this.busPoints = queryLB.getAllBusLocation();
		queryLB.close();
		this.process();
	}
	public TimePeriod(LocalDateTime from, LocalDateTime to){
		this.from = from;
		this.to = to;
		this.travelStats = new ArrayList<LegCollection>();
		BusLocationsQuery queryLB = new BusLocationsQuery();
		this.busPoints = queryLB.getAllBusLocation();
		queryLB.close();
		this.process();
	}
	public TimePeriod(String from, String to){
		this.from = LocalDateTime.parse(from);
		this.to= LocalDateTime.parse(to);
		this.travelStats = new ArrayList<LegCollection>();
		BusLocationsQuery queryLB = new BusLocationsQuery();
		this.busPoints = queryLB.getAllBusLocation();
		queryLB.close();
		this.process();
	}
	public TimePeriod(int yFrom, int moFrom, int dFrom, int hFrom, int yTo, int moTo, int dTo, int hTo){
		this.from = LocalDateTime.of(yFrom, moFrom, dFrom, hFrom, 0);
		this.to = LocalDateTime.of(yTo, moTo, dTo, hTo, 0);
		this.travelStats = new ArrayList<LegCollection>();
		BusLocationsQuery queryLB = new BusLocationsQuery();
		this.busPoints = queryLB.getAllBusLocation();
		queryLB.close();
		this.process();
	}
	
	private List<BusLocation> closeTo (WayPoint wp){
		List<BusLocation> listBL = new ArrayList<BusLocation>();
		for (int i=0; i<this.busPoints.size(); i++){
			if (this.busPoints.get(i).calculateDistance(wp)<this.distanceMax)
				listBL.add(this.busPoints.get(i));
		}
		return listBL;
	}
	
	private void process(){
		WayPointsQuery queryWP = new WayPointsQuery();
		List<String> busId = queryWP.getBusIdByDate(this.from, this.to);
		int distanceleg;
		this.distanceMax = 1000.0;
		HashMap<Duration, Integer> statStop = new HashMap<Duration, Integer>();
		statStop.put(Duration.ofMinutes(1), 0);
		statStop.put(Duration.ofMinutes(5), 0);
		statStop.put(Duration.ofMinutes(10), 0);
		
		for (String id : busId){
			List<WayPoint> wayPoints = queryWP.getWayPointsByDate(id, this.from, this.to, -1);
			ArrayList<TravelLeg> legs = new ArrayList<TravelLeg>();
			int k=0;
			WayPoint wp = wayPoints.get(k);
			boolean depart =true;
			BusLocation blStart = null;
			List<BusLocation> listBL;
			while (depart){
				while (k<wayPoints.size() && wp.getSpeed()>2){
					k++;
					wp = wayPoints.get(k);
				}
				if ((listBL = this.closeTo(wp)).size()!=0 || k>=wayPoints.size()){
					int index = 0;
					for (int p=1; p<listBL.size(); p++){
						if ((int)listBL.get(p).calculateDistance(wp)<(int)listBL.get(index).calculateDistance(wp))
							index = p;;
					}
					blStart = listBL.get(index);
					depart=false;
				}
				else
					k++;
			}
			int indexStart = k;
			int indexStopStart = -1;
			int indexStopEnd = -1;
			BusLocation blEnd = blStart;
			distanceleg = 0;
			for (int i=k+1; i<wayPoints.size(); i++){
				wp = wayPoints.get(i);
				distanceleg+=(int)wp.calculateDistance(wayPoints.get(i-1));
				if (wp.getSpeed()<=2){
					if ((listBL = this.closeTo(wp)).size()!=0){
						int index = 0;
						for (int p=1; p<listBL.size(); p++){
							if ((int)listBL.get(p).calculateDistance(wp)<(int)listBL.get(index).calculateDistance(wp))
								index = p;;
						}
						blEnd = listBL.get(index);
						if (!blEnd.getCode().equals(blStart.getCode())){
							HashMap<Duration, Integer> map = new HashMap<Duration, Integer>();
							map.put(Duration.ofMinutes(1), statStop.get(Duration.ofMinutes(1)));
							map.put(Duration.ofMinutes(5), statStop.get(Duration.ofMinutes(5)));
							map.put(Duration.ofMinutes(10), statStop.get(Duration.ofMinutes(10)));
							legs.add(new TravelLeg(blStart, blEnd, wayPoints.get(indexStart).getLocalDateTime(), wayPoints.get(i).getLocalDateTime(), map, distanceleg));
							statStop.put(Duration.ofMinutes(1), 0);
							statStop.put(Duration.ofMinutes(5), 0);
							statStop.put(Duration.ofMinutes(10), 0);
							blStart = blEnd;
							indexStart = i;
							indexStopStart = -1;
							indexStopEnd = -1;
							distanceleg = 0;
						}
						else{
							indexStart = i;
							distanceleg = 0;
						}
					}
					else{
						if (indexStopStart == -1){
							indexStopStart = i;
							indexStopEnd = i;
						}
						else if (i == indexStopEnd+1)
							indexStopEnd++;
						else {
							int duration = wayPoints.get(indexStopEnd).getTime() - wayPoints.get(indexStopStart).getTime();
							if (duration < 60){}
							else if (duration < 300)
								statStop.put(Duration.ofMinutes(1), (statStop.get(Duration.ofMinutes(5))+1));
							else if (duration < 600)
								statStop.put(Duration.ofMinutes(5), (statStop.get(Duration.ofMinutes(10))+1));
							else{
								statStop.put(Duration.ofMinutes(10), (statStop.get(Duration.ofMinutes(10))+1));
							}
							indexStopStart = -1;
							indexStopEnd = -1;
						}
					}
				}
			}
			boolean exist = false;
			for (TravelLeg tl : legs){
				for (LegCollection lc : this.travelStats){
					if ((tl.getPointA().getCode().equals(lc.getPointA().getCode()) && tl.getPointB().getCode().equals(lc.getPointB().getCode())) || (tl.getPointA().getCode().equals(lc.getPointB().getCode()) && tl.getPointB().getCode().equals(lc.getPointA().getCode()))){
						exist = true;
						lc.add(tl);
					}
				}
				if (!exist){
					this.travelStats.add(new LegCollection(tl));
				}
			}
		}
		queryWP.close();
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
