package de.upb.crc901.services.core;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

public interface StreamHandler<T> {
	public T read(JsonParser jsonIn) throws IOException;
	public void write(JsonGenerator jsonOut, T data) throws IOException;
	public Class<T> getSupportedSemanticClass();
}
