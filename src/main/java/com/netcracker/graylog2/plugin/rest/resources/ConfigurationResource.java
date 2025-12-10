package com.netcracker.graylog2.plugin.rest.resources;

import com.netcracker.graylog2.plugin.obfuscation.ObfuscationSystemException;
import com.netcracker.graylog2.plugin.obfuscation.configuration.Configuration;
import com.netcracker.graylog2.plugin.obfuscation.configuration.ConfigurationProvider;
import com.netcracker.graylog2.plugin.obfuscation.configuration.ConfigurationService;
import com.netcracker.graylog2.plugin.obfuscation.configuration.SynchronizationMode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.graylog2.audit.jersey.NoAuditEvent;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.shared.rest.resources.RestResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Graylog url: https://{graylog-server-url}/api/plugins/com.netcracker.graylog2.plugin/{rest-api}
 */
@RequiresAuthentication
@Path("/obfuscation/configuration")
@Api(value = "Obfuscation Configuration Plugin API")
public class ConfigurationResource extends RestResource implements PluginRestResource {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationResource.class);

    private final ConfigurationService configurationService;

    @Inject
    public ConfigurationResource(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get current configuration")
    public Response getConfiguration() {
        Map<String, Object> serializedConfiguration = configurationService.getSerializedConfiguration();
        JSONObject jsonObject = new JSONObject(serializedConfiguration);

        return Response.ok(jsonObject.toString(4)).build();
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @NoAuditEvent("I don't know what to write here")
    @ApiOperation(value = "Install new configuration")
    public Response installConfiguration(@NotNull String jsonConfiguration) {
        try {
            JSONObject jsonObject = new JSONObject(jsonConfiguration);
            Map<String, Object> configurationParameters = jsonObject.toMap();

            configurationService.installConfiguration(configurationParameters);
            return Response.ok("Success").build();
        } catch (JSONException exception) {
            log.error("The input configuration json is invalid. " +
                      "Reason: " + exception.getMessage() + ". " +
                      "JSON=[" + jsonConfiguration + "]", exception);
            return Response.serverError().entity("Invalid json syntax. Reason: " + exception.getMessage()).build();
        } catch (ObfuscationSystemException exception) {
            log.error("The configuration is invalid. Reason: " + exception.getMessage(), exception);
            return Response.serverError().entity("Invalid input configuration. Reason: " + exception.getMessage()).build();
        }
    }

    @PUT
    @Path("/sync")
    @Produces(MediaType.TEXT_PLAIN)
    @NoAuditEvent("I don't know what to write here")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiOperation(value = "Synchronized configuration")
    public Response syncConfiguration(@ApiParam(name = "sync_mode",
            value = "The mode which used to sync configuration",
            required = true) @FormParam("sync_mode") @NotNull SynchronizationMode syncMode) {
        try {
            configurationService.synchronizeConfiguration(syncMode);
            return Response.ok("Success").build();
        } catch (ObfuscationSystemException exception) {
            String exceptionName = exception.getClass().getSimpleName();
            log.error("The configuration synchronization was failed with " + exceptionName + " exception. " +
                      "Reason: " + exception.getMessage(), exception);
            return Response.serverError().entity(exception.getMessage()).build();
        }
    }

    @PUT
    @Path("/reset")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Reset configuration")
    @NoAuditEvent("I don't know what to write here")
    public Response resetConfiguration() {
        try {
            configurationService.resetConfiguration();
            return Response.ok("Success").build();
        } catch (ObfuscationSystemException exception) {
            String exceptionName = exception.getClass().getSimpleName();
            log.error("The reset configuration was failed with " + exceptionName + " exception. " +
                      "Reason: " + exception.getMessage(), exception);
            return Response.serverError().entity(exception.getMessage()).build();
        }
    }

    @PUT
    @Path("/restore")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Restore configuration")
    @NoAuditEvent("I don't know what to write here")
    public Response restoreConfiguration() {
        ConfigurationProvider configurationProvider = configurationService.getConfigurationProvider();
        Configuration currentConfiguration = configurationService.getCurrentConfiguration();
        try {
            configurationProvider.restoreConfiguration(currentConfiguration);
            return Response.ok("Success").build();
        } catch (ObfuscationSystemException exception) {
            String exceptionName = exception.getClass().getSimpleName();
            log.error("The restore configuration was failed with " + exceptionName + " exception. " +
                      "Reason: " + exception.getMessage(), exception);
            return Response.serverError().entity(exception.getMessage()).build();
        }
    }
}

