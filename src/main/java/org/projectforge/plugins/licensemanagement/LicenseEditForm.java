/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.licensemanagement;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRights;
import org.projectforge.web.common.MultiChoiceListHelper;
import org.projectforge.web.user.UsersComparator;
import org.projectforge.web.user.UsersProvider;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteMaxLengthTextField;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.InputPanel;

import com.vaynberg.wicket.select2.Select2MultiChoice;

/**
 * This is the edit formular page.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LicenseEditForm extends AbstractEditForm<LicenseDO, LicenseEditPage>
{
  private static final long serialVersionUID = -6208809585214296635L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LicenseEditForm.class);

  MultiChoiceListHelper<PFUserDO> assignOwnersListHelper;

  public LicenseEditForm(final LicenseEditPage parentPage, final LicenseDO data)
  {
    super(parentPage, data);
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newGrid16();
    {
      // Organization
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("organization"));
      final PFAutoCompleteMaxLengthTextField organizationField = new PFAutoCompleteMaxLengthTextField(InputPanel.WICKET_ID,
          new PropertyModel<String>(data, "organization")) {
        @Override
        protected List<String> getChoices(final String input)
        {
          return getBaseDao().getAutocompletion("organization", input);
        }
      };
      organizationField.withMatchContains(true).withMinChars(2).withFocus(true);
      WicketUtils.setStrong(organizationField);
      fs.add(organizationField);
    }
    {
      // Product
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.licensemanagement.product"));
      final PFAutoCompleteMaxLengthTextField productField = new PFAutoCompleteMaxLengthTextField(InputPanel.WICKET_ID,
          new PropertyModel<String>(data, "product")) {
        @Override
        protected List<String> getChoices(final String input)
        {
          return getBaseDao().getAutocompletion("product", input);
        }
      };
      productField.withMatchContains(true).withMinChars(2);
      productField.setRequired(true);
      WicketUtils.setStrong(productField);
      fs.add(productField);
    }
    {
      // Version
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.licensemanagement.version"));
      final RequiredMaxLengthTextField versionField = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data,
          "version"));
      WicketUtils.setStrong(versionField);
      fs.add(versionField);
    }
    {
      // UpdateFromVersion
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.licensemanagement.updateFromVersion"), true);
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "updateFromVersion")));
      fs.addHelpIcon(getString("plugins.licensemanagement.updateFromVersion.tooltip"));
    }
    {
      // Device
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.licensemanagement.device"), true);
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "device")));
      fs.addHelpIcon(getString("plugins.licensemanagement.device.tooltip"));
    }
    {
      // Number of license:
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.licensemanagement.numberOfLicenses"));
      final MinMaxNumberField<Integer> maxNumberField = new MinMaxNumberField<Integer>(InputPanel.WICKET_ID, new PropertyModel<Integer>(
          data, "numberOfLicenses"), 0, 999999);
      WicketUtils.setSize(maxNumberField, 6);
      fs.add(maxNumberField);
    }
    {
      // Owners
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.licensemanagement.owner"), true).setLabelSide(false);
      final UsersProvider usersProvider = new UsersProvider();
      assignOwnersListHelper = new MultiChoiceListHelper<PFUserDO>().setComparator(new UsersComparator()).setFullList(
          usersProvider.getSortedUsers());
      final Collection<PFUserDO> owners = ((LicenseDao) getBaseDao()).getSortedOwners(data);
      if (owners != null) {
        for (final PFUserDO owner : owners) {
          assignOwnersListHelper.addOriginalAssignedItem(owner).assignItem(owner);
        }
      }
      final Select2MultiChoice<PFUserDO> ownersChoice = new Select2MultiChoice<PFUserDO>(fs.getSelect2MultiChoiceId(),
          new PropertyModel<Collection<PFUserDO>>(this.assignOwnersListHelper, "assignedItems"), usersProvider);
      fs.add(ownersChoice);
    }
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    {
      // Valid since
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.licensemanagement.validSince"));
      final DatePanel validSinceDatePanel = new DatePanel(fs.newChildId(), new PropertyModel<Date>(data, "validSince"), DatePanelSettings
          .get().withTargetType(java.sql.Date.class).withSelectProperty("validSince"));
      fs.add(validSinceDatePanel);
    }
    gridBuilder.newColumnPanel(DivType.COL_40);
    {
      // Valid until
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.licensemanagement.validUntil"));
      final DatePanel validUntilDatePanel = new DatePanel(fs.newChildId(), new PropertyModel<Date>(data, "validUntil"), DatePanelSettings
          .get().withTargetType(java.sql.Date.class).withSelectProperty("validUntil"));
      fs.add(validUntilDatePanel);
    }
    gridBuilder.newColumnsPanel();
    {
      // License holder
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.licensemanagement.licenseHolder"), true);
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "licenseHolder")));
    }
    {
      // Text key
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.licensemanagement.key"), true);
      final LicenseManagementRight right = (LicenseManagementRight) UserRights.instance().getRight(LicenseDao.USER_RIGHT_ID);
      if (right.isLicenseKeyVisible(getUser(), data) == true) {
        fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "key"))).setAutogrow();
      } else {
        fs.add(new DivTextPanel(fs.newChildId(), getString("plugins.licensemanagement.key.notvisible")));
        fs.addHelpIcon(getString("plugins.licensemanagement.key.notvisible.tooltip"));
      }
    }
    {
      // Text comment
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("comment"));
      fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "comment"))).setAutogrow();
    }
    if (isNew() == false) {
      // Clone button for existing and not deleted invoices:
      final Button cloneButton = new Button(SingleButtonPanel.WICKET_ID, new Model<String>("clone")) {
        @Override
        public final void onSubmit()
        {
          parentPage.cloneLicense();
        }
      };
      cloneButton.setDefaultFormProcessing(false);
      final SingleButtonPanel cloneButtonPanel = new SingleButtonPanel(actionButtons.newChildId(), cloneButton, getString("clone")) {
        /**
         * @see org.apache.wicket.Component#isVisible()
         */
        @Override
        public boolean isVisible()
        {
          return isNew() == false;
        }
      };
      actionButtons.add(2, cloneButtonPanel);
    }
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
