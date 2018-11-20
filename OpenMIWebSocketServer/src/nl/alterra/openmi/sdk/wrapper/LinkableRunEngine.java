/* ***************************************************************************
 *
 *    Copyright (C) 2006 OpenMI Association
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *    or look at URL www.gnu.org/licenses/lgpl.html
 *
 *    Contact info:
 *      URL: www.openmi.org
 *      Email: sourcecode@openmi.org
 *      Discussion forum available at www.sourceforge.net
 *
 *      Coordinator: Roger Moore, CEH Wallingford, Wallingford, Oxon, UK
 *
 *****************************************************************************
 *
 * The classes in the utilities package are mostly a direct translation from
 * the C# version. They successfully pass the unit tests (which were also
 * taken from the C# version), but so far no extensive time as been put into
 * them.
 *
 ****************************************************************************/
package nl.alterra.openmi.sdk.wrapper;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.alterra.openmi.sdk.backbone.Event;
import nl.alterra.openmi.sdk.backbone.LinkableComponent;
import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import nl.alterra.openmi.sdk.buffer.SmartBuffer;
import nl.alterra.openmi.sdk.utilities.CalendarConverter;

import org.openmi.standard.IArgument;
import org.openmi.standard.IDimension;
import org.openmi.standard.IDimension.DimensionBase;
import org.openmi.standard.IEvent.EventType;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.IQuantity;
import org.openmi.standard.ITime;
import org.openmi.standard.ITimeSpan;
import org.openmi.standard.ITimeStamp;
import org.openmi.standard.IValueSet;

import cn.edu.whu.openmi.provenance.SmartBufferInfo;
import cn.edu.whu.openmi.smw.SimpleWrapper;

/**
 * LinkableRunEngine class
 * 
 * The SmartWrapper class is the OpenMI default implementation of the
 * ILinkableComponent interface for time stepping model engines.
 * 
 * The OpenMI standard puts a lot of responsibilities on the LinkableComponents.
 * Such components must implement all be bookkeeping for handling links added
 * through the AddLink method and they may have to return values that are
 * interpolated, extrapolated or aggregated in time and space in order to
 * conform with the GetValues request. When you look at the ILinkableComponent
 * interface you will not find any methods that directly will trigger the model
 * engine to run or do a time step, such things are assumed to be handled behind
 * the scene. So, to figure out, based on the interface definitions alone, what
 * exactly do to your model system may seem difficult. The design of the
 * SmartWrapper class and associated classes located in the Wrapper package,
 * Buffer package, and the Spatial package will provide you with one possible
 * way to go.
 */
public abstract class LinkableRunEngine extends LinkableComponent implements Serializable {
    
    // TODO: The elementset version number should be checked and the
    // elementmapper
    // called in order to update the mapping a matrix when the version has
    // changed

    protected SmartInputLinkSet smartInputLinkSet = null;
    protected SmartOutputLinkSet smartOutputLinkSet = null;
    protected IRunEngine engineApiAccess = null;
    protected boolean engineWasAssigned;
    protected boolean initializeWasInvoked;
    protected boolean prepareForCompotationWasInvoked;
    protected boolean isBusy = false;
    protected ArrayList<EventType> publishedEventTypes;
    protected double timeEpsilon; // used when comparing time in the IsLater
    // method (see property TimeEpsilon)

    //add by zmd, 20160114保存缓存数据，溯源推理使用
//   protected Map<ITime, SmartBuffer> bufferInfos = new LinkedHashMap<ITime,SmartBuffer>();
    protected List<SmartBufferInfo> bufferInfos = new ArrayList<SmartBufferInfo>();
    
    protected ArrayList<String> validationWarningMessages;
    protected ArrayList<String> validationErrorMessages;

    // @@@@@@@ CONSTRUCTORS @@@@@@@

    /**
     * Constructor method for the SmartWrapper class
     */
    public LinkableRunEngine() {
        super("LinkableRunEngine");

        engineWasAssigned = false;
        initializeWasInvoked = false;
        prepareForCompotationWasInvoked = false;
        timeEpsilon = 1.0 / (1000.0 * 3600.0 * 24.0);

        publishedEventTypes = new ArrayList<EventType>();
        publishedEventTypes.add(EventType.DataChanged);
        publishedEventTypes.add(EventType.Informative);
        publishedEventTypes.add(EventType.SourceAfterGetValuesCall);
        publishedEventTypes.add(EventType.SourceBeforeGetValuesReturn);
        publishedEventTypes.add(EventType.TargetAfterGetValuesReturn);
        publishedEventTypes.add(EventType.TargetBeforeGetValuesCall);

        // publishedEventTypes.Add(EventType.Other); // Not supported by
        // LinkableRunEngine at this time
        // publishedEventTypes.Add(EventType.GlobalProgress); // Not supported
        // by LinkableRunEngine at this time
        // publishedEventTypes.Add(EventType.TimeStepProgres); // Not supported
        // by LinkableRunEngine at this time
        // publishedEventTypes.Add(EventType.ValueOutOfRange); // Not supported
        // by LinkableRunEngine at this time
        // publishedEventTypes.Add(EventType.Warning); // Not supported by
        // LinkableRunEngine at this time

        validationWarningMessages = new ArrayList();
        validationErrorMessages = new ArrayList();

    }

    // @@@@@@@ GETTERS AND SETTERS @@@@@@@

    /**
     * Implementation of
     *
     * @see ILinkableComponent#getEarliestInputTime()
     */
    @Override
    public ITimeStamp getEarliestInputTime() {
        return engineApiAccess.getEarliestNeededTime();
    }

    /**
     * GETTER for TimeEpsilon This _timeEpsilon variable is used when comparing
     * the current time in the engine with the time specified in the parameters
     * for the GetValue method. if ( requestedTime > engineTime + _timeEpsilon)
     * then PerformTimestep().. The default values for _timeEpsilon is
     * double.Epsilon = 4.94065645841247E-324 The default value may be too small
     * for some engines, in which case the _timeEpsilon can be changed the class
     * that you have inherited from LinkableRunEngine og LinkableEngine.
     *
     * @return the time epsilon
     */
    public double getTimeEpsilon() {
        return timeEpsilon;
    }

    /**
     * SETTER for TimeEspilon
     *
     * @param time set the TimeEpsilon
     * @see LinkableRunEngine#getTimeEpsilon()
     *      description of timeEpsilon
     */
    public void setTimeEpsilon(double time) {
        timeEpsilon = time;
    }

    /*
      * already abstract
      *
      * @see org.openmi.standard.ILinkableComponent#getTimeHorizon()
      */
    @Override
    public abstract ITimeSpan getTimeHorizon();

    /*
      * (non-Javadoc)
      *
      * @see org.openmi.standard.ILinkableComponent#getInputExchangeItemSize()
      */
    @Override
    public abstract int getInputExchangeItemCount();

    /*
      * (non-Javadoc)
      *
      * @see org.openmi.standard.ILinkableComponent#getOutputExchangeItemSize()
      */
    @Override
    public abstract int getOutputExchangeItemCount();

    /*
      * (non-Javadoc)
      *
      * @see org.openmi.standard.ILinkableComponent#getModelDescription()
      */
    @Override
    public abstract String getModelDescription();

    /*
      * (non-Javadoc)
      *
      * @see org.openmi.standard.ILinkableComponent#getModelID()
      */
    @Override
    public abstract String getModelID();

    /**
     * @return irunengine
     */
    public IRunEngine getEngineApiAccess() {
        return engineApiAccess;
    }

    /**
     * @see ILinkableComponent#getComponentDescription()
     */
    @Override
    public String getComponentDescription() {
        return engineApiAccess.getComponentDescription();
    }

    /**
     * @see ILinkableComponent#getComponentID()
     */
    @Override
    public String getComponentID() {
        if (engineApiAccess != null) {
            return engineApiAccess.getComponentID();
        }
        else {
            return null;
        }
    }

    // @@@@@@@ OTHERS @@@@@@@
    /*
      * (non-Javadoc)
      *
      * @see org.openmi.standard.ILinkableComponent#addLink(org.openmi.standard.ILink)
      *      Implementation of the same method in the
      *      org.openmi.standard.ILinkableComponent interface Exception - Thrown
      *      when invoked before the Initialize method has been invoked - Thrown
      *      when invoked before the PrepareForComputaion method has been invoked -
      *      Thown for Inconsistant role and SourceComponentID and
      *      TargetComponentID - Thown when SourceComponent.ID or
      *      TargetComponent.ID in Link does not match the Component ID for the
      *      component to which the Link was added
      */
    @Override
    public void addLink(ILink newLink) {
        try {
            if (!initializeWasInvoked) {
                throw new Exception("AddLink method in the SmartWrapper cannot be invoked before the Initialize method has been invoked");
            }
            if (prepareForCompotationWasInvoked) {
                throw new Exception("AddLink method in the SmartWrapper cannot be invoked after the PrepareForComputation method has been invoked");
            }

            if (newLink.getTargetComponent() == this) {
                this.smartInputLinkSet.addLink(newLink);
                super.addLink(newLink);
            }
            else if (newLink.getSourceComponent() == this) {
                this.smartOutputLinkSet.addLink(newLink);
                super.addLink(newLink);
            }
            else {
                throw new Exception("SourceComponent.ID or TargetComponent.ID in Link does not match the Component ID for the component to which the Link was added");
            }
        }
        catch (Exception e) {
            String message = "Exception in LinkableComponent. ";
            message += "ComponentID: " + this.getComponentID() + "\n";
            throw new RuntimeException(message, e);
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see org.openmi.standard.ILinkableComponent#dispose()
      */
    @Override
    public void dispose() {
        engineApiAccess.dispose();
    }

    @Override
    public void finishHook() {
        engineApiAccess.finish();
    }

    /*
      * @see org.openmi.standard.ILinkableComponent#getInputExchangeItem(int)
      */
    @Override
    public abstract IInputExchangeItem getInputExchangeItem(int index);

    /*
      * @see org.openmi.standard.ILinkableComponent#getOutputExchangeItem(int)
      */
    @Override
    public abstract IOutputExchangeItem getOutputExchangeItem(int index);

    @Override
    public EventType getPublishedEventType(int providedEventTypeIndex) {
        switch (providedEventTypeIndex) {
            case 0:
                return EventType.TimeStepProgress;
            case 1:
                return EventType.DataChanged;
            case 2:
                return EventType.TargetBeforeGetValuesCall;
            case 3:
                return EventType.SourceAfterGetValuesCall;
            case 4:
                return EventType.TargetBeforeGetValuesCall;
            case 5:
                return EventType.TargetAfterGetValuesReturn;
            case 6:
                return EventType.Informative;
            default:
                throw new RuntimeException("Iligal index in GetPublishedEventType()");
        }
    }

    @Override
    public int getPublishedEventTypeCount() {
        return 7;
    }

    /**
     * @param time
     * @param link
     * @return valueSet
     */
    @SuppressWarnings({"OverlyLongMethod"})
    
    @Override
    public IValueSet getValuesHook(ITime time, ILink link) {
        String LinkID = link.getID();

        try {
            checkTimeArgumentInGetvaluesMethod(time);

            Event eventA = new Event(EventType.SourceAfterGetValuesCall);
            eventA.setDescription("GetValues(t = " + iTimeToString(time) + ", ");
            if (LinkID.equals("Trigger")) {
                eventA.setDescription(eventA.getDescription() + "QS = " + smartOutputLinkSet.getLink(LinkID).getSourceQuantity().getID() + " ,QT = " + smartOutputLinkSet.getLink(LinkID).getTargetQuantity().getID());
            }
            eventA.setDescription(eventA.getDescription() + ") <<<===");
            eventA.setSender(this);
            eventA.setSimulationTime(timeToTimeStamp(engineApiAccess.getCurrentTime()));
            eventA.setAttribute("GetValues time argument : ", iTimeToString(time));
            sendEvent(eventA);

            ScalarSet engineResult = new ScalarSet();

            if (isBusy == false) {
                while (isLater(time, engineApiAccess.getCurrentTime())) {
                   
                	//add by zmd,20151026,将类实现中的AdvanceTime去掉。
                	if (this.engineApiAccess instanceof SimpleWrapper) {
							((SimpleWrapper)(this.engineApiAccess)).advanceTime();
					}
                	
                	isBusy = true;

                    //Update input links
                    smartInputLinkSet.updateInput();
                    isBusy = false;

                    //Perform Timestep
                    if (engineApiAccess.performTimeStep()) {

                        //Update buffer with engine values, Time is timestamp
                        smartOutputLinkSet.updateBuffers(engineApiAccess.getCurrentTime());
                       
                        Event eventD = new Event(EventType.DataChanged);
                        eventD.setDescription("After PerformTimeStep. Engine time = " + iTimeToString(engineApiAccess.getCurrentTime()) + "  ==== VVV ===");
                        eventD.setSender(this);
                        eventD.setSimulationTime(timeToTimeStamp(engineApiAccess.getCurrentTime()));
                        sendEvent(eventD);
                        
                        /*//add by zmd 2015.03.12，
                        if (this.engineApiAccess instanceof SimpleWrapper) {
							((SimpleWrapper)(this.engineApiAccess)).advanceTime();
						}*/
                    }
                }
            }

            if (time instanceof TimeStamp) {
                engineResult = (ScalarSet) smartOutputLinkSet.getValue(time, LinkID);
            }
            else if (time instanceof ITimeSpan) {
                engineResult = (ScalarSet) smartOutputLinkSet.getValue(time, LinkID);
            }

            //add by zmd, 20160114记录溯源信息
            SmartBufferInfo smartBufferInfo = smartOutputLinkSet.getSmartBuffer(LinkID);
//            bufferInfos.put(time, smartBuffer);
            smartBufferInfo.setLinkId(LinkID);
            smartBufferInfo.setTime(time);
            bufferInfos.add(smartBufferInfo);
            
            // --- Build and send the Trace event ---
            Event eventB = new Event(EventType.SourceBeforeGetValuesReturn);
            eventB.setDescription("GetValues(t = " + iTimeToString(time) + ", ");
            if (LinkID.equals("Trigger")) {
                eventB.setDescription(eventB.getDescription() + "QS = " + smartOutputLinkSet.getLink(LinkID).getSourceQuantity().getID() + " ,QT = " + smartOutputLinkSet.getLink(LinkID).getTargetQuantity().getID());
            }
            eventB.setDescription(eventB.getDescription() + ") <<<===");
            eventB.setSender(this);
            eventB.setSimulationTime(timeToTimeStamp(engineApiAccess.getCurrentTime()));
            sendEvent(eventB);

            // --- Build and send the Message event ---
            Event messageEvent = new Event(EventType.Informative);
            String resultsString;
            resultsString = "First 5 returned Values {";
            for (int i = 0; i < Math.min(engineResult.size(), 5); i++) {
                resultsString += new Double(engineResult.getScalar(i)).toString() + ", ";
            }
            resultsString += "}";
            messageEvent.setDescription(resultsString + " for time = " + iTimeToString(time) + " Engine time = " + iTimeToString(engineApiAccess.getCurrentTime()));
            messageEvent.setSender(this);
            messageEvent.setSimulationTime(timeToTimeStamp(engineApiAccess.getCurrentTime()));
            sendEvent(messageEvent);

            return engineResult;

        }
        catch (Exception e) {
            String message = "Exception in LinkableComponent. ComponentID: ";
            message += this.getComponentID();
            message+="\n"+e.getMessage();
            e.printStackTrace();
            System.err.println(e.getMessage());
            throw new RuntimeException(message, e);
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see org.openmi.backbone.LinkableComponent#initialize(org.openmi.standard.IArgument[])
      */
    @Override
    public void initializeHook(IArgument[] properties) {

        HashMap tempMap = new HashMap();
        for (int i = 0; i < properties.length; i++) {
            tempMap.put(properties[i].getKey(), properties[i].getValue());
        }

        setEngineApiAccess();
        this.engineWasAssigned = true;

        smartInputLinkSet = new SmartInputLinkSet();
        smartInputLinkSet.initialize(engineApiAccess);
        smartOutputLinkSet = new SmartOutputLinkSet();
        smartOutputLinkSet.initialize(engineApiAccess);

        engineApiAccess.initialize(tempMap);
        initializeWasInvoked = true;
    }

    protected abstract void setEngineApiAccess();

    @Override
    public void prepareHook() {
        try {
            if (!engineWasAssigned) {
                throw new RuntimeException("PrepareForComputation method in SmartWrapper cannot be invoked before the EngineApiAccess has been assigned");
            }
            if (!initializeWasInvoked) {
                throw new RuntimeException("PrepareForComputation method in SmartWrapper cannot be invoked before the Initialize method has been invoked");
            }

            validate();
            if (validationErrorMessages.size() > 0) {
                String errorMessage = "";
                for (String str : validationErrorMessages) {
                    errorMessage += "Error: " + str + ". ";
                }
                throw new Exception(errorMessage);
            }

            smartOutputLinkSet.updateBuffers(engineApiAccess.getCurrentTime()); //Update the buffers with the model initial values
            prepareForCompotationWasInvoked = true;
        }
        catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (Exception e) {
            String message = "Exception in LinkableComponent. ";
            message += "ComponentID: " + this.getComponentID() + "\n";
            throw new RuntimeException(message, e);
        }
    }

    @Override
    public void removeLink(String LinkID) {
        try {
            if (!initializeWasInvoked) {
                throw new Exception("Illegal invocation of RemoveLink method before invocation of Initialize method");
            }
            if (prepareForCompotationWasInvoked) {
                throw new Exception("Illegal invocation of RemoveLink method after invocation of Prepare method");
            }
            boolean inputLinkWasRemoved = this.smartInputLinkSet.removeLink(LinkID);
            boolean outputLinkWasRemoved = this.smartOutputLinkSet.removeLink(LinkID);
            if (inputLinkWasRemoved == false && outputLinkWasRemoved == false) {
                throw new Exception("Failed to find LinkID in internal link list in method RemoveLink(). LinkID = " + LinkID);
            }

            super.removeLink(LinkID);

            if (inputLinkWasRemoved == true && outputLinkWasRemoved == true) {
                throw new Exception("Failed to remove link in RemoveLink method. Same linkID appears both as inputLink and outputLink. LinkID = " + LinkID);
            }
        }
        catch (Exception e) {
            String message = "Exception in LinkableComponent. ";
            message += "ComponentID: " + this.getComponentID() + "\n";
            throw new RuntimeException(message, e);
        }
    }

    public String keepCurrentState() {
        throw new UnsupportedOperationException("keepCurrentState was called but the engine does not implement IManageState");
    }

    public void restoreState(String stateID) throws Exception {
        throw new UnsupportedOperationException("restoreState was called but the engine does not implement IManageState");
    }

    public void clearState(String stateID) {
        throw new UnsupportedOperationException("clearState was called but the engine does not implement IManageState");
    }

    /**
     * @return all links
     */
    private ArrayList<ILink> getAllLinks() {
        ArrayList<ILink> links = new ArrayList<ILink>();

        for (int i = 0; i < smartInputLinkSet.smartLinkList.size(); i++) {
            if (smartInputLinkSet.smartLinkList.get(i) instanceof SmartInputLink) {
                SmartInputLink inputLink = (SmartInputLink) smartInputLinkSet.smartLinkList.get(i);
                links.add(inputLink.link);
            }
        }
        for (int i = 0; i < smartOutputLinkSet.smartLinkList.size(); i++) {
            if (smartOutputLinkSet.smartLinkList.get(i) instanceof SmartOutputLink) {
                SmartOutputLink outputLink = (SmartOutputLink) smartOutputLinkSet.smartLinkList.get(i);
                links.add(outputLink.link);
            }
        }
        return links;
    }

    public static TimeStamp timeToTimeStamp(ITime time) {
        TimeStamp t;

        if (time instanceof ITimeStamp) {
            t = new TimeStamp(((ITimeStamp) time).getModifiedJulianDay());
        }
        else {
            t = new TimeStamp(((ITimeSpan) time).getEnd().getModifiedJulianDay());
        }

        return t;
    }

    /**
     * Will compare two times. If the first argument t1, is later than the
     * second argument t2 the method will return true. Otherwise false will be
     * returned. t1 and t2 can be of types ITimeSpan or ITimeStamp.
     *
     * @param t1
     * @param t2
     * @return t1 later then t2
     */
    protected boolean isLater(ITime t1, ITime t2) {
        double mt1;
        double mt2;
        boolean isLater = false;

        mt1 = timeToTimeStamp(t1).getModifiedJulianDay();
        mt2 = timeToTimeStamp(t2).getModifiedJulianDay();

        if (mt1 > mt2 + Double.MIN_VALUE) {
            isLater = true;
        }

        return isLater;

    }

    public static String iTimeToString(ITime time) throws Exception {
        String timeString;
        if (time instanceof ITimeStamp) {
            timeString = CalendarConverter.modifiedJulian2Gregorian(((ITimeStamp) time).getModifiedJulianDay()).toString();
        }
        else if (time instanceof ITimeSpan) {
            timeString = "[" + CalendarConverter.modifiedJulian2Gregorian(((ITimeSpan) time).getStart().getModifiedJulianDay()).toString() + ", " + CalendarConverter.modifiedJulian2Gregorian(((ITimeSpan) time).getEnd().getModifiedJulianDay()).toString() + "]";
        }
        else {
            throw new Exception("Illegal type used for time, must be org.openmi.standard.ITimeStamp or org.openmi.standard.TimeSpan");
        }

        return timeString;
    }

    /*
      * The implementation of
      *
      * @see org.openmi.standard.ILinkableComponent#validate()
      */
    @Override
    public String validate() {

        validationErrorMessages.clear();
        validationWarningMessages.clear();

        // --- Validate links ----
        for (ILink link : getAllLinks()) {
            // check dimension
            if (!compareDimensions(link.getSourceQuantity().getDimension(),
                    link.getTargetQuantity().getDimension())) {
                validationWarningMessages.add("Different dimensions used in link from " + link.getSourceComponent().getModelID() + " to " + link.getTargetComponent().getModelID());
            }
            // check valuetype
            if (link.getSourceQuantity().getValueType() != IQuantity.ValueType.Scalar || link.getTargetQuantity().getValueType() != IQuantity.ValueType.Scalar) {
                validationErrorMessages.add("Component " + this.getComponentID() + "does not support VectorSets");
            }

            // check unit
            if (link.getSourceQuantity().getUnit() == null || link.getTargetQuantity().getUnit() == null) {
                validationErrorMessages.add("Unit equals null in link from " + link.getSourceComponent().getModelID() + " to " + link.getTargetComponent().getModelID());
            }
            else if (link.getSourceQuantity().getUnit().getConversionFactorToSI() == 0.0 || link.getTargetQuantity().getUnit().getConversionFactorToSI() == 0) {
                validationErrorMessages.add("Unit conversion factor equals zero in link from " + link.getSourceComponent().getModelID() + " to " + link.getTargetComponent().getModelID());
            }
        }

        String validationString = "";
        for (String str : validationErrorMessages) {
            validationString += "Error: " + str + " ";
        }

        for (String str : validationWarningMessages) {
            validationString += "Warning: " + str + ". ";
        }

        return validationString;
    }

    // @@@@@@@ PRIVATE MEMBERS @@@@@@@

    private boolean compareDimensions(IDimension dimension1,
            IDimension dimension2) {
        boolean isSameDimension = true;
        for (DimensionBase db : DimensionBase.values()) {
            //for (int i = 0; i < (int) DimensionBase.NUM_BASE_DIMENSIONS.value(); i++) {
            if (dimension1.getPower(db) != dimension2.getPower(db)) {
                isSameDimension = false;
            }
        }
        return isSameDimension;
    }

    private void checkTimeArgumentInGetvaluesMethod(ITime time)
            throws Exception {

        if (time instanceof ITimeSpan) {
            if (this.engineApiAccess instanceof IEngine) {
                if (((ITimeSpan) time).getStart().getModifiedJulianDay() < ((IEngine) this.engineApiAccess).getTimeHorizon().getStart().getModifiedJulianDay()) {
                    throw new RuntimeException("GetValues method was invoked using a time argument that represents a time before the allowed time horizon");
                }

                if (((ITimeSpan) time).getEnd().getModifiedJulianDay() > ((IEngine) this.engineApiAccess).getTimeHorizon().getEnd().getModifiedJulianDay()) {
                    throw new Exception("GetValues method was invoked using a time argument that represents a time that is after the allowed time horizon");
                }
            }
        }
        else if (time instanceof ITimeStamp) {
            if (this.engineApiAccess instanceof IEngine) {
                if (((ITimeStamp) time).getModifiedJulianDay() < ((IEngine) this.engineApiAccess).getTimeHorizon().getStart().getModifiedJulianDay()) {
                    throw new RuntimeException("GetValues method was invoked using a time argument that represents a time before the allowed time horizon");
                }

                if (((ITimeStamp) time).getModifiedJulianDay() > ((IEngine) this.engineApiAccess).getTimeHorizon().getEnd().getModifiedJulianDay()) {
                    throw new RuntimeException("GetValues method was invoked using a time argument that represents a time that is after the allowed time horizon");
                }
            }
        }
        else {
            throw new Exception("Illegal data type for time was used in argument to GetValues method. Type must be org.openmi.standard.ITimeStamp or org.openmi.standard.ITimeSpan");
        }
    }

}
