package srv;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientThread extends Thread {

	BufferedReader tokOdKlijenta = null;
	ClientThread[] klijenti;
	Socket komunikacioniSoket = null;
	PrintStream tokKaKlijentu = null;
	BufferedReader tokKaFajluKartice = null;
	BufferedReader citajIzFajlaUsers = null;
	PrintWriter upisiUfajlUsers = null;
	BufferedReader citajIzFL = null;
	BufferedReader citajIzNN = null;
	BufferedReader citajIzKV = null;
	PrintWriter upisiUfl = null;
	PrintWriter upisiUnn = null;
	PrintWriter upisiUkv = null;
	BufferedReader citajIzFajlaUplate = null;
	PrintWriter upisiUfajlUplate = null;
	List<String> humanitarneAkcije = new LinkedList<>();

	public ClientThread(ClientThread[] klijenti, Socket komunikacijaSaKlijentom, List<String> humanitarneAkcije) {
		this.klijenti = klijenti;
		komunikacioniSoket = komunikacijaSaKlijentom;
		this.humanitarneAkcije = humanitarneAkcije;
	}

	public void run() {
		try {
			tokOdKlijenta = new BufferedReader(new InputStreamReader(komunikacioniSoket.getInputStream()));
			tokKaKlijentu = new PrintStream(komunikacioniSoket.getOutputStream());
			// ucitavamo sadrzaj fajla users, trebace nam kod registracije i login-a
			citajIzFajlaUsers = new BufferedReader(new FileReader("users.txt"));
			String sadrzajFajlaUsers = "";
			String red;
			do {
				red = citajIzFajlaUsers.readLine();
				if (red != null)
					sadrzajFajlaUsers = sadrzajFajlaUsers + "\n" + red;
			} while (red != null);
			// najpre ucitavam sve brojeve kartica u string kako bih mogao da izvrsim
			// validaciju
			tokKaFajluKartice = new BufferedReader(new FileReader("kartice.txt"));
			String sadrzajFajlaKartice = "";
			String redFajla;
			do {
				redFajla = tokKaFajluKartice.readLine();
				if (redFajla != null)
					sadrzajFajlaKartice = sadrzajFajlaKartice + "\n" + redFajla;
			} while (redFajla != null);
			System.out.println(sadrzajFajlaKartice); // vidi samo server
			tokKaKlijentu.println("Dobrodosli na aplikaciju za prikupljanje humanitarne pomoci!");
			do {
				tokKaKlijentu.println("Izaberite opciju unosom njenog rednog broja:");
				tokKaKlijentu.println("1.Uplata humanitarne pomoci");
				tokKaKlijentu.println("2.Registracija korisnika");
				tokKaKlijentu.println("3.Prijavljivanje na sistem");
				tokKaKlijentu.println("4.Pregled ukupno skupljenih sredstava");
				tokKaKlijentu.println("5.Pregled transakcija");
				tokKaKlijentu.println("6.Izlaz iz aplikacije");
				int izbor = Integer.parseInt(tokOdKlijenta.readLine());
				switch (izbor) {
				case 1:
					do {
						tokKaKlijentu.println(
								"Izaberite jednu od trenutno aktivnih humanitarnih akcija unosom njene sifre:");
						for (int i = 0; i < humanitarneAkcije.size(); i++) {
							tokKaKlijentu.println(humanitarneAkcije.get(i));
						}
						String unos = tokOdKlijenta.readLine();
						switch (unos) {
						case "fl":
							tokKaKlijentu.println("Izabrali ste akciju: " + humanitarneAkcije.get(0));
							upisiUfl = new PrintWriter(new BufferedWriter(new FileWriter("fl.txt", true)));
							break;
						case "nn":
							tokKaKlijentu.println("Izabrali ste akciju: " + humanitarneAkcije.get(1));
							upisiUnn = new PrintWriter(new BufferedWriter(new FileWriter("nn.txt", true)));
							break;
						case "kv":
							tokKaKlijentu.println("Izabrali ste akciju: " + humanitarneAkcije.get(2));
							upisiUkv = new PrintWriter(new BufferedWriter(new FileWriter("kv.txt", true)));
							break;
						default:
							tokKaKlijentu.println("Neispravan unos! Pokusajte opet");
							continue;
						}
						break;
					} while (true);

					tokKaKlijentu.println("Unesite Vase ime i prezime");
					String ime = tokOdKlijenta.readLine();
					tokKaKlijentu.println("Unesite Vasu adresu");
					String adresa = tokOdKlijenta.readLine();
					boolean temp;
					String kartica;
					do {
						temp = false;
						tokKaKlijentu.println("Unesite broj kartice u formatu xxxx-xxxx-xxxx-xxxx: ");
						kartica = tokOdKlijenta.readLine();
						if (kartica.trim().length() != 19 || !kartica.contains("-")) {
							tokKaKlijentu.println("Neispravan unos, pokusajte ponovo..");
							temp = true;
						} else if (!sadrzajFajlaKartice.contains(kartica)) {
							tokKaKlijentu.println("Unet broj kartice ne postoji u bazi, pokusajte ponovo..");
							temp = true;
						}
					} while (temp);

					boolean pom = true;
					do {
						tokKaKlijentu.println("Unesite CVV: ");
						int cvv = Integer.parseInt(tokOdKlijenta.readLine());
						if (cvv / 100 <= 1 || cvv / 100 > 9)
							tokKaKlijentu.println("Neispravan unos, pokusajte ponovo..");
						else if (!sadrzajFajlaKartice.contains(kartica + ";" + cvv + ";")) {
							tokKaKlijentu.println("Unet CVV ne postoji u bazi, pokusajte ponovo..");
						} else
							pom = false;
					} while (pom);
					int iznos;
					do {
						tokKaKlijentu.println("Unesite iznos koji zelite da uplatite: ");
						iznos = Integer.parseInt(tokOdKlijenta.readLine());
						if (iznos < 200)
							tokKaKlijentu.println("Iznos ne sme biti manji od 200 dinara, pokusajte ponovo..");
						else
							break;
					} while (true);
					tokKaKlijentu.println("Uspesno ste izvrsili uplatu!");
					// odavde se salje izvestaj o transakciji
					tokKaKlijentu.println("#Ime i prezime uplatioca: " + ime);
					tokKaKlijentu.println("#Adresa uplatioca: " + adresa);
					GregorianCalendar vreme = new GregorianCalendar();
					tokKaKlijentu.println("#Datum i vreme uplate: " + vreme.getTime());
					tokKaKlijentu.println("#Uplacen iznos: " + iznos);
					tokKaKlijentu.println("#- - - HVALA VAM STO STE KORISTILI NASE USLUGE - - -");
					upisiUfajlUplate = new PrintWriter(new BufferedWriter(new FileWriter("uplate.txt", true)));
					upisiUfajlUplate.println(ime + " " + vreme.getTime() + " " + iznos);
					upisiUfajlUplate.close();
					if (upisiUfl != null) {
						upisiUfl.print(iznos + ";");
						upisiUfl.close();
					} else if (upisiUnn != null) {
						upisiUnn.print(iznos + ";");
						upisiUnn.close();
					} else if (upisiUkv != null) {
						upisiUkv.print(iznos + ";");
						upisiUkv.close();
					}
					continue;
				case 2:
					String username;
					while (true) {
						tokKaKlijentu.println("Unesite zeljeno korisnicko ime:");
						username = tokOdKlijenta.readLine();
						if (!sadrzajFajlaUsers.contains("@" + username))
							break; // ako ne postoji taj username u bazi izadji iz petlje
						else
							tokKaKlijentu.println("Zauzeto korisnicko ime.. Pokusajte ponovo");
					}
					tokKaKlijentu.println("Unesite zeljenu lozinku:");
					String pass = tokOdKlijenta.readLine();
					tokKaKlijentu.println("Unesite Vase ime i prezime:");
					String imeR = tokOdKlijenta.readLine();
					tokKaKlijentu.println("Unesite Vas JMBG");
					String jmbg = tokOdKlijenta.readLine();
					tokKaKlijentu.println("Unesite broj kartice u formatu xxxx-xxxx-xxxx-xxxx: ");
					String karticaR = tokOdKlijenta.readLine();
					tokKaKlijentu.println("Unesite Vas imejl");
					String email = tokOdKlijenta.readLine();
					String korisnik = "@" + username + ";" + pass + ";" + imeR + ";" + jmbg + ";" + karticaR + ";"
							+ email + ";" + "\n";
					upisiUfajlUsers = new PrintWriter(new BufferedWriter(new FileWriter("users.txt", true)));
					upisiUfajlUsers.println(korisnik);
					tokKaKlijentu.println("Uspesno ste se registrovali!");
					upisiUfajlUsers.close();
					continue;
				case 3:
					String userL = null, passL;
					for (int i = 1; i <= 4; i++) {
						tokKaKlijentu.println("Korisnicko ime:");
						userL = tokOdKlijenta.readLine();
						tokKaKlijentu.println("Lozinka:");
						passL = tokOdKlijenta.readLine();
						if (sadrzajFajlaUsers.contains(userL + ";" + passL)) {
							tokKaKlijentu.println("Dobrodosao/la " + userL);
							break;
						} else if (i == 4) {
							tokKaKlijentu.println("Nemate vise pokusaja!");
							return;
						} else
							tokKaKlijentu.println(
									"Korisnicko ime i/ili lozinka nije u bazi registrovanih korisnika. Imate jos "
											+ (4 - i) + " pokusaja");
					}
					while (true) {
						tokKaKlijentu.println("Izaberite opciju unosom njenog rednog broja:");
						tokKaKlijentu.println("1.Uplata humanitarne pomoci");
						tokKaKlijentu.println("2.Pregled ukupno skupljenih sredstava");
						tokKaKlijentu.println("3.Izadji iz rezima za ulogovane korisnike");
						int odluka = Integer.parseInt(tokOdKlijenta.readLine().trim());
						if (odluka == 1) {
							tokKaKlijentu.println(
									"Izaberite jednu od trenutno aktivnih humanitarnih akcija unosom njene sifre:");
							for (int i = 0; i < humanitarneAkcije.size(); i++) {
								tokKaKlijentu.println(humanitarneAkcije.get(i));
							}
							String unos = tokOdKlijenta.readLine();
							switch (unos) {
							case "fl":
								tokKaKlijentu.println("Izabrali ste akciju: " + humanitarneAkcije.get(0));
								upisiUfl = new PrintWriter(new BufferedWriter(new FileWriter("fl.txt", true)));
								break;
							case "nn":
								tokKaKlijentu.println("Izabrali ste akciju: " + humanitarneAkcije.get(1));
								upisiUnn = new PrintWriter(new BufferedWriter(new FileWriter("nn.txt", true)));
								break;
							case "kv":
								tokKaKlijentu.println("Izabrali ste akciju: " + humanitarneAkcije.get(2));
								upisiUkv = new PrintWriter(new BufferedWriter(new FileWriter("kv.txt", true)));
								break;
							}
							pom = true;
							do {
								tokKaKlijentu.println("Unesite CVV vase kartice: ");
								int cvv = Integer.parseInt(tokOdKlijenta.readLine());
								if (cvv / 100 <= 1 || cvv / 100 > 9)
									tokKaKlijentu.println("Neispravan unos, pokusajte ponovo..");
								else if (!sadrzajFajlaKartice.contains(";"+ cvv + ";")) { 
									tokKaKlijentu.println("Unet CVV ne postoji u bazi, pokusajte ponovo..");
								} else
									pom = false;
							} while (pom);
							do {
								tokKaKlijentu.println("Unesite iznos koji zelite da uplatite: ");
								iznos = Integer.parseInt(tokOdKlijenta.readLine());
								if (iznos < 200)
									tokKaKlijentu.println("Iznos ne sme biti manji od 200 dinara, pokusajte ponovo..");
								else
									break;
							} while (true);
							tokKaKlijentu.println("Uspesno ste izvrsili uplatu!");
							tokKaKlijentu.println("#Korisnicko ime: " + userL);
							vreme = new GregorianCalendar();
							tokKaKlijentu.println("#Datum i vreme uplate: " + vreme.getTime());
							tokKaKlijentu.println("#Uplacen iznos: " + iznos);
							tokKaKlijentu.println("#- - - HVALA VAM STO STE KORISTILI NASE USLUGE - - -");
							upisiUfajlUplate = new PrintWriter(new BufferedWriter(new FileWriter("uplate.txt", true)));
							upisiUfajlUplate.println(userL + " " + vreme.getTime() + " " + iznos);
							upisiUfajlUplate.close();
							if (upisiUfl != null) {
								upisiUfl.print(iznos + ";");
								upisiUfl.close();
							} else if (upisiUnn != null) {
								upisiUnn.print(iznos + ";");
								upisiUnn.close();
							} else if (upisiUkv != null) {
								upisiUkv.print(iznos + ";");
								upisiUkv.close();
							}
						} else if (odluka == 2) {
							tokKaKlijentu.println("Izaberite akciju za koju zelite da vidite prikupljen iznos:");
							for (int i = 0; i < humanitarneAkcije.size(); i++) {
								tokKaKlijentu.println(humanitarneAkcije.get(i));
							}
							String unos = tokOdKlijenta.readLine();
							switch (unos) {
							case "fl":
								tokKaKlijentu.println("Izabrali ste akciju: " + humanitarneAkcije.get(0));
								citajIzFL = new BufferedReader(new FileReader("fl.txt"));
								String[] flUplate = citajIzFL.readLine().split(";");
								int flTotal = 0;
								for (int i = 0; i < flUplate.length; i++) {
									flTotal += Integer.parseInt(flUplate[i]);
								}
								tokKaKlijentu.println("Prikupljen iznos: " + flTotal);
								break;
							case "nn":
								tokKaKlijentu.println("Izabrali ste akciju: " + humanitarneAkcije.get(1));
								citajIzNN = new BufferedReader(new FileReader("nn.txt"));
								String[] nnUplate = citajIzNN.readLine().split(";");
								int nnTotal = 0;
								for (int i = 0; i < nnUplate.length; i++) {
									nnTotal += Integer.parseInt(nnUplate[i]);
								}
								tokKaKlijentu.println("Prikupljen iznos: " + nnTotal);
								break;
							case "kv":
								tokKaKlijentu.println("Izabrali ste akciju: " + humanitarneAkcije.get(2));
								citajIzKV = new BufferedReader(new FileReader("kv.txt"));
								String[] kvUplate = citajIzKV.readLine().split(";");
								int kvTotal = 0;
								for (int i = 0; i < kvUplate.length; i++) {
									kvTotal += Integer.parseInt(kvUplate[i]);
								}
								tokKaKlijentu.println("Prikupljen iznos: " + kvTotal);
								break;
							}
						} else if (odluka == 3)
							break;
						else tokKaKlijentu.println("Neispravan unos, pokusajte opet..");
					}
					continue;
				case 4:
					tokKaKlijentu.println("Izaberite akciju za koju zelite da vidite prikupljen iznos:");
					for (int i = 0; i < humanitarneAkcije.size(); i++) {
						tokKaKlijentu.println(humanitarneAkcije.get(i));
					}
					String unos = tokOdKlijenta.readLine();
					switch (unos) {
					case "fl":
						tokKaKlijentu.println("Izabrali ste akciju: " + humanitarneAkcije.get(0));
						citajIzFL = new BufferedReader(new FileReader("fl.txt"));
						String[] flUplate = citajIzFL.readLine().split(";");
						int flTotal = 0;
						for (int i = 0; i < flUplate.length; i++) {
							flTotal += Integer.parseInt(flUplate[i]);
						}
						tokKaKlijentu.println("Prikupljen iznos: " + flTotal);
						break;
					case "nn":
						tokKaKlijentu.println("Izabrali ste akciju: " + humanitarneAkcije.get(1));
						citajIzNN = new BufferedReader(new FileReader("nn.txt"));
						String[] nnUplate = citajIzNN.readLine().split(";");
						int nnTotal = 0;
						for (int i = 0; i < nnUplate.length; i++) {
							nnTotal += Integer.parseInt(nnUplate[i]);
						}
						tokKaKlijentu.println("Prikupljen iznos: " + nnTotal);
						break;
					case "kv":
						tokKaKlijentu.println("Izabrali ste akciju: " + humanitarneAkcije.get(2));
						citajIzKV = new BufferedReader(new FileReader("kv.txt"));
						String[] kvUplate = citajIzKV.readLine().split(";");
						int kvTotal = 0;
						for (int i = 0; i < kvUplate.length; i++) {
							kvTotal += Integer.parseInt(kvUplate[i]);
						}
						tokKaKlijentu.println("Prikupljen iznos: " + kvTotal);
						break;
					}
					tokKaKlijentu.println();
					continue;
				case 5:
					// Pregled transakcija
					citajIzFajlaUplate = new BufferedReader(new FileReader("uplate.txt"));
					String[] uplate = new String[1000];
					String row;
					int i = 0;
					do {
						row = citajIzFajlaUplate.readLine();
						if (row != null) {
							uplate[i] = row;
							i++;
						}
					} while (row != null);
					citajIzFajlaUplate.close();
					// dobili smo niz uplata, sada zelimo da odredimo kolika je stvarna duzina niza
					int duzina = 0;
					for (String payment : uplate) {
						if (payment != null)
							duzina++;
					}
					if (duzina <= 10) {
						for (int j = 0; j < 10; j++) {
							tokKaKlijentu.println(uplate[j]);
						}
					} else {
						for (int j = duzina - 10; j <= duzina-1; j++) {
							tokKaKlijentu.println(uplate[j]);
						}
						tokKaKlijentu.println();
					}
					continue;
				case 6:
					tokKaKlijentu.println("Uspesno ste izasli iz aplikacije");
					break;
				default:
					tokKaKlijentu.println("Neispravan unos! Pokusajte opet");
					continue;
				}
				break;
			} while (true);
			komunikacioniSoket.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
