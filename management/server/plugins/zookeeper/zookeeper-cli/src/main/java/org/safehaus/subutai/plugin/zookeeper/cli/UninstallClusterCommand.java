package org.safehaus.subutai.plugin.zookeeper.cli;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;

import java.util.UUID;


/**
 * Displays the last log entries
 */
@Command(scope = "zookeeper", name = "uninstall-cluster", description = "Command to uninstall Zookeeper cluster")
public class UninstallClusterCommand extends OsgiCommandSupport {

    private Zookeeper zookeeperManager;
    private Tracker tracker;

    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void setZookeeperManager(Zookeeper zookeeperManager) {
        this.zookeeperManager = zookeeperManager;
    }

    public Zookeeper getZookeeperManager() {
        return zookeeperManager;
    }


    @Argument(index = 0, name = "clusterName", description = "The name of the cluster.", required = true, multiValued = false)
    String clusterName = null;

    protected Object doExecute() {
        UUID uuid = zookeeperManager.uninstallCluster(clusterName);
        int logSize = 0;
        while (!Thread.interrupted()) {
            ProductOperationView po = tracker.getProductOperation( ZookeeperClusterConfig.PRODUCT_KEY, uuid);
            if (po != null) {
                if (logSize != po.getLog().length()) {
                    System.out.print(po.getLog().substring(logSize, po.getLog().length()));
                    System.out.flush();
                    logSize = po.getLog().length();
                }
                if (po.getState() != ProductOperationState.RUNNING) {
                    break;
                }
            } else {
                System.out.println("Product operation not found. Check logs");
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                break;
            }
        }
        return null;
    }
}