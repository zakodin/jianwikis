/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.netbeansrcp.taskmodel;

import com.netbeansrcp.taskmodel.api.Task;
import com.netbeansrcp.taskmodel.api.TaskManager;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jiafan1
 */
@ServiceProvider(service=TaskManager.class)
public class TaskManagerImpl implements TaskManager {

    private List<Task> topLevelTasks;
    private PropertyChangeSupport pss;

    public TaskManagerImpl(){
        this.topLevelTasks = new ArrayList<Task>();
        this.pss = new PropertyChangeSupport(this);
    }

    @Override
    public synchronized Task createTask() {
        Task task = new TaskImpl();
        this.topLevelTasks.add(task);
        this.pss.firePropertyChange(PROP_TASKLIST_ADD, null, task);

        return task;
    }

    @Override
    public synchronized Task createTask(String name, String parentId) {
        Task task = new TaskImpl(name, parentId);
        Task parent = this.getTask(parentId);
        if (null != parent)
            parent.addChild(task);
        this.pss.firePropertyChange(PROP_TASKLIST_ADD, parent, task);

        return task;
    }

    @Override
    public synchronized void removeTask(String id) {
        Task task = this.getTask(id);
        if (null != task) {
        }
        Task parent = this.getTask(task.getParentId());
        if (null != parent) {
            parent.remove(task);
        }
        this.topLevelTasks.remove(task);
        this.pss.firePropertyChange(PROP_TASKLIST_REMOVE,
                parent, task);
    }

    @Override
    public List<Task> getTopLevelTasks() {
        return Collections.unmodifiableList(this.topLevelTasks);
    }

    @Override
    public Task getTask(String id) {
        for (Task task : this.topLevelTasks) {
            Task found = this.findTask(task, id);
            if(null != found)
                return found;
        }

        return null;
    }

    private Task findTask(Task task, String id){
        if(id.equals(task.getId()))
            return task;
        for(Task child: task.getChildren()){
            Task found = this.findTask(child, id);
            if(null != found)
                return found;
        }

        return null;
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pss.addPropertyChangeListener(listener);
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pss.removePropertyChangeListener(listener);
    }

}
