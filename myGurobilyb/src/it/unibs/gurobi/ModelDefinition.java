package it.unibs.gurobi;

/**
 * classe che permette di creare un generico modello 
 * di problema, su cui lavorare e eseguire metodi
 * @author federicosabbadini
 */
public class ModelDefinition {

	private double [] c; //vettore coefficienti variabili in F.O. 
	// c è il vettore che ha nelle colonne i coefficienti delle variabili in funzione obiettivo
	
	private double [][] A; //matrice coefficienti variabili nei vincoli
	// A è la matrice che ha nelle righe i vincoli, e nelle colonne le variabili del problema
	
	private double [] b; // vettore termini noti dei vincoli
	// b è il vettore che ha nelle colonne i termini noti dei vincoli
	
	private char [] versus; // vettore versi dei vincoli
	// b è il vettore che ha nelle colonne i versi dei vincoli
	
	private int optimizationSense; // 1= MINIMIZZA, -1=MASSIMIZZA
	// optimizationSense si riferisce al verso della funzione obiiettivo
	
	private int numVars;
	private int numConstrs;
	
	
	/**
	 * costruttore
	 * @param c
	 * @param a
	 * @param b
	 * @param versus
	 * @param optimizationSense
	 */
	public ModelDefinition(double[] c, double[][] a, double[] b, char[] versus, int optimizationSense) {
		super();
		
		this.c = c;
		this.A = a;
		this.b = b;
		this.versus = versus;
		this.optimizationSense = optimizationSense;
		
		this.numVars = c.length;
		this.numConstrs = b.length;
	}


	// metodi Getters & Setters
	public double getC(int i) {
		return c[i];
	}
	public double[] getC() {
		return c;
	}
	public void setC(int i, double valore) {
		this.c[i] = valore;
	}


	public double getA(int i, int j) {
		return A[i][j];
	}
	public double[][] getA() {
		return A;
	}
	public void setA(int i, int j, double valore) {
		this.A[i][j] = valore;
	}


	public double getB(int i) {
		return b[i];
	}
	public double[] getB() {
		return b;
	}
	public void setB(int i, double valore) {
		this.b[i] = valore;
	}


	public char getVersus(int i) {
		return versus[i];
	}
	public char[] getVersus() {
		return versus;
	}
	public void setVersus(int i, char valore) {
		this.versus[i] = valore;
	}


	public int getOptimizationSense() {
		return optimizationSense;
	}
	public void setOptimizationSense(int optimizationSense) {
		this.optimizationSense = optimizationSense;
	}
	
	
	public int getNumVars() {
		return numVars;
	}
	public void setNumVars(int numVars) {
		this.numVars = numVars;
	}
	
	
	public int getNumConstrs() {
		return numConstrs;
	}
	public void setNumConstrs(int numConstrs) {
		this.numConstrs = numConstrs;
	}
}