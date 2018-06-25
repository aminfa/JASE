/**
 * ServiceCompositionResult.java
 * Copyright (C) 2017 Paderborn University, Germany
 * 
 * @author: Felix Mohr (mail@felixmohr.de)
 */

/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.upb.crc901.services.core;

import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;

@SuppressWarnings("serial")
public class ServiceCompositionResult extends HashMap<String, JASEDataObject> {

	public void addBody(HttpBody returnedBody) {
		for(String field : returnedBody.getState().currentFieldNames()) {
			JASEDataObject jdo = returnedBody.getState().retrieveField(field);
			super.put(field, jdo);
		}
	}
//	/**
//	 * Rewrite the 'host' attribute for all servicehandlers whose host was "local".
//	 * 
//	 */
//	private JASEDataObject rewriteHosts(JASEDataObject jdo , String host) {
//		if(jdo.getData() instanceof ServiceHandle && !((ServiceHandle)jdo.getData()).isRemote()) {
//			// rewrite host attribute
//			ServiceHandle translatedHandler = ((ServiceHandle)jdo.getData()).withExternalHost(host);
//			jdo = new JASEDataObject(jdo.getType(), translatedHandler);
//			return jdo;
//		}
//		else {
//			return jdo;
//		}
//	}
	

}
