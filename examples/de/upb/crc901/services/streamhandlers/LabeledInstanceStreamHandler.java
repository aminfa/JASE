package de.upb.crc901.services.streamhandlers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import de.upb.crc901.services.core.StreamHandler;
import jaicore.ml.core.SimpleLabeledInstanceImpl;
import jaicore.ml.interfaces.LabeledInstance;

/**
 * Streamhandler implementation for the semantic type: LabeledInstance
 * 
 * Example of labeledinstance: {"attributes":[1.0,2.0],"label":"A"}
 * 
 * @author aminfaez
 *
 */
public class LabeledInstanceStreamHandler implements StreamHandler<LabeledInstance<String>> {
	InstanceStreamHandler delegateStreamHandler = new InstanceStreamHandler();
	@Override
	public LabeledInstance<String> read(JsonParser jsonIn) throws IOException {
		LabeledInstance<String> labeledInstance = new SimpleLabeledInstanceImpl();
		while (jsonIn.nextToken() != JsonToken.END_OBJECT) { 
		    String fieldname = jsonIn.getCurrentName();
		    if ("attributes".equals(fieldname)) {
		        jsonIn.nextToken();
				delegateStreamHandler.readInto(jsonIn, labeledInstance);
		    }
		    if ("label".equals(fieldname)) {
		    		jsonIn.nextToken();
		        String label = jsonIn.getText();
		        labeledInstance.setLabel(label);
		    }
		}
		return labeledInstance;
	}

	@Override
	public void write(JsonGenerator jsonOut, LabeledInstance<String> data) throws IOException {
		jsonOut.writeStartObject();
		jsonOut.writeFieldName("attributes");
		delegateStreamHandler.write(jsonOut, data);
		jsonOut.writeStringField("label", data.getLabel());
		jsonOut.writeEndObject();
	}

	@Override
	public Class<LabeledInstance<String>> getSupportedSemanticClass() {
		Class<?> labeledInstanceClass = LabeledInstance.class;
		return (Class<LabeledInstance<String>>) labeledInstanceClass;
	}

}
