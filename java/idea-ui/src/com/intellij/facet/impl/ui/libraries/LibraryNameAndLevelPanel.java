/*
 * Copyright 2000-2010 JetBrains s.r.o.
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
 */
package com.intellij.facet.impl.ui.libraries;

import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainer;
import com.intellij.ui.EnumComboBoxModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Avdeev
 */
class LibraryNameAndLevelPanel {
  private JTextField myLibraryNameField;
  private JComboBox myLevelComboBox;
  private JPanel myPanel;
  private JLabel myLevelLabel;

  LibraryNameAndLevelPanel(String libraryName, @Nullable LibrariesContainer.LibraryLevel level) {
    if (level != null) {
      final Map<LibrariesContainer.LibraryLevel, String> levels = new HashMap<LibrariesContainer.LibraryLevel, String>();
      levels.put(LibrariesContainer.LibraryLevel.GLOBAL, ProjectBundle.message("combobox.item.global.library"));
      levels.put(LibrariesContainer.LibraryLevel.PROJECT, ProjectBundle.message("combobox.item.project.library"));
      levels.put(LibrariesContainer.LibraryLevel.MODULE, ProjectBundle.message("combobox.item.module.library"));
      myLevelComboBox.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          final Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
          if (value instanceof LibrariesContainer.LibraryLevel) {
            final LibrariesContainer.LibraryLevel level = (LibrariesContainer.LibraryLevel)value;
            setText(levels.get(level));
          }
          return component;
        }
      });
      myLevelComboBox.setModel(new EnumComboBoxModel<LibrariesContainer.LibraryLevel>(LibrariesContainer.LibraryLevel.class));
      myLevelComboBox.setSelectedItem(level);
    }
    else {
      myLevelLabel.setVisible(false);
      myLevelComboBox.setVisible(false);
    }
    myLibraryNameField.setText(libraryName);
  }

  public String getLibraryName() {
    return myLibraryNameField.getText();
  }

  public LibrariesContainer.LibraryLevel getLibraryLevel() {
    return (LibrariesContainer.LibraryLevel)myLevelComboBox.getSelectedItem();
  }

  public JPanel getPanel() {
    return myPanel;
  }
}
