 

package ca.mcgill.ecse223.quoridor;

import ca.mcgill.ecse223.quoridor.model.Quoridor;
import ca.mcgill.ecse223.quoridor.view.QuoridorView;

public class QuoridorApplication {

	private static Quoridor quoridor;

	private static QuoridorView view;
	
	public static void main(String[] args) {
		// Start UI
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                view = new QuoridorView();
                view.setVisible(true);
                view.initLoadScreen();
            }
        });
	}


	public static Quoridor getQuoridor() {
		if (quoridor == null) {
			quoridor = new Quoridor();
		}
 		return quoridor;
	}

}
