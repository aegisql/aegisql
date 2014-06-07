package com.aegisql.access;

public class TableAccesorID {

	private String schema;
	private String table;
	private String alias;
	private String accessorField;
	private String accessorId;
	
	public TableAccesorID() {

	}

	public TableAccesorID(String schema, String table, String alias, String accessorField, String accessorId) {
		this.schema        = schema;
		this.table         = table;
		this.alias         = alias;
		this.accessorField = accessorField;
		this.accessorId    = accessorId;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getAccessorField() {
		return accessorField;
	}

	public void setAccessorField(String accessorField) {
		this.accessorField = accessorField;
	}

	public String getAccessorId() {
		return accessorId;
	}

	public void setAccessorId(String accessorId) {
		this.accessorId = accessorId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((accessorField == null) ? 0 : accessorField.hashCode());
		result = prime * result
				+ ((accessorId == null) ? 0 : accessorId.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
		result = prime * result + ((table == null) ? 0 : table.hashCode());
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
		TableAccesorID other = (TableAccesorID) obj;
		if (accessorField == null) {
			if (other.accessorField != null)
				return false;
		} else if (!accessorField.equals(other.accessorField))
			return false;
		if (accessorId == null) {
			if (other.accessorId != null)
				return false;
		} else if (!accessorId.equals(other.accessorId))
			return false;
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		if (table == null) {
			if (other.table != null)
				return false;
		} else if (!table.equals(other.table))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TableAccesorID `" + schema + "`.`" + table
				+ "` `" + accessorField + "` = '"
				+ accessorId + "'";
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	
}
