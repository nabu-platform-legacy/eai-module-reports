package nabu.reporting.analytics.types;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import nabu.services.jdbc.types.Page;

@XmlRootElement
public class ResultSet {
	private List<Object> results;
	private Date timestamp;
	private Page page;
	
	public List<Object> getResults() {
		return results;
	}
	public void setResults(List<Object> results) {
		this.results = results;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public Page getPage() {
		return page;
	}
	public void setPage(Page page) {
		this.page = page;
	}
}
