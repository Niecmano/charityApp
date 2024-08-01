package srv;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
	
	static ClientThread[] klijenti = new ClientThread[10];

	public static void main(String[] args) {
		List<String> humanitarneAkcije = new LinkedList<>();
		humanitarneAkcije.add("1. Pomoc ugrozenima od poplava u naselju. (sifra:fl)");
		humanitarneAkcije.add("2. Prikupljanje sredstava za lecenje dece obolele od leukemije (sifra:nn)");
		humanitarneAkcije.add("3. Prikupljanje novca za izgradnju narodne kuhinje. (sifra:kv)");
		try (ServerSocket konektivniSoket = new ServerSocket(4201)) {
			System.out.println("Cekam na konekcije...");
			while (true) {
				Socket komunikacijaSaKlijentom = konektivniSoket.accept();
				System.out.println("Konekcija uspostavljena...");
				for (int i = 0; i < klijenti.length; i++) {
					if (klijenti[i] == null) {
						klijenti[i] = new ClientThread(klijenti, komunikacijaSaKlijentom, humanitarneAkcije);
						klijenti[i].start();
						break;
					}
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}