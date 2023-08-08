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

package zingg.spark.core.documenter;

import org.apache.spark.sql.Row;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import zingg.common.core.documenter.RowAdapter;

public class SparkRowAdapter extends RowAdapter<Row>{

     public SparkRowAdapter(Row row, ObjectWrapper ow) {
          super(row, ow); 
     }

     @Override  // coming from TemplateSequenceModel
     public int size() throws TemplateModelException {
          return row.size();
     }
     
     @Override  // coming from TemplateSequenceModel
     public TemplateModel get(int index) throws TemplateModelException {
          return wrap(row.get(index));
     }

}