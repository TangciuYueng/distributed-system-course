#!/bin/bash

if [ $# -ne 1 ]; then 
    echo one file name plz
    exit 1
fi

line_cnt=$(wc -l < $1)
echo $line_cnt
echo $line_cnt > 2151298-hw1-q3.log

char_cnt=$(wc -m < $1)
echo $char_cnt
echo $char_cnt >> 2151298-hw1-q3.log

start_time=$(head -n 1 $1 | awk '{print $1}')
end_time=$(tail -n 1 $1 | awk '{print $1}')
# convert into timestamp
# -d to specify the time string
start_time_sec=$(date -d $start_time +%s)
end_time_sec=$(date -d $end_time +%s)
interval=$((end_time_sec - start_time_sec))
echo $interval
echo $interval >> 2151298-hw1-q3.log

# get 3 col with awk
# gsub to remove ","
avg=$(awk '{
        for (i = NF-2; i <= NF; i++) {
            gsub(",", "", $i);
            sum[i] += $i;
        }
        count++
    } 
    END {
        if (count >0) {
            for (i = NF-2; i <= NF; i++) {
                avg[i] = sum[i] / count;
            }
            printf "%.2f %.2f %.2f\n", avg[NF-2], avg[NF - 1], avg[NF];
        }
    }' $1)

echo $avg
echo $avg >> 2151298-hw1-q3.log
