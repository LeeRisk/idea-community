package com.intellij.openapi.vcs.changes.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListEditHandler;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vcs.changes.issueLinks.IssueLinkRenderer;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

/**
 * @author yole
 */
public class ChangeListChooserPanel extends JPanel {
  private JPanel myPanel;
  private JRadioButton myRbExisting;
  private JRadioButton myRbNew;
  private JComboBox myExisitingsCombo;
  private EditChangelistPanel myNewListPanel;
  @Nullable private final ChangeListEditHandler myHandler;
  private final Consumer<Boolean> myOkEnabledListener;

  public ChangeListChooserPanel(@Nullable final ChangeListEditHandler handler, @NotNull final Consumer<Boolean> okEnabledListener) {
    super(new BorderLayout());
    myHandler = handler;
    myOkEnabledListener = okEnabledListener;
    add(myPanel, BorderLayout.CENTER);

    myRbExisting.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        updateEnabledItems();
      }
    });
  }

  public void init(final Project project) {

    myExisitingsCombo.setRenderer(new ColoredListCellRenderer() {

      private final IssueLinkRenderer myLinkRenderer = new IssueLinkRenderer(project, this);
      protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if (value instanceof LocalChangeList) {
          myLinkRenderer.appendTextWithLinks(((LocalChangeList)value).getName(),
                                             ((LocalChangeList)value).isDefault() ? SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES : SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
      }
    });
    myNewListPanel.init(project, null);
  }

  public void setChangeLists(Collection<? extends ChangeList> changeLists) {
    final DefaultComboBoxModel model = new DefaultComboBoxModel();
    for (ChangeList list : changeLists) {
      model.addElement(list);
    }

    myExisitingsCombo.setModel(model);
  }

  public void setDefaultName(String name) {
    myNewListPanel.setName(name);
  }

  private void updateEnabledItems() {
    if (myRbExisting.isSelected()) {
      myExisitingsCombo.setEnabled(true);
      myNewListPanel.setEnabled(false);
      myExisitingsCombo.requestFocus();
    }
    else {
      myExisitingsCombo.setEnabled(false);
      myNewListPanel.setEnabled(true);
      myNewListPanel.requestFocus();
    }
  }

  @Nullable
  public LocalChangeList getSelectedList(Project project) {
    if (myRbNew.isSelected()) {
      String newText = myNewListPanel.getName();
      if (ChangeListManager.getInstance(project).findChangeList(newText) != null) {
        Messages.showErrorDialog(project,
                                 VcsBundle.message("changes.newchangelist.warning.already.exists.text", newText),
                                 VcsBundle.message("changes.newchangelist.warning.already.exists.title"));
        return null;
      }
    }

    if (myRbExisting.isSelected()) {
      return (LocalChangeList)myExisitingsCombo.getSelectedItem();
    }
    else {
      LocalChangeList changeList =
        ChangeListManager.getInstance(project).addChangeList(myNewListPanel.getName(), myNewListPanel.getDescription());
      myNewListPanel.changelistCreatedOrChanged(changeList);
      return changeList;
    }
  }

  public void setDefaultSelection(final ChangeList defaultSelection) {
    if (defaultSelection == null) {
      myExisitingsCombo.setSelectedIndex(0);
    }
    else {
      myExisitingsCombo.setSelectedItem(defaultSelection);
    }


    if (defaultSelection != null) {
      myRbExisting.setSelected(true);
    }
    else {
      myRbNew.setSelected(true);
    }

    updateEnabledItems();
  }

  public JComponent getPreferredFocusedComponent() {
    return myRbExisting.isSelected() ? myExisitingsCombo : myNewListPanel.getPrefferedFocusedComponent();
  }

  private void createUIComponents() {
    myNewListPanel = new EditChangelistPanel(myHandler) {

      @Override
      protected void nameChanged(String errorMessage) {
        myOkEnabledListener.consume(errorMessage == null);
      }
    };
  }
}