package com.aegisql.sqltest.mysql;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.aegisql.sqltest.mysql.test_schema_1.TestSchema1Test;

@RunWith(Suite.class)
@SuiteClasses({TestSchema1Test.class})
public class AllMySqlTests {

}
