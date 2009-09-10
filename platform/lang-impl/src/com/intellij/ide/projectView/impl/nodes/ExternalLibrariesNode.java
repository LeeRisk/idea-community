/*
 * @author max
 */
package com.intellij.ide.projectView.impl.nodes;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.util.Icons;
import com.intellij.util.PathUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ExternalLibrariesNode extends ProjectViewNode<String> {
  public ExternalLibrariesNode(Project project, ViewSettings viewSettings) {
    super(project, "External Libraries", viewSettings);
  }

  @Override
  public boolean contains(@NotNull VirtualFile file) {
    return someChildContainsFile(file);
  }

  @NotNull
  @Override
  public Collection<? extends AbstractTreeNode> getChildren() {
    final List<AbstractTreeNode> children = new ArrayList<AbstractTreeNode>();
    ProjectFileIndex fileIndex = ProjectRootManager.getInstance(getProject()).getFileIndex();
    Module[] modules = ModuleManager.getInstance(getProject()).getModules();
    Set<Library> processedLibraries = new THashSet<Library>();
    Set<Sdk> processedSdk = new THashSet<Sdk>();

    for (Module module : modules) {
      final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
      final OrderEntry[] orderEntries = moduleRootManager.getOrderEntries();
      for (final OrderEntry orderEntry : orderEntries) {
        if (orderEntry instanceof LibraryOrderEntry) {
          final LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry)orderEntry;
          final Library library = libraryOrderEntry.getLibrary();
          if (library == null) continue;
          if (processedLibraries.contains(library)) continue;
          processedLibraries.add(library);

          if (!hasExternalEntries(fileIndex, libraryOrderEntry)) continue;

          final String libraryName = library.getName();
          if (libraryName == null || libraryName.length() == 0) {
            addLibraryChildren(orderEntry, children, getProject(), this);
          }
          else {
            children.add(new NamedLibraryElementNode(getProject(), new NamedLibraryElement(null, orderEntry), getSettings()));
          }
        }
        else if (orderEntry instanceof JdkOrderEntry) {
          final Sdk jdk = ((JdkOrderEntry)orderEntry).getJdk();
          if (jdk != null) {
            if (processedSdk.contains(jdk)) continue;
            processedSdk.add(jdk);
            children.add(new NamedLibraryElementNode(getProject(), new NamedLibraryElement(null, orderEntry), getSettings()));
          }
        }
      }
    }
    return children;
  }

  public static void addLibraryChildren(final OrderEntry entry, final List<AbstractTreeNode> children, Project project, ProjectViewNode node) {
    final PsiManager psiManager = PsiManager.getInstance(project);
    final VirtualFile[] files = entry.getFiles(OrderRootType.CLASSES);
    for (final VirtualFile file : files) {
      final PsiDirectory psiDir = psiManager.findDirectory(file);
      if (psiDir == null) {
        continue;
      }
      children.add(new PsiDirectoryNode(project, psiDir, node.getSettings()));
    }
  }

  private static boolean hasExternalEntries(ProjectFileIndex index, OrderEntry orderEntry) {
    VirtualFile[] files = orderEntry.getFiles(OrderRootType.CLASSES);
    for (VirtualFile file : files) {
      if (!index.isInContent(PathUtil.getLocalFile(file))) return true;
    }

    return false;
  }

  @Override
  protected void update(PresentationData presentation) {
    presentation.setPresentableText(IdeBundle.message("node.projectview.external.libraries"));
    presentation.setIcons(Icons.LIBRARY_ICON);
  }
}