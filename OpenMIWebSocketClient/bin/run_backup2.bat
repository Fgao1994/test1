@echo off
cd D:\websocket\websocket-workspace\OpenMIWebSocketClient\bin

set /a Count=10

echo --------------------------Local----------------------------------------
echo start time:%time%
set /a StartH=%time:~0,2%
if  /i %StartH%  LSS  10   ( set  StartH=%time:~1,1% )
set /a StartM=%time:~3,2% 
if  /i %StartM%  LSS  10   ( set  StartM=%time:~4,1% )
set /a StartS=%time:~6,2% 
if  /i %StartS%  LSS  10   ( set  StartS=%time:~7,1% )
set /a StartMS=%time:~9,2%
if  /i %StartMS%  LSS  10   ( set  StartMS=%time:~10,1% )
::echo StartS:%StartS%,StartM:%StartM%,StartMS:%StartMS%

for /l %%i in (1,1,%Count%) do (
java -classpath ".;../lib/dom4j-1.6.1.jar;../lib/Java-WebSocket-1.3.0.jar;../lib/log4j-1.2.17.jar;../lib/jaxen-1.1.1.jar" cn.edu.whu.openmi.models.topmodel.TopmodelLocalMainTest
)

echo entime:%time%
set /a EndH = %time:~0,2%
if  /i %EndH%  LSS  10   ( set  EndH=%time:~1,1% )
set /a EndM=%time:~3,2% 
if  /i %EndM%  LSS  10   ( set  EndM=%time:~4,1% )
set /a EndS=%time:~6,2% 
if  /i %EndS%  LSS  10   ( set  EndS=%time:~7,1% )
set /a EndMS=%time:~9,2% 
if  /i %EndMS%  LSS  10   ( set  EndMS=%time:~10,1% )

::echo EndM:%EndM%,EndS:%EndS%,EndMS:%EndMS%
set /a  diffH=%EndH%-%StartH%
set /a  diffM=%EndM%-%StartM%
set /a  diffS=%EndS%-%StartS%
set /a  diffMS =%EndMS%-%StartMS%
set /a  diff_local = ((%diffH%*3600+%diffM%*60+%diffS%)*1000+%diffMS%)/%Count%
echo diffM:%diffM%,diffS:%diffS%,diffMS:%diffMS%
echo websocket spend:%diff_local%

echo --------------------------HTTP----------------------------------------

echo start time:%time%
set /a StartH=%time:~0,2%
if  /i %StartH%  LSS  10   ( set  StartH=%time:~1,1% )
set /a StartM=%time:~3,2% 
if  /i %StartM%  LSS  10   ( set  StartM=%time:~4,1% )
set /a StartS=%time:~6,2% 
if  /i %StartS%  LSS  10   ( set  StartS=%time:~7,1% )
set /a StartMS=%time:~9,2%
if  /i %StartMS%  LSS  10   ( set  StartMS=%time:~10,1% )
::echo StartS:%StartS%,StartM:%StartM%,StartMS:%StartMS%

for /l %%i in (1,1,%Count%) do (
java -classpath ".;../lib/dom4j-1.6.1.jar;../lib/Java-WebSocket-1.3.0.jar;../lib/log4j-1.2.17.jar;../lib/jaxen-1.1.1.jar" cn.edu.whu.httpmodel.HttpTopmodelMainTest
)

echo entime:%time%
set /a EndH = %time:~0,2%
if  /i %EndH%  LSS  10   ( set  EndH=%time:~1,1% )
set /a EndM=%time:~3,2% 
if  /i %EndM%  LSS  10   ( set  EndM=%time:~4,1% )
set /a EndS=%time:~6,2% 
if  /i %EndS%  LSS  10   ( set  EndS=%time:~7,1% )
set /a EndMS=%time:~9,2% 
if  /i %EndMS%  LSS  10   ( set  EndMS=%time:~10,1% )

::echo EndM:%EndM%,EndS:%EndS%,EndMS:%EndMS%
set /a  diffH=%EndH%-%StartH%
set /a  diffM=%EndM%-%StartM%
set /a  diffS=%EndS%-%StartS%
set /a  diffMS =%EndMS%-%StartMS%
set /a  diff_http = ((%diffH%*3600+%diffM%*60+%diffS%)*1000+%diffMS%)/%Count%
echo diffM:%diffM%,diffS:%diffS%,diffMS:%diffMS%
echo websocket spend:%diff_http%

echo --------------------------WebSocket----------------------------------------

echo start time:%time%
set /a StartH=%time:~0,2%
if  /i %StartH%  LSS  10   ( set  StartH=%time:~1,1% )
set /a StartM=%time:~3,2% 
if  /i %StartM%  LSS  10   ( set  StartM=%time:~4,1% )
set /a StartS=%time:~6,2% 
if  /i %StartS%  LSS  10   ( set  StartS=%time:~7,1% )
set /a StartMS=%time:~9,2%
if  /i %StartMS%  LSS  10   ( set  StartMS=%time:~10,1% )
::echo StartS:%StartS%,StartM:%StartM%,StartMS:%StartMS%

for /l %%i in (1,1,%Count%) do (
java -classpath ".;../lib/dom4j-1.6.1.jar;../lib/Java-WebSocket-1.3.0.jar;../lib/log4j-1.2.17.jar;../lib/jaxen-1.1.1.jar" cn.edu.whu.wssync.WSSyncMainTest
)

echo entime:%time%
set /a EndH = %time:~0,2%
if  /i %EndH%  LSS  10   ( set  EndH=%time:~1,1% )
set /a EndM=%time:~3,2% 
if  /i %EndM%  LSS  10   ( set  EndM=%time:~4,1% )
set /a EndS=%time:~6,2% 
if  /i %EndS%  LSS  10   ( set  EndS=%time:~7,1% )
set /a EndMS=%time:~9,2% 
if  /i %EndMS%  LSS  10   ( set  EndMS=%time:~10,1% )

::echo EndM:%EndM%,EndS:%EndS%,EndMS:%EndMS%
set /a  diffH=%EndH%-%StartH%
set /a  diffM=%EndM%-%StartM%
set /a  diffS=%EndS%-%StartS%
set /a  diffMS =%EndMS%-%StartMS%
set /a  diff_websocket = ((%diffH%*3600+%diffM%*60+%diffS%)*1000+%diffMS%)/%Count%
echo diffM:%diffM%,diffS:%diffS%,diffMS:%diffMS%
echo websocket spend:%diff_websocket%

echo ##############################Final##########################################
echo local spend:%diff_local%,Http spend:%diff_http%,websocket spend:%diff_websocket%
pause

