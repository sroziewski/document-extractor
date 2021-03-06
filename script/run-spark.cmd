@ECHO OFF

::Path to key
set key-folder=%DEV_TOOLS%\keys
set key-name=private.key.ppk

::Set cluster name every time a new instance is created
set remote-name=%1

:: Destination folder
set dst-folder=%2
set file-name=%3
set class-name=%4

@ECHO [info] Attempting to submit %dst-folder%%file-name% to Apache Spark Master at %remote-name%
@ECHO [info] Submitting %class-name% to Apache Spark

:: Create temporary script to be run on remote server
echo spark-submit --class %class-name% %dst-folder%%file-name% > scripts\remote-job.sh

:: Run script on remote machine
plink -i %key-folder%\%key-name% -t %remote-name% -m scripts\remote-job.sh

:: Clean
del /F scripts\remote-job.sh

@ECHO [info] Remote command finished.

