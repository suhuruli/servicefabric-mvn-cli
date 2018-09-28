package com.microsoft.servicefabric;

import com.microsoft.applicationinsights.TelemetryClient;

import org.apache.maven.plugin.logging.Log;
import java.util.HashMap;
import java.util.Map;

public class TelemetryHelper {
	private static final TelemetryClient client= new TelemetryClient();

	public static boolean sendEvent(TelemetryEventType type, String value, Log logger){
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(type.toString(), value);
		try{
            client.trackEvent("MVNCLI" + type.toString(), properties, null);
            client.flush();
		}
        finally{
            try {
                // This sleep is to ensure that the telemetry event is sent before the mvn goal completes as App Insights behaves in asynchronus way.
                // Github issue https://github.com/Microsoft/ApplicationInsights-Java/issues/416 
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                logger.error("Failed sending telemetry event");
            }
        }
		return true;
	}
}