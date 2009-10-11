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
package net.sf.hajdbc.balancer;

import java.util.Set;

import net.sf.hajdbc.Database;

/**
 * Factory for creating balancers.
 * @author Paul Ferraro
 */
public interface BalancerFactory
{
	/**
	 * Create a balancer.
	 * @param <Z> database connection source
	 * @param <D> database descriptor
	 * @param database set of initial databases
	 * @return a new balancer.
	 */
	<Z, D extends Database<Z>> Balancer<Z, D> createBalancer(Set<D> databases);
}
