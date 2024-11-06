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

package nabu.reporting.analytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import nabu.reporting.analytics.types.DataSet;
import nabu.reporting.analytics.types.DataSource;
import nabu.reporting.analytics.types.DataSource.DataSourceType;
import nabu.reporting.analytics.types.ResultSet;
import nabu.services.jdbc.types.Page;
import nabu.utils.reflection.Node;
import nabu.utils.types.ParameterDescription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.module.misc.executor.ExecutorArtifact;
import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.services.api.ServiceResult;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.KeyValuePair;
import be.nabu.libs.types.api.SimpleType;

@WebService
public class Services {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private ExecutionContext context;

	@WebResult(name = "results")
	public List<ResultSet> runAll(@WebParam(name = "dataSets") List<DataSet> dataSets, @WebParam(name = "executorServiceId") String executorServiceId) throws InterruptedException, ExecutionException, ServiceException {
		final List<ResultSet> results = new ArrayList<ResultSet>();
		if (dataSets != null) {
			if (executorServiceId != null) {
				ExecutorArtifact resolve = context.getServiceContext().getResolver(ExecutorArtifact.class).resolve(executorServiceId);
				if (resolve == null) {
					throw new IllegalArgumentException("Could not find executor service: " + executorServiceId);
				}
				List<Future<ServiceResult>> futures = new ArrayList<Future<ServiceResult>>();
				for (final DataSet dataSet : dataSets) {
					DefinedService service = context.getServiceContext().getResolver(DefinedService.class).resolve("nabu.reporting.analytics.Services.run");
					ComplexContent input = service.getServiceInterface().getInputDefinition().newInstance();
					input.set("dataSet", dataSet);
					futures.add(resolve.run(service, input, context.getSecurityContext().getToken()));
				}
				
				for (ServiceResult result : EAIRepositoryUtils.combine(futures).get()) {
					if (result.getException() != null) {
						throw result.getException();
					}
					results.add((ResultSet) result.getOutput().get("result"));
				}
			}
			else {
				for (final DataSet dataSet : dataSets) {
					results.add(run(dataSet));
				}
			}
		}
		return results;
	}
	
	@WebResult(name = "result")
	public ResultSet run(@WebParam(name = "dataSet") DataSet dataSet) throws InterruptedException, ExecutionException, ServiceException {
		return run(dataSet, context);
	}
	
	@SuppressWarnings("unchecked")
	private ResultSet run(@WebParam(name = "dataSet") DataSet dataSet, ExecutionContext context) throws InterruptedException, ExecutionException, ServiceException {
		Service service = (Service) EAIResourceRepository.getInstance().resolve(dataSet.getId());
		if (service == null) {
			throw new IllegalArgumentException("No service found for: " + dataSet.getId());
		}
		
		ComplexType inputDefinition = service.getServiceInterface().getInputDefinition();
		Element<?> limitField = inputDefinition.get("limit");
		Element<?> offsetField = inputDefinition.get("offset");
		Element<?> parametersField = inputDefinition.get("parameters");
		Element<?> totalRowCountField = inputDefinition.get("totalRowCount");
		Element<?> orderByField = inputDefinition.get("orderBy");
		Element<?> connectionIdField = inputDefinition.get("connection");
		
		boolean isParameters = false;
		ComplexType input;
		// we use the parameters object
		if (isNumeric(limitField) && isNumeric(offsetField) && parametersField != null && parametersField.getType() instanceof ComplexType && !parametersField.getType().isList(parametersField.getProperties())) {
			isParameters = true;
			input = (ComplexType) parametersField.getType();
		}
		else {
			input = service.getServiceInterface().getInputDefinition();
		}
		
		ComplexContent instance = input.newInstance();
		if (dataSet.getParameters() != null) {
			for (KeyValuePair parameter : dataSet.getParameters()) {
				if (parameter.getKey() != null && parameter.getValue() != null) {
					instance.set(parameter.getKey(), parameter.getValue());
				}
			}
		}
		
		if (isParameters) {
			ComplexContent newInstance = service.getServiceInterface().getInputDefinition().newInstance();
			newInstance.set("parameters", instance);
			instance = newInstance;
		}
		
		if (limitField != null && dataSet.getLimit() != null) {
			instance.set("limit", dataSet.getLimit());
		}
		if (offsetField != null && dataSet.getOffset() != null) {
			instance.set("offset", dataSet.getOffset());
		}
		if (totalRowCountField != null && (dataSet.getLimit() != null || dataSet.getOffset() != null)) {
			instance.set("totalRowCount", true);
		}
		if (dataSet.getOrderBy() != null && orderByField != null) {
			instance.set("orderBy", dataSet.getOrderBy());
		}
		if (dataSet.getConnectionId() != null && connectionIdField != null) {
			instance.set("connection", dataSet.getConnectionId());
		}
		
		Date timestamp = new Date();
		Future<ServiceResult> run = EAIResourceRepository.getInstance().getServiceRunner().run(service, context, instance);
		ServiceResult serviceResult = run.get();
		
		if (serviceResult.getException() != null) {
			throw serviceResult.getException();
		}
		
		ComplexContent output = serviceResult.getOutput();
		List<Object> results = null;

		if (output == null) {
			throw new RuntimeException("No content returned for dataset: " + dataSet.getId());
		}
		
		boolean foundList = false;
		for (Element<?> child : TypeUtils.getAllChildren(output.getType())) {
			if (child.getType() instanceof ComplexType && child.getType().isList(child.getProperties())) {
				foundList = true;
				results = (List<Object>) output.get(child.getName());
				break;
			}
		}
		// presumably a gauge?
		if (!foundList) {
			results = Arrays.asList(new Object[] { output });
		}
		
		ResultSet resultSet = new ResultSet();
		resultSet.setResults(results);
		resultSet.setTimestamp(timestamp);
		if (dataSet.getLimit() != null || dataSet.getOffset() != null) {
			Object totalRowCount = output.get("totalRowCount");
			resultSet.setPage(Page.build(totalRowCount == null ? results.size() : ((Number) totalRowCount).longValue(), dataSet.getOffset(), dataSet.getLimit()));
		}
		return resultSet;
	}
	
	@WebResult(name = "sources")
	public List<DataSource> sources(@WebParam(name = "ids") List<String> entries) {
		List<DataSource> sources = new ArrayList<DataSource>();
		if (entries != null) {
			for (String id : entries) {
				if (id != null) {
					sources(EAIResourceRepository.getInstance().getEntry(id), sources);
				}
			}
		}
		return sources;
	}
	
	private void sources(Entry entry, List<DataSource> sources) {
		if (entry != null) {
			if (entry.isNode()) {
				if (Service.class.isAssignableFrom(entry.getNode().getArtifactClass())) {
					try {
						Set<DataSourceType> types = new HashSet<DataSourceType>();
						DefinedService service = (DefinedService) entry.getNode().getArtifact();
						List<ParameterDescription> output = null, input = null;
						boolean isNumbers = true, isAllSimple = true;
						for (Element<?> element : TypeUtils.getAllChildren(service.getServiceInterface().getOutputDefinition())) {
							if (!(element.getType() instanceof SimpleType) || !isNumeric(element)) {
								isNumbers = false;
							}
							if (!(element.getType() instanceof SimpleType)) {
								isAllSimple = false;
							}
							// we need a list result set with multiple fields
							if (element.getType() instanceof ComplexType && element.getType().isList(element.getProperties())) {
								boolean isSimple = true;
								List<Element<?>> allChildren = new ArrayList<Element<?>>(TypeUtils.getAllChildren((ComplexType) element.getType()));
								for (Element<?> child : allChildren) {
									if (!(child.getType() instanceof SimpleType)) {
										isSimple = false;
										break;
									}
								}
								if (isSimple) {
									// can always make a table out of it
									types.add(DataSourceType.TABULAR);
									// if there is only one numeric field or the second value is at least a numeric field (for the y-axis), we can make a line/bar out of it
									if (allChildren.size() == 1 && allChildren.get(0).getType() instanceof SimpleType && Number.class.isAssignableFrom(((SimpleType<?>) allChildren.get(0).getType()).getInstanceClass())) {
										types.add(DataSourceType.SERIES);
									}
									else if (allChildren.size() > 1 && allChildren.get(1).getType() instanceof SimpleType && Number.class.isAssignableFrom(((SimpleType<?>) allChildren.get(1).getType()).getInstanceClass())) {
										types.add(DataSourceType.SERIES);
									}
									if (allChildren.size() > 2 && allChildren.get(1).getType() instanceof SimpleType && Number.class.isAssignableFrom(((SimpleType<?>) allChildren.get(1).getType()).getInstanceClass())
											&& allChildren.get(2).getType() instanceof SimpleType && Number.class.isAssignableFrom(((SimpleType<?>) allChildren.get(2).getType()).getInstanceClass())) {
										types.add(DataSourceType.WATERFALL);
									}
									if (allChildren.size() >= 2 && allChildren.get(1).getType() instanceof SimpleType && Number.class.isAssignableFrom(((SimpleType<?>) allChildren.get(1).getType()).getInstanceClass())) {
										types.add(DataSourceType.GAUGE);
									}
								}
								// if we have a complex type but it does not result in a tabular or series, ignore this service
								// otherwise it can be harder to extract the result
								if (types.isEmpty()) {
									break;
								}
							}
							if (!types.isEmpty()) {
								output = Node.toParameters((ComplexType) element.getType());
								break;
							}
						}
						
						ComplexType inputDefinition = service.getServiceInterface().getInputDefinition();
						Element<?> limit = inputDefinition.get("limit");
						Element<?> offset = inputDefinition.get("offset");
						Element<?> parameters = inputDefinition.get("parameters");
						Element<?> orderBy = inputDefinition.get("orderBy");
						Element<?> connectionId = inputDefinition.get("connection");
						
						boolean paged = false;
						// if we have an offset field, a limit field we assume you have a paged service
						if (isNumeric(limit) && isNumeric(offset)) {
							paged = true;
							// if you have a non-list parameters field, we assume these are the input parameters, this matches the jdbc service spec
							if (parameters != null && parameters.getType() instanceof ComplexType && !parameters.getType().isList(parameters.getProperties())) {
								input = Node.toParameters((ComplexType) parameters.getType());
							}
						}
						// if not defined above, we take the entire input definition and remove limit & offset
						if (input == null) {
							input = Node.toParameters(inputDefinition);
							Iterator<ParameterDescription> iterator = input.iterator();
							while (iterator.hasNext()) {
								ParameterDescription parameter = iterator.next();
								if (parameter.getName().equals("offset") || parameter.getName().equals("limit") || parameter.getName().equals("connection") || parameter.getName().equals("orderBy")) {
									iterator.remove();
								}
							}
						}
						
						// if all the outputs are numbers, we can still have a gauge
						if (types.isEmpty() && isNumbers) {
							types.add(DataSourceType.GAUGE);
							output = Node.toParameters(service.getServiceInterface().getOutputDefinition());
						}
						if (isAllSimple && !types.contains(DataSourceType.FACT)) {
							types.add(DataSourceType.FACT);
							if (output == null) {
								output = Node.toParameters(service.getServiceInterface().getOutputDefinition());
							}
						}
						if (types.contains(DataSourceType.GAUGE) && !types.contains(DataSourceType.FACT)) {
							types.add(DataSourceType.FACT);
						}
						if (!types.isEmpty() && output != null && !output.isEmpty()) {
							DataSource source = new DataSource();
							source.setId(entry.getId());
							source.setTypes(new ArrayList<DataSourceType>(types));
							source.setInput(input);
							source.setOutput(output);
							source.setPaged(paged);
							source.setOrderable(orderBy != null);
							source.setDatabase(connectionId != null);
							sources.add(source);
						}
					}
					catch (Exception e) {
						logger.error("Could not load service: " + entry.getId(), e);
					}
				}
			}
			// recurse
			for (Entry child : entry) {
				sources(child, sources);
			}
		}
	}
	
	private static boolean isNumeric(Element<?> element) {
		return element != null && element.getType() instanceof SimpleType 
			&& (Number.class.isAssignableFrom(((SimpleType<?>) element.getType()).getInstanceClass())
					|| Date.class.isAssignableFrom(((SimpleType<?>) element.getType()).getInstanceClass()));
	}
}
