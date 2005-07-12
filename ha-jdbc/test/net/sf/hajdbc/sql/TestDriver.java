/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (C) 2005 Paul Ferraro
 * 
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by the 
 * Free Software Foundation; either version 2.1 of the License, or (at your 
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact: ferraro@users.sourceforge.net
 */
package net.sf.hajdbc.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Reference;

import net.sf.hajdbc.AbstractTestCase;

/**
 * Unit test for {@link Driver}.
 * @author  Paul Ferraro
 * @since   1.0
 */
public class TestDriver extends AbstractTestCase
{
	private MockDriver mockDriver = new MockDriver();
	private Driver driver = new Driver();
	private Context context;
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		DriverManager.registerDriver(this.mockDriver);
		
		Properties properties = new Properties();
		
		properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "net.sf.hajdbc.sql.MockInitialContextFactory");
		
		this.context = new InitialContext(properties);
		
		Reference reference = new Reference(DataSource.class.toString(), "net.sf.hajdbc.sql.MockDataSourceFactory", null);
		
		this.context.bind("datasource1", reference);
		this.context.bind("datasource2", reference);
	}

	/**
	 * @see net.sf.hajdbc.AbstractTestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		DriverManager.deregisterDriver(mockDriver);
		
		this.context.unbind("datasource1");
		this.context.unbind("datasource2");
		
		super.tearDown();
	}

	/**
	 * Test method for {@link Driver} static initialization.
	 */
	public void testRegister()
	{
		Iterator drivers = Collections.list(DriverManager.getDrivers()).iterator();
		
		boolean registered = false;
		
		while (drivers.hasNext())
		{
			if (Driver.class.isInstance(drivers.next()))
			{
				registered = true;
			}
		}
		
		assertTrue(registered);
	}

	/**
	 * Test method for {@link Driver#acceptsURL(String)}.
	 */
	public void testAcceptsURL()
	{
		try
		{
			boolean accepted = this.driver.acceptsURL("jdbc:ha-jdbc:database-cluster");
			
			assertTrue(accepted);

			accepted = this.driver.acceptsURL("jdbc:ha-jdbc:no-such-cluster");

			assertFalse(accepted);

			accepted = this.driver.acceptsURL("jdbc:ha-jdbc:");
			
			assertFalse(accepted);

			accepted = this.driver.acceptsURL("jdbc:ha-jdbc");
			
			assertFalse(accepted);

			accepted = this.driver.acceptsURL("jdbc:test:database1");
			
			assertFalse(accepted);
		}
		catch (SQLException e)
		{
			this.fail(e);
		}
	}

	/**
	 * Test method for {@link Driver#connect(String, Properties)}
	 */
	public void testConnect()
	{
		try
		{
			Connection connection = this.driver.connect("jdbc:ha-jdbc:database-cluster", null);
			
			assertNotNull(connection);
			assertEquals("net.sf.hajdbc.sql.Connection", connection.getClass().getName());
		}
		catch (SQLException e)
		{
			this.fail(e);
		}
	}

	/**
	 * Test method for {@link Driver#getMajorVersion()}
	 */
	public void testGetMajorVersion()
	{
		int major = this.driver.getMajorVersion();
		
		assertEquals(1, major);
	}

	/**
	 * Test method for {@link Driver#getMinorVersion()}
	 */
	public void testGetMinorVersion()
	{
		int minor = this.driver.getMinorVersion();
		
		assertEquals(0, minor);
	}

	/**
	 * Test method for {@link Driver#getPropertyInfo(String, Properties)}
	 */
	public void testGetPropertyInfo()
	{
		try
		{
			DriverPropertyInfo[] info = this.driver.getPropertyInfo("jdbc:ha-jdbc:database-cluster", null);
			
			assertNotNull(info);
		}
		catch (SQLException e)
		{
			this.fail(e);
		}
	}

	/**
	 * Test method for {@link Driver#jdbcCompliant()}
	 */
	public void testJdbcCompliant()
	{
		boolean compliant = this.driver.jdbcCompliant();
		
		assertTrue(compliant);
	}
}
