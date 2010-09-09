/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeansrcp.tasksource;

import org.netbeansrcp.tasksource.api.TaskSource;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author jiafan1
 */
@ServiceProvider(service=TaskSource.class)
public class TaskSourceImpl implements TaskSource {

    public Lookup getLookup() {
        Lookup l1 = WindowManager.getDefault().findTopComponent("TaskEditorTopComponent").getLookup();
        Lookup l2 = WindowManager.getDefault().findTopComponent("TaskDuplicatorTopComponent").getLookup();

        return new ProxyLookup(new Lookup[]{l1, l2});
    }

}
