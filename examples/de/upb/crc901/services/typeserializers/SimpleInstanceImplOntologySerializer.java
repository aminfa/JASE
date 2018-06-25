package de.upb.crc901.services.typeserializers;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.upb.crc901.services.core.IOntologySerializer;
import de.upb.crc901.services.core.JASEDataObject;
import jaicore.ml.interfaces.Instance;

public class SimpleInstanceImplOntologySerializer implements IOntologySerializer<Instance>  {
	private static final List<String> supportedTypes = Arrays.asList(new String[] {"Instance"});
	
	public  Instance unserialize(final JASEDataObject jdo) {
		if(jdo.getData() instanceof Instance) {
			return (Instance) jdo.getData();
		}
		else {
			throw typeMismatch(jdo);
		}
	}

	@Override
	public JASEDataObject serialize(Instance object) {
		return new JASEDataObject("Instance", object);
	}

	@Override
	public Collection<String> getSupportedSemanticTypes() {
		return supportedTypes;
	}

}
