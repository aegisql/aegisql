package com.aegisql.access;

public class AccessPattern {

	private final String groupName;
	private final String userName;
	private final String hostName;
	private final String deviceName;
	private final String accessor;
	private final String accessorId;
	
	private final int hash;
	
	public final static AccessPattern DEFAULT_ACCESSOR = new AccessPattern(null,null,null,null,null,null);
	
	public AccessPattern(String groupName, String userName, String hostName, String deviceName, String accessor, String accessorId) {
		this.groupName  = groupName  == null ? "%" : groupName;
		this.userName   = userName   == null ? "%" : userName;
		this.hostName   = hostName   == null ? "%" : hostName;
		this.deviceName = deviceName == null ? "%" : deviceName;
		this.accessor   = accessor   == null ? "%" : accessor;
		this.accessorId = accessorId;
		hash = _hashCode();
	}

	public String getGroupName() {
		return groupName;
	}

	public String getUserName() {
		return userName;
	}

	public String getHostName() {
		return hostName;
	}

	public String getAccessorField() {
		return accessor;
	}
	
	@Override
	public String toString() {
		return "'" + groupName + "'#'" + userName + "'@'" + hostName + "'@'" + deviceName + "'@'" + accessor + "'@'" + accessorId + "'";
	}
 
	public String whereString() {
		StringBuffer sb = new StringBuffer();
		sb.append("_GROUP_ = '").append(groupName).append("' AND ");
		sb.append("_USER_ = '").append(userName).append("' AND ");
		sb.append("_HOST_ = '").append(hostName).append("' AND ");
		sb.append("_DEVICE_ = '").append(deviceName).append("' AND ");
		sb.append("_ACCESSOR_ = '").append(accessor).append("'");
		return sb.toString();
	}



	@Override
	public int hashCode() {
		return hash;
	}

	public int _hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((accessor == null) ? 0 : accessor.hashCode());
		result = prime * result
				+ ((accessorId == null) ? 0 : accessorId.hashCode());
		result = prime * result
				+ ((deviceName == null) ? 0 : deviceName.hashCode());
		result = prime * result
				+ ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result
				+ ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result
				+ ((userName == null) ? 0 : userName.hashCode());
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
		AccessPattern other = (AccessPattern) obj;
		if (accessor == null) {
			if (other.accessor != null)
				return false;
		} else if (!accessor.equals(other.accessor))
			return false;
		if (accessorId == null) {
			if (other.accessorId != null)
				return false;
		} else if (!accessorId.equals(other.accessorId))
			return false;
		if (deviceName == null) {
			if (other.deviceName != null)
				return false;
		} else if (!deviceName.equals(other.deviceName))
			return false;
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equals(other.groupName))
			return false;
		if (hostName == null) {
			if (other.hostName != null)
				return false;
		} else if (!hostName.equals(other.hostName))
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}

	public String getAccessorId() {
		return accessorId;
	}

	public String getDeviceName() {
		return deviceName;
	}
	
}
