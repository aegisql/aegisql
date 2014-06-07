package com.aegisql.access;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aegisql.authentication.Group;

public class AccessPatternsGenerator {
	
	public final static Logger log = LoggerFactory.getLogger(AccessPatternsGenerator.class);

	public static Set<AccessPattern> buildAllAccessors( String uName, String dName, String hName, List<Group> groups ){
		Set<AccessPattern> set = new LinkedHashSet<>();

		for( Group group: groups ) {
			String accessorId = null;
			if( group.getUseAccessor() == null || "".equals(group.getUseAccessor())) {
				continue;
			} else {
				accessorId = group.getAccessorId() == null ? null : group.getAccessorId().toString();
			}
			String gName = group.getGroupName();
			String aName = group.getUseAccessor();
			
//			g	u	h	d	a
//			g 	u	h	d	%
//			g	u	h	%	a
//			g	u	h	%	%
//			g	u	%	d	a
//			g	u	%	d	%
//			g	u	%	%	a
//			g	u	%	%	%
//			g	%	h	d	a
//			g 	%	h	d	%
//			g	%	h	%	a
//			g	%	h	%	%
//			g	%	%	d	a
//			g	%	%	d	%
//			g	%	%	%	a
//			g	%	%	%	%			
//			%	u	h	d	a
//			%	u	h	d	%
//			%	u	h	%	a
//			%	u	h	%	%
//			%	u	%	d	a
//			%	u	%	d	%
//			%	u	%	%	a
//			%	u	%	%	%
//			%	%	h	d	a
//			%	%	h	d	%
//			%	%	h	%	a
//			%	%	h	%	%
//			%	%	%	d	a
//			%	%	%	d	%
//			%	%	%	%	a
//			%	%	%	%	%		

			for( String g: new String[]{gName,null}) {
				for( String u: new String[]{uName,null}) {
					for( String h: new String[]{hName,null}) {
						for( String d: new String[]{dName,null}) {
							for( String a: new String[]{aName,null}) {
								AccessPattern accessor = new AccessPattern(g, u, h, d, a, a == null ? null : accessorId );
								set.add(accessor);
							}
						}
					}					
				}				
			}			
		}
		return set;
	}
}
