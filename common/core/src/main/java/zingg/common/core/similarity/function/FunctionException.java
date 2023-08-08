/*
 * Zingg
 * Copyright (C) 2021-Present  Zingg Labs,inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package zingg.common.core.similarity.function;

import zingg.common.core.ZinggException;

public class FunctionException extends ZinggException {
	/** Constructor ZinggException creates a new ZinggException instance. */
	public FunctionException() {
	}

	/**
	 * Constructor ZinggException creates a new ZinggException instance.
	 *
	 * @param string
	 *            of type String
	 */
	public FunctionException(String string) {
		super(string);
	}

	/**
	 * Constructor ZinggException creates a new ZinggException instance.
	 *
	 * @param string
	 *            of type String
	 * @param throwable
	 *            of type Throwable
	 */
	public FunctionException(String string, Throwable throwable) {
		super(string, throwable);
	}

	/**
	 * Constructor ZinggException creates a new ZinggException instance.
	 *
	 * @param throwable
	 *            of type Throwable
	 */
	public FunctionException(Throwable throwable) {
		super(throwable);
	}
}
