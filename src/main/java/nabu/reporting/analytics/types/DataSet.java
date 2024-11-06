/*
* Copyright (C) 2017 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package nabu.reporting.analytics.types;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import be.nabu.libs.types.api.KeyValuePair;

@XmlRootElement
public class DataSet {
	private String id, description, name, connectionId;
	private List<KeyValuePair> parameters;
	private Long offset;
	private Integer limit;
	private List<String> orderBy;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@NotNull
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<KeyValuePair> getParameters() {
		return parameters;
	}
	public void setParameters(List<KeyValuePair> parameters) {
		this.parameters = parameters;
	}
	public Long getOffset() {
		return offset;
	}
	public void setOffset(Long offset) {
		this.offset = offset;
	}
	public Integer getLimit() {
		return limit;
	}
	public void setLimit(Integer limit) {
		this.limit = limit;
	}
	public List<String> getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(List<String> orderBy) {
		this.orderBy = orderBy;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getConnectionId() {
		return connectionId;
	}
	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}
}
