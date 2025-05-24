package it.unibs.gurobi;
import java.util.ArrayList;
import java.util.List;
import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.DoubleParam;
import gurobi.GRB.IntAttr;
import gurobi.GRB.IntParam;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

/**
 * classe per effettuare azioni su un modelDefinition
 * @author federicosabbadini
 */
public class ModelSolver {

	
	private ModelDefinition modelDefinition;
	private GRBEnv env;
	private GRBModel model;
	private GRBVar[] vars;
	private GRBConstr[] constrs; 
	public double ε = 1e-5; // valore di tolleranza (utilizzato ad esempio per arrotondare certi valori)
	private double [] varValues; // vettore che contiene le variabili
	private double [] rc; // vettore che contiene i coefficienti di costo ridotto delle variabili
	private double [] slacks; // vettore che contiene le variabili di slack
	private double [] sp;// vettore che contiene i prezzi ombra delle slack
	private double solValue;
	
	
	/**
	 * costruttore
	 * @param modelDefinition
	 */
	public ModelSolver(ModelDefinition modelDefinition) {
		this.modelDefinition = modelDefinition;
		buildModel();
	}
	
	
	// Getters & Setters
	public ModelDefinition getModelDefinition() {
		return modelDefinition;
	}
	public double[] getVarValues() {
		return varValues;
	}
	public double getSolValue() {
		return solValue;
	}
	public double[] getRc() {
		return rc;
	}
	public double[] getSp() {
		return sp;
	}
	public double[] getSlacks() {
		return slacks;
	}
	public double getε() {
		return ε;
	}
	public GRBModel getModel() {
		return model;
	}
	public GRBVar[] getVars() {
		return vars;
	}
	public GRBConstr[] getConstrs() {
		return constrs;
	}
	public boolean isDegenere() {
		return degenere();
	}
	public boolean isAdditionalOptimum() {
		return additionalOptimum();
	}


	/**
	 * metodo per costruire un modelSolver
	 */
	private void buildModel() {
		
		try {
			
			env = new GRBEnv();
			env.set(IntParam.Presolve, 0);
			env.set(IntParam.Method, 0);
			env.set(DoubleParam.Heuristics, 0);
			env.set(IntParam.OutputFlag, 0);
			
			model = new GRBModel(env);
			
			addVariables(); // aggiunta delle variabili al modelSolver
			addConstraints(); // aggiunta dei metodi al modelSolver
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * metodo che permette l'aggiunta di costruttori al modelSolver,
	 * sfruttando le informazioni fornite dal modelDefinition
	 * @throws GRBException
	 */
	private void addConstraints() throws GRBException {
		
		constrs = new GRBConstr[modelDefinition.getNumConstrs()];
		
		for (int i=0; i<modelDefinition.getNumConstrs(); i++) {
			GRBLinExpr expr = new GRBLinExpr();
			
			for (int j=0; j<modelDefinition.getNumVars(); j++) 
				if (modelDefinition.getA(i, j) != 0)
					expr.addTerm(modelDefinition.getA(i, j), vars[j]);
			
			constrs[i] = model.addConstr(expr, modelDefinition.getVersus(i), modelDefinition.getB(i), "vincolo" + (i+1));
		}
		
		
	}

	/**
	 * metodo che permette l'aggiunta di variabili al modelSolver,
	 * sfruttando le informazioni fornite dal modelDefinition
	 * @throws GRBException
	 */
	private void addVariables() throws GRBException {
		
		vars = new GRBVar[modelDefinition.getNumVars()];
		
		for (int i=0; i<modelDefinition.getNumVars(); i++) 
			vars[i] = model.addVar(0, GRB.INFINITY, modelDefinition.getC(i), GRB.CONTINUOUS, "x" + (i+1));
		
		model.set(IntAttr.ModelSense, modelDefinition.getOptimizationSense());
	}

	
	/**
	 * metodo per creare tutte le variabili di cui un modelSolver 
	 * ha bisogno, ottimizzandone il model associato
	 * @return
	 */
	public double solve() {
		
		try {
			
			model.optimize();
			
			if(model.get(IntAttr.Status) != GRB.UNBOUNDED 
					&& model.get(IntAttr.Status) != GRB.INF_OR_UNBD 
					&& model.get(IntAttr.Status) != GRB.INFEASIBLE ) {
				
				createSolutionValue();
				createVarValues();
				createRc();
				createSlacks();
				createSP();
			}
			
		} catch (GRBException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

	
	/**
	 * metodo per determinare se un certo model ha soluzione ottima finita
	 * @return
	 */
	public boolean hasFiniteSolution() {
		
		try {
			
			if(model.get(IntAttr.Status) == GRB.OPTIMAL 
					&& model.get(IntAttr.Status) != GRB.UNBOUNDED 
					&& model.get(IntAttr.Status) != GRB.INF_OR_UNBD ) 
				return true;
						
		} catch (GRBException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	
	/**
	 * metodo per costruire i valori delle variabili di un modelSolver
	 */
	private void createVarValues() {
		
		varValues = new double [vars.length];
		
		try {
			
			for (int i=0; i<varValues.length; i++) 
				varValues[i] = 
					(vars[i].get(DoubleAttr.X) <= ε && vars[i].get(DoubleAttr.X) >= -ε) ? 0.0: vars[i].get(DoubleAttr.X);
			
		} catch (GRBException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * metodo per costruire il valore della soluzione di un modelSolver
	 */
	private void createSolutionValue() { 
		
		try {
			
			if (hasFiniteSolution()) solValue = model.get(GRB.DoubleAttr.ObjVal);
			
		} catch (GRBException e) {
			e.printStackTrace();
		}	
	} 
	
	/**
	 * metodo per determinare i coefficienti di costo ridotto delle variabili di un modelSolver
	 */
	private void createRc() {
		
		rc = new double[vars.length];
		
		try {
			
			for(int i=0; i<rc.length; i++) {
				rc[i] = (vars[i].get(DoubleAttr.RC)<=ε 
						&& vars[i].get(DoubleAttr.RC)>= -ε) ? 0.0: vars[i].get(DoubleAttr.RC); 
			}
			
		} catch (GRBException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * metodo per costruire le variabili di slack di un modelSolver
	 * N.B. che per accedere alle slack è necessario indagare i vincoli
	 */
	private void createSlacks() {
		
		slacks = new double[constrs.length];
		
		try {
			
			for (int i=0; i<slacks.length; i++) {
				slacks[i] = (constrs[i].get(DoubleAttr.Slack)<=ε 
						&& constrs[i].get(DoubleAttr.Slack)>=-ε) ? 0.0: constrs[i].get(DoubleAttr.Slack);
			}
			
		} catch (GRBException e) {
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * metodo per determinare i prezzi ombra delle slack di un modelSolver
	 * N.B. che per accedere alle slack è necessario indagare i vincoli
	 */
	private void createSP() {
		
		sp = new double [constrs.length];
		
		try {
			
			for (int i=0; i<sp.length; i++) 
				sp[i] = (constrs[i].get(DoubleAttr.Pi)<=ε 
				&& constrs[i].get(DoubleAttr.Pi)>=-ε) ? 0.0: constrs[i].get(DoubleAttr.Pi);
			
		} catch (GRBException e) {
			e.printStackTrace();
		}
		
	}

	
	/**
	 * metodo per scrivere su file
	 * @param name
	 */
	public void write (String name) {
		
		try {
			
			model.write(name + ".lp" );
			
		} catch (GRBException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * metodo per libearer le risorse in memoria
	 */
	public void dispose () {
		
		model.dispose();
		
		try {
			
			env.dispose();
			
		} catch (GRBException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * metodo per determinare le variabili in base di un modelSolver
	 * @return
	 */
	public List<Integer> getBasicVariables() {
		List<Integer> vBasic = new ArrayList<>();
		
		int counter=0;
		
		// faccio scorrere tutte le variabili del modelDefinition
		for (GRBVar var: model.getVars()) {
			
			try {
				
				// verifico il valore di questo attributo
				if (var.get(IntAttr.VBasis) ==0.0) 
					vBasic.add(counter);
				
			} catch (GRBException e) {
				e.printStackTrace();
			}
			
			counter++;
		}
		return vBasic;
	}
	
	/**
	 * metodo per determinare le variabili non in base di un modelSolver
	 * @return
	 */
	public List<Integer> getNonBasicVariables() {
		List<Integer> nonvBasic = new ArrayList<>();
		
		int counter=0;
		
		// faccio scorrere tutte le variabili del modelDefinition
		for (GRBVar var: model.getVars()) {
			
			try {
				
				// verifico il valore di questo attributo
				if (var.get(IntAttr.VBasis) !=0.0) 
					nonvBasic.add(counter);
				
			} catch (GRBException e) {
				e.printStackTrace();
			}
			
			counter++;
		}
		return nonvBasic;
	}
	
	
	/**
	 * metodo per determinare le slack in base di un modelSolver
	 * * N.B. che per accedere alle slack è necessario indagare i vincoli
	 * @return
	 */
	public List<Integer> getBasicSlackVariables() {
		List<Integer> vBasic = new ArrayList<>();
		
		int counter=0;
		
		// faccio scorrere tutte le slack del modelDefinition
		for (GRBConstr constr: model.getConstrs()) {
			
			try {
				
				// verifico il valore di questo attributo
				if (constr.get(IntAttr.CBasis) ==0) 
					vBasic.add(counter);
				
			} catch (GRBException e) {
				e.printStackTrace();
			}
			
			counter++;
		}
		return vBasic;
	}
	
	
	/**
	 * metodo per determinare le slack non in base di un modelSolver
	 * * N.B. che per accedere alle slack è necessario indagare i vincoli
	 * @return
	 */
	public List<Integer> getNonBasicSlackVariables() {
		List<Integer> nonvBasic = new ArrayList<>();
		
		int counter=0;
		
		// faccio scorrere tutte le slack del modelDefinition
		for (GRBConstr constr: model.getConstrs()) {
			
			try {
				
				// verifico il valore di questo attributo
				if (constr.get(IntAttr.CBasis) !=0) 
					nonvBasic.add(counter);
				
			} catch (GRBException e) {
				e.printStackTrace();
			}
			
			counter++;
		}
		return nonvBasic;
	}
	
	
	/**
	 * metodo per verificare se la soluzione ottenuta è degenere
	 * (una soluzione è degenere Sse almeno 1 variabile in base ha valore = 0)
	 * @return
	 */
	private boolean degenere() {


		try {
			
			boolean degenere = false;
			
			// faccio scorrere tutte le variabili del modelDefinition
			for(GRBVar v: vars) {
				
				// verifico il valore di questi due attributi
				if(v.get(IntAttr.VBasis)==0 && v.get(GRB.DoubleAttr.X)==0) {  
					degenere=true;
				}
			}
			
			if(degenere) return true;
			
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
	private boolean additionalOptimum () {

		try {
			
			boolean multiplo = false;
			
			// faccio scorrere tutte le variabili del modelDefinition
			for(GRBVar v: vars) {
				
				// verifico il valore di questi due attributi
				if(v.get(IntAttr.VBasis)!=0 && v.get(GRB.DoubleAttr.RC)==0) {  
					multiplo=true;
				}
					
			}
			if(multiplo) return true;
			
		} catch (GRBException e) {
			e.printStackTrace();
		}
		
		return false;
		
	}
	
	
}