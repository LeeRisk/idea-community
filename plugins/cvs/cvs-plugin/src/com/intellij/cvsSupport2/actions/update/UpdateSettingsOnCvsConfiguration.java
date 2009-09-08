package com.intellij.cvsSupport2.actions.update;

import com.intellij.cvsSupport2.config.CvsConfiguration;
import com.intellij.cvsSupport2.config.DateOrRevisionSettings;
import com.intellij.cvsSupport2.cvsoperations.dateOrRevision.RevisionOrDate;
import com.intellij.cvsSupport2.cvsoperations.dateOrRevision.RevisionOrDateImpl;
import com.intellij.cvsSupport2.keywordSubstitution.KeywordSubstitutionWrapper;
import org.netbeans.lib.cvsclient.command.KeywordSubstitution;

/**
 * author: lesya
 */
public class UpdateSettingsOnCvsConfiguration implements UpdateSettings{
  private final boolean myCleanCopy;
  private final CvsConfiguration myConfiguration;
  private final boolean myResetSticky;

  public UpdateSettingsOnCvsConfiguration(final boolean pruneEmptyDirectories, final int mergingMode, final String mergeWithBranchOneName,
                                          final String mergeWithBranchTwoName, final boolean createNewDirectories,
                                          final String updateKeyboardSubstitution, final DateOrRevisionSettings updateDateOrRevisionSettings,
                                          final boolean makeNewFilesReadonly, final boolean cleanCopy, final boolean resetSticky) {
    myCleanCopy = cleanCopy;
    myResetSticky = resetSticky;

    myConfiguration = new CvsConfiguration();
    myConfiguration.PRUNE_EMPTY_DIRECTORIES = pruneEmptyDirectories;
    myConfiguration.MERGING_MODE = mergingMode;
    myConfiguration.MERGE_WITH_BRANCH1_NAME = mergeWithBranchOneName;
    myConfiguration.MERGE_WITH_BRANCH2_NAME = mergeWithBranchTwoName;
    myConfiguration.CREATE_NEW_DIRECTORIES = createNewDirectories;
    myConfiguration.UPDATE_KEYWORD_SUBSTITUTION = updateKeyboardSubstitution;
    myConfiguration.UPDATE_DATE_OR_REVISION_SETTINGS = updateDateOrRevisionSettings;
    myConfiguration.MAKE_NEW_FILES_READONLY = makeNewFilesReadonly;
  }

  public UpdateSettingsOnCvsConfiguration(CvsConfiguration configuration, boolean cleanCopy, boolean resetSticky) {
    myCleanCopy = cleanCopy;
    myConfiguration = configuration;
    myResetSticky = resetSticky;
  }

  public boolean getPruneEmptyDirectories() {
    return myConfiguration.PRUNE_EMPTY_DIRECTORIES;
  }

  public String getBranch1ToMergeWith() {
    if (myConfiguration.MERGING_MODE == CvsConfiguration.DO_NOT_MERGE) return null;
    return myConfiguration.MERGE_WITH_BRANCH1_NAME;
  }

  public String getBranch2ToMergeWith() {
    if (myConfiguration.MERGING_MODE == CvsConfiguration.DO_NOT_MERGE) return null;
    if (myConfiguration.MERGING_MODE == CvsConfiguration.MERGE_WITH_BRANCH) return null;
    return myConfiguration.MERGE_WITH_BRANCH2_NAME;
  }

  public boolean getResetAllSticky() {
    return myResetSticky;
  }

  public boolean getDontMakeAnyChanges() {
    return false;
  }

  public boolean getCreateDirectories() {
    return myConfiguration.CREATE_NEW_DIRECTORIES;
  }

  public boolean getCleanCopy() {
    return myCleanCopy;
  }

  public KeywordSubstitution getKeywordSubstitution() {
    KeywordSubstitutionWrapper value = KeywordSubstitutionWrapper.getValue(myConfiguration.UPDATE_KEYWORD_SUBSTITUTION);
    if (value == null) return null;
    return value.getSubstitution();
  }

  public RevisionOrDate getRevisionOrDate() {
    return RevisionOrDateImpl.createOn(myConfiguration.UPDATE_DATE_OR_REVISION_SETTINGS);
  }

  public boolean getMakeNewFilesReadOnly() {
    return myConfiguration.MAKE_NEW_FILES_READONLY;
  }
}