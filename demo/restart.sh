#!/bin/sh
name=demo-0.0.1.jar
pid=`ps -ef | grep ${name}|grep -v grep|awk '{print $2}'`
for id in $pid
	do
	   kill -9 $id
	done 
nohup java -jar ./${name} >>./log.txt &
tail -f ./log.txt
