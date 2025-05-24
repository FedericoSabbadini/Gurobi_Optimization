package it.unibs.gurobi;
import java.util.ArrayList;
import java.util.List;
import org.ejml.simple.SimpleMatrix;

import gurobi.GRB;
import gurobi.GRB.IntAttr;
import gurobi.GRBException;
import gurobi.GRBVar;

/**
 * classe con metodi statici di utilità generale
 * @author federicosabbadini
 */
public class Methods {
	
	
	/**
	 * metodo che consente di generare l'ottimo multiplo del problema
	 * @param model
	 * @param ms
	 * @return String da essere stampata
	 */
	public static String generateAdditionalOptimal(ModelDefinition model, ModelSolver ms) {
		
		StringBuffer descrizione = new StringBuffer();
		double ε = ms.getε();
		SimpleMatrix A = new SimpleMatrix(model.getA()); 
		// creo una simplematrix A con cui opererò in seguito, e su cui lavorare per costruire le nuove matrici
		

		double [] vars = ms.getVarValues();
		double [] slacks = ms.getSlacks();
		double [] rc = ms.getRc();
		double [] sp = ms.getSp();
		
		// stampo a video le informazioni relative alla soluzione corrente
		descrizione.append("Soluzione corrente:\n");
		for (int i=0; i<vars.length; i++) 
			if (vars[i]> ε) descrizione.append("x" + i + " -> " + vars[i] + "\n");
		
		for (int i=0; i<slacks.length; i++) 
			if (slacks[i]> ε) descrizione.append("s" + i + " -> " + slacks[i] + "\n");
		
		
		// utilizzo i metodi del modelSolver per determinare la tipologia di 
		// variabile, e i rispettivi coefficienti di costo ridotto
		List<Integer> varInBase = ms.getBasicVariables();
		List<Integer> varNonInBase = ms.getNonBasicVariables();
		List<Integer> slackvarInBase = ms.getBasicSlackVariables();
		List<Integer> slackvarNonInBase = ms.getNonBasicSlackVariables();
		
		
	// OBIETTIVO: individuare la variabile candidata a entrare
	// faccio scorrere i coefficienti di costo ridotto e trovo il minimo
		int indexV= -1;
		int indexS= -1;
		
		for (int i=0; i<varNonInBase.size(); i++) {
			int v = varNonInBase.get(i);
			
			if (rc[v] <= ε && rc[v] >= -ε)
				indexV=i;
		}

		for (int i=0; i<slackvarNonInBase.size(); i++) {
			int s = slackvarNonInBase.get(i);
			
			if (sp[s] <= ε && sp[s] >= -ε)
				indexS=i;
		}
		
		int colonnaN=-1;
		if (indexV>=0) {
			descrizione.append("La variabile x" + varNonInBase.get(indexV) + "(in posizione " + indexV 
					+ " nella matrice N) è fuori base con Coefficiente di costo ridotto 0\n");
			colonnaN=indexV;
		}
		if (indexS>=0) {
			descrizione.append("La slack s" + slackvarNonInBase.get(indexS) + "(in posizione " + (varNonInBase.size()+indexS) 
					+ " nella matrice N) è fuori base con Coefficiente di costo ridotto 0\n");
			colonnaN= varNonInBase.size() + indexS;
		}

		
	// creo le nuove matrici per determinare il nuovo ottimo

		SimpleMatrix B = extractMatrix(A, varInBase, slackvarInBase); // matrice variabili e slack in base
		SimpleMatrix BAM1 = B.invert();  // = B^-1
		SimpleMatrix N = extractMatrix(A, varNonInBase, slackvarNonInBase); // matrice variabili e slack non in base
		
		
		SimpleMatrix b = new SimpleMatrix( new double [][] {model.getB()}).transpose(); 
		SimpleMatrix BAM1Pb = BAM1.mult(b); // = B^-1 * b = valore delle variabili in base
		
		double [] cb = new double [varInBase.size() + slackvarInBase.size()]; 
		//vettore dei coefficienti delle variabili in base, in funzione obiettivo 
		// N.B. sicuramente l slack non saranno in funzione obiettivo (coefficiente = 0)
		
		for (int i=0; i<varInBase.size(); i++) {
			int var = varInBase.get(i);
			cb[i] = model.getC(var);
		}
		
		SimpleMatrix cbMatrix = new SimpleMatrix( new double[][] {cb});
		
		SimpleMatrix result1 = cbMatrix.mult(BAM1Pb); // = cb^T * B^-1 * b
		descrizione.append("Valore funzione obiettivo iniziale: " + result1.get(0, 0) + "\n\n");
		
		
	// OBIETTIVO: individuare la variabile candidata a uscire
	// calcolo tutti i coefficienti per il rapporto minimo
		
		SimpleMatrix BAM1PN = BAM1.mult(N); // = B^-1 * n
		
		int indexMin = -1;
		double minRapporto = Double.MAX_VALUE;
		
		// cerco l'indice associato alla variabile in base con rapporto minimo
		for (int i=0; i<BAM1PN.numRows(); i++) {
			double rapporto = BAM1Pb.get(i, 0) / BAM1PN.get(i, colonnaN);
			// colonnaN è la colonna della variabile candidata a entrare
			
			if (rapporto >= 0 && rapporto < minRapporto) {
				minRapporto = rapporto;
				indexMin = i;
			}
		}
		
		// la variabile candidata può essere sia una variabile, che una slack
		int varUscente = -1;
		int slackUscente = -1;
	
		if (indexMin < varInBase.size()) {
			varUscente = varInBase.get(indexMin);
			descrizione.append("La variabile uscente è x" + varUscente + ", con rapporto minimo = " + minRapporto + "\n\n" );
			varInBase.remove(indexMin);
			
		} else {
			slackUscente = slackvarInBase.get(indexMin - varInBase.size());
			slackvarInBase.remove(indexMin - varInBase.size());
			descrizione.append("La slack uscente è x" + slackUscente + ", con rapporto minimo = " + minRapporto + "\n\n" );
		}
		
		
		// tolgo la variabile che deve uscire, e metto la variabile candidata a entrare
		// all'interno dei vettori varInBase e varNonInBase
		if (indexV >= 0) {
			int var = varNonInBase.get(indexV);
			boolean added = false;
			
			for (int i=0; i<varInBase.size(); i++) {
				int element = varInBase.get(i);
				
				if (var < element) {
					varInBase.add(i, var);
					added = true;
					break;
				}
			}
			
			if (!added) varInBase.add(var);
		}
		
		// tolgo la slack che deve uscire, e metto la slack candidata a entrare
		// all'interno dei vettori slackvarInBase e slackvarNonInBase
		if (indexS >= 0) {
			int slackvar = slackvarNonInBase.get(indexS);
			boolean added = false;
			
			for (int i=0; i<slackvarInBase.size(); i++) {
				int element = slackvarInBase.get(i);
				
				if (slackvar < element) {
					slackvarInBase.add(i, slackvar);
					added = true;
					break;
				}
			}
			
			if (!added) slackvarInBase.add(slackvar);
		}
			
		
	// OBIETTIVO: calcolare la nuova soluzione ottima
		// effettuo gli stessi passaggi dell'inizio, calcolando le 
		// nuove matrici a partire dalle nuove variabili in/fuori base
		
		SimpleMatrix newB = extractMatrix(A, varInBase, slackvarInBase);
		SimpleMatrix newBAM1 = newB.invert();
		SimpleMatrix solution = newBAM1.mult(b);
		
		descrizione.append("Nuova soluzione: \n");
		
		
		for (int i=0; i<varInBase.size(); i++) 
			descrizione.append("x" +varInBase.get(i) + " -> " + solution.get(i, 0) + "\n");
		
		for (int i=0; i<slackvarInBase.size(); i++) 
			descrizione.append("x" +slackvarInBase.get(i) + " -> " + solution.get(i + varInBase.size(), 0)+ "\n");
		
		// calcolo del nuovo vettore Cb
		double [] newCb = new double[varInBase.size() + slackvarInBase.size()];
		
		for (int i=0; i<varInBase.size(); i++) {
			int var = varInBase.get(i);
			newCb[i] = model.getC(var);
		}
		
		SimpleMatrix newCbMatrix = new SimpleMatrix(new double[][] {newCb});
		SimpleMatrix result = newCbMatrix.mult(solution);
		
		descrizione.append("\nValore nuova Funzione Obiettivo: " + result.get(0,0));
	
		return descrizione.toString();	
	}	
	
	

	/**
	 * metodo che a partire da una matrice, permette 
	 * di estrarre solo le colonne indicate in input 
	 * @param A
	 * @param varsSelected
	 * @param slacksSelected
	 * @return
	 */
	private static SimpleMatrix extractMatrix(SimpleMatrix A, List<Integer> varsSelected, List<Integer> slacksSelected) {
		
		List <SimpleMatrix> columns = new ArrayList<>();
		
		for (int i=0; i<varsSelected.size(); i++) 
			// estrazione di una colonna
			columns.add(A.extractVector(false, varsSelected.get(i))); 
		
		SimpleMatrix B = null;
		if (columns.size()>0) {
			B = columns.get(0);
			
			for (int i=1; i<columns.size(); i++) 
				B = B.concatColumns(columns.get(i));
		}
		
		for (int i=0; i<slacksSelected.size(); i++) {
			int slack = slacksSelected.get(i);
			
			double[][] column = new double[A.numRows()][1];
			
			column[slack][0] = 1;
			if (B==null) {
				B = new SimpleMatrix(column);
			} else {
				B = B.concatColumns(new SimpleMatrix(column));
			}	
		} 
		
		return B;	
	}
	
	
	
	/* metodi già implementati nella classe relativa al modelSolver
	 * 
	 * public static String degenere(ModelSolver ms) {

		StringBuffer descrizione = new StringBuffer();
		
		try {
			
			// è degenere <-> almeno 1 var in base ha val 0
			boolean degenere = false;
			
			for(GRBVar v: ms.getVars()) {
				if(v.get(IntAttr.VBasis)==0 && v.get(GRB.DoubleAttr.X)==0) {  
					degenere=true;
				}
			}
			
			if(degenere) descrizione.append("\n	La soluzione è degenere");
			else descrizione.append("\n	La soluzione non è degenere\n");
			
		} catch (GRBException e) {
			e.printStackTrace();
		}
		
		return descrizione.toString();
	}
	
	
	public static String additionalOptimum (ModelSolver ms) {
		
		StringBuffer descrizione = new StringBuffer();
		
		try {
			
			//soluz multipla?
			//sol ottimo multiplo <-> ho almeno una var fuori base con ccr = 0
			Boolean multiplo = false;
			
			
			for(GRBVar v: ms.getVars()) {
				if(v.get(IntAttr.VBasis)!=0 && v.get(GRB.DoubleAttr.RC)==0) {  
					multiplo=true;
				}
					
			}
			if(multiplo) System.out.println("\n	La soluzione è multipla");
			else System.out.println("\n	La soluzione non è multipla");
			
		} catch (GRBException e) {
			e.printStackTrace();
		}
		
		return descrizione.toString();
		
	}
	 */
}