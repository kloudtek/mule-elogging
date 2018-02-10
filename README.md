# Mule Enterprise Logging

The object of mule-elogging is to provide a powerful logging framework for mule ESB in order to significantly facilitate
troubleshooting and identifying problems areas by:

1) logging payload and all metadata at all inbound/outbound integration points
2) adding a transaction id to track transactions end-to-end
3) Measure how long each outbound operation took, as well how long the whole inbound operation took to complete

Here's an example of an inbound API call being logged:

```json
{
  "loggerName": "mulepayload",
  "loggerFqcn": "org.apache.logging.log4j.spi.AbstractLogger",
  "threadName": "[logtest-1.0.0-SNAPSHOT].api-httpListenerConfig.worker.01",
  "level": "INFO",
  "type": "inbound",
  "message": "inbound mule message",
  "mule.request.content": "{NullPayload}",
  "mule.request.encoding": "UTF-8",
  "mule.request.mimeType": "*/*",
  "mule.request.payloadClass": "org.mule.transport.NullPayload",
  "mule.request.inboundProperties.http.request.uri": "/api/ping",
  "mule.request.inboundProperties.http.query.string": "",
  "mule.request.inboundProperties.http.query.params": "ParameterMap{[]}",
  "mule.request.inboundProperties.http.listener.path": "/api/*",
  "mule.request.inboundProperties.http.remote.address": "/127.0.0.1:51328",
  "mule.request.inboundProperties.http.uri.params": "ParameterMap{[]}",
  "mule.request.inboundProperties.mule_tx_id": "6affe84e-dcff-4eaa-8018-e40a0c8d312a",
  "mule.request.inboundProperties.accept": "*/*",
  "mule.request.inboundProperties.host": "localhost:8081",
  "mule.request.inboundProperties.http.version": "HTTP/1.1",
  "mule.request.inboundProperties.http.method": "GET",
  "mule.request.inboundProperties.http.relative.path": "/api/ping",
  "mule.request.inboundProperties.http.request.path": "/api/ping",
  "mule.request.inboundProperties.http.scheme": "http",
  "mule.request.inboundProperties.user-agent": "curl/7.54.0",
  "mule.request.outboundProperties.mule_tx_id": "6affe84e-dcff-4eaa-8018-e40a0c8d312a",
  "mule.response.content": "{\"status\":\"200\",\"message\":\"Alive\",\"apiVersion\":\"v1\",\"appVersion\":\"1.0.0\",\"build\":\"example\"}",
  "mule.response.encoding": "UTF-8",
  "mule.response.mimeType": "application/json",
  "mule.response.payloadClass": "java.lang.String",
  "mule.response.inboundProperties.http.request.uri": "/api/ping",
  "mule.response.inboundProperties.http.query.string": "",
  "mule.response.inboundProperties.http.query.params": "ParameterMap{[]}",
  "mule.response.inboundProperties.http.listener.path": "/api/*",
  "mule.response.inboundProperties.http.remote.address": "/127.0.0.1:51328",
  "mule.response.inboundProperties.http.uri.params": "ParameterMap{[]}",
  "mule.response.inboundProperties.mule_tx_id": "6affe84e-dcff-4eaa-8018-e40a0c8d312a",
  "mule.response.inboundProperties.accept": "*/*",
  "mule.response.inboundProperties.host": "localhost:8081",
  "mule.response.inboundProperties.http.version": "HTTP/1.1",
  "mule.response.inboundProperties.http.method": "GET",
  "mule.response.inboundProperties.http.relative.path": "/api/ping",
  "mule.response.inboundProperties.http.request.path": "/api/ping",
  "mule.response.inboundProperties.http.scheme": "http",
  "mule.response.inboundProperties.user-agent": "curl/7.54.0",
  "mule.response.outboundProperties.http.status": "200",
  "mule.response.outboundProperties.Content-Type": "application/json",
  "mule.response.outboundProperties.mule_tx_id": "6affe84e-dcff-4eaa-8018-e40a0c8d312a",
  "mule.response.flowVars._ApikitResponseTransformer_contractMimeTypes": "[application/json]",
  "mule.response.flowVars._ApikitResponseTransformer_AcceptedHeaders": "*/*",
  "mule.response.flowVars._ApikitResponseTransformer_bestMatchRepresentation": "application/json",
  "mule.response.flowVars._ApikitResponseTransformer_apikitRouterRequest": "yes",
  "messageSourceUri": "http://localhost:8081/api/ping",
  "messageSourceName": "http://localhost:8081/api/ping",
  "duration": 72,
  "flowName": "api-main",
  "flowFileName": "api.xml",
  "flowFileLine": "10",
  "tcMap": {
    "mule_tx_id": "6affe84e-dcff-4eaa-8018-e40a0c8d312a"
  },
  "timestamp": "2018-02-08T18:39:49.978Z"
}
```

ELogging is divided into two components:

The first one is a JSON logger library so that all information can be logged as structured data that can be easily read by
a log shipping software like logstash.

The second one is a custom controller that you can add to your application to log 

# Installation 

## log4j library:

You will need to download the [log4j library](http://central.maven.org/maven2/com/kloudtek/mule/elogging/mule-elogging-log4j2/0.9.10/mule-elogging-log4j2-0.9.10.jar),
and add it to your mule runtime in the directory `lib/boot`

For beta connectors you can download the source code and build it with devkit to find it available on your local repository. Then you can add it to Studio

For released connectors you can download them from the update site in Anypoint Studio. 
Open Anypoint Studio, go to Help → Install New Software and select Anypoint Connectors Update Site where you’ll find all available connectors.

#Usage

Mule ELogging will provide you a two components: Log Inbound and Log Outbound

# Reporting Issues

We use GitHub:Issues for tracking issues with this connector. You can report new issues at this link http://github.com/Kloudtek/mule-elogging/issues.
