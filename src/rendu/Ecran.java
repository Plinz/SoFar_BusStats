package rendu;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import connection.LegCollection;
import connection.TimePeriod;

public class Ecran extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Container container;
	private JButton go;
	private JButton export;
	private JPanel nord;
	private JPanel nordest;
	private JPanel centre;
	private int hfrom;
	private int hto;
	private UtilDateModel model;
	private Properties p;
	private JTable tableau;
	private JDatePickerImpl datePicker;
	private JDatePanelImpl datePanel;
	private final String[] heures= {"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24"};
	private JComboBox<String> heuref;
	private JComboBox<String> heuret;
	private TimePeriod td;
	private List<LegCollection> list;
	private JTextField tag;
	private JScrollPane scroll;
	private String tagcsv;
	
	public Ecran(){
		
		//Modifier le titre
			this.setTitle("Analize");
		
		//Taille et Modif
			this.setSize(900,600);
			this.setResizable(true);
		
		//instanciation
			this.go = new JButton("Search");
			this.export = new JButton("export csv");
			this.export.setEnabled(false);
			this.nord = new JPanel();
			this.nordest = new JPanel();
			this.centre = new JPanel();
			this.heuref = new JComboBox<String>(heures);
			this.heuret = new JComboBox<String>(heures);
			this.tag = new JTextField("tag");
			this.tagcsv = "";
			
		//JdatePicker
			this.model = new UtilDateModel();
			this.model.setDate(2015, 10, 5);
			this.model.setSelected(true);
			
		//poperties for JDatePanelImpl
			this.p = new Properties();
			this.p.put("text.today", "Today");
			this.p.put("text.month", "Month");
			this.p.put("text.year", "Year");
			this.datePanel = new JDatePanelImpl(model, p);
			this.datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
			
		//Panel nord
			this.nord.setSize(900,100);
			this.nord.setLayout(new GridLayout(1, 5));
			this.nordest.setLayout(new GridLayout(1, 2));
			this.nord.add(tag);
			this.nord.add(datePicker);
			this.nord.add(heuref);
			this.nord.add(heuret);
			this.nordest.add(go);
			this.nordest.add(export);
			this.nordest.setVisible(true);
			this.nord.add(nordest);
			this.nord.setVisible(true);
		
		//Combo box
			this.heuref.setSelectedItem(6);
			this.heuret.setSelectedItem(22);
			this.heuref.addActionListener (new ActionListener () {
			    @SuppressWarnings("unchecked")
				public void actionPerformed(ActionEvent e) {
			    	heuref = ((JComboBox<String>)e.getSource());
			        String tmp = (String)heuref.getSelectedItem();
			        hfrom = Integer.parseInt(tmp);
			    }
			});
			this.heuret.addActionListener (new ActionListener () {
			    @SuppressWarnings("unchecked")
				public void actionPerformed(ActionEvent e) {
			    	 heuret = (JComboBox<String>)e.getSource();
			         String tmp2 = (String)heuret.getSelectedItem();
			         hto = Integer.parseInt(tmp2);
			    }
			});
				
		//table & centre 
			this.centre.setLayout(new BorderLayout());
			String[][] donnees ={};	
		    //                  String    String   hashMap      int           TRavelLEgs  Long
		    String[] entetes = {"PointA","PointB","Stop 1-5","Stop 5-10","Stop > 10 ","TravelTime","Time in Second","Distance"};
		    TableDonnee tablemodel = new TableDonnee(donnees,entetes);
		    this.tableau = new JTable(tablemodel);
		    this.centre.add(tableau.getTableHeader(), BorderLayout.NORTH);
		    this.centre.add(tableau, BorderLayout.CENTER);
		    this.centre.setSize(500,500);
			this.centre.setBackground(Color.BLACK);
			this.centre.setVisible(true);

			// search
			this.go.addActionListener(new ActionListener()
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
				    String[][] donnees = new String[0][8];
				    String[] rowData = new String[8];
				    tablemodel.setDataVector(donnees, entetes);
					for(int i = 0; i < list.size() ; i++ ){
					 	rowData[0] = list.get(i).getPointA().getName();
					   	rowData[1] = list.get(i).getPointB().getName();
					   	rowData[2] = list.get(i).getAvgStopsByTime().get(Duration.ofMinutes(1))+"";
					   	rowData[3] = list.get(i).getAvgStopsByTime().get(Duration.ofMinutes(5))+"";
					   	rowData[4] = list.get(i).getAvgStopsByTime().get(Duration.ofMinutes(10))+"";
					   	Duration tmp =list.get(i).getAvgTravelTime();
					   	rowData[5] = ""+(int)tmp.toHours()+"h "+(int)tmp.toMinutes()%60+"m "+(int)tmp.getSeconds()%60+"s";
					   	rowData[6] = (int)tmp.getSeconds()+"";
					   	rowData[7] = ((int)list.get(i).getAvgDistance())+"";
					   	tablemodel.addRow(rowData);
					}
					if (list.size()!=0)
						export.setEnabled(true);
			    }
			});;
			
			
			//exporter
			this.export.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					tagcsv = tag.getText();
					try {
						Exporter exp = new Exporter();
						exp.exportTable(tableau, new File("results.csv"),tagcsv);
					} catch (IOException ex) {
						System.out.println(ex.getMessage());
						ex.printStackTrace();
					}
				}
			});;
		
		//corps
			this.setLayout(new BorderLayout());
			this.container = this.getContentPane();
			this.container.add(nord,BorderLayout.NORTH);
			this.scroll = new JScrollPane(centre);
			this.container.add(scroll,BorderLayout.CENTER);
		
		
		
		
			
		//Exit et Pop
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setLocation(50, 50);
			//this.pack();
		
	}
	
	class TableDonnee extends DefaultTableModel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
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

}
