package com.aegisql.testing_tools.dao.test_demo;

import java.sql.SQLException;

import com.aegisql.access.ColumnAccess;
import com.aegisql.access.TableAccess;
import com.aegisql.dao.jdbc_utils.TableAccessProtection;
import com.aegisql.testing_tools.AccessPatterns;
import com.aegisql.testing_tools.AccessSchema;

public class Orders {
	public static void removeProtectionAnyMikeAny(TableAccessProtection pt) throws SQLException {
		pt.removeProtection(AccessPatterns.ANY_MIKE_ANY, "TEST_DEMO", "ORDERS");
	}

	public static void removeProtectionAnyNikitaAny(TableAccessProtection pt) throws SQLException {
		pt.removeProtection(AccessPatterns.ANY_NIKITA_ANY, "TEST_DEMO", "ORDERS");
	}

	public static void removeProtectionUserNikitaAny(TableAccessProtection pt) throws SQLException {
		pt.removeProtection(AccessPatterns.USER_NIKITA_ANY, "TEST_DEMO", "ORDERS");
	}

	public static void removeProtectionUserAnyAny(TableAccessProtection pt) throws SQLException {
		pt.removeProtection(AccessPatterns.USER_ANY_ANY, "TEST_DEMO", "ORDERS");
	}

	public static void removeProtectionPowerUserAnyAny(TableAccessProtection pt) throws SQLException {
		pt.removeProtection(AccessPatterns.POWER_USER_ANY_ANY, "TEST_DEMO", "ORDERS");
	}

	public static void removeProtectionAdminAnyAny(TableAccessProtection pt) throws SQLException {
		pt.removeProtection(AccessPatterns.ADMIN_ANY_ANY, "TEST_DEMO", "ORDERS");
	}

	public static void removeProtectionGuestAnyAny(TableAccessProtection pt) throws SQLException {
		pt.removeProtection(AccessPatterns.GUEST_ANY_ANY, "TEST_DEMO", "ORDERS");
	}

	public static void addProtectionAnyMikeAny(TableAccessProtection pt) throws SQLException {
		TableAccess ordersAccess       = new TableAccess("ORDERS", AccessSchema.INSERT_SELECT_UPDATE_DELETE);
		ColumnAccess ordersId          = new ColumnAccess("ID", AccessSchema.SELECT);
		ColumnAccess ordersAccountId   = new ColumnAccess("ACCOUNT_ID", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersDescription = new ColumnAccess("DESCRIPTION", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersReceived    = new ColumnAccess("ORDER_RECEIVED", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersProcessed   = new ColumnAccess("ORDER_PROCESSED", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersStatusId    = new ColumnAccess("ORDER_STATUS_ID", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersAccesorId   = new ColumnAccess("ACCESSOR_ID", AccessSchema.INSERT_SELECT);
		pt.addProtection(AccessPatterns.ANY_MIKE_ANY, ordersAccess, ordersId,ordersAccountId,ordersDescription,ordersReceived,ordersProcessed,ordersStatusId,ordersAccesorId);
	}

	public static void addProtectionAnyNikitaAny(TableAccessProtection pt) throws SQLException {
		TableAccess ordersAccess       = new TableAccess("ORDERS", AccessSchema.INSERT_SELECT_UPDATE_DELETE);
		ColumnAccess ordersId          = new ColumnAccess("ID", AccessSchema.SELECT);
		ColumnAccess ordersAccountId    = new ColumnAccess("ACCOUNT_ID", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersDescription = new ColumnAccess("DESCRIPTION", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersReceived    = new ColumnAccess("ORDER_RECEIVED", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersProcessed   = new ColumnAccess("ORDER_PROCESSED", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersStatusId    = new ColumnAccess("ORDER_STATUS_ID", AccessSchema.INSERT_SELECT_UPDATE);
		pt.addProtection(AccessPatterns.ANY_NIKITA_ANY, ordersAccess, ordersId,ordersAccountId,ordersDescription,ordersReceived,ordersProcessed,ordersStatusId);
	}

	public static void addProtectionUserNikitaAny(TableAccessProtection pt) throws SQLException {
		TableAccess ordersAccess       = new TableAccess("ORDERS", AccessSchema.INSERT_SELECT_UPDATE_DELETE);
		ColumnAccess ordersId          = new ColumnAccess("ID", AccessSchema.SELECT);
		ColumnAccess ordersAccountId    = new ColumnAccess("ACCOUNT_ID", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersDescription = new ColumnAccess("DESCRIPTION", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersReceived    = new ColumnAccess("ORDER_RECEIVED", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersProcessed   = new ColumnAccess("ORDER_PROCESSED", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersStatusId    = new ColumnAccess("ORDER_STATUS_ID", AccessSchema.INSERT_SELECT_UPDATE);
		pt.addProtection(AccessPatterns.USER_NIKITA_ANY, ordersAccess, ordersId,ordersAccountId,ordersDescription,ordersReceived,ordersProcessed,ordersStatusId);
	}

	public static void addProtectionAdminAnyAny(TableAccessProtection pt) throws SQLException {
		TableAccess ordersAccess       = new TableAccess("ORDERS", AccessSchema.INSERT_SELECT_UPDATE_DELETE);
		ColumnAccess ordersId          = new ColumnAccess("ID", AccessSchema.SELECT);
		ColumnAccess ordersAccountId    = new ColumnAccess("ACCOUNT_ID", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersDescription = new ColumnAccess("DESCRIPTION", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersReceived    = new ColumnAccess("ORDER_RECEIVED", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersProcessed   = new ColumnAccess("ORDER_PROCESSED", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersStatusId    = new ColumnAccess("ORDER_STATUS_ID", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersAccesorId   = new ColumnAccess("ACCESSOR_ID", AccessSchema.INSERT_SELECT);
		pt.addProtection(AccessPatterns.ADMIN_ANY_ANY, ordersAccess, ordersId,ordersAccountId,ordersDescription,ordersReceived,ordersProcessed,ordersStatusId,ordersAccesorId);
	}

	public static void addProtectionGuestAnyAny(TableAccessProtection pt) throws SQLException {
		TableAccess ordersAccess       = new TableAccess("ORDERS", AccessSchema.SELECT);
		ColumnAccess ordersAccountId    = new ColumnAccess("ACCOUNT_ID", AccessSchema.SELECT);
		ColumnAccess ordersDescription = new ColumnAccess("DESCRIPTION", AccessSchema.SELECT);
		ColumnAccess ordersReceived    = new ColumnAccess("ORDER_RECEIVED", AccessSchema.SELECT);
		ColumnAccess ordersProcessed   = new ColumnAccess("ORDER_PROCESSED", AccessSchema.SELECT);
		ColumnAccess ordersStatusId    = new ColumnAccess("ORDER_STATUS_ID", AccessSchema.SELECT);
		pt.addProtection(AccessPatterns.GUEST_ANY_ANY, ordersAccess,ordersAccountId,ordersDescription,ordersReceived,ordersProcessed,ordersStatusId);
	}

	public static void addProtectionUserAnyAny(TableAccessProtection pt) throws SQLException {
		TableAccess ordersAccess       = new TableAccess("ORDERS", AccessSchema.SELECT_UPDATE);
		ColumnAccess ordersId          = new ColumnAccess("ID", AccessSchema.SELECT);
		ColumnAccess ordersAccountId    = new ColumnAccess("ACCOUNT_ID", AccessSchema.SELECT_UPDATE);
		ColumnAccess ordersDescription = new ColumnAccess("DESCRIPTION", AccessSchema.SELECT_UPDATE);
		ColumnAccess ordersReceived    = new ColumnAccess("ORDER_RECEIVED", AccessSchema.SELECT_UPDATE);
		ColumnAccess ordersProcessed   = new ColumnAccess("ORDER_PROCESSED", AccessSchema.SELECT_UPDATE);
		ColumnAccess ordersStatusId    = new ColumnAccess("ORDER_STATUS_ID", AccessSchema.SELECT_UPDATE);
		ColumnAccess ordersAccesorId   = new ColumnAccess("ACCESSOR_ID", AccessSchema.SELECT);
		pt.addProtection(AccessPatterns.USER_ANY_ANY, ordersAccess, ordersId,ordersAccountId,ordersDescription,ordersReceived,ordersProcessed,ordersStatusId,ordersAccesorId);
	}

	public static void addProtectionPowerUserAnyAny(TableAccessProtection pt) throws SQLException {
		TableAccess ordersAccess       = new TableAccess("ORDERS", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersId          = new ColumnAccess("ID", AccessSchema.SELECT);
		ColumnAccess ordersAccountId    = new ColumnAccess("ACCOUNT_ID", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersDescription = new ColumnAccess("DESCRIPTION", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersReceived    = new ColumnAccess("ORDER_RECEIVED", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersProcessed   = new ColumnAccess("ORDER_PROCESSED", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersStatusId    = new ColumnAccess("ORDER_STATUS_ID", AccessSchema.INSERT_SELECT_UPDATE);
		ColumnAccess ordersAccesorId   = new ColumnAccess("ACCESSOR_ID", AccessSchema.INSERT_SELECT);
		pt.addProtection(AccessPatterns.POWER_USER_ANY_ANY, ordersAccess, ordersId,ordersAccountId,ordersDescription,ordersReceived,ordersProcessed,ordersStatusId,ordersAccesorId);
	}


}
