/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.openapi.roots.ui.configuration;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectJdksModel;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Computable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: anna
 * Date: 05-Jun-2006
 */
public class ProjectJdkConfigurable implements UnnamedConfigurable {
  private JdkComboBox myCbProjectJdk;
  private JPanel myJdkPanel;
  private final Project myProject;
  private final ProjectJdksModel myJdksModel;
  private final SdkModel.Listener myListener = new SdkModel.Listener() {
    public void sdkAdded(Sdk sdk) {
      reloadModel();
    }

    public void beforeSdkRemove(Sdk sdk) {
      reloadModel();
    }

    public void sdkChanged(Sdk sdk, String previousName) {
      reloadModel();
    }

    public void sdkHomeSelected(Sdk sdk, String newSdkHome) {
      reloadModel();
    }
  };

  private boolean myFreeze = false;

  public ProjectJdkConfigurable(Project project, final ProjectJdksModel jdksModel) {
    myProject = project;
    myJdksModel = jdksModel;
    myJdksModel.addListener(myListener);
    init();
  }

  @Nullable
  public Sdk getSelectedProjectJdk() {
    return myJdksModel.findSdk(myCbProjectJdk.getSelectedJdk());
  }

  public JComponent createComponent() {
    return myJdkPanel;
  }

  private void reloadModel() {
    myFreeze = true;
    final Sdk projectJdk = myJdksModel.getProjectJdk();
    myCbProjectJdk.reloadModel(new JdkComboBox.NoneJdkComboBoxItem(), myProject);
    final String sdkName = projectJdk == null ? ProjectRootManager.getInstance(myProject).getProjectJdkName() : projectJdk.getName();
    if (sdkName != null) {
      final Sdk jdk = myJdksModel.findSdk(sdkName);
      if (jdk != null) {
        myCbProjectJdk.setSelectedJdk(jdk);
      } else {
        myCbProjectJdk.setInvalidJdk(sdkName);
        clearCaches(null);
      }
    } else {
      myCbProjectJdk.setSelectedJdk(null);
    }
    myFreeze = false;
  }

  private void init() {
    myJdkPanel = new JPanel(new GridBagLayout());
    myCbProjectJdk = new JdkComboBox(myJdksModel);
    myCbProjectJdk.insertItemAt(new JdkComboBox.NoneJdkComboBoxItem(), 0);
    myCbProjectJdk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (myFreeze) return;
        final Sdk oldJdk = myJdksModel.getProjectJdk();
        myJdksModel.setProjectJdk(myCbProjectJdk.getSelectedJdk());
        clearCaches(oldJdk);
      }
    });
    myJdkPanel.add(new JLabel(ProjectBundle.message("module.libraries.target.jdk.project.radio")), new GridBagConstraints(0, 0, 3, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 4, 0), 0, 0));
    myJdkPanel.add(myCbProjectJdk, new GridBagConstraints(0, 1, 1, 1, 0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0));
    final JButton setUpButton = myCbProjectJdk.createSetupButton(myProject, myJdksModel, new JdkComboBox.NoneJdkComboBoxItem());
    myJdkPanel.add(setUpButton, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0));
    myCbProjectJdk.appendEditButton(myProject, myJdkPanel, new GridBagConstraints(GridBagConstraints.RELATIVE, 1, 1, 1, 1.0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0), new Computable<Sdk>() {
      @Nullable
      public Sdk compute() {
        return myJdksModel.getProjectJdk();
      }
    });
  }

  private void clearCaches(final Sdk oldJdk) {
    final ModuleStructureConfigurable rootConfigurable = ModuleStructureConfigurable.getInstance(myProject);
    Module[] modules = rootConfigurable.getModules();
    for (Module module : modules) {
      rootConfigurable.getContext().clearCaches(module, oldJdk, getSelectedProjectJdk());
    }
  }

  public boolean isModified() {
    final Sdk projectJdk = ProjectRootManager.getInstance(myProject).getProjectJdk();
    return !Comparing.equal(projectJdk, getSelectedProjectJdk());
  }

  public void apply() throws ConfigurationException {
    ProjectRootManager.getInstance(myProject).setProjectJdk(getSelectedProjectJdk());
  }

  public void reset() {
    reloadModel();

    final String sdkName = ProjectRootManager.getInstance(myProject).getProjectJdkName();
    if (sdkName != null) {
      final Sdk jdk = myJdksModel.findSdk(sdkName);
      if (jdk != null) {
        myCbProjectJdk.setSelectedJdk(jdk);
      } else {
        myCbProjectJdk.setInvalidJdk(sdkName);
      }
    } else {
      myCbProjectJdk.setSelectedJdk(null);
    }
  }

  public void disposeUIResources() {
    myJdksModel.removeListener(myListener);
    myJdkPanel = null;
    myCbProjectJdk = null;
  }

}