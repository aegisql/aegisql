package com.aegisql.profile;

public class ProfileEntry {

	private final long timestamp;
	private final String entry;
	
	public ProfileEntry(String entry) {
		this.timestamp = System.nanoTime();
		this.entry     = entry;
	}

	public String getEntry() {
		return entry;
	}

	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return "ProfileEntry [" + timestamp + ": " + entry
				+ "]";
	}
	
	
	
}
