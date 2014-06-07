package com.aegisql.testing_tools.dao.test_demo;

import java.sql.SQLException;

import com.aegisql.access.ColumnAccess;
import com.aegisql.access.TableAccess;
import com.aegisql.dao.jdbc_utils.TableAccessProtection;
import com.aegisql.testing_tools.AccessSchema;
import com.aegisql.testing_tools.AccessPatterns;

public class Account {

	public static void removeProtectionAnyMikeAny(TableAccessProtection pt) throws SQLException {
		pt.removeProtection(AccessPatterns.ANY_MIKE_ANY, "TEST_DEMO", "ACCOUNT");
	}

	public static void removeProtectionAnyNikitaAny(TableAccessProtection pt) throws SQLException {
		pt.removeProtection(AccessPatterns.ANY_NIKITA_ANY, "TEST_DEMO", "ACCOUNT");
	}

	public static void removeProtectionUserNikitaAny(TableAccessProtection pt) throws SQLException {
		pt.removeProtection(AccessPatterns.USER_NIKITA_ANY, "TEST_DEMO", "ACCOUNT");
	}

	public static void removeProtectionUserAnyAny(TableAccessProtection pt) throws SQLException {
		pt.removeProtection(AccessPatterns.USER_ANY_ANY, "TEST_DEMO", "ACCOUNT");
	}

	public static void removeProtectionPowerUserAnyAny(TableAccessProtection pt) throws SQLException {
		pt.removeProtection(AccessPatterns.POWER_USER_ANY_ANY, "TEST_DEMO", "ACCOUNT");
	}

	public static void removeProtectionAdminAnyAny(TableAccessProtection pt) throws SQLException {
		pt.removeProtection(AccessPatterns.ADMIN_ANY_ANY, "TEST_DEMO", "ACCOUNT");
	}

	public static void removeProtectionGuestAnyAny(TableAccessProtection pt) throws SQLException {
		pt.removeProtection(AccessPatterns.GUEST_ANY_ANY, "TEST_DEMO", "ACCOUNT");
	}

	public static void addProtectionAnyMikeAny(TableAccessProtection pt) throws SQLException {
		TableAccess accountAccess       = new TableAccess("ACCOUNT", AccessSchema.INSERT_SELECT_UPDATE_DELETE);
		ColumnAccess accountId          = new ColumnAccess("ID", AccessSchema.SELECT);
		ColumnAccess accountUserName    = new ColumnAccess("USER_NAME", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess accountUserAddress = new ColumnAccess("USER_ADDRESS", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess accountAccesorId   = new ColumnAccess("ACCESSOR_ID", AccessSchema.INSERT_SELECT);
		pt.addProtection(AccessPatterns.ANY_MIKE_ANY, accountAccess, accountId,accountUserName,accountUserAddress,accountAccesorId);
	}

	public static void addProtectionAnyNikitaAny(TableAccessProtection pt) throws SQLException {
		TableAccess accountAccess       = new TableAccess("ACCOUNT", AccessSchema.INSERT_SELECT_UPDATE_DELETE);
		ColumnAccess accountId          = new ColumnAccess("ID", AccessSchema.SELECT);
		ColumnAccess accountUserName    = new ColumnAccess("USER_NAME", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess accountUserAddress = new ColumnAccess("USER_ADDRESS", AccessSchema.INSERT_SELECT_UPDATE);
		pt.addProtection(AccessPatterns.ANY_NIKITA_ANY, accountAccess, accountId,accountUserName,accountUserAddress);
	}

	public static void addProtectionUserNikitaAny(TableAccessProtection pt) throws SQLException {
		TableAccess accountAccess       = new TableAccess("ACCOUNT", AccessSchema.INSERT_SELECT_UPDATE_DELETE);
		ColumnAccess accountId          = new ColumnAccess("ID", AccessSchema.SELECT);
		ColumnAccess accountUserName    = new ColumnAccess("USER_NAME", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess accountUserAddress = new ColumnAccess("USER_ADDRESS", AccessSchema.INSERT_SELECT_UPDATE);
		pt.addProtection(AccessPatterns.USER_NIKITA_ANY, accountAccess, accountId,accountUserName,accountUserAddress);
	}

	public static void addProtectionAdminAnyAny(TableAccessProtection pt) throws SQLException {
		TableAccess accountAccess       = new TableAccess("ACCOUNT", AccessSchema.INSERT_SELECT_UPDATE_DELETE);
		ColumnAccess accountId          = new ColumnAccess("ID", AccessSchema.SELECT);
		ColumnAccess accountUserName    = new ColumnAccess("USER_NAME", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess accountUserAddress = new ColumnAccess("USER_ADDRESS", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess accountAccesorId   = new ColumnAccess("ACCESSOR_ID", AccessSchema.INSERT_SELECT);
		pt.addProtection(AccessPatterns.ADMIN_ANY_ANY, accountAccess, accountId,accountUserName,accountUserAddress,accountAccesorId);
	}

	public static void addProtectionGuestAnyAny(TableAccessProtection pt) throws SQLException {
		TableAccess accountAccess       = new TableAccess("ACCOUNT", AccessSchema.SELECT);
		ColumnAccess accountUserName    = new ColumnAccess("USER_NAME", AccessSchema.SELECT);
		ColumnAccess accountUserAddress = new ColumnAccess("USER_ADDRESS", AccessSchema.SELECT);
		pt.addProtection(AccessPatterns.GUEST_ANY_ANY, accountAccess,accountUserName,accountUserAddress);
	}

	public static void addProtectionUserAnyAny(TableAccessProtection pt) throws SQLException {
		TableAccess accountAccess       = new TableAccess("ACCOUNT", AccessSchema.SELECT_UPDATE);
		ColumnAccess accountId          = new ColumnAccess("ID", AccessSchema.SELECT);
		ColumnAccess accountUserName    = new ColumnAccess("USER_NAME", AccessSchema.SELECT_UPDATE);
		ColumnAccess accountUserAddress = new ColumnAccess("USER_ADDRESS", AccessSchema.SELECT_UPDATE);
		ColumnAccess accountAccesorId   = new ColumnAccess("ACCESSOR_ID", AccessSchema.SELECT);
		pt.addProtection(AccessPatterns.USER_ANY_ANY, accountAccess, accountId,accountUserName,accountUserAddress,accountAccesorId);
	}

	public static void addProtectionPowerUserAnyAny(TableAccessProtection pt) throws SQLException {
		TableAccess accountAccess       = new TableAccess("ACCOUNT", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess accountId          = new ColumnAccess("ID", AccessSchema.SELECT);
		ColumnAccess accountUserName    = new ColumnAccess("USER_NAME", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess accountUserAddress = new ColumnAccess("USER_ADDRESS", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess accountAccesorId   = new ColumnAccess("ACCESSOR_ID", AccessSchema.INSERT_SELECT);
		pt.addProtection(AccessPatterns.POWER_USER_ANY_ANY, accountAccess, accountId,accountUserName,accountUserAddress,accountAccesorId);
	}

	
}
