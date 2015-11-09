package rendu;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import connection.LegCollection;
import connection.TimePeriod;

public class Ecran extends JFrame{
	
	private Container container;
	private JButton go;
	private JPanel nord;
	private JPanel centre;
	private int hfrom;
	private int hto;
	private UtilDateModel model;
	private Properties p;
	private JTable tableau;
	private JDatePickerImpl datePicker;
	private JDatePanelImpl datePanel;
	private final String[] heures= {"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24"};
	private JComboBox heuref;
	private JComboBox heuret;
	private TimePeriod td;
	private List<LegCollection> list;
	
	public Ecran(){
		
		//Modifier le titre
			this.setTitle("Analize");
		
		//Taille et Modif
			this.setSize(900,600);
			this.setResizable(false);
		
		//instanciation
			go = new JButton("Search");
			nord = new JPanel();
			centre = new JPanel();
			//ftime = new JTextField("From time");
			//ttime = new JTextField("To time");
			heuref = new JComboBox<String>(heures);
			heuret = new JComboBox<String>(heures);
			LocalDateTime a = LocalDateTime.of(2016, 1, 1, 8, 5);
			LocalDateTime b = LocalDateTime.of(2016, 1, 1, 22, 0);
				td = new TimePeriod(a,b);
			
			
		//JdatePicker
			model = new UtilDateModel();
			model.setDate(2015, 10, 5);
			model.setSelected(true);
		//poperties for JDatePanelImpl
			p = new Properties();
			p.put("text.today", "Today");
			p.put("text.month", "Month");
			p.put("text.year", "Year");
			datePanel = new JDatePanelImpl(model, p);
			datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
			
		//Panel nord
			nord.setSize(900,100);
			nord.setLayout(new GridLayout(1, 4));
			nord.add(datePicker);
			nord.add(heuref);
			nord.add(heuret);
			nord.add(go);
			nord.setVisible(true);
		
		//Combo box
			heuref.setSelectedItem(6);
			heuret.setSelectedItem(22);
			heuref.addActionListener (new ActionListener () {
			    public void actionPerformed(ActionEvent e) {
			    	 heuref = (JComboBox)e.getSource();
			         String tmp = (String)heuref.getSelectedItem();
			         hfrom = Integer.parseInt(tmp);
			    }
			});
			heuret.addActionListener (new ActionListener () {
			    public void actionPerformed(ActionEvent e) {
			    	 heuret = (JComboBox)e.getSource();
			         String tmp2 = (String)heuret.getSelectedItem();
			         hto = Integer.parseInt(tmp2);
			    }
			});
				
		//table & centre 
			centre.setLayout(new BorderLayout());
			String[][] donnees ={};	
		    //                  String    String   hashMap      int           TRavelLEgs  Long
		    String[] entetes = {"PointA","PointB","Stop 1-5","Stop 5-10","Stop > 10 ","TravelTime","Distance"};
		    TableDonnee tablemodel = new TableDonnee(donnees,entetes);
		    tableau = new JTable(tablemodel);
		    centre.add(tableau.getTableHeader(), BorderLayout.NORTH);
		    centre.add(tableau, BorderLayout.CENTER);
		    centre.setSize(500,500);
			centre.setBackground(Color.BLACK);
			centre.setVisible(true);

			// search
			go.addActionListener(new ActionListener()
			{
			    public void actionPerformed(ActionEvent e)
			    {
			    	LocalDate selectedDate = ((Date)datePicker.getModel().getValue()).toInstant().atZone(ZoneId.of("Europe/Athens")).toLocalDate();
			       System.out.println("Date : "+selectedDate+"\n"+
			        					"from : "+hfrom+"\n"+
			        					"to : "+hto+"\n");
			       LocalDateTime fromtmp = selectedDate.atStartOfDay().plusHours(hfrom);
			       LocalDateTime totmp = selectedDate.atStartOfDay().plusHours(hto);
			       td = new TimePeriod(fromtmp,totmp);
			       list = td.getTravelStats();
			       String[][] donnees = new String[0][7];
			       String[] rowData = new String[7];
			       tablemodel.setDataVector(donnees, entetes);
				   for(int i = 0; i < list.size() ; i++ ){
				    	rowData[0] = list.get(i).getPointA().getName();
				    	rowData[1] = list.get(i).getPointB().getName();
				    	rowData[2] = list.get(i).getAvgStopsByTime().get(Duration.ofMinutes(1))+"";
				    	rowData[3] = list.get(i).getAvgStopsByTime().get(Duration.ofMinutes(5))+"";
				    	rowData[4] = list.get(i).getAvgStopsByTime().get(Duration.ofMinutes(10))+"";
				    	Duration tmp =list.get(i).getAvgTravelTime();
				    	rowData[5] = ""+(int)tmp.toHours()+"h "+(int)tmp.toMinutes()%60+"m "+(int)tmp.toMinutes()%60+"s";
				    	rowData[6] = ((int)list.get(i).getAvgDistance())+" meters";
				    	tablemodel.addRow(rowData);
				    }
			    }
			});;
		
		//corps
			this.setLayout(new BorderLayout());
			container = this.getContentPane();
			container.add(nord,BorderLayout.NORTH);
			container.add(centre,BorderLayout.CENTER);
		
		
		
		
			
		//Exit et Pop
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setLocation(50, 50);
			//this.pack();
		
	}
	
	class TableDonnee extends DefaultTableModel{
		private String[][] donnee;
		private String[] title;
		
		public TableDonnee(String[][] donnee, String[] title){
			this.donnee=donnee;
			this.title=title;
		}
		
		public String getColumnName(int col) {
			return this.title[col];
		}
		
	    public int getColumnCount() {
	    	return this.title.length;
	    }          
	}
	
	/*//tracer une ligne
	 * public void paintcomponent(Graphics g) {
        super.paintComponents(g);
        g.drawLine(0, 0, 200, 900);
        g.setColor(Color.BLACK);
    }*/
}
