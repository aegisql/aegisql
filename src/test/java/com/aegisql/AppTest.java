package com.aegisql;

import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.aegisql.jdbc42.impl.AegisConnection;
import com.aegisql.testing_tools.ClassUtils;
import com.aegisql.testing_tools.MySQLLocalClient;
/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super("testApp");
    }

   
    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigorous Test :-)
     * @throws ClassNotFoundException 
     */
    public void testApp() throws Exception
    {
    	
		MySQLLocalClient mysql = new MySQLLocalClient("jdbc:mysql://localhost?user=root&password=");
		
		mysql.resetDatabase("aegis_information_schema");
		mysql.resetDatabase("AUTHENTICATION");
		mysql.resetDatabase("TEST_DEMO");
		
		mysql.executeFile(ClassUtils.getAbsolutePath("aegis_information_schema_TEST_DEMO_ACCOUNT.sql"));
		mysql.executeFile(ClassUtils.getAbsolutePath("aegis_information_schema_TEST_DEMO_ORDERS.sql"));
		
		mysql.executeFile(ClassUtils.getAbsolutePath("AUTHENTICATION_USERS.sql"));
		mysql.executeFile(ClassUtils.getAbsolutePath("AUTHENTICATION_GROUPS.sql"));
		mysql.executeFile(ClassUtils.getAbsolutePath("AUTHENTICATION_USER_GROUP_MAP.sql"));

		mysql.executeFile(ClassUtils.getAbsolutePath("TEST_DEMO_ACCOUNT.sql"));
		mysql.executeFile(ClassUtils.getAbsolutePath("TEST_DEMO_ORDERS.sql"));
    	
//    	Connection connect = DriverManager.getConnection("jdbc:aegisql:mysql://localhost/TEST_DEMO?user=mike_db&password=12345");
//    	Connection connect = DriverManager.getConnection("jdbc:aegisql:mysql://localhost/TEST_DEMO?user=mike_db&password=12345&authentication_provider=xml_authentication");
    	Connection connect = DriverManager.getConnection("jdbc:aegisql:mysql://localhost/TEST_DEMO?user=mike_db&password=12345&authentication_provider=default_authentication");
    	AegisConnection ac = (AegisConnection)connect;
    	ac.setAuthenticationSchema("AUTHENTICATION");
    	System.out.println("Connection Class loaded: " + connect.getClass().getName());
    	assertNotNull(connect);
    	Statement statement = connect.createStatement();
    	assertNotNull(statement);
    	statement.executeUpdate("DELETE FROM ACCOUNT SUBMITTED BY 'mike' IDENTIFIED BY '12345'");
    	// Be careful with assigning accessor ID insert rights for generic groups and users
    	// this can be a security issue
    	//statement.executeUpdate("INSERT INTO ACCOUNT(USER_NAME,USER_ADDRESS,ACCESSOR_ID) VALUES('Mike','Ramsey',1)  SUBMITTED BY 'mike' IDENTIFIED BY '12345'");
    	statement.executeUpdate("INSERT INTO ACCOUNT(USER_NAME,USER_ADDRESS) VALUES('Mike','Ramsey')  SUBMITTED BY 'mike' IDENTIFIED BY '12345'");
    	//ResultSet rs = statement.executeQuery("SELECT ID,USER_NAME,USER_ADDRESS FROM ACCOUNT SUBMITTED BY 'mike' IDENTIFIED BY '12345'");
    	//rs.next();
    	//System.out.println("ResultSet: "+rs.getString("USER_NAME")+" from "+rs.getString("USER_ADDRESS"));
    	//assertNotNull(rs);
    }
}
