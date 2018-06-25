package de.upb.crc901.services.typeserializers;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.upb.crc901.services.core.IOntologySerializer;
import de.upb.crc901.services.core.JASEDataObject;
import de.upb.crc901.services.core.ServiceHandle;
import weka.core.Instances;

public class ServiceHandleOntologySerializer implements IOntologySerializer<ServiceHandle>  {

	private static List<String> supportedTypes = Arrays.asList("ServiceHandle");
	@Override
	public ServiceHandle unserialize(JASEDataObject jdo) {
		return (ServiceHandle) jdo.getData();
	}

	@Override
	public JASEDataObject serialize(ServiceHandle object) {
		return new JASEDataObject("ServiceHandle", object);
	}

	@Override
	public Collection<String> getSupportedSemanticTypes() {
		return supportedTypes;
	}

}
