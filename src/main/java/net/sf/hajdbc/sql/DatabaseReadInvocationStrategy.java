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

import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.Dialect;
import net.sf.hajdbc.ExceptionFactory;
import net.sf.hajdbc.Messages;
import net.sf.hajdbc.balancer.Balancer;
import net.sf.hajdbc.state.StateManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paul Ferraro
 * @param <D> 
 * @param <T> 
 * @param <R> 
 */
public class DatabaseReadInvocationStrategy<Z, D extends Database<Z>, T, R, E extends Exception> implements InvocationStrategy<Z, D, T, R, E>
{
	private static Logger logger = LoggerFactory.getLogger(DatabaseReadInvocationStrategy.class);
	
	/**
	 * @see net.sf.hajdbc.sql.InvocationStrategy#invoke(net.sf.hajdbc.sql.SQLProxy, net.sf.hajdbc.sql.Invoker)
	 */
	@Override
	public R invoke(SQLProxy<Z, D, T, E> proxy, Invoker<Z, D, T, R, E> invoker) throws E
	{
		SortedMap<D, R> map = this.invokeAll(proxy, invoker);
		
		return map.get(map.firstKey());
	}
	
	protected SortedMap<D, R> invokeAll(SQLProxy<Z, D, T, E> proxy, Invoker<Z, D, T, R, E> invoker) throws E
	{
		SortedMap<D, R> resultMap = new TreeMap<D, R>();
		
		DatabaseCluster<Z, D> cluster = proxy.getDatabaseCluster();
		Balancer<Z, D> balancer = cluster.getBalancer();
		Dialect dialect = cluster.getDialect();
		StateManager stateManager = cluster.getStateManager();
		ExceptionFactory<E> exceptionFactory = proxy.getExceptionFactory();
		
		while (true)
		{
			D database = balancer.next();
			
			if (database == null)
			{
				throw exceptionFactory.createException(Messages.NO_ACTIVE_DATABASES.getMessage(cluster));
			}
			
			T object = proxy.getObject(database);
			
			balancer.beforeInvocation(database);
			
			try
			{
				R result = invoker.invoke(database, object);
				
				resultMap.put(database, result);
				
				return resultMap;
			}
			catch (Exception e)
			{
				E exception = exceptionFactory.createException(e);
				
				if (exceptionFactory.indicatesFailure(exception, dialect))
				{
					if (cluster.deactivate(database, stateManager))
					{
						logger.error(Messages.DATABASE_DEACTIVATED.getMessage(database, cluster), exception);
					}
				}
				else
				{
					throw exception;
				}
			}
			finally
			{
				balancer.afterInvocation(database);
			}
		}
	}
}
