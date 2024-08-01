package kl;

import java.net.*;
import java.io.*;

public class Client extends Thread {

	static boolean kraj = false;
	static BufferedReader tastatura;
	static Socket komunikacijaSaServerom;
	static BufferedReader tokOdServera;
	static PrintStream tokKaServeru;
	static PrintWriter upisiUfajlTransakcija;

	public static void main(String[] args) {
		try {
			upisiUfajlTransakcija = new PrintWriter(new BufferedWriter(new FileWriter("transakcija.txt")));
			komunikacijaSaServerom = new Socket("localhost", 4201);
			tastatura = new BufferedReader(new InputStreamReader(System.in));
			tokOdServera = new BufferedReader(new InputStreamReader(komunikacijaSaServerom.getInputStream()));
			tokKaServeru = new PrintStream(komunikacijaSaServerom.getOutputStream());
			new Thread(new Client()).start(); // pokrece se istovremeno metoda run jer zelimo istovremeno da primamo i saljemo poruke
			while (!kraj) {
				String poruka = tastatura.readLine();
				tokKaServeru.println(poruka);
			}
			komunikacijaSaServerom.close();
		} catch (UnknownHostException e) {
			System.out.println("Server sa prosledjenim parametrima nije pronadjen");
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void run() { // ova metoda sluzi da primamo poruke
		String msg;
		try {
			while ((msg = tokOdServera.readLine()) != null) {
				if(msg.contains("#")) {
					upisiUfajlTransakcija.println(msg.replaceAll("#",""));
				}
				else System.out.println(msg);
			}
		} catch (IOException e) {
			System.out.println(e);
		}
		finally {
			upisiUfajlTransakcija.close();
		}
	}
}
