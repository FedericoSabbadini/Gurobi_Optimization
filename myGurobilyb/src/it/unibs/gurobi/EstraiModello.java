package it.unibs.gurobi;
import gurobi.GRB.CharAttr;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.IntAttr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;

/**
 * classe per estrarre un problema da un file lp e ritornarlo come codice
 * @author federicosabbadini
 */
public class EstraiModello {

	private String percorsoIstanza; //sarà il nome del file lp
	
	
	/**
	 * costruttore
	 * @param percorsoIstanza
	 */
	public EstraiModello(String percorsoIstanza) {
		super();
		this.percorsoIstanza = percorsoIstanza;
	}
	
	
	/**
	 * metodo che consente l'estrazione dei dati, tramite l'opportuna creazione di matrici . . .
	 * @return
	 */
	public ModelDefinition estrai() {
		
	
		try {
			
		// definizione di un modello
			GRBEnv env = new GRBEnv();
			GRBModel model = new GRBModel(env, percorsoIstanza);
			
			
		// A è la matrice che ha nelle righe i vincoli, e nelle colonne le variabili del problema
		// creazione di A
			double [][] A = new double [model.getConstrs().length][model.getVars().length];
			
			for (int i=0; i<A.length; i++) {
				for (int j=0; j<A[i].length; j++) {
					A[i][j] = model.getCoeff(model.getConstr(i), model.getVar(j));
				}
			}
			
			
		// b è il vettore che ha nelle colonne i termini noti dei vincoli
		// creazione di b
			double [] b = new double[model.getConstrs().length];
			char [] versus = new char[model.getConstrs().length];
			
			for (int i=0; i< b.length; i++) {
				b[i] = model.getConstr(i).get(DoubleAttr.RHS);
				versus[i] = model.getConstr(i).get(CharAttr.Sense);
			}
			
			
		// optimizationSense si riferisce al verso della funzione obiiettivo
			int optimizationSense = model.get(IntAttr.ModelSense);
			
			
		// c è il vettore che ha nelle colonne i coefficienti delle variabili in funzione obiettivo
		// creazione di c
			double [] c = new double [model.getVars().length];
			
			for (int i=0; i<c.length ; i++) {
				c[i] = model.getVar(i).get(DoubleAttr.Obj);
			}
			
			
			return new ModelDefinition(c, A, b, versus, optimizationSense);
	
		} catch (GRBException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}