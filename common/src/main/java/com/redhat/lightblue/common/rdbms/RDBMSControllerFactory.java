/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.redhat.lightblue.common.rdbms;

import com.redhat.lightblue.common.rdbms.RDBMSDataSourceMap;
import com.redhat.lightblue.config.ControllerConfiguration;
import com.redhat.lightblue.config.ControllerFactory;
import com.redhat.lightblue.config.DataSourcesConfiguration;
import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.rdbms.RDBMSCRUDController;

/**
 *
 * @author lcestari
 */
public class RDBMSControllerFactory implements ControllerFactory {

    @Override
    public CRUDController createController(ControllerConfiguration cfg, DataSourcesConfiguration ds) {
            RDBMSDataSourceMap rds = new RDBMSDataSourceMap(ds);
            return new RDBMSCRUDController(rds);
    }
}