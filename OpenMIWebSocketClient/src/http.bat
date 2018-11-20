@echo off
cd D:\websocket\websocket-workspace\OpenMIWebSocketClient\bin
set /a Count=10
echo --------------------------HTTP----------------------------------------

echo starttime:%time%

set /a Zero = %time:~0,1%
IF %Zero% == 0 (
	set /a StartH=%time:~1,1%
) ELSE (
	set /a StartH=%time:~0,2%
)

set /a Zero = %time:~3,1%
IF %Zero% == 0 (
	set /a StartM=%time:~4,1%
) ELSE (
	set /a StartM=%time:~3,2%
)

set /a Zero = %time:~6,1%
IF %Zero% == 0 (
	set /a StartS=%time:~7,1%
) ELSE (
	set /a StartS=%time:~6,2% 
)

set /a Zero = %time:~9,1%
IF %Zero% == 0 (
	set /a StartMS=%time:~10,1%
) ELSE (
	set /a StartMS=%time:~9,2% 
)

::echo StartS:%StartS%,StartM:%StartM%,StartMS:%StartMS%

for /l %%i in (1,1,%Count%) do (
	
    java -classpath ".;../lib/dom4j-1.6.1.jar;../lib/Java-WebSocket-1.3.0.jar;../lib/log4j-1.2.17.jar;../lib/jaxen-1.1.1.jar" cn.edu.whu.test.wshttp.MyHttpClient
    
    
)

echo endtime:%time%

set /a Zero = %time:~0,1%
IF %Zero% == 0 (
	set /a EndH=%time:~1,1%
) ELSE (
	set EndH=%time:~0,2%
)

set /a Zero = %time:~3,1%
IF %Zero% == 0 (
	set /a EndM=%time:~4,1%
) ELSE (
	set /a EndM=%time:~3,2%
)

set /a Zero = %time:~6,1%
IF %Zero% == 0 (
	set /a EndS=%time:~7,1%
) ELSE (
	set /a EndS=%time:~6,2%
)

set /a Zero = %time:~9,1%
IF %Zero% == 0 (
	set /a EndMS=%time:~10,1%
) ELSE (
	set /a EndMS=%time:~9,2% 
)

::echo EndM:%EndM%,EndS:%EndS%,EndMS:%EndMS%
set /a  diffH=%EndH%-%StartH%
set /a  diffM=%EndM%-%StartM%
set /a  diffS=%EndS%-%StartS%
set /a  diffMS =(%EndMS%-%StartMS%)*10

set /a  diff_http = ((%diffH%*3600+%diffM%*60+%diffS%)*1000+%diffMS%)/%Count%
echo diffH:%diffH%,diffM:%diffM%,diffS:%diffS%,diffMS:%diffMS%
echo http spend:%diff_http%
pause