
# export HADOOP_NAME=2.5.2
# export INST_DIR=~/instrumentation_bug1
INST_OP=$1
INST_ROUND=$2
NUM=$3

echo $@

YCSB_NAME=ycsb-0.12.0
HBASE_NAME=hbase-1.2.5

echo $NUM
bash ~/old_setup.sh stop
bash ~/old_setup.sh clean
bash ~/old_setup.sh
bash ~/old_setup.sh start
sleep 10

cd ~/$HBASE_NAME
echo "create 'ycsb', 'cf'" | ./bin/hbase shell 
sleep 10
cd -
# bash ~/old_setup.sh stop
# bash ~/old_setup.sh start

if [ "$INST_OP" = "add" ]
then
    echo instrument
    bash ~/run_n.sh inject
    bash ~/run_n.sh $@
    sleep 5
    name=r$INST_ROUND
elif [ "$INST_OP" = "baseline" ]
then
    name=baseline
else
    name=nothing
fi

BUGNUM=${4}_${name}
sleep 10

cd ~/$YCSB_NAME
mkdir $BUGNUM
./bin/ycsb load hbase10 -P workloads/workloada -s \
           -p exportfile=result.csv \
           -p recordcount=$NUM \
           -p operationcount=$NUM 2>&1 | tee $BUGNUM/write$name.log
           
bash  ~/run_n.sh  collect | tee  $BUGNUM/write$name.result

./bin/ycsb run hbase10 -P workloads/workloada -s \
           -p exportfile=result.csv \
           -p recordcount=$NUM \
           -p operationcount=$NUM 2>&1 | tee $BUGNUM/read$name.log
           
bash  ~/run_n.sh  collect | tee  $BUGNUM/read$name.result
mv /data/* ./$BUGNUM/
           