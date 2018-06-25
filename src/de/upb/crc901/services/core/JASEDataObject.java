package de.upb.crc901.services.core;

import java.io.Serializable;

@SuppressWarnings("serial")
public class JASEDataObject implements Serializable {
	private final String type;
	private final Object object;

	public JASEDataObject(String type, Object object) {
		super();
		this.type = java.util.Objects.requireNonNull(type);
		this.object = java.util.Objects.requireNonNull(object);
	}

	public String getType() {
		return type;
	}
	
	public Object getData() {
		return object;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JASEDataObject other = (JASEDataObject) obj;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	public String toString() {
		return type + ": " + object.toString();
	}
	
	/**
	 * Returns true if the type string of this object equals the given semantic name
	 */
	public boolean isofType(String semanticType) {
		return getType().equals(semanticType);
	}
	

}
