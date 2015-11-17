package rendu;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import connection.BusLocation;
import connection.BusLocationsQuery;
import connection.DBConnection;
import connection.WayPoint;
import connection.WayPointsQuery;

public class ClosestPoint {

	public static void main(String[] args){
		DBConnection base = new DBConnection("antoine", "sofar", "localhost", 3306, "waypoints");
		BusLocationsQuery blQuery = new BusLocationsQuery();
		WayPointsQuery wpQuery = new WayPointsQuery();
		
		List<WayPoint> wayPoints = wpQuery.getWayPointsByDate(LocalDateTime.of(2014, 9, 1, 0, 0), LocalDateTime.of(2014, 9, 30, 23, 59), -1);
		List<BusLocation> busLocations = blQuery.getAllBusLocation();
		for (BusLocation bl : busLocations){
			int tmp = 999999	;
			for (WayPoint wp : wayPoints){
				if (wp.getSpeed()<=2){
					int dist = (int)bl.calculateDistance(wp);
					tmp = (dist<tmp) ? dist : tmp;
				}
			}
			tmp = tmp<100 ? 100 : tmp;
			PreparedStatement prep;
			try {
				prep = base.getCon().prepareStatement("UPDATE buspl_locations SET name=?, code=?, lon=?, lat=?, rangeMin=? WHERE name=?;");
				prep.setString(1, bl.getName());
				prep.setString(2, bl.getCode());
				prep.setDouble(3, bl.getLng());
				prep.setDouble(4, bl.getLat());
				prep.setInt(5, tmp);
				prep.setString(6, bl.getName());
				prep.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}
		base.closeConnection();
		System.out.println("BDD close");
	}

}
