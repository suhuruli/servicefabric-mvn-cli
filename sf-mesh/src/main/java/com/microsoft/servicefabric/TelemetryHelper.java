package com.microsoft.servicefabric;

import com.microsoft.applicationinsights.TelemetryClient;

import org.apache.maven.plugin.logging.Log;
import java.util.HashMap;
import java.util.Map;

public class TelemetryHelper {
	// private static final TelemetryClient client= new TelemetryClient();

	public static boolean sendEvent(TelemetryEventType type, String value, Log logger){
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(type.toString(), value);
        // client.trackEvent("MVNCLI" + type.toString(), properties, null);
        // client.flush();
		return true;
	}
}