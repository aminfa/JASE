package de.upb.crc901.services.streamhandlers;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import de.upb.crc901.services.core.StreamHandler;
import jaicore.ml.core.SimpleLabeledInstanceImpl;
import jaicore.ml.core.SimpleLabeledInstancesImpl;
import jaicore.ml.interfaces.Instances;
import jaicore.ml.interfaces.LabeledInstance;
import jaicore.ml.interfaces.LabeledInstances;

/**
 * Streamhandler implementation for the semantic type: LabeledInstances
 * 
 * Example of labeledinstances: {"instances":[[1.0,2.0],[3.0,4.0]],"labels":["A","B"]}
 * 
 * @author aminfaez
 *
 */
public class LabeledInstancesStreamHandler implements StreamHandler<LabeledInstances<String>> {

	private InstancesStreamHandler delegateInstancesStreamHandler = new InstancesStreamHandler();
	private StringListStreamHandler delegateStringListStreamHandler = new StringListStreamHandler();
	
	@Override
	public Class<LabeledInstances<String>> getSupportedSemanticClass() {
		Class<?> labeledInstacesClass = LabeledInstances.class;
		return (Class<LabeledInstances<String>>) labeledInstacesClass;
	}

	@Override
	public void write(JsonGenerator jsonOut, LabeledInstances<String> data) throws IOException {
		jsonOut.writeStartObject();
		jsonOut.writeFieldName("instances");
		delegateInstancesStreamHandler.writeList(jsonOut, data);
		jsonOut.writeFieldName("labels");
		jsonOut.writeStartArray();
		for(LabeledInstance<String> labeledInstance : data) {
			jsonOut.writeString(labeledInstance.getLabel());
		}
		jsonOut.writeEndArray();
		jsonOut.writeEndObject();
	}

	@Override
	public LabeledInstances<String> read(JsonParser jsonIn) throws IOException {
		LabeledInstances<String> labeledInstances = new SimpleLabeledInstancesImpl();
		Instances instances = null;
		List<String> labels = null;
		while (jsonIn.nextToken() != JsonToken.END_OBJECT) { 
		    String fieldname = jsonIn.getCurrentName();
		    if ("instances".equals(fieldname)) {
		        jsonIn.nextToken();
		        instances = delegateInstancesStreamHandler.read(jsonIn);
		    }
		    if ("labels".equals(fieldname)) {
		    		jsonIn.nextToken();
		    		labels = delegateStringListStreamHandler.read(jsonIn);
		    }
		}
		for(int i = 0; i <  labels.size(); i ++) {
			LabeledInstance<String> instance = new SimpleLabeledInstanceImpl();
			instance.setLabel(labels.get(i));
			instance.addAll(instances.get(i));
			labeledInstances.add(instance);
		}
		return labeledInstances;
	}

}
