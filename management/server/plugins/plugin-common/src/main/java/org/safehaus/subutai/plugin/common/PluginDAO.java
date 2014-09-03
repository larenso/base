package org.safehaus.subutai.plugin.common;


import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;

import java.util.ArrayList;
import java.util.List;


/**
 * PluginDAO is used to manage cluster configuration information in database
 */
public class PluginDAO
{
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DbManager dbManager;


    public PluginDAO( final DbManager dbManager )
    {
        Preconditions.checkNotNull( dbManager, "Db Manager is null" );
        this.dbManager = dbManager;
    }


    public void saveInfo( String source, String key, Object info ) throws DBException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        Preconditions.checkNotNull( info, "Info is null" );

        dbManager.executeUpdate2( "insert into product_info(source,key,info) values (?,?,?)", source.toLowerCase(),
            key.toLowerCase(),
            gson.toJson( info ) );
    }


    /**
     * Returns all POJOs from DB identified by source key
     *
     * @param source - source key
     * @param clazz - class of POJO
     *
     * @return - list of POJOs
     */
    public <T> List<T> getInfo( String source, Class<T> clazz ) throws DBException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkNotNull( clazz, "Class is null" );

        List<T> list = new ArrayList<>();
        try
        {
            ResultSet rs = dbManager
                .executeQuery2( "select info from product_info where source = ?", source.toLowerCase() );
            if ( rs != null )
            {
                for ( Row row : rs )
                {
                    String info = row.getString( "info" );
                    list.add( gson.fromJson( info, clazz ) );
                }
            }
        }
        catch ( JsonSyntaxException ex )
        {
            throw new DBException( ex.getMessage() );
        }
        return list;
    }


    /**
     * Returns POJO from DB
     *
     * @param source - source key
     * @param key - pojo key
     * @param clazz - class of POJO
     *
     * @return - POJO
     */
    public <T> T getInfo( String source, String key, Class<T> clazz ) throws DBException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        Preconditions.checkNotNull( clazz, "Class is null" );

        try
        {

            ResultSet rs = dbManager
                .executeQuery2( "select info from product_info where source = ? and key = ?", source.toLowerCase(),
                    key.toLowerCase() );
            if ( rs != null )
            {
                Row row = rs.one();
                if ( row != null )
                {

                    String info = row.getString( "info" );
                    return gson.fromJson( info, clazz );
                }
            }
        }
        catch ( JsonSyntaxException ex )
        {
            throw new DBException( ex.getMessage() );
        }
        return null;
    }


    /**
     * deletes POJO from DB
     *
     * @param source - source key
     * @param key - POJO key
     */
    public void deleteInfo( String source, String key ) throws DBException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );

        dbManager.executeUpdate2( "delete from product_info where source = ? and key = ?", source.toLowerCase(),
            key.toLowerCase() );
    }
}