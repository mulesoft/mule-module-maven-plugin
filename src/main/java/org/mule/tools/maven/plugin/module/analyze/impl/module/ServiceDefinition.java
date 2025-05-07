/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.impl.module;

import java.util.List;
import java.util.Objects;

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

  @Override
  public int hashCode() {
    return Objects.hash(serviceImplementations, serviceInterface);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ServiceDefinition other = (ServiceDefinition) obj;
    return Objects.equals(serviceImplementations, other.serviceImplementations)
        && Objects.equals(serviceInterface, other.serviceInterface);
  }

  @Override
  public String toString() {
    return this.getClass().getName() + "{" + serviceInterface + ": " + serviceImplementations.toString() + "}";
  }

}
