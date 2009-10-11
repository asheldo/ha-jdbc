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

import java.sql.Connection;
import java.sql.SQLException;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.util.reflect.ProxyFactory;

/**
 * @author Paul Ferraro
 * @param <D> 
 * @param <P> 
 */
public class ConnectionInvocationStrategy<Z, D extends Database<Z>, P> extends DatabaseWriteInvocationStrategy<Z, D, P, Connection, SQLException>
{
	private P connectionFactory;
	private TransactionContext<Z, D> transactionContext;
	
	/**
	 * @param cluster 
	 * @param connectionFactory the factory from which to create connections
	 * @param transactionContext 
	 */
	public ConnectionInvocationStrategy(DatabaseCluster<Z, D> cluster, P connectionFactory, TransactionContext<Z, D> transactionContext)
	{
		super(cluster.getNonTransactionalExecutor());
		
		this.connectionFactory = connectionFactory;
		this.transactionContext = transactionContext;
	}

	/**
	 * @see net.sf.hajdbc.sql.DatabaseWriteInvocationStrategy#invoke(net.sf.hajdbc.sql.SQLProxy, net.sf.hajdbc.sql.Invoker)
	 */
	@Override
	public Connection invoke(SQLProxy<Z, D, P, SQLException> proxy, Invoker<Z, D, P, Connection, SQLException> invoker) throws SQLException
	{
		return ProxyFactory.createProxy(Connection.class, new ConnectionInvocationHandler<Z, D, P>(this.connectionFactory, proxy, invoker, this.invokeAll(proxy, invoker), this.transactionContext));
	}
}
