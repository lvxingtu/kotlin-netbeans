/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package org.jetbrains.kotlin.refactorings.rename;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.Position;
import org.jetbrains.kotlin.descriptors.SourceElement;
import org.jetbrains.kotlin.highlighter.occurrences.OccurrencesUtils;
import org.jetbrains.kotlin.navigation.references.ReferenceUtils;
import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;

/**
 *
 * @author Alexander.Baratynski
 */
public class RenamePerformer {
    
    public static Map<FileObject, List<OffsetRange>> getRenameRefactoringMap(FileObject fo, PsiElement psi, String newName) {
        Map<FileObject, List<OffsetRange>> ranges = 
            new HashMap<FileObject, List<OffsetRange>>();
        KtElement ktElement = PsiTreeUtil.getNonStrictParentOfType(psi, KtElement.class);
        if (ktElement == null) {
            return ranges;
        }
        
        List<? extends SourceElement> sourceElements = ReferenceUtils.resolveToSourceDeclaration(ktElement);
        if (sourceElements.isEmpty()) {
            return ranges;
        }
        
        List<? extends SourceElement> searchingElements = OccurrencesUtils.getSearchingElements(sourceElements);
        Project project = ProjectUtils.getKotlinProjectForFileObject(fo);
        if (project == null) {
            return ranges;
        }
        
        for (KtFile file : ProjectUtils.getSourceFiles(project)) {
            List<OffsetRange> occurrencesRanges = OccurrencesUtils.search(searchingElements, file);
            File f = new File(file.getVirtualFile().getPath());
            FileObject fileObject = FileUtil.toFileObject(f);
            if (fileObject != null) {
                ranges.put(fileObject, occurrencesRanges);
            }
        }
     
        for (SourceElement searchElement : searchingElements) {
            if (!(searchElement instanceof KotlinSourceElement)) {
                continue;
            }
            KotlinSourceElement ktSourceElement = (KotlinSourceElement) searchElement;
            
        }
        
        return ranges;
    }
    
    public static List<PositionBounds> createPositionBoundsForFO(FileObject fo, List<OffsetRange> ranges) {
        List<PositionBounds> bounds = Lists.newArrayList();
        CloneableEditorSupport ces = GsfUtilities.findCloneableEditorSupport(fo);
        
        if (ces == null) {
            return bounds;
        }
        
        for (OffsetRange range : ranges) {
            PositionRef startRef = ces.createPositionRef(range.getStart(), Position.Bias.Forward);
            PositionRef endRef = ces.createPositionRef(range.getEnd(), Position.Bias.Forward);
            
            bounds.add(new PositionBounds(startRef, endRef));
        }
        
        return bounds;
    }
    
}