package com.netcracker.graylog2.plugin.rest.resources;

import com.google.common.collect.ImmutableMap;
import com.netcracker.graylog2.plugin.obfuscation.ObfuscationEngine;
import com.netcracker.graylog2.plugin.obfuscation.ObfuscationRequest;
import com.netcracker.graylog2.plugin.obfuscation.ObfuscationResponse;
import com.netcracker.graylog2.plugin.obfuscation.replace.TextReplacers;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.graylog2.audit.jersey.NoAuditEvent;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.shared.rest.resources.RestResource;
import org.json.JSONArray;
import org.json.JSONObject;

@RequiresAuthentication
@Path("/obfuscation")
public class ObfuscationResource extends RestResource implements PluginRestResource {

  private final ObfuscationEngine obfuscationEngine;

  @Inject
  public ObfuscationResource(ObfuscationEngine obfuscationEngine) {
    this.obfuscationEngine = obfuscationEngine;
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.TEXT_PLAIN)
  @NoAuditEvent("I don't know what to write here")
  public Response doObfuscation(@NotNull String text) {
    ObfuscationRequest obfuscationRequest = new ObfuscationRequest(text);
    ObfuscationResponse obfuscationResponse = obfuscationEngine.obfuscateText(obfuscationRequest);
    return Response.ok(obfuscationResponse).build();
  }

  @GET
  @Path("/replacers")
  @Produces(MediaType.APPLICATION_JSON)
  @NoAuditEvent("I don't know what to write here")
  public Response getReplacers() {
    return Response.ok(
            Collections.singletonMap(
                "text-replacers",
                TextReplacers.getAllTextReplacers().stream()
                    .map(
                        textReplacer ->
                            ImmutableMap.of(
                                "name", textReplacer.getName(),
                                "example", textReplacer.getExample()))
                    .collect(Collectors.toList())))
        .build();
  }

  @POST
  @Path("/regex/compile/test")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @NoAuditEvent("I don't know what to write here")
  public Response testCompileRegularExpressions(String json) {
    JSONArray expressions = new JSONObject(json).getJSONArray("expressions");
    return Response.ok(RegularExpressionCompileTester.testCompile(expressions)).build();
  }
}
