package com.aegisql.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
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
import com.aegisql.access.TableQueryType;

/**
 * Find all used tables and columns within an select statement.
 */
public class QueryAnalizer implements SelectVisitor, FromItemVisitor, ExpressionVisitor, ItemsListVisitor {

	private final static Logger log = LoggerFactory.getLogger(QueryAnalizer.class.getName());

	
	private Map<String,Set<String>> allColumns     = new HashMap<String, Set<String>>();
	private Map<String,String> tableAliase         = new HashMap<String,String>();
	private Map<String,String> tableAliaseReversed = null;
	private String currentTable;
	private List<String> tables;
	private boolean hasStar = false;
	private TableQueryType currentQueryType;
	private final TableQueryType queryType;
	private final Statement statement;
	private final Map<String,TableQueryType> tblQueryType = new HashMap<String, TableQueryType>(); //Incomplete, must support same table in several different statements
	/**
	 * There are special names, that are not table names but are parsed as
	 * tables. These names are collected here and are not included in the tables
	 * - names anymore.
	 */
	private List<String> otherItemNames;
	
	public QueryAnalizer(Statement statement) {
		this.statement = statement;
		this.queryType = TableQueryType.valueOf(statement);
		this.currentQueryType = queryType;
		init();
		buildTableList(statement);
		buildColumnList(statement);
	}
	

	public List<String> getTableList() {
		return tables;
	}
	
	public final Map<String,Set<String>> getAllColumns() {
		return allColumns;
	}
	
	private void buildColumnList(Statement statement) {
		if( (allColumns.size() == 2) && allColumns.containsKey(null) ) {
			Set<String> names = allColumns.remove(null);
			for(String table: allColumns.keySet() ) {
				allColumns.get(table).addAll(names);
			}
		}
	}

	public final List<String> getAllTables() {
		return tables;
	}

	public final boolean hasStar() {
		return hasStar;
	}

	public TableQueryType getQueryType() {
		return queryType;
	}
	
	public TableQueryType getQueryType( String table ) {
		return tblQueryType.get(table);
	}
	
	public Statement getStatement() {
		return statement;
	}
	
	public final Map<String, String> getTableAliase() {
		return tableAliase;
	}

	public final Map<String, String> getReversedTableAliase() {
		if(tableAliaseReversed == null) {
			tableAliaseReversed = new HashMap<>();
			for( Entry<String, String> e : tableAliase.entrySet()) {
				tableAliaseReversed.put(e.getValue(), e.getKey());
			}
		}
		return tableAliaseReversed;
	}

	private void buildTableList(Statement statement) {
		if( statement instanceof Insert ) buildTableList((Insert)statement);
		else if( statement instanceof Replace) buildTableList((Replace)statement);
		else if( statement instanceof Select ) buildTableList((Select)statement);
		else if( statement instanceof Update ) buildTableList((Update)statement);
		else if( statement instanceof Delete ) buildTableList((Delete)statement);
		else throw new RuntimeException("Unsupported class for table name lookup: " + statement.getClass().getName());
	}
	
	private void buildTableList(Delete delete) {
		String tableName = delete.getTable().getWholeTableName();
		currentTable = tableName;
		currentQueryType = TableQueryType.DELETE;
		Set<String> columns = allColumns.get(tableName);
		if(columns == null) {
			columns = new HashSet<String>();
			allColumns.put(tableName, columns);
		}

		tables.add(delete.getTable().getName());
		tblQueryType.put(tableName, TableQueryType.DELETE);
		tblQueryType.put(delete.getTable().getName(), TableQueryType.DELETE);
		tblQueryType.put(delete.getTable().getWholeTableName(), TableQueryType.DELETE);
		Expression where = delete.getWhere();
		if( where != null ) {
			delete.getWhere().accept(this);
		}
	}

	private void buildTableList(Insert insert) {
		String tableName = insert.getTable().getWholeTableName();
		currentTable = tableName;
		currentQueryType = TableQueryType.INSERT;
		Set<String> columns = allColumns.get(tableName);
		if(columns == null) {
			columns = new HashSet<String>();
			allColumns.put(tableName, columns);
		}

		tables.add(insert.getTable().getName());
		tblQueryType.put(tableName, TableQueryType.INSERT);
		tblQueryType.put(insert.getTable().getName(), TableQueryType.INSERT);
		tblQueryType.put(insert.getTable().getWholeTableName(), TableQueryType.INSERT);

		if (insert.getColumns() != null) {
			log.debug("Items Columns "+insert.getColumns());
			for(Column column : insert.getColumns() ) {
				column.accept(this);
			}
		}

		if (insert.getItemsList() != null) {
			log.debug("Items List "+insert.getColumns());
			insert.getItemsList().accept(this);
		}
	}

	private void buildTableList(Replace insert) {
		String tableName = insert.getTable().getWholeTableName();
		currentTable = tableName;
		currentQueryType = TableQueryType.REPLACE;
		Set<String> columns = allColumns.get(tableName);
		if(columns == null) {
			columns = new HashSet<String>();
			allColumns.put(tableName, columns);
		}

		tables.add(insert.getTable().getName());
		tblQueryType.put(tableName, TableQueryType.REPLACE);
		tblQueryType.put(insert.getTable().getName(), TableQueryType.REPLACE);
		tblQueryType.put(insert.getTable().getWholeTableName(), TableQueryType.REPLACE);

		if (insert.getColumns() != null) {
			log.debug("Items Columns "+insert.getColumns());
			for(Column column : insert.getColumns() ) {
				column.accept(this);
			}
		}

		if (insert.getItemsList() != null) {
			log.debug("Items List "+insert.getColumns());
			insert.getItemsList().accept(this);
		}
	}

	private void buildTableList(Select select) {
		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) {
				withItem.accept(this);
			}
		}
		select.getSelectBody().accept(this);
		PlainSelect plain = (PlainSelect) select.getSelectBody();
		if (plain.getJoins() != null) {
			for (Join join : plain.getJoins()) {
				if(join.getOnExpression() != null) {
					join.getOnExpression().accept(this);
				}
				join.getRightItem().accept(this);
			}
		}

		processPlainSelectColumns(plain);
	}

	private void buildTableList(Update update) {
		tables.add(update.getTable().getName());
		tblQueryType.put(update.getTable().getName(), TableQueryType.UPDATE);
		tblQueryType.put(update.getTable().getWholeTableName(), TableQueryType.UPDATE);

		String tableName = update.getTable().getWholeTableName();
		currentTable = tableName;
		currentQueryType = TableQueryType.UPDATE;
		Set<String> columns = allColumns.get(tableName);
		if(columns == null) {
			columns = new HashSet<String>();
			allColumns.put(tableName, columns);
		}
		
		if (update.getExpressions() != null) {
			for (Expression expression : update.getExpressions()) {
				expression.accept(this);
			}
		}

		if (update.getFromItem() != null) {
			update.getFromItem().accept(this);
		}

		if (update.getJoins() != null) {
			for (Join join : update.getJoins()) {
				join.getRightItem().accept(this);
			}
		}

		for(Column column : update.getColumns() ) {
			column.accept(this);
		}
		
		if (update.getWhere() != null) {
			update.getWhere().accept(this);
		}
	}
	
	private void processPlainSelectColumns(PlainSelect plain) {
		FromItem from = plain.getFromItem();
		String tableName = null;
		if( from != null ) {
			tableName = ((Table)from).getWholeTableName();
			tblQueryType.put(tableName, TableQueryType.SELECT);
			tblQueryType.put(((Table)from).getName(), TableQueryType.SELECT);
		}

		Set<String> columns = allColumns.get(tableName);
		if((columns == null)&&(from != null)) {
			columns = new HashSet<String>();
			allColumns.put(tableName, columns);
		}
		for(SelectItem i:plain.getSelectItems()){
			log.debug("QA: Select Item type "+i.getClass().getSimpleName());
			if(i instanceof SelectExpressionItem) {
				SelectExpressionItem at = (SelectExpressionItem)i;
				if(! (at.getExpression() instanceof Column) ) continue;
				String atName = ((Column)at.getExpression()).getTable().getWholeTableName();
				log.debug("SelectExpressionItem table alias "+atName);
				if(allColumns.containsKey(atName)) {
					columns = allColumns.get(atName);
				} else if( tableAliase.containsKey(atName) ) {
					String alias = tableAliase.get(atName);
					if(allColumns.containsKey(alias)) {
						columns = allColumns.get(alias);
					} else {
						columns = new HashSet<String>();
						allColumns.put(alias, columns);						
					}
				}
/*INS*/
				List<Join> joins = plain.getJoins();
				if(joins != null)
				for(Join j:joins) {
					List<Column> used = j.getUsingColumns();
					Expression e = j.getOnExpression();
				}
/*INS*/

			} else if ( i instanceof AllTableColumns ) { // a.*,b.*
				hasStar = true;
				AllTableColumns ai = (AllTableColumns)i;
				String atName = ai.getTable().getWholeTableName();
				log.debug("AllTableColumns table alias "+atName);
				if(allColumns.containsKey(atName)) {
					columns = allColumns.get(atName);
				} else if( tableAliase.containsKey(atName) ) {
					String alias = tableAliase.get(atName);
					if(allColumns.containsKey(alias)) {
						columns = allColumns.get(alias);
					} else {
						columns = new HashSet<String>();
						allColumns.put(alias, columns);
					}
				}
			} else if ( i instanceof AllColumns ) { // *
				hasStar = true;
				List<Join> joins = plain.getJoins();
				if(joins != null)
				for(Join j:joins) {
					Set<String> jColumns;
					Table t = (Table)j.getRightItem();
					String tName = t.getWholeTableName();
					if(allColumns.containsKey(tName)) {
						jColumns = allColumns.get(tName);
						jColumns.add("*");
					} else {
						jColumns = new HashSet<String>();
						jColumns.add("*");
						allColumns.put(tName, jColumns);						
					}
				}
				
			}
			columns.add(i.toString());
		}

	}

	@Override
	public final void visit(WithItem withItem) {
		otherItemNames.add(withItem.getName().toLowerCase());
		withItem.getSelectBody().accept(this);
	}

	@Override
	public void visit(PlainSelect plainSelect) {
		log.debug("Plain Select "+plainSelect);

		FromItem from = plainSelect.getFromItem();
		
		if( from != null ) {
			from.accept(this);
		}

		if (plainSelect.getJoins() != null) {
			for (Join join : plainSelect.getJoins()) {
				join.getRightItem().accept(this);
			}
		}
		if (plainSelect.getWhere() != null) {
			log.debug("Where: "+plainSelect.getWhere() + " type:"+plainSelect.getWhere().getClass());
			plainSelect.getWhere().accept(this);
		}

	}

	@Override
	public final void visit(Table tableName) {
		currentTable = tableName.getWholeTableName();
		String tableWholeName = tableName.getWholeTableName();
		String tableShortName = tableName.getName();
		String alias          = tableName.getAlias();
		log.debug("table: {} {} {}",tableWholeName, alias, currentQueryType);
		if(alias!= null && !tableAliase.containsKey(alias) && tableWholeName != null ) {
			tableAliase.put(alias, tableWholeName);
		}
		
		if (!otherItemNames.contains(tableWholeName.toLowerCase())
				&& !tables.contains(tableWholeName)) {
			if(tableWholeName!=null) {
				tables.add(tableWholeName);
				tblQueryType.put(tableWholeName, currentQueryType);
				tblQueryType.put(tableShortName, currentQueryType);
			}
		}
	}

	@Override
	public final void visit(SubSelect subSelect) {
		PlainSelect plain = (PlainSelect) subSelect.getSelectBody();
		String current = currentTable;
		TableQueryType qt = currentQueryType;
		subSelect.getSelectBody().accept(this);
		currentTable = current;
		currentQueryType = qt;
		processPlainSelectColumns(plain);
	}

	@Override
	public final void visit(Addition addition) {
		visitBinaryExpression(addition);
	}

	@Override
	public final void visit(AndExpression andExpression) {
		visitBinaryExpression(andExpression);
	}

	@Override
	public final void visit(Between between) {
		between.getLeftExpression().accept(this);
		between.getBetweenExpressionStart().accept(this);
		between.getBetweenExpressionEnd().accept(this);
	}

	@Override
	public final void visit(Column tableColumn) {
		String tableName = tableColumn.getTable().getName();
		if(tableName == null){
			tableName=currentTable;
			log.debug("Column {} Internal Table is NULL, use current: {}",tableColumn.getWholeColumnName(),tableName);
		} else {
			log.debug("Column {} Internal Table: {}",tableColumn.getWholeColumnName(),tableName);
		}
		if(tableAliase.containsKey(tableName)) {
			tableName = tableAliase.get(tableName);
			log.debug("Column Table Alias "+tableName);
		}
		Set<String> columns = allColumns.get(tableName);
		if(columns == null ) {
			columns = new HashSet<String>();
			allColumns.put(tableName, columns);
		}
		columns.add(tableColumn.getWholeColumnName());
	}

	@Override
	public final void visit(Division division) {
		visitBinaryExpression(division);
	}

	@Override
	public final void visit(DoubleValue doubleValue) {
	}

	@Override
	public final void visit(EqualsTo equalsTo) {
		visitBinaryExpression(equalsTo);
	}

	@Override
	public final void visit(Function function) {
	}

	@Override
	public final void visit(GreaterThan greaterThan) {
		visitBinaryExpression(greaterThan);
	}

	@Override
	public final void visit(GreaterThanEquals greaterThanEquals) {
		visitBinaryExpression(greaterThanEquals);
	}

	@Override
	public final void visit(InExpression inExpression) {
		inExpression.getLeftExpression().accept(this);
		inExpression.getRightItemsList().accept(this);
	}

	@Override
	public final void visit(InverseExpression inverseExpression) {
		inverseExpression.getExpression().accept(this);
	}

	@Override
	public final void visit(IsNullExpression isNullExpression) {
	}

	@Override
	public final void visit(JdbcParameter jdbcParameter) {
	}

	@Override
	public final void visit(LikeExpression likeExpression) {
		visitBinaryExpression(likeExpression);
	}

	@Override
	public final void visit(ExistsExpression existsExpression) {
		existsExpression.getRightExpression().accept(this);
	}

	@Override
	public final void visit(LongValue longValue) {
	}

	@Override
	public final void visit(MinorThan minorThan) {
		visitBinaryExpression(minorThan);
	}

	@Override
	public final void visit(MinorThanEquals minorThanEquals) {
		visitBinaryExpression(minorThanEquals);
	}

	@Override
	public final void visit(Multiplication multiplication) {
		visitBinaryExpression(multiplication);
	}

	@Override
	public final void visit(NotEqualsTo notEqualsTo) {
		visitBinaryExpression(notEqualsTo);
	}

	@Override
	public final void visit(NullValue nullValue) {
	}

	@Override
	public final void visit(OrExpression orExpression) {
		visitBinaryExpression(orExpression);
	}

	@Override
	public final void visit(Parenthesis parenthesis) {
		parenthesis.getExpression().accept(this);
	}

	@Override
	public final void visit(StringValue stringValue) {
	}

	@Override
	public final void visit(Subtraction subtraction) {
		visitBinaryExpression(subtraction);
	}

	public final void visitBinaryExpression(BinaryExpression binaryExpression) {
		binaryExpression.getLeftExpression().accept(this);
		binaryExpression.getRightExpression().accept(this);
	}

	@Override
	public final void visit(ExpressionList expressionList) {
		for (Expression expression : expressionList.getExpressions()) {
			expression.accept(this);
		}

	}

	@Override
	public final void visit(DateValue dateValue) {
	}

	@Override
	public final void visit(TimestampValue timestampValue) {
	}

	@Override
	public final void visit(TimeValue timeValue) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.CaseExpression)
	 */
	@Override
	public final void visit(CaseExpression caseExpression) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.WhenClause)
	 */
	@Override
	public final void visit(WhenClause whenClause) {
	}

	@Override
	public final void visit(AllComparisonExpression allComparisonExpression) {
		allComparisonExpression.getSubSelect().getSelectBody().accept(this);
	}

	@Override
	public final void visit(AnyComparisonExpression anyComparisonExpression) {
		anyComparisonExpression.getSubSelect().getSelectBody().accept(this);
	}

	@Override
	public final void visit(SubJoin subjoin) {
		subjoin.getLeft().accept(this);
		subjoin.getJoin().getRightItem().accept(this);
	}

	@Override
	public final void visit(Concat concat) {
		visitBinaryExpression(concat);
	}

	@Override
	public final void visit(Matches matches) {
		visitBinaryExpression(matches);
	}

	@Override
	public final void visit(BitwiseAnd bitwiseAnd) {
		visitBinaryExpression(bitwiseAnd);
	}

	@Override
	public final void visit(BitwiseOr bitwiseOr) {
		visitBinaryExpression(bitwiseOr);
	}

	@Override
	public final void visit(BitwiseXor bitwiseXor) {
		visitBinaryExpression(bitwiseXor);
	}

	@Override
	public final void visit(CastExpression cast) {
		cast.getLeftExpression().accept(this);
	}

	@Override
	public final void visit(Modulo modulo) {
		visitBinaryExpression(modulo);
	}

	@Override
	public final void visit(AnalyticExpression analytic) {
	}

	@Override
	public final void visit(SetOperationList list) {
		for (PlainSelect plainSelect : list.getPlainSelects()) {
			visit(plainSelect);
		}
	}

	@Override
	public final void visit(ExtractExpression eexpr) {
	}

	@Override
	public final void visit(LateralSubSelect lateralSubSelect) {
		lateralSubSelect.getSubSelect().getSelectBody().accept(this);
	}

	@Override
	public final void visit(MultiExpressionList multiExprList) {
		for (ExpressionList exprList : multiExprList.getExprList()) {
			exprList.accept(this);
		}
	}

	@Override
	public final void visit(ValuesList valuesList) {
	}

	private void init() {
		otherItemNames      = new ArrayList<String>();
		tables              = new ArrayList<String>();
		allColumns          = new HashMap<String, Set<String>>();
		tableAliase         = new HashMap<String,String>();
		tableAliaseReversed = null;
		currentTable        = null;
	}

	@Override
	public final void visit(IntervalExpression iexpr) {
	}

    @Override
    public final void visit(JdbcNamedParameter jdbcNamedParameter) {
    }

	@Override
	public final void visit(OracleHierarchicalExpression oexpr) {
	}

	@Override
	public final void visit(SubmittedBy submittedBy) {
	}

}
