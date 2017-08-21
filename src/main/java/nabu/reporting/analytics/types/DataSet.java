package nabu.reporting.analytics.types;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import be.nabu.libs.types.api.KeyValuePair;

@XmlRootElement
public class DataSet {
	private String id, description;
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
}
