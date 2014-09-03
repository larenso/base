/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.lucene.ui;

import com.vaadin.ui.Component;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.lucene.api.*;
import org.safehaus.subutai.server.ui.api.PortalModule;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class LuceneUI implements PortalModule {

	public static final String MODULE_IMAGE = "lucene.png";

	private static Lucene luceneManager;
	private static AgentManager agentManager;
	private static Tracker tracker;
	private static Hadoop hadoopManager;
	private static CommandRunner commandRunner;
	private static ExecutorService executor;

	public LuceneUI(AgentManager agentManager, Tracker tracker, Hadoop hadoopManager, Lucene luceneManager, CommandRunner commandRunner) {
		LuceneUI.agentManager = agentManager;
		LuceneUI.tracker = tracker;
		LuceneUI.hadoopManager = hadoopManager;
		LuceneUI.luceneManager = luceneManager;
		LuceneUI.commandRunner = commandRunner;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public static Lucene getLuceneManager() {
		return luceneManager;
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
		luceneManager = null;
		agentManager = null;
		hadoopManager = null;
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
		return FileUtil.getFile( LuceneUI.MODULE_IMAGE, this );
	}


	public Component createComponent() {
		return new LuceneForm();
	}

}