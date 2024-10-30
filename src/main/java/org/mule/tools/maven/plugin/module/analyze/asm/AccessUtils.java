/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.module.analyze.asm;

import org.objectweb.asm.Opcodes;

public class AccessUtils {

  private AccessUtils() {}

  public static boolean isProtected(int access) {
    return (access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED;
  }

  public static boolean isPublic(int access) {
    return (access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC;
  }

  public static boolean isPrivate(int access) {
    return (access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE;
  }

  public static boolean isFinal(int access) {
    return (access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL;
  }

  public static boolean isPackage(int access) {
    return !isProtected(access) && !isPublic(access) && !isPrivate(access);
  }
}
