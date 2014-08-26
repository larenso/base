/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.solr.api;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


public class SolrClusterConfig implements ConfigBase {

    public static final String PRODUCT_KEY = "Solr2";
    public static final String PRODUCT_NAME = "solr";
    private String templateName = PRODUCT_NAME;
    private String clusterName = "";
    private int numberOfNodes = 1;
    private Set<Agent> nodes = new HashSet<>();


    public String getClusterName() {
        return clusterName;
    }


    public SolrClusterConfig setClusterName( String clusterName ) {
        this.clusterName = clusterName;
        return this;
    }


    public String getTemplateName() {
        return templateName;
    }


    public void setTemplateName( final String templateName ) {
        this.templateName = templateName;
    }


    public void setNumberOfNodes( final int numberOfNodes ) {
        this.numberOfNodes = numberOfNodes;
    }


    @Override
    public String getProductName() {
        return PRODUCT_KEY;
    }


    public int getNumberOfNodes() {
        return numberOfNodes;
    }


    public Set<Agent> getNodes() {
        return nodes;
    }


    public void setNodes( final Set<Agent> nodes ) {
        this.nodes = nodes;
    }


    @Override
    public String toString() {
        return new ToStringBuilder( this, ToStringStyle.SHORT_PREFIX_STYLE ).append( "clusterName", clusterName )
                                                                            .append( "numberOfNodes", numberOfNodes )
                                                                            .append( "nodes", nodes ).toString();
    }
}