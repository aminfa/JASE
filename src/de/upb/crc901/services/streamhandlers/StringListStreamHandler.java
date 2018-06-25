package de.upb.crc901.services.streamhandlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import de.upb.crc901.services.core.StreamHandler;

public class StringListStreamHandler implements StreamHandler<List<String>> {

	@Override
	public Class<List<String>> getSupportedSemanticClass() {
		Class<?> listClass = List.class;
		return (Class<List<String>>) listClass;
	}

	@Override
	public List<String> read(JsonParser jsonIn) throws IOException {
		List<String> stringList = new ArrayList<>();
		// next string is start array
		assert jsonIn.currentToken() == JsonToken.START_ARRAY;
		while(jsonIn.nextToken() != JsonToken.END_ARRAY) {
			if(jsonIn.currentToken() == JsonToken.VALUE_STRING) {
				stringList.add(jsonIn.getValueAsString());
			}
		}
		return stringList;
	}

	@Override
	public void write(JsonGenerator jsonOut, List<String> data) throws IOException {
		jsonOut.writeStartArray();
		for(String value : data) {
			jsonOut.writeString(value);
		}
		jsonOut.writeEndArray();
	}

}
