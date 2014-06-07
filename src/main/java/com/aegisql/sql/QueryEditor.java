package com.aegisql.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.update.Update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aegis.submitter.SubmittedBy;
import com.aegisql.access.TableAccesorID;
import com.aegisql.access.TableQueryType;

/**
 * Find all used tables and columns within an select statement.
 */
public class QueryEditor implements SelectVisitor, FromItemVisitor,	ExpressionVisitor, ItemsListVisitor {

	private final static Logger log = LoggerFactory.getLogger(QueryEditor.class.getName());

	private Map<String, List<String>> replaceColumns;

	private Map<String, String> tableAliase = new HashMap<String, String>();
	private String currentTable;
	private List<String> tables;
	/**
	 * There are special names, that are not table names but are parsed as
	 * tables. These names are collected here and are not included in the tables
	 * - names anymore.
	 */
	private List<String> otherItemNames;
	private final TableQueryType mainQueryType;
	private TableQueryType currentQueryType;

	public int selectReplaceStar(Select select,
			Map<String, List<String>> replaceColumns) {
		this.replaceColumns = replaceColumns;
		init();
		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) {
				withItem.accept(this);
			}
		}
		select.getSelectBody().accept(this);
		PlainSelect plain = (PlainSelect) select.getSelectBody();
		return processPlainSelectColumns(plain);

	}

	public QueryEditor( TableQueryType mainQueryType ) {
		this.mainQueryType = mainQueryType;
	}
	
	private TableAccesorID tableAccessId;
	private Statement statement;
	
	public void whereAddAccessor( Statement statement, TableAccesorID id, TableQueryType queryType ) {
		tableAccessId         = id;
		this.statement        = statement;
		this.currentQueryType = queryType;
		init();
		if(statement instanceof Select ) {
			whereAddAccessor((Select)statement, id.getSchema(), id.getTable(), id.getAlias(), id.getAccessorField(), id.getAccessorId(), queryType);
		} else if(statement instanceof Update ) {
			whereAddAccessor((Update)statement, id.getSchema(), id.getTable(), id.getAlias(), id.getAccessorField(), id.getAccessorId(),queryType);				
		} else  if(statement instanceof Insert ) {
			whereAddAccessor((Insert)statement, id.getSchema(), id.getTable(), id.getAlias(), id.getAccessorField(), id.getAccessorId(), queryType);				
		} else  if(statement instanceof Replace ) {
			whereAddAccessor((Replace)statement, id.getSchema(), id.getTable(), id.getAlias(), id.getAccessorField(), id.getAccessorId(), queryType);				
		} else  if(statement instanceof Delete ) {
			whereAddAccessor((Delete)statement, id.getSchema(), id.getTable(), id.getAlias(), id.getAccessorField(), id.getAccessorId(), queryType);				
		} else {
			throw new RuntimeException("Unimplemented edit support for statement "+statement);
		}
		
	}
			
	public void whereAddAccessor(Select select, String schema, String tableName, String alias, String accessor, String accesorId, TableQueryType queryType) {
		PlainSelect plain = (PlainSelect) select.getSelectBody();
		whereAddAccessor(plain, schema, tableName, alias, accessor, accesorId);
	}

	public void whereAddAccessor(PlainSelect select, String schema, String tableName, String alias, String accessor, String accesorId) {
		EqualsTo eql = buildAccesorEqualsExpression(schema, tableName, alias, accessor, accesorId);

		if (select.getWhere() != null) {
			Expression where = select.getWhere();
			if( ! where.toString().contains(eql.toString())) {
				AndExpression andExp = new AndExpression(where, eql);
				select.setWhere(andExp);
			}
		} else {
			select.setWhere(eql);
		}
	}

	public void whereAddAccessor(Update update, String schema, String tableName, String alias, String accessor, String accesorId, TableQueryType queryType) {
		EqualsTo eql = buildAccesorEqualsExpression(schema, tableName, alias, accessor, accesorId);

		if (update.getWhere() != null) {
			Expression where = update.getWhere();
			if( ! where.toString().contains(eql.toString())) {
				AndExpression andExp = new AndExpression(where, eql);
				update.setWhere(andExp);
			}
		} else {
			update.setWhere(eql);
		}
	}

	public void whereAddAccessor(Delete delete, String schema, String tableName, String alias, String accessor, String accesorId, TableQueryType queryType) {
		EqualsTo eql = buildAccesorEqualsExpression(schema, tableName, alias, accessor, accesorId);
		if (delete.getWhere() != null) {
			Expression where = delete.getWhere();
			if( ! where.toString().contains(eql.toString())) {
				AndExpression andExp = new AndExpression(where, eql);
				delete.setWhere(andExp);
			}
		} else {
			delete.setWhere(eql);
		}
		buildTableList(delete);
		delete.getWhere().accept(this);
	}
	
	private void buildTableList(Delete delete) {
		init();
		String tableName = delete.getTable().getWholeTableName();
		currentTable = tableName;
		tables.add(delete.getTable().getName());
	}
	
	public void whereAddAccessor(Insert insert, String schema, String tableName, String alias, String accessor, String accesorId, TableQueryType queryType) {
		Column accessorCol = new Column();
		Table table = new Table();
		LongValue value = new LongValue("" + accesorId);
		accessorCol.setColumnName(accessor);
		table.setSchemaName(schema);
		table.setName(tableName);
		accessorCol.setTable(table);
		
		if(TableQueryType.INSERT == queryType) {
			insert.getColumns().add(accessorCol);
			if(insert.getExpressions().size() > 0 ) {
				StringValue expression = new StringValue("''");
				expression.setValue(tableAccessId.getAccessorId());
				insert.getExpressions().add(expression);
			}
		}
		if (insert.getItemsList() != null) {
			ItemsList il = insert.getItemsList();
			if (il instanceof ExpressionList) {
				ExpressionList el = (ExpressionList) insert.getItemsList();
				if( ! el.getExpressions().contains(value)) { // not sure  it works. add tests
					el.getExpressions().add(value);
				}
			} else if (il instanceof MultiExpressionList) {
				MultiExpressionList mel = (MultiExpressionList) insert.getItemsList();
				for( ExpressionList el: mel.getExprList()) {
					if( ! el.getExpressions().contains(value)) { // not sure  it works. add tests
						el.getExpressions().add(value);
					}
				}
			} else if (il instanceof SubSelect) {
				il.accept(this);
			}
		}
		//The last processed select will be the one following insert.
		//Think about possible exceptions of this rule
		if( lastSelect != null ) {
			if(TableQueryType.INSERT == mainQueryType && TableQueryType.INSERT == currentQueryType ) {
				SelectExpressionItem item = new SelectExpressionItem();
				StringValue expression = new StringValue("''");
				expression.setValue(tableAccessId.getAccessorId());
				item.setExpression(expression);
				item.setAlias(tableAccessId.getAccessorField());
				lastSelect.getSelectItems().add( item );
			}
		}
		
	}

	public void whereAddAccessor(Replace replace, String schema, String tableName, String alias, String accessor, String accesorId, TableQueryType queryType) {
		Column accessorCol = new Column();
		Table table = new Table();
		LongValue value = new LongValue("" + accesorId);
		accessorCol.setColumnName(accessor);
		table.setSchemaName(schema);
		table.setName(tableName);
		accessorCol.setTable(table);
		
		if(TableQueryType.REPLACE == queryType) {
			replace.getColumns().add(accessorCol);
			if(replace.getExpressions().size() > 0 ) {
				StringValue expression = new StringValue("''");
				expression.setValue(tableAccessId.getAccessorId());
				replace.getExpressions().add(expression);
			}
		}
		if (replace.getItemsList() != null) {
			ItemsList il = replace.getItemsList();
			if (il instanceof ExpressionList) {
				ExpressionList el = (ExpressionList) replace.getItemsList();
				if( ! el.getExpressions().contains(value)) { // not sure  it works. add tests
					el.getExpressions().add(value);
				}
			} else if (il instanceof MultiExpressionList) {
				MultiExpressionList mel = (MultiExpressionList) replace.getItemsList();
				for( ExpressionList el: mel.getExprList()) {
					if( ! el.getExpressions().contains(value)) { // not sure  it works. add tests
						el.getExpressions().add(value);
					}
				}
			} else if (il instanceof SubSelect) {
				log.debug("==== QE: Found Item List {}",il);
				il.accept(this);
			}
		}
		//The last processed select will be the one following insert.
		//Think about possible exceptions of this rule
		if( lastSelect != null ) {
			if(TableQueryType.REPLACE == mainQueryType && TableQueryType.REPLACE == currentQueryType ) {
				SelectExpressionItem item = new SelectExpressionItem();
				StringValue expression = new StringValue("''");
				expression.setValue(tableAccessId.getAccessorId());
				item.setExpression(expression);
				item.setAlias(tableAccessId.getAccessorField());
				lastSelect.getSelectItems().add( item );
			}
		}
		
	}

	private EqualsTo buildAccesorEqualsExpression(String schema,
			String tableName, String alias, String accessor, String accesorId) {
		EqualsTo eql = new EqualsTo();
		Column left = new Column();
		Table table = new Table();
		LongValue right = new LongValue(accesorId);

		left.setColumnName(accessor);
		if( alias==null) {
			table.setSchemaName(schema);
			table.setName(tableName);
		} else {
			table.setName(alias);
		}
		left.setTable(table);
		eql.setLeftExpression(left);
		eql.setRightExpression(right);

		return eql;
	}

	public List<String> getTableList(Replace replace) {
		init();
		tables.add(replace.getTable().getName());
		if (replace.getExpressions() != null) {
			for (Expression expression : replace.getExpressions()) {
				expression.accept(this);
			}
		}
		if (replace.getItemsList() != null) {
			replace.getItemsList().accept(this);
		}

		return tables;
	}

	PlainSelect lastSelect = null;
	
	private int processPlainSelectColumns(PlainSelect plain) {
		lastSelect = plain;
		String tableName = ((Table) plain.getFromItem()).getWholeTableName();
		log.debug("QE: Select Item {} {} {}" ,tableName, mainQueryType,currentQueryType);
		
		if( ! (statement instanceof Select) && tableAccessId != null) {
			whereAddAccessor(plain, null, tableName, tableAliase.get(tableName), tableAccessId.getAccessorField(), tableAccessId.getAccessorId());
		}
		
		List<String> columns;
		List<SelectItem> fixedList = new ArrayList<SelectItem>();
		
		/*
		 * whereAddAccessor(PlainSelect select, String schema, String tableName, String alias, String accessor, String accesorId)
		 * */
		
		for (SelectItem i : plain.getSelectItems()) {
			log.debug("QE: Select Item {} {}" ,i,i.getClass());
			if (i instanceof SelectExpressionItem) {
				fixedList.add(i);
				continue;
			} else if (i instanceof AllTableColumns) { // a.*,b.*
				AllTableColumns ai = (AllTableColumns) i;
				String atName = ai.getTable().getWholeTableName();
				log.debug("AllTableColumns table alias " + atName);
				if (replaceColumns.containsKey(atName)) {
					columns = replaceColumns.get(atName);
					fixedList.addAll(SelectItemsFactory.getSelectItemsList(
							atName, columns));
				} else if (tableAliase.containsKey(atName)) {
					String alias = tableAliase.get(atName);
					columns = replaceColumns.get(alias);
					fixedList.addAll(SelectItemsFactory.getSelectItemsList(
							alias, columns));
				}
			} else if (i instanceof AllColumns) { // *
				if (replaceColumns.containsKey(tableName)) {
					columns = replaceColumns.get(tableName);
					fixedList.addAll(SelectItemsFactory.getSelectItemsList(
							tableName, columns));
				} else if (tableAliase.containsKey(tableName)) {
					String alias = tableAliase.get(tableName);
					columns = replaceColumns.get(alias);
					fixedList.addAll(SelectItemsFactory.getSelectItemsList(
							alias, columns));
				}
				List<Join> joins = plain.getJoins();
				if (joins != null)
					for (Join j : joins) {
						Table t = (Table) j.getRightItem();
						String tName = t.getWholeTableName();
						if (replaceColumns.containsKey(tName)) {
							columns = replaceColumns.get(tName);
							fixedList.addAll(SelectItemsFactory
									.getSelectItemsList(tName, columns));
						} else if (tableAliase.containsKey(tName)) {
							String alias = tableAliase.get(tName);
							columns = replaceColumns.get(alias);
							fixedList.addAll(SelectItemsFactory
									.getSelectItemsList(alias, columns));
						}
					}

			}
		}
		plain.setSelectItems(fixedList);
		return fixedList.size();
	}

	@Override
	public void visit(WithItem withItem) {
		otherItemNames.add(withItem.getName().toLowerCase());
		withItem.getSelectBody().accept(this);
	}

	@Override
	public void visit(PlainSelect plainSelect) {
		log.debug("Plain Select " + plainSelect);

		plainSelect.getFromItem().accept(this);

		if (plainSelect.getJoins() != null) {
			for (Join join : plainSelect.getJoins()) {
				join.getRightItem().accept(this);
			}
		}
		if (plainSelect.getWhere() != null) {
			Expression where = plainSelect.getWhere();
			log.debug("Where: " + plainSelect.getWhere() + " type:"
					+ plainSelect.getWhere().getClass());
			where.accept(this);
		} else {
		}

	}

	@Override
	public void visit(Table tableName) {
		currentTable = tableName.getWholeTableName();
		String tableWholeName = tableName.getWholeTableName();
		String alias = tableName.getAlias();
		log.debug("table: {} {}", tableWholeName, alias);
		if (alias != null && !tableAliase.containsKey(alias)
				&& tableWholeName != null) {
			tableAliase.put(alias, tableWholeName);
		}

		if (!otherItemNames.contains(tableWholeName.toLowerCase())
				&& !tables.contains(tableWholeName)) {
			tables.add(tableWholeName);
		}
	}

	@Override
	public void visit(SubSelect subSelect) {
		PlainSelect plain = (PlainSelect) subSelect.getSelectBody();
		String current = currentTable;
		subSelect.getSelectBody().accept(this);
		currentTable = current;
		processPlainSelectColumns(plain);
	}

	@Override
	public void visit(Addition addition) {
		visitBinaryExpression(addition);
	}

	@Override
	public void visit(AndExpression andExpression) {
		visitBinaryExpression(andExpression);
	}

	@Override
	public void visit(Between between) {
		between.getLeftExpression().accept(this);
		between.getBetweenExpressionStart().accept(this);
		between.getBetweenExpressionEnd().accept(this);
	}

	@Override
	public void visit(Column tableColumn) {
		String tableName = tableColumn.getTable().getName();
		if (tableName == null) {
			tableName = currentTable;
			log.debug("Column {} Internal Table is NULL, use current: {}",
					tableColumn.getWholeColumnName(), tableName);
		} else {
			log.debug("Column {} Internal Table: {}",
					tableColumn.getWholeColumnName(), tableName);

		}
		if (tableAliase.containsKey(tableName)) {
			tableName = tableAliase.get(tableName);
			log.debug("Column Table Alias " + tableName);
		}
	}

	@Override
	public void visit(Division division) {
		visitBinaryExpression(division);
	}

	@Override
	public void visit(DoubleValue doubleValue) {
	}

	@Override
	public void visit(EqualsTo equalsTo) {
		visitBinaryExpression(equalsTo);
	}

	@Override
	public void visit(Function function) {
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		visitBinaryExpression(greaterThan);
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		visitBinaryExpression(greaterThanEquals);
	}

	@Override
	public void visit(InExpression inExpression) {
		inExpression.getLeftExpression().accept(this);
		inExpression.getRightItemsList().accept(this);
	}

	@Override
	public void visit(InverseExpression inverseExpression) {
		inverseExpression.getExpression().accept(this);
	}

	@Override
	public void visit(IsNullExpression isNullExpression) {
	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
	}

	@Override
	public void visit(LikeExpression likeExpression) {
		visitBinaryExpression(likeExpression);
	}

	@Override
	public void visit(ExistsExpression existsExpression) {
		existsExpression.getRightExpression().accept(this);
	}

	@Override
	public void visit(LongValue longValue) {
	}

	@Override
	public void visit(MinorThan minorThan) {
		visitBinaryExpression(minorThan);
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		visitBinaryExpression(minorThanEquals);
	}

	@Override
	public void visit(Multiplication multiplication) {
		visitBinaryExpression(multiplication);
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		visitBinaryExpression(notEqualsTo);
	}

	@Override
	public void visit(NullValue nullValue) {
	}

	@Override
	public void visit(OrExpression orExpression) {
		visitBinaryExpression(orExpression);
	}

	@Override
	public void visit(Parenthesis parenthesis) {
		parenthesis.getExpression().accept(this);
	}

	@Override
	public void visit(StringValue stringValue) {
	}

	@Override
	public void visit(Subtraction subtraction) {
		visitBinaryExpression(subtraction);
	}

	public void visitBinaryExpression(BinaryExpression binaryExpression) {
		binaryExpression.getLeftExpression().accept(this);
		binaryExpression.getRightExpression().accept(this);
	}

	@Override
	public void visit(ExpressionList expressionList) {
		for (Expression expression : expressionList.getExpressions()) {
			expression.accept(this);
		}

	}

	@Override
	public void visit(DateValue dateValue) {
	}

	@Override
	public void visit(TimestampValue timestampValue) {
	}

	@Override
	public void visit(TimeValue timeValue) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser
	 * .expression.CaseExpression)
	 */
	@Override
	public void visit(CaseExpression caseExpression) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser
	 * .expression.WhenClause)
	 */
	@Override
	public void visit(WhenClause whenClause) {
	}

	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {
		allComparisonExpression.getSubSelect().getSelectBody().accept(this);
	}

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		anyComparisonExpression.getSubSelect().getSelectBody().accept(this);
	}

	@Override
	public void visit(SubJoin subjoin) {
		subjoin.getLeft().accept(this);
		subjoin.getJoin().getRightItem().accept(this);
	}

	@Override
	public void visit(Concat concat) {
		visitBinaryExpression(concat);
	}

	@Override
	public void visit(Matches matches) {
		visitBinaryExpression(matches);
	}

	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		visitBinaryExpression(bitwiseAnd);
	}

	@Override
	public void visit(BitwiseOr bitwiseOr) {
		visitBinaryExpression(bitwiseOr);
	}

	@Override
	public void visit(BitwiseXor bitwiseXor) {
		visitBinaryExpression(bitwiseXor);
	}

	@Override
	public void visit(CastExpression cast) {
		cast.getLeftExpression().accept(this);
	}

	@Override
	public void visit(Modulo modulo) {
		visitBinaryExpression(modulo);
	}

	@Override
	public void visit(AnalyticExpression analytic) {
	}

	@Override
	public void visit(SetOperationList list) {
		for (PlainSelect plainSelect : list.getPlainSelects()) {
			visit(plainSelect);
		}
	}

	@Override
	public void visit(ExtractExpression eexpr) {
	}

	@Override
	public void visit(LateralSubSelect lateralSubSelect) {
		lateralSubSelect.getSubSelect().getSelectBody().accept(this);
	}

	@Override
	public void visit(MultiExpressionList multiExprList) {
		for (ExpressionList exprList : multiExprList.getExprList()) {
			exprList.accept(this);
		}
	}

	@Override
	public void visit(ValuesList valuesList) {
	}

	private void init() {
		otherItemNames = new ArrayList<String>();
		tables = new ArrayList<String>();
		tableAliase = new HashMap<String, String>();
		currentTable = null;
	}

	@Override
	public void visit(IntervalExpression iexpr) {
	}

	@Override
	public void visit(JdbcNamedParameter jdbcNamedParameter) {
	}

	@Override
	public void visit(OracleHierarchicalExpression oexpr) {
	}

	@Override
	public void visit(SubmittedBy submittedBy) {
	}

}
