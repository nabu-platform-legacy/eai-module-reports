package nabu.reporting.analytics.types;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import nabu.utils.types.ParameterDescription;

@XmlRootElement
public class DataSource {
	private String id;
	private DataSourceType type;
	private boolean paged, streaming, orderable;
	private List<ParameterDescription> input;
	private List<ParameterDescription> output;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public DataSourceType getType() {
		return type;
	}
	public void setType(DataSourceType type) {
		this.type = type;
	}

	public enum DataSourceType {
		GAUGE,
		TABULAR,
		SERIES
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
}
