/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.netbeansrcp.taskmodel.api;

import java.beans.PropertyChangeListener;
import java.util.List;

/**
 *
 * @author jiafan1
 */
public interface TaskManager {
    Task createTask();

    Task createTask(String name, String parentId);

    void removeTask(String id);

    List<Task> getTopLevelTasks();

    Task getTask(String id);

    static final String PROP_TASKLIST_ADD = "TASK_LIST_ADD";
    static final String PROP_TASKLIST_REMOVE = "TASK_LIST_REMOVE";

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);
}
