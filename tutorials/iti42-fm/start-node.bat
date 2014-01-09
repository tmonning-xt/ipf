@echo off

rem --------------------------------------------------------
rem  Derive JMX port from second argument (or use default)
rem --------------------------------------------------------
set PORT=%1
if "%PORT%" == "" set PORT=1801

set REGPORT=%2
if "%REGPORT%" == "" set REGPORT=1091

rem --------------------------------------------------------
rem  JMX and cluster node system properties
rem --------------------------------------------------------
set JAVA_OPTS=-Xmx256m -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=%PORT% -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dorg.apache.camel.jmx.rmiConnector.registryPort=%REGPORT%

rem --------------------------------------------------------
rem  Startup tutorial server (cluster node)
rem --------------------------------------------------------
"%JAVA_HOME%\bin\java.exe" -javaagent:lib\spring-agent.jar %JAVA_OPTS% -cp conf;lib\* org.openehealth.ipf.tutorials.iti42.Server
