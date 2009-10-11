/*
 * HA-JDBC: High-Availability JDBC
 * Copyright 2004-2009 Paul Ferraro
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.hajdbc.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.util.reflect.ProxyFactory;

/**
 * @author Paul Ferraro
 * @param <D> 
 * @param <S> 
 */
public class LazyResultSetInvocationStrategy<Z, D extends Database<Z>, S extends Statement> extends DatabaseReadInvocationStrategy<Z, D, S, ResultSet, SQLException>
{
	private S statement;
	private TransactionContext<Z, D> transactionContext;
	private FileSupport<SQLException> fileSupport;
	
	/**
	 * @param statement the statement from which to create result sets
	 * @param transactionContext 
	 * @param fileSupport 
	 */
	public LazyResultSetInvocationStrategy(S statement, TransactionContext<Z, D> transactionContext, FileSupport<SQLException> fileSupport)
	{
		this.statement = statement;
		this.transactionContext = transactionContext;
		this.fileSupport = fileSupport;
	}
	
	/**
	 * @see net.sf.hajdbc.sql.DatabaseReadInvocationStrategy#invoke(net.sf.hajdbc.sql.SQLProxy, net.sf.hajdbc.sql.Invoker)
	 */
	@Override
	public ResultSet invoke(SQLProxy<Z, D, S, SQLException> proxy, Invoker<Z, D, S, ResultSet, SQLException> invoker) throws SQLException
	{
		return ProxyFactory.createProxy(ResultSet.class, new ResultSetInvocationHandler<Z, D, S>(this.statement, proxy, invoker, this.invokeAll(proxy, invoker), this.transactionContext, this.fileSupport));
	}
}
