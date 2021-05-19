package gate.termraider.util;

import java.io.Serializable;

public class DocumentIdentifier implements Serializable {
	
	private static final long serialVersionUID = -6093334229254695896L;
	
	private String identifier;
	private int index;
	
	public DocumentIdentifier(String identifier, int index) {
		this.identifier = identifier;
		this.index = index;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public int getIndex() {
		return index;
	}
	
	@Override
	public String toString() {
		return String.format("%s [%d]", identifier, index);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + index;
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
		DocumentIdentifier other = (DocumentIdentifier) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		if (index != other.index)
			return false;
		return true;
	}
}
