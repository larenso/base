package org.safehaus.subutai.core.environment.cli;


import org.safehaus.subutai.core.environment.api.EnvironmentManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by bahadyr on 6/21/14.
 */
@Command(scope = "environment", name = "destroy", description = "Command to destroy environment",
        detailedDescription = "Command to destroy environment by name")
public class DestroyEnvironmentCommand extends OsgiCommandSupport
{

    EnvironmentManager environmentManager;

    @Argument(name = "environmentName", index = 0, required = true, multiValued = false,
            description = "Environment name", valueToShowInHelp = "Environment name")
    String environmentName;


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        boolean destroyResult = environmentManager.destroyEnvironment( environmentName );
        if ( destroyResult )
        {
            System.out.println( "Environment destroyed successfully." );
        }
        else
        {
            System.out.println( "Environment destroy failed." );
        }
        return null;
    }


    public String getEnvironmentName()
    {
        return environmentName;
    }


    public void setEnvironmentName( final String environmentName )
    {
        this.environmentName = environmentName;
    }
}
