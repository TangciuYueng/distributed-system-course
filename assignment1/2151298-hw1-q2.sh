#!/bin/bash
cnt=0

res=$(uptime)
echo $res
echo $res > 2151298-hw1-q2.log

while :
do 
    sleep 10

    res=$(uptime)
    echo $res
    echo $res >> 2151298-hw1-q2.log

    let cnt++

    if ((cnt >= 15)); then
        break
    fi
done
