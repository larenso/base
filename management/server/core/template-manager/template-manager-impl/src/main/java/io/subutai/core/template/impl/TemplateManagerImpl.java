package io.subutai.core.template.impl;


import java.util.Set;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.protocol.Template;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.core.template.api.TemplateManager;


public class TemplateManagerImpl implements TemplateManager
{
    private static final Logger LOG = LoggerFactory.getLogger( TemplateManagerImpl.class.getName() );
    private static final String GORJUN_LIST_TEMPLATES_URL = "http://localhost:8338/kurjun/rest/template/list";

    private static final int TEMPLATE_CACHE_TTL_SEC = 30;
    private Set<Template> templatesCache = Sets.newHashSet();
    private long lastTemplatesFetchTime;


    protected WebClient getWebClient()
    {
        return RestUtil.createWebClient( GORJUN_LIST_TEMPLATES_URL, 3000, 5000, 1 );
    }


    @Override
    public Set<Template> getTemplates()
    {

        if ( templatesCache.isEmpty()
                || ( System.currentTimeMillis() - lastTemplatesFetchTime ) / 1000 >= TEMPLATE_CACHE_TTL_SEC )
        {
            WebClient webClient = null;
            Response response = null;

            try
            {
                webClient = getWebClient();

                response = webClient.get();

                Set<Template> freshTemplateList =
                        JsonUtil.fromJson( response.readEntity( String.class ), new TypeToken<Set<Template>>()
                        {
                        }.getType() );

                lastTemplatesFetchTime = System.currentTimeMillis();

                if ( !CollectionUtil.isCollectionEmpty( freshTemplateList ) )
                {
                    templatesCache = freshTemplateList;
                }
            }
            catch ( Exception e )
            {
                LOG.error( "Error getting templates from local Gorjun", e );
            }
            finally
            {
                RestUtil.close( response, webClient );
            }
        }

        return templatesCache;
    }


    @Override
    public Template getTemplate( final String id )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( id ) );

        for ( Template template : getTemplates() )
        {
            if ( template.getId().equalsIgnoreCase( id ) )
            {
                return template;
            }
        }

        return null;
    }


    @Override
    public Template getTemplateByName( final String name )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ) );

        for ( Template template : getTemplates() )
        {
            if ( template.getName().equalsIgnoreCase( name ) )
            {
                return template;
            }
        }

        return null;
    }
}
