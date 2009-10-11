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
package net.sf.hajdbc.balancer.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.balancer.AbstractSetBalancer;

/**
 * Balancer implementation whose {@link #next()} implementation returns a random database.
 * The probability that a given database will be returned is: <em>weight / total-weight</em>.
 * 
 * @author  Paul Ferraro
 * @param <D> either java.sql.Driver or javax.sql.DataSource
 */
public class RandomBalancer<P, D extends Database<P>> extends AbstractSetBalancer<P, D>
{
	private volatile List<D> databaseList = Collections.emptyList();

	private Random random = new Random();

	/**
	 * @see net.sf.hajdbc.balancer.Balancer#next()
	 */
	@Override
	public D next()
	{
		List<D> list = this.databaseList;
		
		return !list.isEmpty() ? list.get(this.random.nextInt(list.size())) : null;
	}
	
	/**
	 * @see net.sf.hajdbc.balancer.AbstractBalancer#added(net.sf.hajdbc.Database)
	 */
	@Override
	protected void added(D database)
	{
		int weight = database.getWeight();
		
		if (weight > 0)
		{
			List<D> list = new ArrayList<D>(this.databaseList.size() + weight);
			
			list.addAll(this.databaseList);
			
			for (int i = 0; i < weight; ++i)
			{
				list.add(database);
			}
			
			this.databaseList = list;
		}
	}

	/**
	 * @see net.sf.hajdbc.balancer.AbstractBalancer#removed(net.sf.hajdbc.Database)
	 */
	@Override
	protected void removed(D database)
	{
		int weight = database.getWeight();

		if (weight > 0)
		{
			List<D> list = new ArrayList<D>(this.databaseList.size() - weight);
			
			int index = this.databaseList.indexOf(database);
			
			list.addAll(this.databaseList.subList(0, index));
			list.addAll(this.databaseList.subList(index + weight, this.databaseList.size()));
			
			this.databaseList = list;
		}
	}

	/**
	 * @see net.sf.hajdbc.balancer.AbstractBalancer#cleared()
	 */
	@Override
	protected void cleared()
	{
		this.databaseList = Collections.emptyList();
	}
}
