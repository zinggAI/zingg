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

package zingg.common.core.documenter;

import freemarker.template.AdapterTemplateModel;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateSequenceModel;
import freemarker.template.WrappingTemplateModel;

public abstract class RowAdapter<R> extends WrappingTemplateModel
		implements TemplateSequenceModel, AdapterTemplateModel {

	protected final R row;

	public RowAdapter(R row, ObjectWrapper ow) {
		super(ow); // coming from WrappingTemplateModel
		this.row = row;
	}

	@Override // coming from AdapterTemplateModel
	public Object getAdaptedObject(Class hint) {
		return row;
	}

}