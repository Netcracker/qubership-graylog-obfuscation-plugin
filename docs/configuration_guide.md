# Graylog Obfuscation Plugin Configuration

At now the graylog obfuscation plugin can be configured only by REST endpoints.

* [REST-Endpoints](#Rest endpoints)
* [JSON-Configuration](#Configuration json)

## Rest endpoints

For all rest endpoints can be found the Postman collection with Environment.
OpenAPI specification for REST API of graylog-obfuscation-plugin you can find [here](api/openapi.yaml).

The graylog-obfuscation-plugin have a following endpoints:

1. Get Configuration
    Get current plugin configuration
    Method: GET
    URL: https://{graylog-server-url}/api/plugins/com.netcracker.graylog2.plugin/obfuscation/configuration
    Parameters: none

2. Install Configuration
    Install the custom configuration to plugin
    * Method: POST
    * URL: https://{graylog-server-url}/api/plugins/com.netcracker.graylog2.plugin/obfuscation/configuration
    * Headers: Content-Type=application/json
    * Parameters: body as configuration json

3. Sync Configuration
    Synchronized current with default configuration.
    The endpoint called in logging-deploy-service run
    * Method: PUT
    * URL: https://{graylog-server-url}/api/plugins/com.netcracker.graylog2.plugin/obfuscation/configuration/sync
    * Parameters: sync_mode={CREATE_ONLY|FORCE_UPDATE|SKIP}

4. Reset Configuration
    Perform reset configuration to default
    * Method: PUT
    * URL: https://{graylog-server-url}/api/plugins/com.netcracker.graylog2.plugin/obfuscation/configuration/reset
    * Parameters: none

5. Obfuscate Text
   * Method: POST
   * URL: https://{graylog-server-url}/api/plugins/com.netcracker.graylog2.plugin/obfuscation
   * Headers: Content-Type=text/plain
   * Parameters: body as text

## Configuration json

* "is-obfuscation-enabled": true|false (If parameter is true, then obfuscation for logs enabled)
* "text-replacer": 'Static Star Replacer' (The type of sensitive data replacement.
  Static Star Replacer is eight stars - ********)
* "stream-titles": [] (The titles of streams for which obfuscation was applied)
* "field-names": [] (The fields in log message which will be obfuscated)
* "white-regular-expressions": [] (The list of white regular expressions)
* "sensitive-regular-expressions": [] (The list of regular expressions for sensitive data search)

## Sensitive Regular Expression

* "id": 1 (The unique ID of regular expression)
* "name": 'Passport Number' (The readable name of regular expression)
* "pattern": '\\d{7}' (The regular expression pattern)
* "importance": 1 (If two regular expressions have interception place in text, that conflict will resolve by importance value)

## White Regular Expression

* "id": 1 (The unique ID of regular expression)
* "name": 'White Passport Number' (The readable name of regular expression)
* "pattern": '3867742' (The regular expression pattern)

