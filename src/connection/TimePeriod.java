package connection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import google.GeocodeService;
import google.GraphHopper;

public class TimePeriod{
	private LocalDateTime from;
	private LocalDateTime to;
	private double distanceMax;
	private List<LegCollection> travelStats;
	private List<BusLocation> busPoints;
	public List<BusLocation> tmpbuslocation = new ArrayList<BusLocation>();
	public List<String> legsMissing = new ArrayList<String>();

	public TimePeriod() {
		this.from = LocalDateTime.now().minusDays(1);
		this.to = LocalDateTime.now();
		this.travelStats = new ArrayList<LegCollection>();
		BusLocationsQuery queryLB = new BusLocationsQuery();
		this.busPoints = queryLB.getAllBusLocation();
		queryLB.close();
	}

	public TimePeriod(LocalDateTime from, LocalDateTime to) {
		this.from = from;
		this.to = to;
		this.travelStats = new ArrayList<LegCollection>();
		BusLocationsQuery queryLB = new BusLocationsQuery();
		this.busPoints = queryLB.getAllBusLocation();
		queryLB.close();
	}

	public TimePeriod(String from, String to) {
		this.from = LocalDateTime.parse(from);
		this.to = LocalDateTime.parse(to);
		this.travelStats = new ArrayList<LegCollection>();
		BusLocationsQuery queryLB = new BusLocationsQuery();
		this.busPoints = queryLB.getAllBusLocation();
		queryLB.close();
	}

	public TimePeriod(int yFrom, int moFrom, int dFrom, int hFrom, int yTo, int moTo, int dTo, int hTo) {
		this.from = LocalDateTime.of(yFrom, moFrom, dFrom, hFrom, 0);
		this.to = LocalDateTime.of(yTo, moTo, dTo, hTo, 0);
		this.travelStats = new ArrayList<LegCollection>();
		BusLocationsQuery queryLB = new BusLocationsQuery();
		this.busPoints = queryLB.getAllBusLocation();
		queryLB.close();
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
	
	public void process() {
		WayPointsQuery queryWP = new WayPointsQuery();
		List<String> busId = queryWP.getBusIdByDate(this.from, this.to);

		// List of all travel done by all bus
		List<List<TravelLeg>> trajets = new ArrayList<List<TravelLeg>>();
		System.out.println("PROCESS ------------------");
		for (String id : busId) {
			System.out.println("bus id ="+busId);
			ArrayList<TravelLeg> legs = extractLegsByBus(queryWP, id);


			// The current travel
			List<TravelLeg> currentTrajet = new ArrayList<TravelLeg>();

			// The last time the bus stop (to verify if is the same bus on the
			// leg after
			TravelLeg lastStop = null;
			boolean exist = false;

			for (TravelLeg tl : legs) {

				if (lastStop != null
						&& (Duration.between(lastStop.getEnd(), tl.getStart()).compareTo(Duration.ofMinutes(30)) > 0
								|| lastStop.getPointB().getName().equals(tl.getPointA().getName()))) {
					trajets.add(currentTrajet);
					currentTrajet.clear();
				}
				lastStop = tl;
				currentTrajet.add(tl);

				for (LegCollection lc : this.travelStats) {
					if ((tl.getPointA().getCode().equals(lc.getPointA().getCode())
							&& tl.getPointB().getCode().equals(lc.getPointB().getCode()))
							|| (tl.getPointA().getCode().equals(lc.getPointB().getCode())
									&& tl.getPointB().getCode().equals(lc.getPointA().getCode()))) {
						exist = true;
						lc.add(tl);
						break;
					}
				}
				if (!exist) {
					this.travelStats.add(new LegCollection(tl));
				}
				exist = false;
			}
		}
		System.out.println("PROCESS LONG LEGS ------------------");
		for (int i = 0; i < this.busPoints.size() - 1; i++) {
			BusLocation start = this.busPoints.get(i);
			for (int j = i + 1; j < this.busPoints.size(); j++) {
				BusLocation end = this.busPoints.get(j);
				boolean alreadyExist = false;
				for (LegCollection lc : this.travelStats) {
					if ((start.getCode().equals(lc.getPointA().getCode())
							&& end.getCode().equals(lc.getPointB().getCode()))
							|| (start.getCode().equals(lc.getPointB().getCode())
									&& end.getCode().equals(lc.getPointA().getCode()))) {
						alreadyExist = true;
						break;
					}
				}
				if (!alreadyExist) {
					extractLegFromTravels(trajets, start, end);
				}
			}
		}
		System.out.println("END PROCESS ------------------");
		queryWP.close();
	}
	


	// Methode to get all bus location where bus pass
	/*
	 * public void processListPoint(){ WayPointsQuery queryWP = new
	 * WayPointsQuery(); List<String> busId = queryWP.getBusIdByDate(this.from,
	 * this.to);
	 * 
	 * List<BusLocation> tmp; this.distanceMax = 1000; boolean exist; for(String
	 * id : busId){ List<WayPoint> wayPoints = queryWP.getWayPointsByDate(id,
	 * this.from, this.to, -1); for (WayPoint wp : wayPoints){ tmp =
	 * this.closeTo(wp); for (BusLocation bl : tmp){ exist = false; for
	 * (BusLocation fin : this.tmpbuslocation){ if (bl.getName()==fin.getName())
	 * exist = true; } if (!exist) this.tmpbuslocation.add(bl); } } } }
	 */

	private ArrayList<TravelLeg> extractLegsByBus(WayPointsQuery queryWP, String id) {
		// Map with statistics on the leg
		HashMap<Duration, Integer> statStop = new HashMap<Duration, Integer>();
		statStop.put(Duration.ofMinutes(1), 0);
		statStop.put(Duration.ofMinutes(5), 0);
		statStop.put(Duration.ofMinutes(10), 0);

		// All wayPoints of the bus id
		List<WayPoint> wayPoints = queryWP.getWayPointsByDate(id, this.from, this.to, -1);

		// All the legs that the bus take on this period
		ArrayList<TravelLeg> legs = new ArrayList<TravelLeg>();

		// Distance of a leg
		int distanceleg = 0;

		// The closer bus location
		BusLocation blClose = null;

		// Index for statistics
		int indexStopStart = -1;
		int indexStopEnd = -1;

		// Index for leg
		int indexStart = 0;
		BusLocation blStart = null;
		BusLocation blEnd = null;

		// True if the GPS signal send regularly information in less than 1min
		boolean continuSignal = true;

		for (int i = 0; i < wayPoints.size(); i++) {
			if (i != 0 && Duration.between(wayPoints.get(i - 1).getLocalDateTime(), wayPoints.get(i).getLocalDateTime())
					.compareTo(Duration.ofMinutes(1)) > 0)
				continuSignal = false;
			WayPoint wp = wayPoints.get(i);
			if (blStart != null)
				distanceleg += (int) wp.calculateDistance(wayPoints.get(i - 1));
			if (wp.getSpeed() <= 2) {
				if ((blClose = this.closeTo(wp)) != null) {
					blEnd = blClose;
					if (blStart != null && !blEnd.getName().equals(blStart.getName())) {

						Duration duration = Duration.between(wayPoints.get(indexStart).getLocalDateTime(),
								wayPoints.get(i).getLocalDateTime());
						
						if(this.validation(distanceleg, duration, continuSignal, blStart, blEnd)){
							HashMap<Duration, Integer> map = new HashMap<Duration, Integer>();
							map.put(Duration.ofMinutes(1), statStop.get(Duration.ofMinutes(1)));
							map.put(Duration.ofMinutes(5), statStop.get(Duration.ofMinutes(5)));
							map.put(Duration.ofMinutes(10), statStop.get(Duration.ofMinutes(10)));
							legs.add(new TravelLeg(blStart, blEnd, wayPoints.get(indexStart).getLocalDateTime(),
									wayPoints.get(i).getLocalDateTime(), map, distanceleg));
						}
						
						//Reinitialisation of variables.
						continuSignal = true;
						statStop.put(Duration.ofMinutes(1), 0);
						statStop.put(Duration.ofMinutes(5), 0);
						statStop.put(Duration.ofMinutes(10), 0);
						blStart = blEnd;
						indexStart = i;
						indexStopStart = -1;
						indexStopEnd = -1;
						distanceleg = 0;
					} else if (blStart == null) {
						blStart = blClose;
						indexStart = i;
						distanceleg = 0;
						statStop.put(Duration.ofMinutes(1), 0);
						statStop.put(Duration.ofMinutes(5), 0);
						statStop.put(Duration.ofMinutes(10), 0);
					} else {
						indexStart = i;
						distanceleg = 0;
					}
				} else {
					if (indexStopStart == -1) {
						indexStopStart = i;
						indexStopEnd = i;
					} else if (i == indexStopEnd + 1)
						indexStopEnd++;
					else {
						int duration = wayPoints.get(indexStopEnd).getTime() - wayPoints.get(indexStopStart).getTime();
						if (duration < 60) {
						} else if (duration < 300)
							statStop.put(Duration.ofMinutes(1), (statStop.get(Duration.ofMinutes(5)) + 1));
						else if (duration < 600)
							statStop.put(Duration.ofMinutes(5), (statStop.get(Duration.ofMinutes(10)) + 1));
						else if (duration < 1800) {
							statStop.put(Duration.ofMinutes(10), (statStop.get(Duration.ofMinutes(10)) + 1));
						} else {
							blStart = null;
							indexStart = -1;
							distanceleg = 0;
							statStop.put(Duration.ofMinutes(1), 0);
							statStop.put(Duration.ofMinutes(5), 0);
							statStop.put(Duration.ofMinutes(10), 0);
						}
						indexStopStart = i;
						indexStopEnd = i;
					}
				}
			}
		}
		return legs;
	}
	
	private boolean validation(int distanceleg, Duration duration, boolean continuSignal, BusLocation blStart, BusLocation blEnd){
		if(continuSignal){
			if(distanceleg > 1000){
				if(duration.getSeconds()> 0 && distanceleg/duration.getSeconds() < 34 && distanceleg/duration.getSeconds() > 1.4){
					int distanceHopper = GraphHopper.getTimeAndDistanceByCoord(blStart.getLat(), blStart.getLng(), blEnd.getLat(), blEnd.getLng())[0];
					if (distanceleg < (0.4*distanceHopper)+distanceHopper && distanceleg > (0.4*distanceHopper)-distanceHopper){
						return true;
					}
				}
			}
		}
		return false;
	}

	private void extractLegFromTravels(List<List<TravelLeg>> trajets, BusLocation start, BusLocation end) {
		TravelLeg best = null;
		LocalDateTime startTime = null;
		HashMap<Duration, Integer> stopsByTime = null;
		int distance = 0;

		boolean debut = true;
		boolean AtoB = true;

		for (List<TravelLeg> traj : trajets) {
			for (TravelLeg tl : traj) {
				if (debut) {
					if (tl.getPointA().getName().equals(start.getName())) {
						debut = false;
						startTime = tl.getStart();
						distance += tl.getDistance();
						stopsByTime = tl.getStopsByTime();
					} else if (tl.getPointA().getName().equals((end.getName()))) {
						debut = false;
						AtoB = false;
						startTime = tl.getStart();
						distance += tl.getDistance();
						stopsByTime = tl.getStopsByTime();
					}
				} else {
					distance += tl.getDistance();
					stopsByTime.put(Duration.ofMinutes(1),
							stopsByTime.get(Duration.ofMinutes(1)) + tl.getStopsByTime().get(Duration.ofMinutes(1)));
					stopsByTime.put(Duration.ofMinutes(5),
							stopsByTime.get(Duration.ofMinutes(5)) + tl.getStopsByTime().get(Duration.ofMinutes(5)));
					stopsByTime.put(Duration.ofMinutes(10),
							stopsByTime.get(Duration.ofMinutes(10)) + tl.getStopsByTime().get(Duration.ofMinutes(10)));
					if (AtoB && tl.getPointB().getName().equals(end.getName())) {
						if (best == null
								|| best.getTravelTime().compareTo(Duration.between(startTime, tl.getEnd())) >= 0)
							best = new TravelLeg(start, end, startTime, tl.getEnd(), stopsByTime, distance);
						break;
					} else if (!AtoB && tl.getPointB().getName().equals(start.getName())) {
						if (best == null
								|| best.getTravelTime().compareTo(Duration.between(startTime, tl.getEnd())) >= 0)
							best = new TravelLeg(end, start, startTime, tl.getEnd(), stopsByTime, distance);
						break;
					}
				}
			}
		}
		if (best != null){
			System.out.println(best.getPointA()+" "+best.getPointB());
			this.travelStats.add(new LegCollection(best));
		}
		else{
			this.legsMissing.add(start.getName()+";"+end.getName());
		}
	}

	private BusLocation closeTo(WayPoint wp) {
		List<BusLocation> listBL = new ArrayList<BusLocation>();
		for (int i = 0; i < this.busPoints.size(); i++) {
			if (this.busPoints.get(i).getRange() < 3200
					&& this.busPoints.get(i).calculateDistance(wp) <= (this.busPoints.get(i).getRange() * 2))
				listBL.add(this.busPoints.get(i));
		}
		if (listBL.size() != 0) {
			BusLocation closest = listBL.get(0);
			for (int p = 0; p < listBL.size(); p++) {
				if ((int) listBL.get(p).calculateDistance(wp) < (int) closest.calculateDistance(wp))
					closest = listBL.get(p);
			}
			return closest;
		}
		return null;
	}
	
	public static void main(String[] args) {
		BusLocationsQuery query = new BusLocationsQuery();
		StatisticsQuery stat = new StatisticsQuery();
	    FileWriter out = null;
		Boolean googleCheck = false;
		try {
			out = new FileWriter(new File("Diff_Hopper_Min"));
			ArrayList<String> columnCount = new ArrayList<String>();
			columnCount.add("PointA");
			columnCount.add("PointB");
			columnCount.add("Distance");
			columnCount.add("MinTime");
			columnCount.add("Hopper_Distance");
			columnCount.add("Hopper_Time");
			if (googleCheck){
				columnCount.add("Google_Distance");
				columnCount.add("Google_Time");
			}
			for(int i=0; i < columnCount.size(); i++)
	    		out.write(columnCount.get(i) + ";");
	    		out.write("\n");
			}
    	 catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
		ArrayList<BusLocation> bl = query.getAllBusLocation();
		try{
			for (int i=0; i<bl.size(); i++){
				for(int j=i+1; j<bl.size(); j++){
					LegCollection lc = stat.selectMin(bl.get(i), bl.get(j), -1, -1);
					if (lc != null){
						out.write(lc.getPointA().getName()+";");
						out.write(lc.getPointB().getName()+";");
						out.write(lc.getAvgDistance()/1000+";");
						out.write(lc.getMinTravelTime().getSeconds()/60+";");
						int[] hopper = GraphHopper.getTimeAndDistanceByCoord(lc.getPointA().getLat(), lc.getPointA().getLng(), lc.getPointB().getLat(), lc.getPointB().getLng());
						System.out.println("GraphHopper : d="+hopper[0]+" t="+hopper[1]);
						out.write(hopper[0]/1000+";");
						out.write(hopper[1]/60000+";");
						if (googleCheck){
							int google[] = GeocodeService.getTimeAndDistanceByCoord(lc.getPointA().getLat(), lc.getPointA().getLng(), lc.getPointB().getLat(), lc.getPointB().getLng());
							out.write(google[0]/1000+";");
							out.write(google[1]/60+";");
							System.out.println("GoogleService : d="+google[0]+" t="+google[1]);
						}
				    	out.write("\n");
					}
				}
			}
			out.close();
		   	System.out.println("end calcul");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
