package com.aegisql.profile;

import java.util.ArrayList;
import java.util.List;

public class Profile {
	
	private final List<ProfileEntry> entries = new ArrayList<ProfileEntry>();
	private final long start;
	
	public Profile(String query) {
		this.start = System.nanoTime();
		entries.add(new ProfileEntry("Started with query: "+query));
	}

	public void addEntry(String entry) {
		entries.add(new ProfileEntry(entry));
	}
	
}
