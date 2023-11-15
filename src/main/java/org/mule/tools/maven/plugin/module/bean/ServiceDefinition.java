/*
 * Copyright © MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.bean;

import java.util.List;

public class ServiceDefinition implements Comparable<ServiceDefinition> {

  private String serviceInterface;
  private List<String> serviceImplementations;

  public String getServiceInterface() {
    return serviceInterface;
  }

  public void setServiceInterface(String serviceInterface) {
    this.serviceInterface = serviceInterface;
  }

  public List<String> getServiceImplementations() {
    return serviceImplementations;
  }

  public void setServiceImplementations(List<String> serviceImplementations) {
    this.serviceImplementations = serviceImplementations;
  }

  @Override
  public int compareTo(ServiceDefinition o) {
    return this.getServiceInterface().compareTo(o.getServiceInterface());
  }
}
