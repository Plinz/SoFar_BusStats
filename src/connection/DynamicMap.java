package connection;
import javax.swing.JFrame;



public class DynamicMap extends JFrame {
		
	public DynamicMap(){
		
		//Modifier le titre
		setTitle("Zakynthos Map");
		
		//Taille et Modif
		setSize(700,600);
		setResizable(false);
			
		//Exit et Pop
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocation(50, 50);
		
	}
	
}
