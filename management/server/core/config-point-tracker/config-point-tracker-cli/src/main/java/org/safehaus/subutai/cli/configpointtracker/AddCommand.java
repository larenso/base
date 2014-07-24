package org.safehaus.subutai.cli.configpointtracker;

import org.safehaus.subutai.api.configpointtracker.ConfigPointTracker;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "config-point-tracker", name = "add" )
public class AddCommand extends OsgiCommandSupport {

    @Argument( index = 0, name = "templateName", required = true )
    private String templateName;

    @Argument( index = 1, name = "configPath", required = true )
    private String configPath;

    private ConfigPointTracker configPointTracker;


    public void setConfigPointTracker( ConfigPointTracker configPointTracker ) {
        this.configPointTracker = configPointTracker;
    }


    protected Object doExecute() {

        configPointTracker.add( templateName, configPath );

        return null;
    }
}