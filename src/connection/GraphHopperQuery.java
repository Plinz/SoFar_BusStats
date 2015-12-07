package connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GraphHopperQuery {
	
	/**Connection to the DataBase**/
	private DBConnection base;

	
	public GraphHopperQuery(){
		this.base = new DBConnection("antoine", "sofar", "localhost", 3306, "waypoints");
	}
	
	
	
	public void insert (BusLocation pointA, BusLocation pointB, int distance, int time){
	    try {
	    	ResultSet result = this.select(pointA, pointB);
	    	if(result.last()){
				PreparedStatement prep = this.base.getCon().prepareStatement("INSERT into buspl_graphhopper values (?, ?, ?, ?);");
				prep.setString(1, pointA.getName());
				prep.setString(2, pointB.getName());
				prep.setInt(3, distance);
				prep.setInt(4, time);
				prep.executeUpdate();
				prep.close();
	    	}
	    } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public ResultSet select (BusLocation pointA, BusLocation pointB){
		try{
			PreparedStatement prep;
			prep = base.getCon().prepareStatement("SELECT * FROM buspl_statistics where (POINT_A=? AND POINT_B=?) OR (POINT_B=? AND POINT_A=?);");
			prep.setString(1, pointA.getName());
			prep.setString(2, pointB.getName());
			prep.setString(3, pointB.getName());
			prep.setString(4, pointA.getName());
			return prep.executeQuery();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public void close(){
		this.base.closeConnection();
	}
}
