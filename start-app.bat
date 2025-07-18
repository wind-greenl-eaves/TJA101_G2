@echo off
echo Starting EatFast Application with Stability Configuration...

REM Create dumps directory if it doesn't exist
if not exist "dumps" mkdir dumps

REM JVM Memory Configuration
set MEMORY_OPTS=-Xms512m -Xmx2g -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m

REM JVM Compiler Stability Configuration
set COMPILER_OPTS=-XX:+TieredCompilation -XX:CompileThreshold=10000 -XX:-BackgroundCompilation

REM GC Configuration - Use G1GC for better stability
set GC_OPTS=-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:G1HeapRegionSize=16m

REM Safety and Stability Configuration
set SAFETY_OPTS=-XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./dumps/

REM Compiler Safety Configuration
set INLINE_OPTS=-XX:MaxInlineSize=35 -XX:FreqInlineSize=325

REM Additional Stability Options
set STABILITY_OPTS=-XX:+UseStringDeduplication -XX:+OptimizeStringConcat -XX:+UnlockExperimentalVMOptions

REM Combine all JVM options
set JVM_OPTS=%MEMORY_OPTS% %COMPILER_OPTS% %GC_OPTS% %SAFETY_OPTS% %INLINE_OPTS% %STABILITY_OPTS%

echo JVM Options: %JVM_OPTS%
echo.

REM Start the application with Maven
mvn spring-boot:run -Dspring-boot.run.jvmArguments="%JVM_OPTS%"

pause