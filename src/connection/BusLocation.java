package connection;

public class BusLocation {
	private String name;
	private String code;
	private double lat;
	private double lng;
	
	public BusLocation(String name, String code, double lng, double lat){
		this.name = name;
		this.code = code;
		this.lat = lat;
		this.lng = lng;
	}
	

	
	public double[] getCoord(){
		return new double[]{this.lat, this.lng};
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getCode() {
		return code;
	}



	public void setCode(String code) {
		this.code = code;
	}



	public double getLat() {
		return lat;
	}



	public void setLat(double lat) {
		this.lat = lat;
	}



	public double getLng() {
		return lng;
	}



	public void setLng(double lng) {
		this.lng = lng;
	}
	
	public double calculateDistance(BusLocation stop){
		double[] coordDepart = this.getCoord();
		double[] coordArrival = stop.getCoord();
		double rlat1 = Math.PI * coordDepart[0]/180;
	    double rlat2 = Math.PI * coordArrival[0]/180;
	 
	    double theta = coordDepart[1]-coordArrival[1];
	    double rtheta = Math.PI * theta/180;
	 
	    double dist = Math.sin(rlat1) * Math.sin(rlat2) + Math.cos(rlat1) * Math.cos(rlat2) * Math.cos(rtheta);
	    double ret = Math.acos(dist) * 180/Math.PI * 60 * 1.1515 * 1.609344 * 1000;
	    return ret;
	}

	public double calculateDistance(WayPoint point){
		double[] coordDepart = this.getCoord();
		double[] coordArrival = point.getCoord();
		double rlat1 = Math.PI * coordDepart[0]/180;
	    double rlat2 = Math.PI * coordArrival[0]/180;
	 
	    double theta = coordDepart[1]-coordArrival[1];
	    double rtheta = Math.PI * theta/180;
	 
	    double dist = Math.sin(rlat1) * Math.sin(rlat2) + Math.cos(rlat1) * Math.cos(rlat2) * Math.cos(rtheta);
	    double ret = Math.acos(dist) * 180/Math.PI * 60 * 1.1515 * 1.609344 * 1000;
	    return ret;
	}
	

}
