package com.aegisql.jdbc42;

import com.aegis.submitter.SubmittedBy;

public interface SubmittedStatement {
	public void setSubmitter(SubmittedBy submitter);
	public SubmittedBy getSubmitter();
}
