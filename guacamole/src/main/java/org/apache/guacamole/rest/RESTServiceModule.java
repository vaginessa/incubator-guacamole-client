/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.guacamole.rest;

import com.google.inject.Scopes;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.aopalliance.intercept.MethodInterceptor;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.apache.guacamole.rest.auth.TokenRESTService;
import org.apache.guacamole.rest.connection.ConnectionRESTService;
import org.apache.guacamole.rest.connectiongroup.ConnectionGroupRESTService;
import org.apache.guacamole.rest.activeconnection.ActiveConnectionRESTService;
import org.apache.guacamole.rest.auth.AuthTokenGenerator;
import org.apache.guacamole.rest.auth.AuthenticationService;
import org.apache.guacamole.rest.auth.SecureRandomAuthTokenGenerator;
import org.apache.guacamole.rest.auth.TokenSessionMap;
import org.apache.guacamole.rest.history.HistoryRESTService;
import org.apache.guacamole.rest.language.LanguageRESTService;
import org.apache.guacamole.rest.patch.PatchRESTService;
import org.apache.guacamole.rest.schema.SchemaRESTService;
import org.apache.guacamole.rest.tunnel.TunnelRESTService;
import org.apache.guacamole.rest.user.UserRESTService;

/**
 * A Guice Module to set up the servlet mappings and authentication-specific
 * dependency injection for the Guacamole REST API.
 *
 * @author James Muehlner
 * @author Michael Jumper
 */
public class RESTServiceModule extends ServletModule {

    /**
     * Singleton instance of TokenSessionMap.
     */
    private final TokenSessionMap tokenSessionMap;

    /**
     * Creates a module which handles binding of REST services and related
     * authentication objects, including the singleton TokenSessionMap.
     *
     * @param tokenSessionMap
     *     An instance of TokenSessionMap to inject as a singleton wherever
     *     needed.
     */
    public RESTServiceModule(TokenSessionMap tokenSessionMap) {
        this.tokenSessionMap = tokenSessionMap;
    }

    @Override
    protected void configureServlets() {

        // Bind session map
        bind(TokenSessionMap.class).toInstance(tokenSessionMap);

        // Bind low-level services
        bind(AuthenticationService.class);
        bind(AuthTokenGenerator.class).to(SecureRandomAuthTokenGenerator.class);

        // Automatically translate GuacamoleExceptions for REST methods
        MethodInterceptor interceptor = new RESTExceptionWrapper();
        requestInjection(interceptor);
        bindInterceptor(Matchers.any(), new RESTMethodMatcher(), interceptor);

        // Bind convenience services used by the REST API
        bind(ObjectRetrievalService.class);

        // Set up the API endpoints
        bind(ActiveConnectionRESTService.class);
        bind(ConnectionGroupRESTService.class);
        bind(ConnectionRESTService.class);
        bind(HistoryRESTService.class);
        bind(LanguageRESTService.class);
        bind(PatchRESTService.class);
        bind(SchemaRESTService.class);
        bind(TokenRESTService.class);
        bind(TunnelRESTService.class);
        bind(UserRESTService.class);

        // Set up the servlet and JSON mappings
        bind(GuiceContainer.class);
        bind(JacksonJsonProvider.class).in(Scopes.SINGLETON);
        serve("/api/*").with(GuiceContainer.class);

    }

}
