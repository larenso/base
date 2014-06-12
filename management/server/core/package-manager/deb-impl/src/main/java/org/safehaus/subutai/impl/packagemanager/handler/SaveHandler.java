package org.safehaus.subutai.impl.packagemanager.handler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.impl.packagemanager.DebPackageManager;
import org.safehaus.subutai.shared.protocol.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveHandler extends AbstractHandler<Collection<PackageInfo>> {

    public SaveHandler(DebPackageManager pm, String hostname) {
        super(pm, hostname);
    }

    @Override
    public Collection<PackageInfo> performAction() {
        Agent a = getAgent();
        if(a != null && checkTargetLocation(a)) {
            RequestBuilder rb = new RequestBuilder(
                    "dpkg -l > " + packageManager.getFilename())
                    .withCwd(packageManager.getLocation());
            Command cmd = packageManager.getCommandRunner().createCommand(rb,
                    new HashSet<>(Arrays.asList(a)));
            packageManager.getCommandRunner().runCommand(cmd);
            if(cmd.hasSucceeded()) {
                ListHandler lh = new ListHandler(packageManager, hostname);
                lh.setFromFile(true);
                return lh.performAction();
            }
        }
        return null;
    }

    @Override
    Logger getLogger() {
        return LoggerFactory.getLogger(SaveHandler.class);
    }

    private boolean checkTargetLocation(Agent a) {
        // check if target directory exists
        AgentResult res = runCommand(a, "[ -d " + packageManager.getLocation() + " ]");
        if(equalsZero(res.getExitCode())) {
            // check if target directory is writable
            res = runCommand(a, "[ -w " + packageManager.getLocation() + " ]");
            if(!equalsZero(res.getExitCode())) {
                getLogger().error("Target directory is not writable");
                return false;
            }
        } else {

            getLogger().warn("Target directory does not exist. Trying to create...");

            res = runCommand(a, "mkdir " + packageManager.getLocation());
            if(!equalsZero(res.getExitCode())) {
                getLogger().error("Target location can not be created: "
                        + res.getStdErr());
                return false;
            }
        }
        return true;
    }

    private AgentResult runCommand(Agent a, String cmd) {
        Command c = packageManager.getCommandRunner().createCommand(
                new RequestBuilder(cmd), new HashSet<>(Arrays.asList(a)));
        packageManager.getCommandRunner().runCommand(c);
        AgentResult res = c.getResults().get(a.getUuid());
        return res;
    }

    private boolean equalsZero(Integer i) {
        return i != null && i == 0;
    }

}
