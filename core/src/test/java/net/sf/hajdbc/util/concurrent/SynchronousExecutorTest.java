/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (C) 2012  Paul Ferraro
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
package net.sf.hajdbc.util.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;

import org.junit.Test;

/**
 * @author Paul Ferraro
 *
 */
public class SynchronousExecutorTest
{
        private ConcurrentLinkedQueue<Object> logger = new ConcurrentLinkedQueue<>();

	@Test
	public void test() throws InterruptedException, ExecutionException
	{
		this.test(Arrays.asList(100, 1, 1), false);
	}

	@Test
	public void reverse() throws InterruptedException, ExecutionException
	{
		this.test(Arrays.asList(1, 100, 100), true);
	}

	public void test(List<Integer> sleeps, boolean reverse) throws InterruptedException, ExecutionException
	{

		ExecutorService service = Executors.newCachedThreadPool();
                SynchronousExecutor syncX = new SynchronousExecutor(service, reverse);
		try
		{
			List<Task> tasks = new ArrayList<>(sleeps.size());
			List<Integer> order = new CopyOnWriteArrayList<>();
			List<Integer> expected = new ArrayList<>(sleeps.size());
			for (int i = 0; i < sleeps.size(); ++i)
			{
				tasks.add(new Task(i, sleeps.get(i), order));
				expected.add(i);
			}

                        logger.add("expected: "); logger.add(expected);

			List<Future<Integer>> futures = syncX.invokeAll(tasks);
			
			List<Integer> results = new ArrayList<>(tasks.size());
			for (Future<Integer> future: futures)
			{
				results.add(future.get());
			}
			
			Assert.assertEquals(expected, results);
			
			// Make sure 1st task finished first, or last if reversed
			Assert.assertEquals(0, order.get(reverse ? 2 : 0).intValue());
		}
		finally
		{
                    for (Object logged : logger) {
                        System.out.println(logged.toString());
                    }
                    System.out.println(syncX.toString());

			service.shutdown();
		}
	}
	
	private class Task implements Callable<Integer>
	{
		private final int index;
		private final long sleep;
		private final List<Integer> order;
		
		Task(int index, long sleep, List<Integer> order)
		{
			this.index = index;
			this.sleep = sleep;
			this.order = order;
		}

                public String toString() {
                    return new StringBuilder("#").append(this.index).append(" for ").append(this.sleep).append("\n").toString();
                }

		@Override
		public Integer call() throws Exception
		{
			try
			{
				Thread.sleep(this.sleep);

				logger.add("slept: "); logger.add(this.index); logger.add("\n");
			}
			catch (InterruptedException e)
			{
                            logger.add("interrupt: "); logger.add(this.index); logger.add("\n");

				Thread.currentThread().interrupt();
			}
                        catch (Exception e) {
                            e.printStackTrace(System.err);                            
                        }
			this.order.add(this.index);
			return this.index;
		}
	}
}
