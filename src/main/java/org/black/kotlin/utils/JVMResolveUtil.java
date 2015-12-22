/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.black.kotlin.utils;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport;
import org.jetbrains.kotlin.cli.jvm.compiler.JvmPackagePartProvider;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.context.ModuleContext;
import org.jetbrains.kotlin.descriptors.PackagePartProvider;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.AnalyzingUtils;
import org.jetbrains.kotlin.resolve.BindingTrace;
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM;

import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author polina
 */
public class JVMResolveUtil {

    public static String TEST_MODULE_NAME = "java-integration-test";
    @NotNull
    public static AnalysisResult analyzeOneFileWithJavaIntegrationAndCheckForErrors(@NotNull KtFile file) {
        return analyzeOneFileWithJavaIntegrationAndCheckForErrors(file, PackagePartProvider.Companion.getEMPTY());
    }

    @NotNull
    public static AnalysisResult analyzeOneFileWithJavaIntegrationAndCheckForErrors(@NotNull KtFile file, @NotNull PackagePartProvider provider) {
        AnalyzingUtils.checkForSyntacticErrors(file);

        AnalysisResult analysisResult = analyzeOneFileWithJavaIntegration(file, provider);

        AnalyzingUtils.throwExceptionOnErrors(analysisResult.getBindingContext());

        return analysisResult;
    }

    @NotNull
    public static AnalysisResult analyzeOneFileWithJavaIntegration(@NotNull KtFile file,  @NotNull KotlinCoreEnvironment environment) {
        return analyzeOneFileWithJavaIntegration(file, new JvmPackagePartProvider(environment));
    }

    @NotNull
    public static AnalysisResult analyzeOneFileWithJavaIntegration(@NotNull KtFile file,  @NotNull PackagePartProvider provider) {
        return analyzeFilesWithJavaIntegration(file.getProject(), Collections.singleton(file), provider);
    }

    @NotNull
    public static AnalysisResult analyzeOneFileWithJavaIntegration(@NotNull KtFile file) {
        return analyzeOneFileWithJavaIntegration(file, PackagePartProvider.Companion.getEMPTY());
    }

    @NotNull
    public static AnalysisResult analyzeFilesWithJavaIntegrationAndCheckForErrors(
            @NotNull Project project,
            @NotNull Collection<KtFile> files
    ) {
        return analyzeFilesWithJavaIntegrationAndCheckForErrors(project, files, PackagePartProvider.Companion.getEMPTY());
    }

    @NotNull
    public static AnalysisResult analyzeFilesWithJavaIntegrationAndCheckForErrors(
            @NotNull Project project,
            @NotNull Collection<KtFile> files,
            @NotNull PackagePartProvider packagePartProvider
    ) {
        for (KtFile file : files) {
            AnalyzingUtils.checkForSyntacticErrors(file);
        }

        AnalysisResult analysisResult = analyzeFilesWithJavaIntegration(project, files, packagePartProvider);

        AnalyzingUtils.throwExceptionOnErrors(analysisResult.getBindingContext());

        return analysisResult;
    }

    @NotNull
    public static AnalysisResult analyzeFilesWithJavaIntegration(
            @NotNull Project project,
            @NotNull Collection<KtFile> files,
            @NotNull KotlinCoreEnvironment environment
    ) {
        return analyzeFilesWithJavaIntegration(project, files, new JvmPackagePartProvider(environment));
    }

    @NotNull
    public static AnalysisResult analyzeFilesWithJavaIntegration(
            @NotNull Project project,
            @NotNull Collection<KtFile> files,
            @NotNull PackagePartProvider packagePartProvider
    ) {

        ModuleContext moduleContext = TopDownAnalyzerFacadeForJVM.createContextWithSealedModule(project, TEST_MODULE_NAME);

        BindingTrace trace = new CliLightClassGenerationSupport.CliBindingTrace();

        return TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegrationWithCustomContext(moduleContext, files, trace, null, null,
                                                                                            packagePartProvider);
    }
}