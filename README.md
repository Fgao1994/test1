# Coupling Components and Services for IEM

  Integrated environmental modelling (IEM) couples environmental models, geoprocessing algorithms, and data together to solve complex environmental problems. There are two prominent modelling frameworks for IEM: component based and service oriented frameworks. The former one has been widely employed in local computer environments, and has its advantage in having concrete and community-wide tools. The latter one can take advantage of Web technologies for integrating distributed environmental models. This project takes the best of both frameworks. Models are shared on the Web as WebSocket services. A hybrid approach to leverage OpenMI and WebSocket for coupling components and services is proposed to provide a flexible IEM environment. 

WebSocket
    WebSocket(https://www.w3.org/TR/websockets/)
    
OpenMI
    OpenMI (https://www.openmi.org/)

## Documentation

* [API Reference](https://github.com/Fgao1994/test1/blob/master/Doc.docx)
* [TOPModel Example](https://github.com/Fgao1994/test1/)
* [Figures](https://github.com/Fgao1994/test1/)

### Overview
  The used case in the proposed IEM framework involves several hydrological models: Precipitation Model, Hargreaves model, and TOPMODEL. The Precipitation Model, Hargreaves model are time-dependent model that will generate runoff on an area. The primary model used to generate runoff is the Topography-based hydrological MODEL (TOPMODEL), which is a continuous simulation model and has been applied in various regions throughout the world. The model takes the daily precipitation, daily evapotranspiration, and topographic index as inputs, and outputs daily runoff. In the case, we wrapped TOPMODEL as a Http and WebSocket service separately.
  
### Key Class/Interface
OpenMIWebSocketClient:

## Status

The project provides a complete and tested implementation of 
TOPModel with OpenMI and WebSocket protocol. The package API 
is stable.
