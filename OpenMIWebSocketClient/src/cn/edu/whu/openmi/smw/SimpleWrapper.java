/**
 * 参考SMW中的Wrapper,实现该类
 */
package cn.edu.whu.openmi.smw;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.alterra.openmi.sdk.backbone.ElementSet;
import nl.alterra.openmi.sdk.backbone.InputExchangeItem;
import nl.alterra.openmi.sdk.backbone.OutputExchangeItem;
import nl.alterra.openmi.sdk.backbone.Quantity;
import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.TimeSpan;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.backbone.Unit;
import nl.alterra.openmi.sdk.wrapper.IEngine;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.ITimeSpan;
import org.openmi.standard.ITimeStamp;
import org.openmi.standard.IValueSet;

import cn.edu.whu.openmi.util.OpenMIError;
import cn.edu.whu.openmi.util.OpenMIUtilities;

public abstract class SimpleWrapper implements IEngine {

	private String _componentID = "Simple_Model_Component";
	private String _componentDescription = "Simple Model Component";
	protected String _modelID;
	protected String _modelDescription;
	protected List<InputExchangeItem> _inputs = new ArrayList<InputExchangeItem>();
	protected List<OutputExchangeItem> _outputs = new ArrayList<OutputExchangeItem>();
	protected double _simulationStartTime;
	protected double _simulationEndTime;
	protected double _currentTime;
	protected double _timeStep; // unit day
	private String _shapefilepath;
	protected Map<String, Quantity> _quantities = new HashMap<String, Quantity>();
	protected Map<String, ElementSet> _elementSets = new HashMap<String, ElementSet>();
	private Map<String, double[]> _vals = new HashMap<String, double[]>();
	private Unit _omiUnits;
	protected ILinkableComponent linkableComponent = null;
	
	private boolean initialized = false;
	private OpenMIError error = new OpenMIError();
	

	private String dateFormat = "yyyy-MM-dd HH:mm:ss";
	// private Map<String,IValueSet> _valuesMap = new HashMap<String,
	// IValueSet>();

	/**
	 * if the initialization process is successful, setInitialized(true) should be invoked in the last
	 * if error occured during the initialization, setInitialzied(false)
	 */
	@Override
	public void initialize(HashMap properties) {
//		this.linkableComponent = linkableComponent;
	}

	@Override
	public  boolean performTimeStep() {
		// TODO Auto-generated method stub
		return false;
	}

	// add by swm
	public double getTimeStep() {
		return _timeStep;
	}

	private boolean setVariablesFromConfigFile(Document doc){
		Element root = doc.getRootElement();
		Element modelInfoEle = (Element)root.selectSingleNode("/Configuration/ModelInfo");
		if (modelInfoEle != null) {
			this._modelID = modelInfoEle.elementText("ID");
			this._modelDescription = modelInfoEle.elementText("Description");
		}
		if (this._modelID  == null || this._modelID.trim().equals("")) {
			this._modelID = "DefaultModelID";
		}
		if (this._modelDescription == null || this._modelDescription.trim().equals("")) {
			this._modelID = "Default model description";
		}
		Element timeHorizonEle = (Element)root.selectSingleNode("/Configuration/TimeHorizon"); 
		// the format of time in configuration file is "yyyy-MM-dd HH:mm:ss"
		if (timeHorizonEle!=null ) {
			String startTime = timeHorizonEle.elementText("StartDateTime");
			String endTime = timeHorizonEle.elementText("EndDateTime");
			String stepTime = timeHorizonEle.elementText("TimeStepInSeconds");
			if (startTime !=null && !startTime.trim().equals("")) {
				Date startDate = OpenMIUtilities.str2Date(startTime, dateFormat);
				if (startDate!=null) {
					this._simulationStartTime = OpenMIUtilities.date2MJulianDate(startDate);
				}
			}
			if (endTime !=null && !endTime.trim().equals("")) {
				Date endDate = OpenMIUtilities.str2Date(endTime, dateFormat);
				if (endDate!=null) {
					this._simulationEndTime = OpenMIUtilities.date2MJulianDate(endDate);
				}
			}
			if (stepTime !=null && !stepTime.trim().equals("")) {
				this._timeStep = Double.parseDouble(stepTime.trim())/(60*60*24);
				//this._timeStep = Double.parseDouble(stepTime.trim())/(60);
			}
		}
		List<Element> outputElements = (List<Element>)root.selectNodes("/Configuration/ExchangeItems/OutputExchangeItem");
		for(Element outputElement:outputElements){
			this._outputs.add(OpenMIUtilities.xml2OutputExchangeItem(outputElement, this.linkableComponent));
		}
		List<Element> inputElements = root.selectNodes("/Configuration/ExchangeItems/InputExchangeItem");
		for(Element inputeElement:inputElements){
			this._inputs.add(OpenMIUtilities.xml2InputExchangeItem(inputeElement, this.linkableComponent));
		}
		
		return true;
	}
	// Configure the parameters of the model by configuration file.
	public boolean setVariablesFromConfigFile(InputStream inStream) {
		SAXReader reader = new SAXReader();
		Document doc = null;
		try {
			doc = reader.read(inStream);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.addError(e.getMessage());
			return false;
		}
		return setVariablesFromConfigFile(doc);
	}
	
	public boolean setVariablesFromConfigFile(String path) {
		SAXReader reader = new SAXReader();
		Document doc = null;
		try {
			doc = reader.read(new File(path));
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.addError(e.getMessage());
			return false;
		}
		return setVariablesFromConfigFile(doc);
	}

	/**
	 * This method will advance the components in time, by a single timestep.
	 * This should be called at the end of Perform Time Step.
	 */
	public void advanceTime() {
		TimeStamp ct = (TimeStamp) this.getCurrentTime();
		_currentTime = ct.getTime() + _timeStep;
		// _currentTime = ct.getTime() + _timeStep/86400.0 ;
	}

	/**
	 * Use to get the shapefile path stored in config.xml Returns the absolute
	 * path to the elementset shapefile
	 */
	public String getShapefilePath() {
		return _shapefilepath;
	}

	/**
	 * Gets the unit value that the component is implemented over returns unitID
	 * from config.xml
	 */
	public String GetUnits() {
		return _omiUnits.getID();
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		System.out.println("Invoke SimpleWrapper.finish()");
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		System.out.println("Invoke SimpleWrapper.finish()");
	}

	@Override
	public void setValues(String QuantityID, String ElementSetID,
			IValueSet values) {
		// TODO Auto-generated method stub
		String key = QuantityID + "_" + ElementSetID;
		ScalarSet scalarSet = (ScalarSet) values;
		double[] vals = new double[values.getCount()];
		for (int i = 0; i < values.getCount(); i++) {
			vals[i] = scalarSet.getValue(i);
		}
		_vals.put(key, vals);
	}

	/**
	 * This method is used to extract values from an upstream component.
	 * QuantityID: The input Quantity ID ElementSetID: The input Element Set ID
	 * return: the values saved under the matching QuantityID and ElementSetID,
	 * from an upstream component
	 */
	@Override
	public IValueSet getValues(String QuantityID, String ElementSetID) {
		// TODO Auto-generated method stub
		String key = QuantityID + "_" + ElementSetID;
		if (_vals.containsKey(key))
			return new ScalarSet(_vals.get(key));
		else if (_elementSets.containsKey(ElementSetID))
			// return new ScalarSet(new
			// double[_elementSets[ElementSetID].ElementCount]);
			return new ScalarSet(new Double[] { (double) _elementSets.get(
					ElementSetID).getElementCount() });
		else
			// return new ScalarSet(new
			// double[_outputs[0].ElementSet.ElementCount]);
			return new ScalarSet(new Double[] { (double) _outputs.get(0)
					.getElementSet().getElementCount() });
	}

	@Override
	public double getMissingValueDefinition() {
		// TODO Auto-generated method stub
		return -999;
	}

	@Override
	public ITimeSpan getTimeHorizon() {
		// TODO Auto-generated method stub
		return new TimeSpan(new TimeStamp(_simulationStartTime), new TimeStamp(
				_simulationEndTime));
	}

	@Override
	public ITime getCurrentTime() {
		// TODO Auto-generated method stub
		if (_currentTime == 0.0) {
			_currentTime = _simulationStartTime;
		}
		return new TimeStamp(_currentTime);
	}

	@Override
	public ITime getInputTime(String QuantityID, String ElementSetID) {
		// This method returns the requested input time to the ILinkableEngine
		// class
		return this.getCurrentTime();
	}

	@Override
	public ITimeStamp getEarliestNeededTime() {
		// TODO Auto-generated method stub
		return (TimeStamp) this.getCurrentTime();
	}

	@Override
	public IOutputExchangeItem getOutputExchangeItem(int exchangeItemIndex) {
		// TODO Auto-generated method stub
		return this._outputs.get(exchangeItemIndex);
	}

	@Override
	public IInputExchangeItem getInputExchangeItem(int exchangeItemIndex) {
		// TODO Auto-generated method stub
		return this._inputs.get(exchangeItemIndex);
	}

	@Override
	public int getInputExchangeItemCount() {
		// TODO Auto-generated method stub
		return _inputs.size();
	}

	@Override
	public int getOutputExchangeItemCount() {
		// TODO Auto-generated method stub
		return _outputs.size();
	}

	@Override
	public String getComponentID() {
		return _componentID;
	}

	@Override
	public String getComponentDescription() {
		// TODO Auto-generated method stub
		return _componentDescription;
	}

	@Override
	public String getModelID() {
		// TODO Auto-generated method stub
		return _modelID;
	}

	@Override
	public String getModelDescription() {
		// TODO Auto-generated method stub
		return _modelDescription;
	}

	
	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public OpenMIError getError() {
		return error;
	}

	public void addError(String error) {
		this.error.add(error);
	}

	// add by zmd
	public static void main(String[] args){
		/*String path  = "E:\\GeoJModelBuilder\\eclipseWS\\OpenMI-1.4-SDK\\src\\cn\\edu\\whu\\openmi\\smw\\Topmodel-Config.xml";
		SimpleWrapperComponet1_1 componet1 = new SimpleWrapperComponet1_1();
		SimpleWrapper simpleWrapper = new SimpleWrapper();
		simpleWrapper.setVariablesFromConfigFile(path);*/
		
	}
}
