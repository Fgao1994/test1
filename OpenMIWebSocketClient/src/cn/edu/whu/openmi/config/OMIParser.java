package cn.edu.whu.openmi.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.openmi.standard.ILinkableComponent;

public class OMIParser {
	private OMIBean omiBean = null;
	private String omiPath = null;
	public OMIParser(String path){
		this.omiPath = path;
	}
	public boolean exec(){
		OMIBean bean = new OMIBean(this.omiPath);
		boolean flag = bean.parse();
		if (flag) {
			this.omiBean = bean;
		}
		return flag;
	}
	
	public ILinkableComponent getInitializedLinkableComponent(){
		ILinkableComponent linkableComponent = null;
		if (this.omiBean == null) {
			if (!exec()) {
				return null;
			}
		}
		String className = this.omiBean.getClassName();
		String jarPath = this.omiBean.getJarPath();
		URL url = null;
		try {
		 url = new File(jarPath).toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url});
		try {
			linkableComponent = (ILinkableComponent)urlClassLoader.loadClass(className).newInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		linkableComponent.initialize(this.omiBean.getArguments());
		return linkableComponent;
	}
	
	public static void main(String[] args){
		String path = "E:\\GeoJModelBuilder\\eclipseWS\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\config\\SimpleWrapperComponent.omi";
		OMIParser parser = new OMIParser(path);
		ILinkableComponent linkableComponent = parser.getInitializedLinkableComponent();
		System.out.println(linkableComponent);
	}
}
