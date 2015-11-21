package connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Month;
import java.util.List;

public class StatisticsQuery {
	
	/**Connection to the DataBase**/
	private DBConnection base;

	
	public StatisticsQuery(){
		this.base = new DBConnection("antoine", "sofar", "localhost", 3306, "waypoints");
	}
	
	public void insertAll (List<LegCollection> list){
	    try {
	    	for (int i=0; i<list.size(); i++){
	    		LegCollection l = list.get(i);
				PreparedStatement prep = this.base.getCon().prepareStatement("insert into buspl_statistics values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				prep.setString(1, l.getTag());
				prep.setString(2, l.getMonth());
				prep.setInt(3, l.getYear());
				prep.setString(4, l.getPointA().getName());
				prep.setString(5, l.getPointB().getName());
				prep.setInt(6, l.getAvgStopsByTime().get(Duration.ofMinutes(1)));
				prep.setInt(7, l.getAvgStopsByTime().get(Duration.ofMinutes(5)));
				prep.setInt(8, l.getAvgStopsByTime().get(Duration.ofMinutes(10)));
				prep.setInt(9, (int)l.getAvgTravelTime().getSeconds());
				prep.setInt(10, (int)l.getSqrtAvgTravelTime().getSeconds());
				prep.setInt(11, l.getAvgDistance());
				prep.setInt(12, l.getLogedLegs().size());
				prep.setInt(13, (int)l.getMinTravelTime().getSeconds());
				prep.setInt(14, (int)l.getMaxTravelTime().getSeconds());

				prep.executeUpdate();
				prep.close();
	    	}
	    } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int selectMin (String PointA, String PointB, int month, int year){
		try {
			PreparedStatement prep;
			if (month > 0 && month < 13 && year > 0){
				prep = this.base.getCon().prepareStatement("Select MIN(MINTIME) from buspl_statistics where (POINT_A=? or POINT_A=?) and (POINT_B=? or POINT_B=?) and MONTH=? and YEAR=?");
				prep.setString(1, PointA);
				prep.setString(2, PointB);
				prep.setString(3, PointA);
				prep.setString(4, PointB);
				prep.setString(5, Month.of(month).toString());
				prep.setInt(6, year);
			}else if(year > 0){
				prep = this.base.getCon().prepareStatement("Select MIN(MINTIME) from buspl_statistics where (POINT_A=? or POINT_A=?) and (POINT_B=? or POINT_B=?) and YEAR=?");
				prep.setString(1, PointA);
				prep.setString(2, PointB);
				prep.setString(3, PointA);
				prep.setString(4, PointB);
				prep.setInt(5, year);				
			}else if(month > 0 && month < 13){
				prep = this.base.getCon().prepareStatement("Select MIN(MINTIME) from buspl_statistics where (POINT_A=? or POINT_A=?) and (POINT_B=? or POINT_B=?) and MONTH=?");
				prep.setString(1, PointA);
				prep.setString(2, PointB);
				prep.setString(3, PointA);
				prep.setString(4, PointB);
				prep.setString(5, Month.of(month).toString());					
			}else{
				prep = this.base.getCon().prepareStatement("Select MIN(MINTIME) from buspl_statistics where (POINT_A=? or POINT_A=?) and (POINT_B=? or POINT_B=?)");
				prep.setString(1, PointA);
				prep.setString(2, PointB);
				prep.setString(3, PointA);
				prep.setString(4, PointB);
			}
			System.out.println(prep);
			ResultSet rs = prep.executeQuery();
			while (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	public void close(){
		this.base.closeConnection();
	}
}
