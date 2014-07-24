package org.safehaus.subutai.configuration.manager.impl.utils;


import java.util.List;

import org.safehaus.subutai.configuration.manager.api.ConfigTypeEnum;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 7/18/14.
 */
public class ConfigBuilder {


    JsonObject jo;


    public ConfigBuilder() {
        this.jo = new JsonObject();
    }


    public JsonObject getConfigJsonObject( String pathToFile, ConfigTypeEnum configTypeEnum ) {
        jo.addProperty( "path", pathToFile );
        jo.addProperty( "type", configTypeEnum.toString() );
        return jo;
    }


    public JsonObject addJsonArrayToConfig( JsonObject jsonObject, List<JsonObject> fields ) {
        JsonArray jsonArray = new JsonArray();
        for ( JsonObject jo : fields ) {
            jsonArray.add( jo );
        }
        jsonObject.add( "configFields", jsonArray );
        return jsonObject;
    }


    public JsonObject buildFieldJsonObject( String fieldName, String label, String required, String uiType,
                                            String value ) {
        JsonObject jo = new JsonObject();
        jo.addProperty( "fieldName", fieldName );
        jo.addProperty( "label", label );
        jo.addProperty( "required", required );
        jo.addProperty( "uiType", uiType );
        jo.addProperty( "value", value );

        return jo;
    }


    public JsonObject getJo() {
        return jo;
    }
}