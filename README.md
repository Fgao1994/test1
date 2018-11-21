# Coupling Components and Services for IEM

Integrated environmental modelling (IEM) couples environmental models, geoprocessing algorithms, and data together to solve complex environmental problems. There are two prominent modelling frameworks for IEM: component based and service oriented frameworks. The former one has been widely employed in local computer environments, and has its advantage in having concrete and community-wide tools. The latter one can take advantage of Web technologies for integrating distributed environmental models. This project takes the best of both frameworks. Models are shared on the Web as WebSocket services. A hybrid approach to leverage OpenMI and WebSocket for coupling components and services is proposed to provide a flexible IEM environment. 

WebSocket(https://www.w3.org/TR/websockets/)
    
OpenMI(https://www.openmi.org/)

## Documentation

* [API Reference](https://github.com/Fgao1994/test1/blob/master/Doc.docx)
* [TOPModel Example](https://github.com/Fgao1994/test1/)
* [Figures](https://github.com/Fgao1994/test1/tree/master/Figures) 
  
## The role of client and server in the IEM case 

To evaluate the effectiveness of deploying models as WebSocket services, we did a comparative analysis of performance over model simulation between WebSocket and HTTP protocols.So client-end includes HTTP-based client and WebSocket-based client, the server-end includes HTTP-based server and WebSocket-based server.

### Client:
1. PrecipitationModel: Read the daily precipitation and wait TOPMODEL pull the daily precipitation;
2. HargreavesModel: Read the daily temperature data and latitude data, and calculate the daily evapotranspiration and to wait TOPMODEL to pull. Note that you can write the daily evapotranspiration to local file system to check;
3. HttpClientTopModel: Pull the daily precipitation and evapotranspiration from another two models, then send a request with the data to HTTP server of TOPMODEL;
4. WebSocketClientTopModel: Pull the daily precipitation and evapotranspiration from another two models, then send a request with the data to WebSocket server of TOPMODEL.

### Server:
1. HttpServerTopModel: Get the daily precipitation and evapotranspiration, then calculate the daily runoff and return to client;
2. WebSocketServerTopModel: Drive the run phase of the OpenMI component and calculate the daily runoff. 

## Status

The project provides a complete and tested implementation of 
a IEM case with OpenMI and WebSocket protocol. The package API 
is stable.

## Contact

Contact: Peng Yue
School of Remote Sensing and Information Engineering, Wuhan University
129 Luoyu Road, Wuhan, Hubei, China, 430079
pyue@whu.edu.cn
Contact: Fan Gao
School of Remote Sensing and Information Engineering, Wuhan University
129 Luoyu Road, Wuhan, Hubei, China, 430079
fgao_whu@163.com

