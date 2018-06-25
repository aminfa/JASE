package de.upb.crc901.services.streamhandlers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import de.upb.crc901.services.core.StreamHandler;
import jaicore.ml.core.SimpleInstanceImpl;
import jaicore.ml.interfaces.Instance;

/**
 * Streamhandler implementation for the semantic type: Instance
 * 
 * Example of instance: [1.0,2.0,3.0]
 * 
 * Parses example of instance: {"0":1.0,"2":2.0,"3":3.0}
 * 
 * @author aminfaez
 *
 */
public class InstanceStreamHandler implements StreamHandler<Instance> {
	
	
	@Override
	public Instance read(JsonParser jsonIn) throws IOException {
		Instance instance = new SimpleInstanceImpl();
		readInto(jsonIn, instance);
		
		
		return instance;
	}
	public void readInto(JsonParser jsonIn, Instance instance) throws IOException{
		while(jsonIn.getCurrentToken() != JsonToken.START_OBJECT && jsonIn.getCurrentToken() != JsonToken.START_ARRAY) {
			jsonIn.nextToken();
		}
		if(jsonIn.getCurrentToken() == JsonToken.START_OBJECT) {
			read_sparse(jsonIn, instance);
		} else {
			read_nonesprase(jsonIn, instance);
		}
	}
	
	public void read_sparse(JsonParser jsonIn, Instance instance) throws IOException {
		assert (jsonIn.currentToken() == JsonToken.START_OBJECT);
		int lastIndex = -1; // points towards last index that was added to instance
		while(jsonIn.nextToken() != JsonToken.END_OBJECT) {
			if(jsonIn.currentToken() == JsonToken.FIELD_NAME) {
			    String fieldname = jsonIn.getCurrentName();
			    int currentIndex = Integer.parseInt(fieldname); // current index is set to the given value
			    jsonIn.nextToken();
			    Double value = jsonIn.getValueAsDouble(); 
			    while(lastIndex < currentIndex) {
			    		instance.add(0D); // fill zeros until the current index is reached
			    		lastIndex++;
			    }
			    instance.set(currentIndex, value); // set current index.
			}
		}
	}
	
	/**
	 * Overloaded read method to be used with LabeledInstance instead of only with instance.
	 */
	public void read_nonesprase(JsonParser jsonIn, Instance instance) throws IOException {
		assert (jsonIn.currentToken() == JsonToken.START_ARRAY);
		
		while(jsonIn.nextToken() != JsonToken.END_ARRAY) {
			JsonToken token = jsonIn.currentToken();
			if(token.isNumeric()) {
				instance.add(jsonIn.getNumberValue().doubleValue());
			}else if("NaN".equals(jsonIn.getValueAsString())){
				instance.add(Double.NaN);
			}
			else {
				throw new IOException("Type mismatch: " + token.asString() + " isn't numeric.");
			}
		}
	}

	public void write(JsonGenerator jsonOut, Instance data) throws IOException {
		write_sparse(jsonOut, data);
	}

	public void write_sparse(JsonGenerator jsonOut, Instance data) throws IOException {
		jsonOut.writeStartObject();
		for(int index = 0, size = data.getNumberOfColumns(); index < size; index++) {
			double value = data.get(index);
			
			if(value != 0D || index == size - 1) { 
				// if it isnt 0 write it's index and value as a sparse format.
				// always write last value to keep the bound at the other side
				jsonOut.writeNumberField(index+"", value);
			}
		}
		jsonOut.writeEndObject();
	}

	public void write_nonesparse(JsonGenerator jsonOut, Instance data) throws IOException {
		Instance instance = (Instance) data;
		jsonOut.writeStartArray();
		for(Double value : instance) {
			if(Double.NaN == value) {
				jsonOut.writeString("NaN");
			} else {
				jsonOut.writeNumber(value);
			}
		}
		jsonOut.writeEndArray();
	}

	@Override
	public Class<Instance> getSupportedSemanticClass() {
		return Instance.class;
	}


}
