package de.upb.crc901.services.mlpipeline;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * 
 * A class that helps 'plan' a ML-pipeline.
 * 
 * Currently a ML-pipeline only exists of a series of AttributeSelection's (0 or more) followed by a single classifier.
 * 
 * @author aminfaez
 *
 */
public class MLPipelinePlan {
	// list of preprocessors
	private List<MLPipe> atrPipes = new LinkedList<>();
	
	// Points to the end of the pipeline.
	private MLPipe cPipe;

	// contains the host name of the next added pipeline.
	private String nextHost;
	
	public MLPipelinePlan onHost(String hostname) {
		this.nextHost = Objects.requireNonNull(hostname);
		return this;
	}
	
	public MLPipelinePlan onHost(String host, int port) {
		return onHost(host + ":" + port);
	}
	

	public MLPipe addAttributeSelection(String classname) {
		Objects.requireNonNull(this.nextHost, "Host needs to be specified before adding pipes to the pipeline.");
		MLPipe asPipe =  new MLPipe(this.nextHost, Objects.requireNonNull(classname));
		atrPipes.add(asPipe); // add to pipe list before returning.
		return asPipe;
	}
	
	public WekaAttributeSelectionPipe addWekaAttributeSelection() {
		Objects.requireNonNull(this.nextHost, "Host needs to be specified before adding pipes to the pipeline.");
		WekaAttributeSelectionPipe asPipe =  new WekaAttributeSelectionPipe(this.nextHost);
		atrPipes.add(asPipe); // add to pipe list before returning.
		return asPipe;
	}
	
	public MLPipe setClassifier(String classifierName) {
		Objects.requireNonNull(this.nextHost, "Host needs to be specified before adding pipes to the pipeline.");
		this.cPipe = new MLPipe(this.nextHost, classifierName); // set cPipe field.
		return cPipe;
	}
	
	/**
	 * Returns True if the plan is 'valid' in the sense that a classifier was set.
	 */
	public boolean isValid() {
		if(cPipe == null) { // if classifier is null return false immediately
			return false;
		}
		for(MLPipe pipe : atrPipes) { 
			if(!pipe.isValid()) {
				return false;
			}
		}
		return true;
	}
	
	public List<MLPipe> getAttrSelections(){
		return atrPipes;
	}
	
	public MLPipe getClassifierPipe() {
		return cPipe;
	}
	
	
	
	// CLASSES for pipe creation.
	abstract class AbstractPipe {
		private final String host;
		
		protected AbstractPipe(String hostname) {
			this.host = Objects.requireNonNull(hostname);
		}
		
		protected String getHost() {
			return this.host;
		}

		protected boolean isValid() {
			return true;
		}
	}
	
	class MLPipe extends AbstractPipe {
		private final String classifierName;
		private final Set<String> classifierOptions = new TreeSet<>();
		private final List<Object> constructorArgs = new ArrayList<>(); 
		
		protected MLPipe(String hostname, String classifierName) {
			super(hostname);
			this.classifierName = Objects.requireNonNull(classifierName);
		}
		

		public MLPipe addOptions(String...additionalOptions) {
			Objects.requireNonNull(additionalOptions);
			for(String newOption : additionalOptions) {
				classifierOptions.add(newOption);
			}
			return this;
		}
		
		public MLPipe addConstructorArgs(Object... args) {
			Objects.requireNonNull(args);
			for(Object newArg : args) {
				this.constructorArgs.add(newArg);
			}
			return this;
		}
		
		public String getName() {
			return classifierName;
		}
		public ArrayList<String> /*ArrayList was explicitly used*/ getOptions(){
			ArrayList<String> options = new ArrayList<>();
			options.addAll(classifierOptions);
			return options;
		}
		public Object[] getArguments() {
			return constructorArgs.toArray();
		}
		
	}

	class WekaAttributeSelectionPipe extends MLPipe {
		private String searcherName, evalName; 
		public static final String classname = "weka.attributeSelection.AttributeSelection";
		protected WekaAttributeSelectionPipe(String host) {
			super(host, classname);
		}
		private Set<String> 	searcherOptions = new TreeSet<>(), 
							evalOptions = new TreeSet<>();
		
		public WekaAttributeSelectionPipe withSearcher(String searcherName) {
			this.searcherName = Objects.requireNonNull(searcherName);
			return this;
		} 
		
		public WekaAttributeSelectionPipe withEval(String evaluator) {
			this.evalName = Objects.requireNonNull(evaluator);
			return this;
		}

		public WekaAttributeSelectionPipe addSearchOptions(String... additionalOptions) {
			addToOptionList(searcherOptions, additionalOptions);
			return this;
		}
		
		public WekaAttributeSelectionPipe addOptions(String... additionalOptions) {
			addToOptionList(evalOptions, additionalOptions);
			return this;
		}
		
		private void addToOptionList(Set<String> optionList, String[] additionalOptions) {
			Objects.requireNonNull(additionalOptions);
			for(String newOption : additionalOptions) {
				optionList.add(newOption);
			}
		}
		
		public String getSearcher() {
			return searcherName;
		}
		public String getEval() {
			return evalName;
		}
		
		public ArrayList<String> getSearcherOptions(){
			ArrayList<String> options = new ArrayList<>();
			options.addAll(searcherOptions);
			return options;
		}
		
		public ArrayList<String> getEvalOptions(){
			ArrayList<String> options = new ArrayList<>();
			options.addAll(evalOptions);
			return options;
		}

		protected boolean isValid() {
			if(isWekaAS() && (searcherName == null || evalName == null)) {
				return false;
			}
			return true;
		}
		
		public boolean isWekaAS() {
			return "weka.attributeSelection.AttributeSelection".equals(getName());
		}

	}
}
