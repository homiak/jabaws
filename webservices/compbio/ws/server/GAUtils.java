package compbio.ws.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import compbio.engine.conf.PropertyHelperManager;
import compbio.stat.ga.AnalyticsConfigData;
import compbio.stat.ga.GoogleAnalyticsTracker;
import compbio.util.PropertyHelper;
import compbio.util.Util;
import compbio.ws.client.Services;

public class GAUtils {

	// MODIFY BEFORE RELEASE!
	final static String VERSION_TYPE = "WAR";

	private static final Logger log = Logger.getLogger(GAUtils.class);

	static PropertyHelper PROP_HELPER = PropertyHelperManager
			.getPropertyHelper();

	// compbio.dundee.ac.uk GA tracker
	private static AnalyticsConfigData config = new AnalyticsConfigData(
			"UA-5356328-1");

	private static GoogleAnalyticsTracker TRACKER = new GoogleAnalyticsTracker(
			config);

	static final boolean IS_GA_ENABLED = isGoogleAnalyticsEnabled();

	private static String SERVER_ADDRESS = getServerIP();

	private static boolean isGoogleAnalyticsEnabled() {
		String val = PROP_HELPER.getProperty("enable.ga");
		if (Util.isEmpty(val)) {
			return false;
		}
		val = val.trim();
		if ("yes".equalsIgnoreCase(val) || "true".equalsIgnoreCase(val)) {
			return true;
		}
		return false;
	}

	private static String getServerIP() {
		String IP = "127.0.0.1";
		if (!anonymizeIP()) {
			try {
				InetAddress localAddrIP = InetAddress.getLocalHost();
				IP = localAddrIP.getCanonicalHostName();
			} catch (UnknownHostException ignored) {
			}
		}
		return IP;
	}

	private static boolean anonymizeIP() {
		String val = PROP_HELPER.getProperty("anonymize.ip");
		if (Util.isEmpty(val)) {
			return false;
		}
		if ("yes".equalsIgnoreCase(val) || "true".equalsIgnoreCase(val)) {
			return true;
		}
		return false;
	}

	static void reportUsage(Services service) {
		String service_name = "UNKNOWN";
		if (service == null) {
			log.warn("GA: Services was NULL!");
		} else {
			service_name = service.toString();
		}
		TRACKER.trackPageViewFromReferrer("JABAWS/2.0/" + service_name,
				service_name, "http://www.compbio.dundee.ac.uk",
				SERVER_ADDRESS, VERSION_TYPE);
		TRACKER.resetSession();
	}
}
