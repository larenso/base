package org.safehaus.subutai.cli.template.manager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.template.manager.TemplateManager;

@Command(scope = "template", name = "setup", description = "checks enviroonment and setups master template")
public class CheckAndSetup extends OsgiCommandSupport {

    private TemplateManager templateManager;

    @Argument(index = 0, required = true)
    private String hostName;

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    protected Object doExecute() throws Exception {
        boolean b = templateManager.setup(hostName);
        if(b) System.out.println("Check and setup successfully completed");
        else System.out.println("Check and setup failed");
        return null;
    }

}