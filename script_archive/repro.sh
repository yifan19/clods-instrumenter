#!/bin/bash

# echo INSTRUMENTATION_HOME$INSTRUMENTATION_HOME
# myinstrumentation_jar=$INSTRUMENTATION_HOME/target/uber-blameMasterInstrument-1.0.jar

# cd $INSTRUMENTATION_HOME
# mvn package -DskipTests

HADOOP_VERSION=$HADOOP_VERSION
INST_OP=$1
INST_ROUND=$2
NUM=$3
# NUM=$3
cd /home/ubuntu/hadoop-$HADOOP_VERSION/

sbin/stop-dfs.sh

echo "reset namenode"
rm -rf /tmp/hadoop-hadoop/
bin/hadoop namenode -format <<< 'y'

echo "reset datanodes"
for slave in $(cat ./etc/hadoop/slaves)
do
    echo $slave
    ssh $slave "rm -rf /tmp/hadoop-ubuntu/"
done

# echo "restart dfs"
sbin/start-dfs.sh
# # exit 0

sleep 20

if [ "$1" == "add" ]
then
    echo instrument
    bash ~/run_n.sh inject
    bash ~/run_n.sh $@ 
    sleep 5
    name=r$INST_ROUND
elif [ "$1" == "baseline" ]
then
    name=baseline
else
    name=nothing
fi
BUGNUM=${4}_${name}
mkdir -p $BUGNUM


./bin/hadoop jar  \
share/hadoop/mapreduce/hadoop-mapreduce-client-jobclient-${HADOOP_VERSION}-tests.jar nnbenchWithoutMR \
-operation createWrite \
-baseDir /abc \
-startTime 0 \
-numFiles $NUM \
-blocksPerFile 1 \
-bytesPerBlock 134217728 \
-replicationFactorPerFile 3 \
-filesize 4096 \
| tee  $BUGNUM/write$name.log

bash  ~/run_n.sh  collect | tee  $BUGNUM/write$name.result

./bin/hadoop jar  \
share/hadoop/mapreduce/hadoop-mapreduce-client-jobclient-${HADOOP_VERSION}-tests.jar nnbenchWithoutMR \
-operation openRead \
-baseDir /abc \
-startTime 0 \
-numFiles $NUM \
-blocksPerFile 1 \
-bytesPerBlock 134217728 \
-replicationFactorPerFile 3 \
-filesize 4096 \
| tee  $BUGNUM/read$name.log

bash  ~/run_n.sh  collect | tee  $BUGNUM/read$name.result
mv /data/* ./$BUGNUM/

# -bytesPerBlock 134217728 \

# [-bytesPerChecksum <value for io.bytes.per.checksum>]
# Note: bytesPerBlock MUST be a multiple of bytesPerChecksum
# ./bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-client-jobclient-2.5.2-tests.jar nnbench \
# 	-operation create_write \
# 	-blockSize 134217728\
# 	-byteToWrite 1342177280\
# 	-numberOfFiles 1\
# 	-replicationFactorPerFile 3

# -operation <one of createWrite, openRead, rename, or delete>
# -baseDir <base output/input DFS path>
# -startTime <time to start, given in seconds from the epoch>
# -numFiles <number of files to create>
# -blocksPerFile <number of blocks to create per file> 
# [-bytesPerBlock <number of bytes to write to each block, default is 1>]
# [-bytesPerChecksum <value for io.bytes.per.checksum>]
# Note: bytesPerBlock MUST be a multiple of bytesPerChecksum
