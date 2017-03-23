package com.github.javaparser.ast.validator;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithTypeArguments;
import com.github.javaparser.ast.nodeTypes.NodeWithTypeParameters;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.validator.chunks.CommonValidators;
import com.github.javaparser.ast.validator.chunks.ModifierValidator;
import com.github.javaparser.ast.validator.chunks.NoBinaryIntegerLiteralsValidator;

/**
 * This validator validates according to Java 1.0 syntax rules.
 */
public class Java1_0Validator extends Validators {
    protected final Validator modifiersWithoutStrictfpAndDefault = new ModifierValidator(false, false);
    protected final Validator noAssertKeyword = new SimpleValidator<>(AssertStmt.class,
            n -> true,
            (n, reporter) -> reporter.report(n, "'assert' keyword is not supported.")
    );
    protected final Validator noInnerClasses = new SimpleValidator<>(ClassOrInterfaceDeclaration.class,
            n -> !n.isTopLevelType(),
            (n, reporter) -> reporter.report(n, "inner classes or interfaces are not supported.")
    );
    protected final Validator noReflection = new SimpleValidator<>(ClassExpr.class,
            n -> true,
            (n, reporter) -> reporter.report(n, "Reflection is not supported.")
    );
    protected final Validator noGenerics = new TreeVisitorValidator((node, reporter) -> {
        if (node instanceof NodeWithTypeArguments) {
            if (((NodeWithTypeArguments<? extends Node>) node).getTypeArguments().isPresent()) {
                reporter.report(node, "Generics are not supported.");
            }
        }
        if (node instanceof NodeWithTypeParameters) {
            if (((NodeWithTypeParameters<? extends Node>) node).getTypeParameters().isNonEmpty()) {
                reporter.report(node, "Generics are not supported.");
            }
        }
    });
    protected final SingleNodeTypeValidator<TryStmt> tryWithoutResources = new SingleNodeTypeValidator<>(TryStmt.class, (n, reporter) -> {
        if (n.getCatchClauses().isEmpty() && !n.getFinallyBlock().isPresent()) {
            reporter.report(n, "Try has no finally and no catch.");
        }
        if (n.getResources().isNonEmpty()) {
            reporter.report(n, "Catch with resource is not supported.");
        }
    });
    protected final Validator noAnnotations = new TreeVisitorValidator((node, reporter) -> {
        if (node instanceof AnnotationExpr || node instanceof AnnotationDeclaration) {
            reporter.report(node, "Annotations are not supported.");
        }
    });
    protected final Validator noEnums = new SimpleValidator<>(EnumDeclaration.class,
            n -> true,
            (n, reporter) -> reporter.report(n, "Enumerations are not supported.")
    );
    protected final Validator noVarargs = new SimpleValidator<>(Parameter.class,
            Parameter::isVarArgs,
            (n, reporter) -> reporter.report(n, "Varargs are not supported.")
    );
    protected final Validator noForEach = new SimpleValidator<>(ForeachStmt.class,
            n -> true,
            (n, reporter) -> reporter.report(n, "For-each loops are not supported.")
    );
    protected final Validator noStaticImports = new SimpleValidator<>(ImportDeclaration.class,
            ImportDeclaration::isStatic,
            (n, reporter) -> reporter.report(n, "Static imports are not supported.")
    );
    protected final Validator noStringsInSwitch = new SimpleValidator<>(SwitchEntryStmt.class,
            n -> n.getLabel().map(l -> l instanceof StringLiteralExpr).orElse(false),
            (n, reporter) -> reporter.report(n.getLabel().get(), "Strings in switch statements are not supported.")
    );
    protected final Validator noBinaryIntegerLiterals = new NoBinaryIntegerLiteralsValidator();


    public Java1_0Validator() {
        super(new CommonValidators());
        add(modifiersWithoutStrictfpAndDefault);
        add(noAssertKeyword);
        add(noInnerClasses);
        add(noReflection);
        add(noGenerics);
        add(tryWithoutResources);
        add(noAnnotations);
        add(noEnums);
        add(noVarargs);
        add(noForEach);
        add(noStaticImports);
        add(noStringsInSwitch);
        add(noBinaryIntegerLiterals);
        // TODO validate "no underscores in numeric literals"
        // TODO validate "no multi-catch"
        // TODO validate "no lambdas"
        // TODO validate "no modules"
        // TODO validate "no default interface methods"
        // TODO validate "no private interface methods"
    }
}
