/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
import org.mule.api.annotation.jpms.PrivilegedApi;

@PrivilegedApi(
    privilegedPackages = {
        "org.foo"
    },
    privilegedArtifactIds = {
        "org.bar:some-extension"
    })
module org.foo.simple {
  
  requires org.mule.runtime.api.annotations;
  
  exports org.foo;
  
}