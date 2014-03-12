/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl;

import org.safehaus.kiskis.mgmt.api.SomeApi;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;

import java.util.List;

/**
 *
 * @author bahadyr
 */
public class SomeImpl implements SomeApi {

    private DbManager dbManager;

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public String sayHello(String name) {
        return name;
    }

    @Override
    public boolean install(String program) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean start(String serviceName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean stop(String serviceName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean status(String serviceName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean purge(String serviceName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean runCommand(String program) {
        return true;
    }

    @Override
    public void writeLog(String log) {
        SomeDAO someDAO = new SomeDAO(dbManager);
        someDAO.writeLog(log);
    }

    @Override
    public List<String> getLogs() {
        SomeDAO someDAO = new SomeDAO(dbManager);
        return someDAO.getLogs();
    }

}
