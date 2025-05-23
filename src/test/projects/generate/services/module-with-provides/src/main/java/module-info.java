/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
module org.bar.service {
  
  requires org.foo.service;
  
  provides org.foo.A
    with org.bar.B;
  
}