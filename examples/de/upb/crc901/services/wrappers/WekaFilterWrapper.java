package de.upb.crc901.services.wrappers;

import java.lang.reflect.Constructor;

import de.upb.crc901.services.core.ServiceWrapper;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.SimpleInstancesImpl;
import weka.core.Instances;
import weka.filters.Filter;

public class WekaFilterWrapper extends ServiceWrapper{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WekaFilterWrapper(Constructor<? extends Object> delegateConstructor, Object[] values) {
		super(delegateConstructor, values);
	}
	
	/**
	 * Applies the useFilter function from Filter using the wrapped filter, delegate. 
	 * @param instances Data to be preprocessed.
	 * @return Preprocessed data
	 * @throws Exception throws by useFilter
	 */
	public Instances preprocess(SimpleInstancesImpl instances) throws Exception {
		Instances wekaInstances = WekaUtil.fromJAICoreInstances(instances);
		Instances filteredInstances = Filter.useFilter(wekaInstances, (Filter) super.delegate);
		//jaicore.ml.interfaces.Instances returnVal = WekaUtil.toJAICoreInstances(filteredInstances);
		return filteredInstances;
	}

}
