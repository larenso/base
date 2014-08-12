package org.safehaus.subutai.ui.elasticsearch;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.elasticsearch.Elasticsearch;
import org.safehaus.subutai.api.elasticsearch.Config;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.shared.protocol.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ElasticsearchUI implements PortalModule {

	public static final String MODULE_IMAGE = "logo.jpeg";

	private static Elasticsearch elasticsearchManager;
	private static AgentManager agentManager;
	private static CommandRunner commandRunner;
	private static Tracker tracker;
	private static ExecutorService executor;

	public ElasticsearchUI( AgentManager agentManager, Elasticsearch elasticsearchManager, Tracker tracker,
                            CommandRunner commandRunner ) {
		ElasticsearchUI.elasticsearchManager = elasticsearchManager;
		ElasticsearchUI.agentManager = agentManager;
		ElasticsearchUI.tracker = tracker;
		ElasticsearchUI.commandRunner = commandRunner;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public static Elasticsearch getElasticsearchManager() {
		return elasticsearchManager;
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
        elasticsearchManager = null;
		agentManager = null;
		tracker = null;
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
		return FileUtil.getFile( ElasticsearchUI.MODULE_IMAGE, this);
	}

	public Component createComponent() {
		return new ElasticsearchForm();
	}

}