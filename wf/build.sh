#!/bin/ksh

USERID=`whoami`

if [ "$1" = "" ];then
 echo "usage:  build.sh env"
 exit 9
fi

#DEBUG=-X
ENV=$1
CLEAN=$2
PROJECT=vc
EAR=$PROJECT.ear


if [ "$ENV" = "dev" -o "$ENV" = "dev1" ];then
   SERVER=mypi
   TARGET=/home/wryan/jvm1/standalone/deployments
   PROJECTS=""
   PROFILES=""
fi



if [ "$TARGET" = "" -a "$TARGETS" = "" ];then
  echo Unrecognized environment
  exit 9
fi


mvn $DEBUG -pl "$PROJECTS" -P "$PROFILES" $CLEAN install
if [ $? != 0 ];then
  echo compile failed
  exit 9
fi


chmod 664 ear/target/$EAR 
set -x
scp -p    ear/target/$EAR $SERVER:$TARGET/../lz 2>/dev/null
RET=$?


if [ $RET != 0 ];then
  echo scp failed, does the lz exist?
  exit 9
fi


if [ "$ENV" != "prod83" ];then
if [ "$ENV" != "prod84" ];then
  ssh $SERVER mv $TARGET/../lz/$EAR $TARGET 2>/dev/null
fi
fi

echo success

#ssh $SERVER cp -p /tmp/$USERID/$EAR $TARGET 2>/dev/null
#echo ENV=$1:SERVER=$SERVER:/tmp/$USERID/$EAR  success

