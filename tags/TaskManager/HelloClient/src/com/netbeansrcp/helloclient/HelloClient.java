/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.netbeansrcp.helloclient;

import com.netbeansrcp.helloservice.HelloService;

/**
 *
 * @author jiafan1
 */
public class HelloClient {

    public static void main(String[] args) {
        new HelloService().hello("NetBeans");
    }
}
