package de.upb.crc901.services.streamhandlers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.upb.crc901.services.core.ServiceHandle;
import de.upb.crc901.services.core.StreamHandler;

public class ServiceHandleStreamHandler implements StreamHandler<ServiceHandle> {

	@Override
	public ServiceHandle read(JsonParser jsonIn) throws IOException {
		JsonToken t = jsonIn.currentToken();
		String id = null;
		String classpath = null;
		String host = null;
		while(t != JsonToken.END_OBJECT) {
			if(t == JsonToken.FIELD_NAME) {
				String fieldName = jsonIn.getCurrentName();
				
				if("id".equals(fieldName)) {
					jsonIn.nextToken();
					id = jsonIn.getValueAsString();
				}else if("classpath".equals(fieldName)) {
					jsonIn.nextToken();
					classpath = jsonIn.getValueAsString();
				}else if("host".equals(fieldName)) {
					jsonIn.nextToken();
					host = jsonIn.getValueAsString();
				}
			}
			t = jsonIn.nextToken();
		}
		return new ServiceHandle(classpath, id).withExternalHost(host);
		
		
	}

	@Override
	public void write(JsonGenerator jsonOut, ServiceHandle data) throws IOException {
		jsonOut.writeStartObject();
		jsonOut.writeStringField("classpath", data.getClasspath());
		jsonOut.writeStringField("host", data.getHost());
		jsonOut.writeStringField("id", data.getId());
		jsonOut.writeEndObject();
	}

	@Override
	public Class<ServiceHandle> getSupportedSemanticClass() {
		return ServiceHandle.class;
	}

}
