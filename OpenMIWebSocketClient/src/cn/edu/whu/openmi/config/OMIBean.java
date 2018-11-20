package cn.edu.whu.openmi.config;

import java.io.File;
import java.util.List;

import nl.alterra.openmi.sdk.backbone.Argument;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import cn.edu.whu.openmi.util.OpenMIUtilities;

public class OMIBean {
	
	private String omiFile = null;
	private String className = null;
	private String jarPath = null;
	private Argument[] arguments = null;
	
	public OMIBean(String path){
		this.omiFile = path;
	}
	
	public boolean parse(){
		SAXReader saxReader = new SAXReader();
		Document doc = null;
		try {
			 doc = saxReader.read(new File(this.omiFile));
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		Element componentElement = doc.getRootElement();
		if (componentElement == null) {
			System.err.println("There is no LinkableComponent node.");
			return false;
		} 
		this.className = componentElement.attributeValue("Type");
		this.jarPath = componentElement.attributeValue("JavaArchive");
		
		List<Element> elements = doc.selectNodes("/LinkableComponent/Arguments/Argument");
		if (elements==null || elements.size()==0 ) {
			return true;
		}
		arguments = new Argument[elements.size()];
		int i = 0;
		for(Element arguElement:elements){
			String key = arguElement.attributeValue("Key");
			String readOnly = arguElement.attributeValue("ReadOnly");
			String value = arguElement.attributeValue("Value");
			Argument argument = new Argument();
			if (!OpenMIUtilities.isEmpty(key)) {
				argument.setKey(key);
			}
			if (!OpenMIUtilities.isEmpty(value)) {
				argument.setValue(value);
			}
			if (!OpenMIUtilities.isEmpty(readOnly)) {
				if (readOnly.trim().equalsIgnoreCase("true") || readOnly.trim().equalsIgnoreCase("false")) {
					argument.setReadOnly(Boolean.parseBoolean(readOnly.trim()));
				}
			}
			arguments[i] = argument;
			i++;
		}
		return true;
	}
	public String getOmiFile() {
		return omiFile;
	}
	public void setOmiFile(String omiFile) {
		this.omiFile = omiFile;
	}
	public String getClassName() {
		return className;
	}
	public String getJarPath() {
		return jarPath;
	}
	public Argument[] getArguments() {
		return arguments;
	}
}
