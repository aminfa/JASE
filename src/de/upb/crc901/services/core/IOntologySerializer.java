/**
 * HttpServiceServer.java
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

import java.util.Collection;

public interface IOntologySerializer<T> {
	public T unserialize(final JASEDataObject jdo);
	public JASEDataObject serialize(final T object);
	public Collection<String> getSupportedSemanticTypes();
	public default RuntimeException typeMismatch(JASEDataObject jdo) {
		return new RuntimeException("The type: " + jdo.getData().getClass() + " of the given instance doesn't match any of the supported types: " + getSupportedSemanticTypes());
	}
}
