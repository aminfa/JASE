package de.upb.crc901.services.wrappers;

import java.lang.reflect.Constructor;
import java.util.List;

import de.upb.crc901.services.core.JASEDataObject;
import de.upb.crc901.services.core.ServiceWrapper;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class WekaAttributeSelectionWrapper extends ServiceWrapper {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	private Attribute cachedClassAttribute;
	private Instances cachedInstances;
	
	
	
	
	public WekaAttributeSelectionWrapper(Constructor<? extends Object> delegateConstructor, JASEDataObject[] values) {
		super(delegateConstructor, values);
	}
	
	public void train(Instances instances) throws Exception {
		this.SelectAttributes(instances);
	}
	
	public Instances preprocess(Instances instances) throws Exception {
		this.SelectAttributes(instances);
		return this.reduceDimensionality(instances);
	}
	
	public void SelectAttributes(Instances instances) throws Exception {
		if(cachedClassAttribute==null) {
			cachedClassAttribute = instances.classAttribute();
			cachedInstances = new Instances(instances, 0);
		}
		((AttributeSelection)delegate).SelectAttributes(instances);
	}
	
	public Instance reduceDimensionality(Instance instance) throws Exception {
		if(cachedInstances==null) {
			throw new RuntimeException("First call SelectAttribute");
		}
		String label = instance.dataset().classAttribute().value(0);
		instance.setDataset(cachedInstances);
		instance.setClassValue(label);
		return ((AttributeSelection)delegate).reduceDimensionality(instance);
	}
	
	public Instances reduceDimensionality(Instances instances) throws Exception {
		if(cachedInstances==null) {
			throw new RuntimeException("First call SelectAttribute");
		}
		for(Instance instance : instances) {
			instance.setDataset(cachedInstances);
		}
		return ((AttributeSelection)delegate).reduceDimensionality(instances);
	}
	
	@Override
	protected void buildDelegate(final Constructor<? extends Object> delegateConstructor, final JASEDataObject[] constructorValues) {
		if(constructorValues.length < 2) {
			throw new RuntimeException("Given length is: " +  constructorValues.length);
		}
		super.buildDelegate(delegateConstructor, constructorValues);
		AttributeSelection selection = (AttributeSelection) getDelegate();
		int parameterIndex = 0;
		
		String asSearcherClasspath = constructorValues[parameterIndex].getData().toString();
		parameterIndex ++;
		// now the 2nd value might be options for the searcher:
		String[] searcherOptions;
		if(constructorValues[parameterIndex].isofType("StringList")){
			@SuppressWarnings("unchecked")
			List<String> optionList = (List<String>) constructorValues[parameterIndex].getData();
			searcherOptions = optionList.toArray(new String[optionList.size()]);
			parameterIndex++;
		}
		else {
			searcherOptions = new String[0];
		}
		// the next arguemtn is the classname of the evaluator
		String asEvaluatorClasspath = constructorValues[parameterIndex].getData().toString(); 
		parameterIndex++;
		
		// the one after, if specified, is the options of evaluator
		String[] evalOptions;
		if(constructorValues.length>parameterIndex &&  constructorValues[parameterIndex].isofType("StringList")) {
			List<String> optionList = (List<String>)  constructorValues[parameterIndex].getData();
			evalOptions = optionList.toArray(new String[optionList.size()]);
		}
		else {
			evalOptions = new String[0];
		}
		
		// now set the search and eval classes
		try {
			ASSearch asSearch = ASSearch.forName(asSearcherClasspath, searcherOptions);
			selection.setSearch(asSearch);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		try {
			ASEvaluation asEvaluator = ASEvaluation.forName(asEvaluatorClasspath, evalOptions);
			selection.setEvaluator(asEvaluator);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
