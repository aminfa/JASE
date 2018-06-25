package de.upb.crc901.services.core;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

/**
 * Base class of wrappers. 
 * Extend this class and put the wrapper's classpath into the classes.json config file.
 * Overwrite or define new Methods for the wrapped class, HttpServiceServer will handle calling the correct methods when invoked.
 * @author aminfaez
 *
 */
public abstract class ServiceWrapper implements Serializable{
	/**
	 * I'm not sure why an abstract class has to specify this value.
	 */
	private static final long serialVersionUID = 1L;
	
	/** The wrapped Object. */
	protected Object delegate;
//	/** The wrapped Object constructor. */
//	public transient final Constructor<? extends Object> delegateConstructor;
//	/** Values fed to constructor when invoking 'buildDelegate()'. */
//	protected transient final Object[] constructorValues;
	
	/** The types corresponding to the parameters of the constructor of the wrapper. */ 
	public static final Class<?>[] CONSTRUCTOR_TYPES = {Constructor.class, JASEDataObject[].class};
	/**
	 * Creates the base wrapper.
	 * @param delegate The wrapped object. Methods which aren't defined are delegated to this object.
	 * @param delegateClassPath The classpath of the wrapped object. To get the specific class of the wrapped instance use: Class.forName(delegateClassPath).
	 */
	public ServiceWrapper(Constructor<? extends Object> delegateConstructor, JASEDataObject[] values) {
		this.buildDelegate(delegateConstructor, values);
		if(this.delegate == null) {
			// buildConstructor didn't assign the delegate field.
			throw new NullPointerException("Delegate wasn't assigned by the 'buildDelegate()' method.");
		}
	}
	
	/**
	 * Returns the inner delegate object.
	 * @return inner delegate
	 */
	public Object getDelegate() {
		return delegate;
	}
	
	/**
	 * Invoked at the end of the constructor to construct the inner delegate using the constructor and the constructorvalues.
	 * Override this method in order to 'override' the constructor of the wrapped service. 
	 * When overriding this method one can change the values inside the constructorValues array and call: super.buildDelegate()
	 * buildDelegate has to set the delegate field or else errors will be thrown down the line.
	 */
	protected void buildDelegate(final Constructor<? extends Object> delegateConstructor, final JASEDataObject[] constructorValues) {
		try {
			// check if the constructor together with the values are not null.
			if(delegateConstructor != null && constructorValues != null) {
				List<?> list = Arrays.asList(constructorValues);
				Object[] parsed = new OntologicalTypeMarshallingSystem().objectArrayFromSemantic(delegateConstructor.getParameterTypes(), (List<JASEDataObject>) list);
				delegate = delegateConstructor.newInstance(parsed);
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// hide the checked exception.
			throw new RuntimeException(e);
		}
	}
	
}
