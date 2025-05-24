package it.unibs.gurobi;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;
import gurobi.*;
import gurobi.GRB.*;
public class ProgettoMain {
// -------------------------classe--di--utilità--per--l'Input---------------------------------------------------------------------------------------
			/**
			 * Classe creata per definire i dati del 
			 * problema in relazione ai valori presi in input
			 * @author federicosabbadini
			 */
			static class ModelloInput {
			
				//attributi
				int n, m, k, x, y, h;
				int [][] d_ij;
				int [] r_i, s_j, alfa_j;
				double costo ;
				
				GRBVar [] z_j; //vettore di variabili binarie legato al quesito III
				GRBVar [][] x_ij; // matrice che conterrà le variabili del problema
				int numVars;
				int numConstrs;
				double [] c; //vettore coefficienti variabili in F.O. 
				// c è il vettore che ha nelle colonne i coefficienti delle variabili in funzione obiettivo
				double [][] A; //matrice coefficienti variabili nei vincoli
				// A è la matrice che ha nelle righe i vincoli, e nelle colonne le variabili del problema
				double [] b; // vettore termini noti dei vincoli
				// b è il vettore che ha nelle colonne i termini noti dei vincoli
				double εMax = 1e7; // valore massimo oltre il quale il problema è 
				// definibile IMPOSSIBILE, a causa del valore troppo elevato della funzione obiettivo
				// e per cui infatti questa perderebbe di senso
				public double ε = 1e-5; 
				
				//costruttore della classe ModelloInput
				public ModelloInput() {
					super();
				
					// creo uno scanner per leggere uno a uno tutti i dati dal file
					try (
							BufferedReader in = new BufferedReader(new FileReader(new File("coppia_24.txt")));
							 Scanner scanner = new Scanner(in);
							){
						
							String str = scanner.next();
							
							if (str.substring(0, 1).equals("n")) 
								n = scanner.nextInt();
							scanner.nextLine();
							
							str = scanner.next();
							if (str.substring(0, 1).equals("m")) 
								m = scanner.nextInt();
							scanner.nextLine();
							
							str = scanner.next();
							if (str.equals("matrice")) {
								scanner.nextLine();
								d_ij = new int [m][n];
								for (int i=0; i<m; i++) {
									for (int j=0; j<n; j++) 
										d_ij[i][j] = scanner.nextInt();
								}
							}

							str = scanner.next();
							if (str.equals("r_i")) {
								scanner.nextLine();
								r_i = new int [n];
								for (int i=0; i<n; i++) 
									r_i[i] = scanner.nextInt();
							}

							str = scanner.next();
							if (str.equals("s_j")) {
								scanner.nextLine();
								s_j = new int [m];
								for (int i=0; i<m; i++) 
									s_j[i] = scanner.nextInt();
							}
							
							str = scanner.next();
							if (str.equals("c")) {
								String c1 = scanner.next();
								costo = Double.parseDouble(c1);
							}
							scanner.nextLine();

							str = scanner.next();
							if (str.equals("k")) 
								 k = scanner.nextInt();
							scanner.nextLine();

							str = scanner.next();
							if (str.equals("alfa_j")) {
								scanner.nextLine();
								alfa_j = new int [m];
								for (int i=0; i<m; i++) 
									alfa_j[i] = scanner.nextInt();
							}
							
							str = scanner.next();
							if (str.equals("h")) 
								 h = scanner.nextInt();			
							scanner.nextLine();

							str = scanner.next();
							if (str.equals("x")) {
								str = scanner.next();
								if (str.equals("y")) 
									x = scanner.nextInt();
								
								y = scanner.nextInt();
							}
							
					} catch (Exception e) {
						e.printStackTrace();
					}			
				}

				//metodi setters
				public void setX_ij(GRBVar[][] xij) {
					this.x_ij = xij;
				}
				public void setD_ij(int x, int y, int valore) {
					d_ij[x][y] = valore;
				}
				public void setNumVars(int numVars) {
					this.numVars = numVars;
				}
				public void setNumConstrs(int numConstrs) {
					this.numConstrs = numConstrs;
				}
				public void setC(double[] c) {
					this.c = c;
				}
				public void setA(double[][] a) {
					A = a;
				}
				public void setB(double[] b) {
					this.b = b;
				}
				public void setK(int k2) {
					this.k = k2;
				}
				public void setZ_j(GRBVar[] z_j) {
					this.z_j = z_j;
				}
			}

// ----------------------------main------------------------------------------------------------------------------------
			public static void main(String[] args) throws GRBException {
			
				//acquisisco i dati dal documento assegnato tramite una classe apposita
				ModelloInput modelloInput = new ModelloInput();
				
				GRBEnv env = new GRBEnv();
				env.set(IntParam.OutputFlag, 0);
				env.set(IntParam.LogToConsole, 0);
				env.start();
				GRBModel model = new GRBModel(env);
				
				//creo il modello passandogli come argomenti i dati ricavati dal documento
				 model = genereateModel(env, modelloInput);

				//utilizzo un try catch per rilevare eventuali eccezioni
				try {        
					//ottimizzo il modello creato
					model.optimize();
//------------------------------------QUESITO--1-----------------------------------------------------------------------
					System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
					System.out.println("GRUPPO <24>");
					System.out.println("Componenti: <Sabbadini> <Toresini>\n");
					//estraggo dati che serviranno in seguito, relativi a matrici del problema
					extractMatrix(model, modelloInput);
					
					System.out.println("QUESITO I:");
					 //stampo a video il valore della funzione obiettivo 
					double valoreObj = model.get(DoubleAttr.ObjVal);
					System.out.print("funzione obiettivo = <" + valoreObj +">\n");
					
					//stampo a video il valore assunto dalle variabili nella funzione obiettivo 
					for (int i=0; i<modelloInput.numVars; i++) {
						System.out.println("<" + model.getVar(i).get(GRB.StringAttr.VarName) 
								+ "> = <" +  model.getVar(i).get(GRB.DoubleAttr.X) + ">");                      
					}
						
					//scrivo se la soluzione è degenere
					if (degenere(model.getVars()) == true) 
						System.out.println("Degenere: sì"  );
					 else 
						System.out.println("Degenere: no"  );
	                 
					//scrivo se ho un ottimo multiplo
					if (additionalOptimum(model.getVars()) == true) 
						System.out.println("Multipla: sì"  );
					 else 
						System.out.println("Multipla: no"  );
					System.out.println();
//--------------------------------------QUESITO--2----------------------------------------------------------
					System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
					System.out.println("QUESITO II:");
					
					//stampo a video i vincoli non attivi
					System.out.println( vincoliNonAttivi(model, modelloInput) );
					
					//calcolo valori legati alla sensitività che serviranno in seguito
					String valoreSensitivitàDistanza = analisiSensitivitaDistanze(modelloInput, model);
					String valoreSensitivitàMagazzini =  analisiSensitivitaMagazzini( modelloInput, model);
					
					//stampo a video l’intervallo del parametro k affinché il problema non ha soluzione
					System.out.println(parametroImpossibileK(env, modelloInput));
					
					//stampo a video la soluzione ottima di base del problema duale
					//di cui -> stampo a video il valore assunto dalle variabili nella funzione obiettivo 
					modelloDuale(env, modelloInput, model);
					
					//stampo a video l'analisi di sensitività effettuata sul magazzino h
					System.out.println( valoreSensitivitàMagazzini);
					
					//stampo a video l'analisi di sensitività effettuata sulla distanza x-y
					System.out.println( valoreSensitivitàDistanza );
//-----------------------------------------------------QUESITO---3------------------------------------------------------
					System.out.println();
					System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
					System.out.println("QUESITO III:");
					
					//costruisco un nuovo modelloInput
					ModelloInput modelloInput2= new ModelloInput();
					// costruisco un nuovo model, con il rispettivo metodo
					GRBModel model3 = genereateModelQuesitoIII(env, modelloInput2);
					model3.optimize();
					
					//stampo a video il valore della funzione obiettivo, il risparmio
					System.out.println("risparmio = <" + risparmio(valoreObj, model3.get(DoubleAttr.ObjVal), modelloInput) + ">");
					
					//stampo a video la lista dei magazzini chiusi
					listaMagazziniChiusi(modelloInput2);
					System.out.println();
					
					//stampo a video la lista dei magazzini non sfruttati
					listaMagazziniMenoSfruttati(modelloInput2);
					System.out.println();
					
					//stampo a video il rilassamento continuo del problema
					rilassamentoContinuo(model3);
//-----------------------------------------------------------------------------------------------------------		
					}	catch (Exception e) {
						e.printStackTrace();
					}			
			}
		
//-----------------------------metodi--di--utilità-------------------------------------------------------------------------------------
				
			/**
			 * metodo per creare un modello
			 * @param env
			 * @param modelloInput
			 * @return
			 * @throws GRBException
			 */
			public static GRBModel genereateModel (GRBEnv env, ModelloInput modelloInput) throws GRBException {
				
				GRBModel model = new GRBModel(env);
				
				//creo la matrice delle mxn variabili e la riempio, definendo le variabili >= 0
				GRBVar[][] xij = new GRBVar[modelloInput.m][modelloInput.n];
				for (int i = 0; i <modelloInput.m; i++) {
					for (int j = 0; j < modelloInput.n; j++) {
						 xij[i][j] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "x_"+i+"_"+j);
					}
				}
				modelloInput.setX_ij(xij);
				
				// aggiungo le distanze acquisite nel modelloInput e pongo infinite le distanze maggiori di k
				// in questo modo non può essere trasportata merce tra magazzini e clienti con distanza > k
				// dato che il problema è un problema di minimo 
				for (int i = 0; i < modelloInput.d_ij.length; i++) {
					for (int j = 0; j < modelloInput.d_ij[0].length; j++) {
						if(modelloInput.d_ij[i][j] > modelloInput.k) 
							modelloInput.setD_ij(i, j, (int)GRB.INFINITY);
						
					}
				}
				
				// creo la funzione obiettivo aggiungendo come coefficienti delle variabili le rispettive distanze moltiplicate
				//per il costo (distanze in km e costo al km)
				GRBLinExpr obj = new GRBLinExpr();
				
				for (int i = 0; i < modelloInput.m; i++) {
					
					for (int j = 0; j < modelloInput.n; j++) {
						
						obj.addTerm(modelloInput.costo * modelloInput.d_ij[i][j],  xij[i][j]);
					}
				}
				//il nostro è un problema di minimo
				model.setObjective(obj, GRB.MINIMIZE);
				
				
				// aggiungo gli m vincoli di produzione 
				for (int i = 0; i < modelloInput.m; i++) {
					GRBLinExpr expr = new GRBLinExpr();
					
					//aggiungo al vincolo una variabile per ogni cliente (n variabili)
					for (int j = 0; j < modelloInput.n; j++) {	
						expr.addTerm(1, xij[i][j]);
					}
					//i vincoli di produzione sono vincoli di <= dove il limite è imposto dalla capacità di
					//stoccaggio del j-esimo magazzino
					model.addConstr(expr, GRB.LESS_EQUAL, modelloInput.s_j[i], "vincolo_produzione_i_"+i);
				}
				
				// aggiungo gli n vincoli di domanda
				for (int j = 0; j < modelloInput.n; j++) {
					GRBLinExpr expr = new GRBLinExpr();
					
					//aggiungo al vincolo una variabile per ogni magazzino (m variabili)
					for (int i = 0; i <modelloInput.m; i++) {
						expr.addTerm(1, xij[i][j]);
					}
					//i vincoli di domanda sono vincoli di = dove il valore è dato dalla richiesta di
					//merce dell'i-esimo cliente
					model.addConstr(expr, GRB.EQUAL, modelloInput.r_i[j], "vincolo_domanda_j_"+j);
				}
				
				
				
				
				return model;
			}
				
			/**
			 * metodo per assegnare correttamente i valori della matrici che utilizzerò nel problema
			 * @param model
			 * @param modelloInput
			 * @throws GRBException
			 */
			private static void extractMatrix(GRBModel model, ModelloInput modelloInput) throws GRBException {
				double [][] A; //matrice coefficienti variabili nei vincoli
				// A è la matrice che ha nelle righe i vincoli, e nelle colonne le variabili del problema
				A = new double [model.getConstrs().length][model.getVars().length];
				for (int i=0; i<A.length; i++) {
					for (int j=0; j<A[i].length; j++) {
						A[i][j] = model.getCoeff(model.getConstr(i), model.getVar(j));
					}
				}
				modelloInput.setA(A);
				
				double [] b; // vettore termini noti dei vincoli
				// b è il vettore che ha nelle colonne i termini noti dei vincoli
				b = new double[model.getConstrs().length];
				for (int i=0; i< b.length; i++) {
					b[i] = model.getConstr(i).get(DoubleAttr.RHS);
				}
				modelloInput.setB(b);
	
		
				double [] c; //vettore coefficienti variabili in F.O. 
				// c è il vettore che ha nelle colonne i coefficienti delle variabili in funzione obiettivo
				c = new double [model.getVars().length];
				for (int i=0; i<c.length ; i++) {
					c[i] = model.getVar(i).get(DoubleAttr.Obj);
				}
				modelloInput.setC(c);
	
				int numVars = c.length;
				modelloInput.setNumVars(numVars);
				int numConstrs = b.length;
				modelloInput.setNumConstrs(numConstrs);
			}

			/**
			 * metodo per verificare se la soluzione ottenuta è degenere
			 * (una soluzione è degenere Sse almeno 1 variabile in base ha valore = 0)
			 * @return
			 */
			private static boolean degenere(GRBVar [] vars) {
				
				try {
					//inizialmente pongo la soluzione non degenere
					boolean degenere = false;
					
					// faccio scorrere tutte le variabili
					for(GRBVar v: vars) 
						
						
						// verifico il valore di questi due attributi
						if(v.get(IntAttr.VBasis)==0 && v.get(GRB.DoubleAttr.X)==0) 
							degenere=true;
				
					if(degenere) return true;
					//se ho almeno una variabile in base che assume valore 0, la soluzione è degenere
					
					
				} catch (GRBException e) {
					e.printStackTrace();
				}
				
				return false;
			}
			
			/**
			 * metodo per verificare se il problema ha ottimo multiplo
			 * (soluzione è ottimo multiplo Sse esiste almeno 
			 * una variabile fuori base con coefficiente di costo ridotto = 0)
			 * @return
			 */
			private static boolean additionalOptimum (GRBVar [] vars) {

				try {
					boolean multiplo = false;
					
					// faccio scorrere tutte le variabili
					for(GRBVar v: vars) 
						
						// verifico il valore di questi due attributi
						if(v.get(IntAttr.VBasis)!=0 && v.get(GRB.DoubleAttr.RC)==0)  
							multiplo=true;	
					
					//se ho almeno una variabile fuori base con coefficiente di costo ridotto uguale a 0, ho un ottimo multiplo
					if(multiplo) return true;
					
				} catch (GRBException e) {
					e.printStackTrace();
				}
				
				return false;
			}

			/**
			 * metodo per creare e stampare il problema duale
			 * @param env
			 * @param modelloInput
			 * @param model
			 * @throws GRBException
			 */
			public static void modelloDuale(GRBEnv env, ModelloInput modelloInput, GRBModel model) throws GRBException {
				GRBModel modelloDuale = new GRBModel(env);
				//creo il modello duale partendo dal primale fornito in argomento
				modelloDuale = model.dualize();
				//ottimizzo il problema duale
				modelloDuale.optimize();
	
				System.out.println("soluzione duale:");
				//ottengo il valore delle variabili ottime del problema duale
				for (int i=0; i<modelloInput.n + modelloInput.m; i++) {
					double val = modelloDuale.getVar(i).get(GRB.DoubleAttr.X);
		
					//arrotondo il valore delle variabili
					
					//stampo le variabili ottime
					System.out.printf("<lambda" + i + "> = <%.4f>\n", val);
	}
}
		
			/**
			 * metodo per individuare e ritornare una stringa per a video il nome dei vincoli 
			 * non attivi (i.e. che non alterano il valore ottimo della funzione obiettivo);
			 * @param solver
			 * @return
			 * @throws GRBException
			 */
			public static String vincoliNonAttivi(GRBModel model, ModelloInput modelloInput) throws GRBException {
				
				StringBuffer descrizione = new StringBuffer();
				descrizione.append("lista dei vincoli non attivi = [");
				
				//scorre i vincoli e stampa a video quelli non attivi, ovvero 
				//i vincoli le cui variabili di slack associate sono diverse da zero 
				//(i vincoli che non passano per il vertice ottimo)
				for (int i=0; i<modelloInput.numConstrs; i++) {
					if (!(model.getConstr(i).get(DoubleAttr.Slack)<=modelloInput.ε  
							&& model.getConstr(i).get(DoubleAttr.Slack)>=-modelloInput.ε))
								descrizione.append(" <vincolo" + (i+1) + "> ");
				}
				descrizione.append("]");
				return descrizione.toString();
			}
			
			/**
			 * metodo per stabilire e stampare a video l’intervallo 
			 * del parametro k affinch ́e il problema non abbia soluzione;
			 * @param modelloInput
			 * @param env
			 * @param model
			 * @return
			 * @throws GRBException
			 */
			private static String parametroImpossibileK(GRBEnv env, ModelloInput modelloInput) throws GRBException {
				StringBuffer descrizione = new StringBuffer();
				
				//pongo il valore minimo di k affinché il problema abbia soluzione uguale al valore ricavato nel documento.
				//studio solo il valore inferiore in quanto se aumento k verso infinito elimino un vincolo, perciò non ho problemi di esistenza della soluzione
				int valInferiore = modelloInput.k;
				double val = 0.0;
				
				while ( val < modelloInput.εMax) {
					valInferiore--;
					GRBModel modelImpossibile = generateModelParamK(env, modelloInput, valInferiore);
					//decremento il valore di k finché il modello a cui passò il valore di k risulta impossibile, a quel punto esco dal ciclo
					val =  modelImpossibile.get(DoubleAttr.ObjVal);	
				}
				descrizione.append("intervallo k = (-INF, " + valInferiore + ")");
				
				//il problema risulta impossibile per valore di k che vanno da -infinito fino al valore minimo consentito (ricavato dal ciclo precedente)
				modelloInput.setK(12);
				return descrizione.toString();
	}
			
			/**
			 * metodo simil-precedente, utilizzato nel caso in cui debba essere 
			 * inserito manualmente un parametro (k) in un modelloInput
			 * @param env
			 * @param modello
			 * @param k
			 * @return
			 * @throws GRBException 
			 */
			private static GRBModel generateModelParamK (GRBEnv env, ModelloInput modello, int k) throws GRBException {
				modello.setK(k);
				GRBModel model = genereateModel(env, modello);
				model.optimize();
				
				return model;
			}
			
			/**
			 * metodo per effettuare l'analisi di sensitività sulla distanza, dati due parametri x e y in input	 
			 * @param solver
			 * @param modelloInput
			 * @return
			 * @throws GRBException
			 */
			public static String analisiSensitivitaDistanze(ModelloInput modelloInput, GRBModel model) throws GRBException {
				
					StringBuffer descrizione = new StringBuffer();
					//ottengo la variabile che corrisponde alla distanza tra magazzino x e cliente y
					GRBVar var = modelloInput.x_ij[modelloInput.y][modelloInput.x];
					
					
					//ottengo gli attributi SAObjUp e SAObjLow che mi dicono i limiti superiore e inferiore
					//del valore Cxy affinchè la base ottima del problema non cambi, dopodichè li divido per il costo
					//in modo da ottenere la distanza Dxy
					double sa_up = var.get(GRB.DoubleAttr.SAObjUp)/modelloInput.costo;
					
					double sa_low = var.get(GRB.DoubleAttr.SAObjLow)/modelloInput.costo;
					
					//controllo se il limite superiore è infinito
					if (sa_up > modelloInput.εMax ) {
						descrizione.append(String.format("intervallo d_xy = [%.4f, +INF] ", sa_low));
						System.out.printf("intervallo d_xy = [%.4f, +INF] ", sa_low);
						return descrizione.toString();
					}
					//stampo l'intervallo di valori di Dxy in cui la base ottima non cambia
					descrizione.append(String.format("intervallo d_xy = [%.4f, %.4f]", sa_low, sa_up));
					return descrizione.toString();
					}

			/**
			 * metodo per effettuare l'analisi di sensitività suli magazzini, dato il parametro h in input
			 * @param solver
			 * @param modelloInput
			 * @return
			 * @throws GRBException
			 */
			public static String analisiSensitivitaMagazzini(ModelloInput modelloInput, GRBModel model) throws GRBException {
					
				StringBuffer descrizione = new StringBuffer();
				//ottengo il vincolo di stoccaggio che corrisponde al magazzino h
				GRBConstr constr = model.getConstr(modelloInput.h);
				//ottengo gli attributi SARHSUp e SARHSLow che mi dicono i limiti superiore e inferiore
				//del valore Sh affinchè la base ottima del problema non cambi
				double sa_up = constr.get(GRB.DoubleAttr.SARHSUp);
				double sa_low = constr.get(GRB.DoubleAttr.SARHSLow);
				
				//controllo se il limite è infinito
				if (sa_up > modelloInput.εMax) {
					descrizione.append("intervallo d_xy = [" + sa_low + ", +INF] ");
					return descrizione.toString();
				}
				//stampo l'intervallo di valori di Sh in cui la base ottima non cambia
				descrizione.append("intervallo s_h = [" + sa_low + ", " + sa_up + "]");
				return descrizione.toString();
				}
			
			/**
			 * metodo per creare un modello di programmazione riferita al quesito 3
			 * @param env
			 * @param modelloInput
			 * @return
			 * @throws GRBException
			 */
			public static GRBModel genereateModelQuesitoIII (GRBEnv env, ModelloInput modelloInput) throws GRBException {
				GRBModel model = new GRBModel(env);
				
				//aggiungo il vettore di variabili binarie z_j che assume valore 
				// 0 in corrispondenza di un magazzino chiuso
				// 1 in corrispondenza di un magazzino aperto
				GRBVar[] z_j = new GRBVar[modelloInput.m];
				GRBVar[][] xij = new GRBVar[modelloInput.m][modelloInput.n];
				
				for (int i = 0; i <modelloInput.m; i++) {
					for (int j = 0; j < modelloInput.n; j++) 
						 xij[i][j] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "x_"+i+"_"+j);
					
					//inizializzo il vettore di variabili binarie z_j
					z_j[i] = model.addVar(0, 1, 0, GRB.BINARY, "z_"+i);
				}
				modelloInput.setX_ij(xij);
				modelloInput.setZ_j(z_j);
				
				//aggiungo le distanze 
				for (int i = 0; i < modelloInput.d_ij.length; i++) {
					for (int j = 0; j < modelloInput.d_ij[0].length; j++) {
						if(modelloInput.d_ij[i][j] > modelloInput.k) 
							modelloInput.setD_ij(i, j, (int)GRB.INFINITY);
					}
				}
				
				//vincoli di produzione 
				for (int i = 0; i < modelloInput.m; i++) {
					GRBLinExpr expr = new GRBLinExpr();
					
					for (int j = 0; j < modelloInput.n; j++) {	
						expr.addTerm(1, xij[i][j]);
					}
					
					//aggiungo un'espressione expr2 per moltiplicare s_j con il vettore di variabili binarie z_j
					//in modo che il limite di merce disponibile in un magazzino chiuso sia 0
					GRBLinExpr expr2 = new GRBLinExpr();
					expr2.addTerm(modelloInput.s_j[i], z_j[i]);;
					
					model.addConstr(expr, GRB.LESS_EQUAL, expr2, "vincolo_produzione_i_"+i);
				}
				
				// vincoli di domanda
				for (int j = 0; j < modelloInput.n; j++) {
					GRBLinExpr expr = new GRBLinExpr();
					
					for (int i = 0; i <modelloInput.m; i++) 
						expr.addTerm(1, xij[i][j] );	
					
					model.addConstr(expr, GRB.EQUAL, modelloInput.r_i[j], "vincolo_domanda_j_"+j);
				}
				
				// funzione obiettivo
				GRBLinExpr obj = new GRBLinExpr();
				
				for (int i = 0; i < modelloInput.m; i++) 
					for (int j = 0; j < modelloInput.n; j++) 
						obj.addTerm(modelloInput.costo * modelloInput.d_ij[i][j],  xij[i][j]);
					
				for (int i = 0; i < modelloInput.m; i++) 
					//moltiplichiamo le variabili binarie z_j per il corrispettivo coefficiente di risparmio alfa_j 
					obj.addTerm(modelloInput.alfa_j[i],  z_j[i]);
			
				model.setObjective(obj, GRB.MINIMIZE);
				
				return model;
			}
					
			/**
			 * metodo per stampare a video la lista dei magazzini chiusi
			 * @param modelloInput2
			 * @throws GRBException
			 */
			public static void listaMagazziniChiusi(ModelloInput modelloInput2) throws GRBException {
				System.out.print("lista magazzini chiusi = [");
				
				// un magazzino è chiuso se la variabile binaria a questo associata è = 0
				// un magazzino è aperto se la variabile binaria a questo associata è = 1
				for (int i=0; i<modelloInput2.z_j.length; i++) {
					if (modelloInput2.z_j[i].get(DoubleAttr.X) == 0) 
						System.out.print( " <"+  i + ">" );
				}
				System.out.print(" ]");
			}
			
			/**
			 * metodo per stampare a video la lista dei magazzini meno sfruttati
			 * @param modelloInput
			 * @param modelloInput2
			 * @throws GRBException
			 */
			public static void listaMagazziniMenoSfruttati(ModelloInput modelloInput) throws GRBException {
				System.out.print("lista magazzini meno sfruttati = [");

				//inizializzo la variabile menoSfruttato, che rappresenta la merce 
				//presente nel magazzino meno sfruttato, ad un valore molto alto
				double menoSfruttato = modelloInput.εMax;
				for (int i = 0; i < modelloInput.m; i++) {
					
					//inizializzo un contatore per l'i-esimo magazzino
					double contatore = 0;
					
					//sommo al contatore la merce trasportata dal magazzino a ciascun cliente
					for (int j = 0; j < modelloInput.n; j++) 	
						contatore = contatore + modelloInput.x_ij[i][j].get(DoubleAttr.X);
					
					//confronto il contatore con la variabile menoSfruttato e la aggiorno
					//qualora il contatore corrispondente a quel magazzino sia più piccolo
					//, controllando che il magazzino non sia chiuso (=0)
					if ( contatore <= menoSfruttato && modelloInput.z_j[i].get(DoubleAttr.X) != 0) 
						menoSfruttato = contatore;		
				}
				
				//stampo a video gli indici dei magazzini con 
				// il valore "menoSfruttato" 
				for (int i = 0; i < modelloInput.m; i++) {
					double contatore = 0;

					for (int j = 0; j < modelloInput.n; j++) 	
						contatore = contatore + modelloInput.x_ij[i][j].get(DoubleAttr.X);
					
					if (contatore == menoSfruttato) System.out.print(" <"+  i + ">");
				}
				System.out.print(" ]");
			}
			
			/**
			 * metodo per calcolare il rilassamento continuo e stamparlo a video
			 * @param model3
			 * @throws GRBException
			 */
			public static void rilassamentoContinuo(GRBModel model) throws GRBException {
				GRBModel rilassamento = model.relax();
				// calcolo l'ottimizzazione del rilassato
				rilassamento.optimize();
				System.out.printf("rilassamento continuo = <%.4f>", rilassamento.get(DoubleAttr.ObjVal));
			}			
			
			/**
			 * metodo per il calcolo del risparmio
			 * @param fObiettivoQuesitoI
			 * @param fObiettivoQuesitoIII
			 * @param modelloInput
			 * @return
			 */
			public static double risparmio(double fObiettivoQuesitoI, double fObiettivoQuesitoIII, ModelloInput modelloInput) {
				
				double sommatore = 0;
				//sommo tutti i coefficienti alfa_j dei risparmi in una variabile

				for (int i=0; i<modelloInput.alfa_j.length; i++) 
					sommatore = sommatore + modelloInput.alfa_j[i];
				
				//sottraggo i coefficienti alfa_j di risparmio dal valore ottenuto per ottenere l'ottimo
				//della funzione obiettivo del modello con risparmi (avevamo aggiunto z_j moltiplicandolo
				// per i coefficienti di risparmio, ma in questo modo vengono contati i risparmi dei magazzini
				//aventi variabile binaria uguale a 1, ovvero quelli aperti, mentre il risparmio si ha
				//in corrispondenza dei magazzini chiusi). Infatti sommando i risparmi dei magazzini aperti
				//e sottraendo la somma di tutti i coefficienti si ha come risultato sottrarre i risparmi
				//ottenuti dai magazzini chiiusi con z_j = 0.
				double valore = fObiettivoQuesitoIII-sommatore;
				
				//a questo punto troviamo il risparmio totale come differenza tra le due funzioni obiettivo
				//(un altro metodo sarebbe stato sommare i coefficienti di risparmio dei magazzini chiusi)

				return fObiettivoQuesitoI-valore;
			}
}