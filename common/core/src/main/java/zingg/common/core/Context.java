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

package zingg.common.core;

import java.io.Serializable;

import zingg.common.client.ZinggClientException;
import zingg.common.client.license.IZinggLicense;
import zingg.common.core.util.BlockingTreeUtil;
import zingg.common.core.util.DSUtil;
import zingg.common.core.util.GraphUtil;
import zingg.common.core.util.HashUtil;
import zingg.common.core.util.ModelUtil;
import zingg.common.core.util.PipeUtilBase;

public interface Context <S,D, R, C,T> extends Serializable {

    public HashUtil<S,D,R,C,T> getHashUtil() ;
    public void setHashUtil(HashUtil<S,D,R,C,T> t) ;
    public GraphUtil<D,R,C> getGraphUtil() ;

    public void setGraphUtil(GraphUtil<D,R,C> t) ;

    public void setModelUtil(ModelUtil<S,T,D,R,C> t);
    public void setBlockingTreeUtil(BlockingTreeUtil<S, D,R,C,T> t) ;

    public ModelUtil<S,T,D,R,C>  getModelUtil();

    public void setPipeUtil(PipeUtilBase<S,D,R,C> pipeUtil);
    public void setDSUtil(DSUtil<S,D,R,C> pipeUtil);
    public DSUtil<S,D,R,C> getDSUtil() ;
    public PipeUtilBase<S,D,R,C> getPipeUtil();
    public BlockingTreeUtil<S, D,R,C,T> getBlockingTreeUtil() ;

    public void init(IZinggLicense license)
        throws ZinggClientException;
    
    public void cleanup();
    
    /**convenience method to set all utils
     * especially useful when you dont want to create the connection/spark context etc
     * */
    public void setUtils();

    public S getSession();

    public void setSession(S session);

    
    //public void initHashFns() throws ZinggClientException;




  
 }


    

