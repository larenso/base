package org.safehaus.subutai.core.configuration.impl.utils;


import com.google.gson.JsonObject;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.DefaultExpressionEngine;
import org.apache.commons.configuration.tree.ExpressionEngine;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.safehaus.subutai.core.configuration.api.ConfigTypeEnum;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * @author dilshat
 */
public class XmlParser implements ConfigParser {

	private final XMLConfiguration config;
	private final ExpressionEngine defaultEngine = new DefaultExpressionEngine();
	private final ExpressionEngine expressEngine = new XPathExpressionEngine();


	public XmlParser(String content) throws ConfigurationException {
		config = new XMLConfiguration();
		config.load(new ByteArrayInputStream(content.getBytes()));
	}


	public Object getProperty(String propertyName) {
		return config.getProperty(propertyName);
	}


	public void setProperty(String propertyName, Object propertyValue) {
		config.setProperty(propertyName, propertyValue);
	}


	public String getStringProperty(String propertyName) {
		return config.getString(propertyName);
	}

	public void addConventionalProperty(String propertyName, Object propertyValue) {
		XMLConfiguration.setDefaultExpressionEngine(defaultEngine);
		config.addProperty("property(-1).name", propertyName);
		config.addProperty("property.value", propertyValue);
	}

	public void setConventionalProperty(String propertyName, Object propertyValue) {
		XMLConfiguration.setDefaultExpressionEngine(expressEngine);
		config.setProperty(String.format("property[name='%s']/value", propertyName), propertyValue);
	}

	public String getXml() throws ConfigurationException {
		StringWriter str = new StringWriter();
		config.save(str);
		return str.toString();
	}

	@Override
	public JsonObject parserConfig(final String pathToConfig, final ConfigTypeEnum configTypeEnum) {
		ConfigBuilder configBuilder = new ConfigBuilder();
		JsonObject jo = configBuilder.getConfigJsonObject(pathToConfig, configTypeEnum);

		List<HierarchicalConfiguration> properties = config.configurationsAt("property");
		config.setDelimiterParsingDisabled(false);

		String s = getConventionalProperty("general.classpaths");
		System.out.println(s);

		List<JsonObject> fields = new ArrayList<>();
		for (HierarchicalConfiguration property : properties) {
			String key = property.getString("name");
			String value = property.getString("value");
			JsonObject field =
					configBuilder.buildFieldJsonObject(key.trim(), key.trim(), true, "textfield", true, value.trim());
			fields.add(field);
		}

		JsonObject njo = configBuilder.addJsonArrayToConfig(jo, fields);

		return njo;
	}

	public String getConventionalProperty(String propertyName) {
		XMLConfiguration.setDefaultExpressionEngine(expressEngine);
		return config.getString(String.format("property[name='%s']/value", propertyName));
	}
}