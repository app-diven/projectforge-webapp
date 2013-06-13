/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.rest.converter;

import java.util.Locale;
import java.util.TimeZone;

import org.hibernate.Hibernate;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.Configuration;
import org.projectforge.registry.Registry;
import org.projectforge.rest.objects.UserObject;
import org.projectforge.user.PFUserDO;

/**
 * For conversion of PFUserDO to user object.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class PFUserDOConverter
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PFUserDOConverter.class);

  public static UserObject getUserObject(PFUserDO userDO)
  {
    if (userDO == null) {
      return null;
    }
    if (Hibernate.isInitialized(userDO) == false) {
      final Integer userId = userDO.getId();
      userDO = Registry.instance().getUserGroupCache().getUser(userDO.getId());
      if (userDO == null) {
        log.error("Oups, user with id '" + userId + "' not found.");
        return null;
      }
    }
    final UserObject user = new UserObject();
    DOConverter.copyFields(user, userDO);
    user.setUsername(userDO.getUsername());
    user.setFirstName(userDO.getFirstname());
    user.setLastName(userDO.getLastname());
    user.setEmail(userDO.getEmail());
    TimeZone timeZone = userDO.getTimeZoneObject();
    if (timeZone == null) {
      timeZone = Configuration.getInstance().getDefaultTimeZone();
    }
    if (timeZone != null) {
      user.setTimeZone(timeZone.getID());
    }
    Locale locale = userDO.getLocale();
    if (locale == null) {
      locale = ConfigXml.getInstance().getDefaultLocale();
    }
    if (locale == null) {
      locale = Locale.getDefault();
    }
    if (locale != null) {
      user.setLocale(locale.toString());
    }
    return user;
  }
}
