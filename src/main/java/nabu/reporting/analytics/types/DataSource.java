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

import javax.xml.bind.annotation.XmlRootElement;

import nabu.utils.types.ParameterDescription;

@XmlRootElement
public class DataSource {
	private String id;
	private List<DataSourceType> types;
	private boolean paged, streaming, orderable, database;
	private List<ParameterDescription> input;
	private List<ParameterDescription> output;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public List<DataSourceType> getTypes() {
		return types;
	}
	public void setTypes(List<DataSourceType> types) {
		this.types = types;
	}

	public enum DataSourceType {
		GAUGE,
		TABULAR,
		SERIES,
		WATERFALL,
		FACT
	}
	
	public List<ParameterDescription> getInput() {
		return input;
	}
	public void setInput(List<ParameterDescription> input) {
		this.input = input;
	}
	public List<ParameterDescription> getOutput() {
		return output;
	}
	public void setOutput(List<ParameterDescription> output) {
		this.output = output;
	}
	public boolean isPaged() {
		return paged;
	}
	public void setPaged(boolean paged) {
		this.paged = paged;
	}
	public boolean isStreaming() {
		return streaming;
	}
	public void setStreaming(boolean streaming) {
		this.streaming = streaming;
	}
	public boolean isOrderable() {
		return orderable;
	}
	public void setOrderable(boolean orderable) {
		this.orderable = orderable;
	}
	public boolean isDatabase() {
		return database;
	}
	public void setDatabase(boolean database) {
		this.database = database;
	}
}
