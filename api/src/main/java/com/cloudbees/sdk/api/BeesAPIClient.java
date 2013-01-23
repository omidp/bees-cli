/*
 * Copyright 2010-2011, CloudBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudbees.sdk.api;

import com.cloudbees.api.BeesClientBase;
import com.cloudbees.api.BeesClientConfiguration;
import com.cloudbees.api.BeesClientException;
import com.cloudbees.api.ErrorResponse;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import java.util.HashMap;
import java.util.Map;

public class BeesAPIClient extends BeesClientBase {
    public BeesAPIClient(BeesClientConfiguration beesClientConfiguration) {
        super(beesClientConfiguration);
    }

    public BeesAPIClient(String serverApiUrl, String apiKey, String secret, String format, String version) {
        super(serverApiUrl, apiKey, secret, format, version);
    }

    public AccountKeysResponse accountKeys(String domain, String user, String password) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("user", user);
        params.put("password", password);
        if (domain != null) params.put("domain", domain);
        String url = getRequestURL("account.keys", params);
        String response = executeRequest(url);
        AccountKeysResponse apiResponse =
            (AccountKeysResponse)readResponse(response);
        return apiResponse;
    }

    public AccountListResponse accountList() throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        String url = getRequestURL("account.list", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        AccountListResponse apiResponse =
            (AccountListResponse)readResponse(response);
        return apiResponse;
    }

    public String call(String action, Map<String, String> params) throws Exception
    {
        String url = getRequestURL(action, params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        return response;
    }

    public void mainCall(String[] args) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        int argIndex = 0;
        if (argIndex < args.length) {
            String action = args[argIndex++];
            for (; argIndex < args.length; argIndex++) {
                String arg = args[argIndex];
                String[] pair = arg.split("=", 2);
                if (pair.length < 2)
                    throw new BeesAPIClient.UsageError("Marlformed call parameter pair: " +
                        arg);
                params.put(pair[0], pair[1]);
            }
            String response = call(action, params);
            System.out.println(response);
        } else
            throw new BeesAPIClient.UsageError("Missing required action argument");
    }

    protected XStream getXStream() throws Exception {
        XStream xstream;
        if (format.equals("json")) {
            xstream = new XStream(new JettisonMappedXmlDriver()) {
                protected MapperWrapper wrapMapper(MapperWrapper next) {
                    return new MapperWrapper(next) {
                        public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                            return definedIn != Object.class ? super.shouldSerializeMember(definedIn, fieldName) : false;
                        }

                    };
                }
            };
        } else if (format.equals("xml")) {
            xstream = new XStream() {
                protected MapperWrapper wrapMapper(MapperWrapper next) {
                    return new MapperWrapper(next) {
                        public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                            return definedIn != Object.class ? super.shouldSerializeMember(definedIn, fieldName) : false;
                        }

                    };
                }
            };
        } else {
            throw new Exception("Unknown format: " + format);
        }

        xstream.processAnnotations(ErrorResponse.class);
        xstream.processAnnotations(AccountKeysResponse.class);
        xstream.processAnnotations(AccountInfo.class);
        xstream.processAnnotations(AccountListResponse.class);

        // BeesAPIClient can be subtyped to offer more commands,
        // yet those may live in separate classloader.
        // use this.getClass().getClassLoader() to ensure
        // that all the request/response classes resolve.
        xstream.setClassLoader(getClass().getClassLoader());

        return xstream;
    }

    protected Object readResponse(String response) throws Exception
    {
        Object obj = getXStream().fromXML(response);
        if (obj instanceof ErrorResponse) {
            throw new BeesClientException((ErrorResponse)obj);
        }
        return obj;
    }

   public static class UsageError extends Exception
    {
        UsageError(String reason)
        {
            super(reason);
        }
    }

}