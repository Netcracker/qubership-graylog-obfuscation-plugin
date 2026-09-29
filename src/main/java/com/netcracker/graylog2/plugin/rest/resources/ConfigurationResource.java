package com.netcracker.graylog2.plugin.rest.resources;

import com.netcracker.graylog2.plugin.obfuscation.ObfuscationSystemException;
import com.netcracker.graylog2.plugin.obfuscation.configuration.Configuration;
import com.netcracker.graylog2.plugin.obfuscation.configuration.ConfigurationProvider;
import com.netcracker.graylog2.plugin.obfuscation.configuration.ConfigurationService;
import com.netcracker.graylog2.plugin.obfuscation.configuration.SynchronizationMode;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.graylog2.audit.jersey.NoAuditEvent;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.shared.rest.resources.RestResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Graylog url: https://{graylog-server-url}/api/plugins/com.netcracker.graylog2.plugin/{rest-api}
 */
@RequiresAuthentication
@Path("/obfuscation/configuration")
public class ConfigurationResource extends RestResource implements PluginRestResource {

  private static final Logger log = LoggerFactory.getLogger(ConfigurationResource.class);

  private final ConfigurationService configurationService;

  @Inject
  public ConfigurationResource(ConfigurationService configurationService) {
    this.configurationService = configurationService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getConfiguration() {
    Map<String, Object> serializedConfiguration = configurationService.getSerializedConfiguration();
    JSONObject jsonObject = new JSONObject(serializedConfiguration);

    return Response.ok(jsonObject.toString(4)).build();
  }

  @POST
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_JSON)
  @NoAuditEvent("I don't know what to write here")
  public Response installConfiguration(@NotNull String jsonConfiguration) {
    try {
      JSONObject jsonObject = new JSONObject(jsonConfiguration);
      Map<String, Object> configurationParameters = jsonObject.toMap();

      configurationService.installConfiguration(configurationParameters);
      return Response.ok("Success").build();
    } catch (JSONException exception) {
      log.error(
          "The input configuration json is invalid. "
              + "Reason: "
              + exception.getMessage()
              + ". "
              + "JSON=["
              + jsonConfiguration
              + "]",
          exception);
      return Response.serverError()
          .entity("Invalid json syntax. Reason: " + exception.getMessage())
          .build();
    } catch (ObfuscationSystemException exception) {
      log.error("The configuration is invalid. Reason: " + exception.getMessage(), exception);
      return Response.serverError()
          .entity("Invalid input configuration. Reason: " + exception.getMessage())
          .build();
    }
  }

  @PUT
  @Path("/sync")
  @Produces(MediaType.TEXT_PLAIN)
  @NoAuditEvent("I don't know what to write here")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response syncConfiguration(@FormParam("sync_mode") @NotNull SynchronizationMode syncMode) {
    try {
      configurationService.synchronizeConfiguration(syncMode);
      return Response.ok("Success").build();
    } catch (ObfuscationSystemException exception) {
      String exceptionName = exception.getClass().getSimpleName();
      log.error(
          "The configuration synchronization was failed with "
              + exceptionName
              + " exception. "
              + "Reason: "
              + exception.getMessage(),
          exception);
      return Response.serverError().entity(exception.getMessage()).build();
    }
  }

  @PUT
  @Path("/reset")
  @Produces(MediaType.TEXT_PLAIN)
  @NoAuditEvent("I don't know what to write here")
  public Response resetConfiguration() {
    try {
      configurationService.resetConfiguration();
      return Response.ok("Success").build();
    } catch (ObfuscationSystemException exception) {
      String exceptionName = exception.getClass().getSimpleName();
      log.error(
          "The reset configuration was failed with "
              + exceptionName
              + " exception. "
              + "Reason: "
              + exception.getMessage(),
          exception);
      return Response.serverError().entity(exception.getMessage()).build();
    }
  }

  @PUT
  @Path("/restore")
  @Produces(MediaType.TEXT_PLAIN)
  @NoAuditEvent("I don't know what to write here")
  public Response restoreConfiguration() {
    ConfigurationProvider configurationProvider = configurationService.getConfigurationProvider();
    Configuration currentConfiguration = configurationService.getCurrentConfiguration();
    try {
      configurationProvider.restoreConfiguration(currentConfiguration);
      return Response.ok("Success").build();
    } catch (ObfuscationSystemException exception) {
      String exceptionName = exception.getClass().getSimpleName();
      log.error(
          "The restore configuration was failed with "
              + exceptionName
              + " exception. "
              + "Reason: "
              + exception.getMessage(),
          exception);
      return Response.serverError().entity(exception.getMessage()).build();
    }
  }
}
