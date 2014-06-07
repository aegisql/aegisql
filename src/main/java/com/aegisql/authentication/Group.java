package com.aegisql.authentication;

public class Group {
	private final long id;
	private final String groupName;
	private final String useAccessor;
	private final boolean isDefault;
	private final Integer accessorId;
	
	private final int hashCode;

	public Group(long id, String groupName, boolean isDefault, String useAccessor, Integer accessorId) {
		this.id          = id;
		this.groupName   = groupName;
		this.isDefault   = isDefault;
		this.accessorId  = accessorId;
		this.useAccessor = useAccessor;
		this.hashCode    = _hashCode();
	}

	public long getId() {
		return id;
	}

	public String getGroupName() {
		return groupName;
	}

	public Integer getAccessorId() {
		return accessorId;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public boolean isGroupManager() {
		return (useAccessor != null) && (accessorId == null);
	}

	public boolean mustUseAccessor() {
		return useAccessor != null;
	}

	public String getUseAccessor() {
		return useAccessor;
	}

	public int _hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((accessorId == null) ? 0 : accessorId.hashCode());
		result = prime * result
				+ ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result + hashCode;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + (isDefault ? 1231 : 1237);
		result = prime * result
				+ ((useAccessor == null) ? 0 : useAccessor.hashCode());
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
		Group other = (Group) obj;
		if (accessorId == null) {
			if (other.accessorId != null)
				return false;
		} else if (!accessorId.equals(other.accessorId))
			return false;
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equals(other.groupName))
			return false;
		if (hashCode != other.hashCode)
			return false;
		if (id != other.id)
			return false;
		if (isDefault != other.isDefault)
			return false;
		if (useAccessor == null) {
			if (other.useAccessor != null)
				return false;
		} else if (!useAccessor.equals(other.useAccessor))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		return "Group [id=" + id + ", groupName=" + groupName
				+ ", useAccessor=" + useAccessor + ", isDefault=" + isDefault + ", accessorId="
				+ accessorId + ", isGroupManager=" + isGroupManager() + "]";
	}


	
}
