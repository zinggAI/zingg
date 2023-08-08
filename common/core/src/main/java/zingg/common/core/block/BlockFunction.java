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

package zingg.common.core.block;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BlockFunction<R> implements Serializable {

    public static final Log LOG = LogFactory.getLog(BlockFunction.class);
		
    Tree<Canopy<R>> tree;
    public BlockFunction(Tree<Canopy<R>> tree) {
        this.tree = tree;
    }
    
    
    public R call(R r) {
        StringBuilder bf = new StringBuilder();
        bf = Block.applyTree(r, tree, tree.getHead(), bf);
        return createRow(r, bf); //RowFactory.create(returnList);			
    }

    public abstract List<Object> getListFromRow(R r) ;

    public abstract R getRowFromList(List<Object> lob);

    public R createRow(R r, StringBuilder bf) {
        List<Object> currentRowValues = getListFromRow(r);
        currentRowValues.add(bf.toString().hashCode());
        if (LOG.isDebugEnabled()) {
            for (Object o: currentRowValues) {
                LOG.debug("return row col is " + o );
            }
        }
        return getRowFromList(currentRowValues);
    }


}
