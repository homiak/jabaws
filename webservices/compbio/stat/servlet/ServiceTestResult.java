package compbio.stat.servlet;

import compbio.ws.client.Services;

/**
 * Value class for test results. Two ServiceTestResult are considered equals if
 * their services are equals.
 * 
 * @author pvtroshin
 * 
 */
public class ServiceTestResult {

	final Services service;
	boolean failed;
	String details;

	public ServiceTestResult(Services service) {
		this.service = service;
	}

	@Override
	public String toString() {
		return "ServiceTestResult [service=" + service + ", failed=" + failed
				+ ", details=" + details.toString() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((service == null) ? 0 : service.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceTestResult other = (ServiceTestResult) obj;
		if (service != other.service)
			return false;
		return true;
	}

	public Services getService() {
		return service;
	}

	public boolean getStatus() {
		return failed;
	}

	public String getDetails() {
		return details;
	}

}
