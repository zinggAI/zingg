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

package zingg.common.core.util;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

public interface DFReader<D, R, C> {

    public DFReader<D,R,C> getReader();

    public DFReader<D,R,C> format(String f);

    public DFReader<D,R,C> option(String k, String v);

    public DFReader<D,R,C> setSchema(String s);

    public ZFrame<D,R,C> load() throws ZinggClientException;

    public ZFrame<D,R,C> load(String location) throws ZinggClientException;

    
}
