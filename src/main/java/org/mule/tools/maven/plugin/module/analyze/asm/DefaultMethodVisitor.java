/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.module.analyze.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;


/**
 * Computes the set of classes referenced by visited code.
 * Inspired by <code>org.objectweb.asm.depend.DependencyVisitor</code> in the ASM dependencies example.
 */
public class DefaultMethodVisitor
        extends MethodVisitor
{

    private final String packageName;
    private final AnnotationVisitor annotationVisitor;

    private final SignatureVisitor signatureVisitor;

    private final ResultCollector resultCollector;

    public DefaultMethodVisitor(String packageName, AnnotationVisitor annotationVisitor, SignatureVisitor signatureVisitor,
                                ResultCollector resultCollector)
    {
        super(Opcodes.ASM5);
        this.packageName = packageName;
        this.annotationVisitor = annotationVisitor;
        this.signatureVisitor = signatureVisitor;
        this.resultCollector = resultCollector;
    }

    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible)
    {
        if (visible)
        {
            resultCollector.addDesc(packageName, desc);

            return annotationVisitor;
        }
        else
        {
            return null;
        }
    }


    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible)
    {
        if (visible)
        {
            resultCollector.addDesc(packageName, desc);

            return annotationVisitor;
        }
        else
        {
            return null;
        }
    }

    public void visitTypeInsn(final int opcode, final String desc)
    {
        //if (desc.charAt(0) == '[')
        //{
        //    resultCollector.addDesc(packageName, desc);
        //}
        //else
        //{
        //    resultCollector.addName(packageName, desc);
        //}
    }

    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc)
    {
        //resultCollector.addName(packageName, owner);
        /*
         * NOTE: Merely accessing a field does not impose a direct dependency on its type. For example, the code line
         * <code>java.lang.Object var = bean.field;</code> does not directly depend on the type of the field. A direct
         * dependency is only introduced when the code explicitly references the field's type by means of a variable
         * declaration or a type check/cast. Those cases are handled by other visitor callbacks.
         */
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf)
    {
        //resultCollector.addName(packageName, owner);
    }

    public void visitLdcInsn(final Object cst)
    {
        //if (cst instanceof Type)
        //{
        //    resultCollector.addType(packageName, (Type) cst);
        //}
    }

    public void visitMultiANewArrayInsn(final String desc, final int dims)
    {
        //resultCollector.addDesc(packageName, desc);
    }

    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type)
    {
        //resultCollector.addName(packageName, type);
    }

    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start,
                                   final Label end, final int index)
    {
        //if (signature == null)
        //{
        //    resultCollector.addDesc(packageName, desc);
        //}
        //else
        //{
        //    addTypeSignature(signature);
        //}
    }


    private void addTypeSignature(final String signature)
    {
        if (signature != null)
        {
            new SignatureReader(signature).acceptType(signatureVisitor);
        }
    }
}
