package com.intellij.application.options.editor;

import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.options.CompositeConfigurable;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class AutoImportOptionsConfigurable extends CompositeConfigurable<AutoImportOptionsProvider> implements EditorOptionsProvider {
  private JPanel myPanel;
  private JPanel myProvidersPanel;

  protected List<AutoImportOptionsProvider> createConfigurables() {
    return Arrays.asList(Extensions.getExtensions(AutoImportOptionsProvider.EP_NAME));
  }

  @Nls
  public String getDisplayName() {
    return ApplicationBundle.message("auto.import");
  }

  public Icon getIcon() {
    return null;
  }

  public String getHelpTopic() {
    return "reference.settingsdialog.IDE.editor.autoimport";
  }

  public JComponent createComponent() {
    myProvidersPanel.removeAll();
    for (AutoImportOptionsProvider provider : getConfigurables()) {
      myProvidersPanel.add(provider.createComponent(), new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1, 0,
                                                                     GridBagConstraints.NORTH,
                                                                     GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0,0));
    }
    myProvidersPanel.add(Box.createVerticalGlue(), new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1, 1,
                                                                     GridBagConstraints.NORTH,
                                                                     GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
    return myPanel;
  }

  public String getId() {
    return "editor.preferences.import";
  }

  public Runnable enableSearch(final String option) {
    return null;
  }
}