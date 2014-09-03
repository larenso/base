package org.safehaus.subutai.plugin.pig.ui;

import com.vaadin.ui.Component;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.pig.api.Config;
import org.safehaus.subutai.plugin.pig.api.Pig;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.server.ui.api.PortalModule;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PigUI implements PortalModule {

	public static final String MODULE_IMAGE = "pig.png";

	private static Pig pigManager;
	private static AgentManager agentManager;
	private static Tracker tracker;
	private static Hadoop hadoopManager;
	private static CommandRunner commandRunner;
	private static ExecutorService executor;

	public PigUI(AgentManager agentManager, Tracker tracker, Hadoop hadoopManager, Pig pigManager, CommandRunner commandRunner) {
		PigUI.agentManager = agentManager;
		PigUI.tracker = tracker;
		PigUI.hadoopManager = hadoopManager;
		PigUI.pigManager = pigManager;
		PigUI.commandRunner = commandRunner;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public static Pig getPigManager() {
		return pigManager;
	}

	public static Hadoop getHadoopManager() {
		return hadoopManager;
	}

	public static ExecutorService getExecutor() {
		return executor;
	}

	public static AgentManager getAgentManager() {
		return agentManager;
	}

	public static CommandRunner getCommandRunner() {
		return commandRunner;
	}

	public void init() {
		executor = Executors.newCachedThreadPool();
	}

	public void destroy() {
		pigManager = null;
		agentManager = null;
		hadoopManager = null;
		tracker = null;
		commandRunner = null;
		executor.shutdown();
	}

	@Override
	public String getId() {
		return Config.PRODUCT_KEY;
	}

	public String getName() {
		return Config.PRODUCT_KEY;
	}

	@Override
	public File getImage() {
		return FileUtil.getFile( PigUI.MODULE_IMAGE, this );
	}

	public Component createComponent() {
		return new PigForm();
	}

}