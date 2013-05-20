/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// task community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// task community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with task program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.rest.converter;

import org.projectforge.rest.objects.TaskObject;
import org.projectforge.task.TaskDO;

/**
 * For conversion of TaskDO to task object.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TaskDOConverter
{
  public static TaskObject getTaskObject(final TaskDO taskDO)
  {
    final TaskObject task = new TaskObject();
    DOConverter.copyFields(task, taskDO);
    task.setParentTaskId(taskDO.getParentTaskId());
    task.setDescription(taskDO.getDescription());
    task.setReference(taskDO.getReference());
    task.setTitle(taskDO.getTitle());
    task.setShortDescription(taskDO.getShortDescription());
    task.setMaxHours(taskDO.getMaxHours());
    task.setPriority(taskDO.getPriority());
    task.setStatus(taskDO.getStatus());
    return task;
  }
}
