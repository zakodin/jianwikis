/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.netbeansrcp.notnegativeidvaliator;

import com.netbeansrcp.taskidgenerator.api.IdValidator;
import org.openide.util.lookup.ServiceProvider;
/**
 *
 * @author jiafan1
 */
@ServiceProvider(service=IdValidator.class)
public class NotNegativeIdValidator implements IdValidator {

    @Override
    public boolean validate(String id) {
        System.out.println("NotNegativeIdValidator.validate(" + id + ")");
        return !id.startsWith("-");
    }

}
