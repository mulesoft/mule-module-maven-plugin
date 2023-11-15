/*
 * Copyright © MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.bean;

public class ServiceDefinition {

  private String serviceInterface;
  private String serviceImplementation;

  ServiceDefinition(String string) {
    final String[] split = string.split(":");
    serviceInterface = split[0];
    serviceImplementation = split[1];
  }

  public String getServiceInterface() {
    return serviceInterface;
  }

  public void setServiceInterface(String serviceInterface) {
    this.serviceInterface = serviceInterface;
  }

  public String getServiceImplementation() {
    return serviceImplementation;
  }

  public void setServiceImplementation(String serviceImplementation) {
    this.serviceImplementation = serviceImplementation;
  }

  @Override
  public String toString() {
    return serviceInterface + ":" + serviceImplementation;
  }
}
