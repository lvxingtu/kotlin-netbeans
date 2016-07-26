package org.black.kotlin.hints;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import java.util.ArrayList;
import java.util.List;
import org.black.kotlin.diagnostics.netbeans.parser.KotlinParser;
import org.black.kotlin.diagnostics.netbeans.parser.KotlinParser.KotlinError;
import org.black.kotlin.diagnostics.netbeans.parser.KotlinParser.KotlinParserResult;
import org.black.kotlin.resolve.AnalysisResultWithProvider;
import org.black.kotlin.resolve.lang.java.NetBeansJavaProjectElementUtils;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.diagnostics.Diagnostic;
import org.jetbrains.kotlin.resolve.AnalyzingUtils;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinHintsProvider implements HintsProvider{

    @Override
    public void computeHints(HintsManager hm, RuleContext rc, List<Hint> hints) {
        KotlinParserResult parserResult = (KotlinParserResult) rc.parserResult;
        FileObject file = parserResult.getSnapshot().getSource().getFileObject();
        
        List<? extends Error> errors = parserResult.getDiagnostics();
        for (Error error : errors) {
            if (error.toString().startsWith("UNRESOLVED_REFERENCE")) {
                
                PsiElement psi = ((KotlinError) error).getPsi();
                String simpleName = psi.getText();
                List<String> suggestions = NetBeansJavaProjectElementUtils.findFQName(
                        ProjectUtils.getKotlinProjectForFileObject(file), simpleName);
                List<HintFix> fixes = new ArrayList<HintFix>();
                for (String suggestion : suggestions) {
                    KotlinAutoImportFix fix = new KotlinAutoImportFix(suggestion);
                    fixes.add(fix);
                }
                
                Hint hint = new Hint(new KotlinAutoImportRule(), "Class not found", file, 
                    new OffsetRange(error.getStartPosition(), error.getEndPosition()), fixes, 10);
                hints.add(hint);
            }
        }
    }

    @Override
    public void computeSuggestions(HintsManager hm, RuleContext rc, List<Hint> list, int i) {
    }

    @Override
    public void computeSelectionHints(HintsManager hm, RuleContext rc, List<Hint> list, int i, int i1) {
    }

    @Override
    public void computeErrors(HintsManager hm, RuleContext rc, List<Hint> list, List<Error> errors) {
        KotlinParserResult parserResult = (KotlinParserResult) rc.parserResult;
        FileObject file = parserResult.getSnapshot().getSource().getFileObject();
        
        AnalysisResultWithProvider analysisResult = null;
        try {
            analysisResult = parserResult.getAnalysisResult();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (analysisResult != null) {
            for (Diagnostic diagnostic : analysisResult.getAnalysisResult().
                        getBindingContext().getDiagnostics().all()) {
                KotlinParser.KotlinError error = new KotlinParser.KotlinError(diagnostic, file);
                errors.add(error);
            }
            for (PsiErrorElement psiError : AnalyzingUtils.getSyntaxErrorRanges(parserResult.getKtFile())){
                KotlinParser.KotlinSyntaxError syntaxError = new KotlinParser.KotlinSyntaxError(psiError, file);
                errors.add(syntaxError);
            }
        }
    }

    @Override
    public void cancel() {
    }

    @Override
    public List<Rule> getBuiltinRules() {
        return Lists.newArrayList();
    }

    @Override
    public RuleContext createRuleContext() {
        return new KotlinRuleContext();
    }
    
}
