#!/bin/bash

HADOOP_HOME_DEFAULT='/home/ubuntu/hadoop-2.8.2/'
# HOME_PATH_DEFAULT='/home/hadoop/log_compare/'
INSTRUMENTATION_HOME='/home/ubuntu/bm_instrument'
# fsck_bash=$HADOOP_HOME_DEFAULT'fsck_npe.sh'
echo HADOOP_HOME=$HADOOP_HOME
# echo COMPARE_HOME$COMPARE_HOME
echo INSTRUMENTATION_HOME$INSTRUMENTATION_HOME
# echo fsck_bash:$fsck_bash

cd $HADOOP_HOME
reportdir=$HADOOP_HOME/logs/hadoop-hadoop-namenode-hadoop-node.log
logfile=$HADOOP_HOME/logs/hadoop-hadoop-namenode-hadoop-node.out

instrumentation_plan_dir=$INST_DIR
myinstrumentation_jar=$INSTRUMENTATION_HOME/target/uber-blameMasterInstrument-1.0.jar


cd -

if [ "$1" == "inject" ]
then
testpid=$(jps | grep -i ' namenode' | awk '{print $1}')
echo $testpid
java -cp $JAVA_HOME/lib/tools.jar:$myinstrumentation_jar \
	ca.uoft.drsg.bminstrument.Launcher $myinstrumentation_jar $testpid
exit 0
fi

# echo java -cp $JAVA_HOME/lib/tools.jar:$myinstrumentation_jar \
# 	ca.uoft.drsg.bminstrument.PseudoClient "delete 2"
# exit 0
#> $reportdir
#> $logfile

#file=$instrumentation_plan_dir/$filename
#java -cp $JAVA_HOME/lib/tools.jar:$myinstrumentation_jar \
#    ca.uoft.drsg.bminstrument.PseudoClient "add $ID6"
#echo "ID 6"

# cd $HADOOP_HOME
# echo 'cd to hadoop home =='
# # pwd
# bash $fsck_bash &
# RUN_PID=$!
# sleep 20



if [ "$1" == "add" ]
then
instrumentation_plans=$(ls $instrumentation_plan_dir/r$2*.properties)
# result_dir=$COMPARE_HOME/python/npe_logs/
echo $instrumentation_plans

for plan in $instrumentation_plans
do
    java -cp $JAVA_HOME/lib/tools.jar:$myinstrumentation_jar \
        ca.uoft.drsg.bminstrument.PseudoClient "add $plan"
done
exit 0
fi

if [ "$1" == "collect" ]
then
java -cp $JAVA_HOME/lib/tools.jar:$myinstrumentation_jar \
        ca.uoft.drsg.bminstrument.PseudoClient "collect all"
fi

# for plan in $(seq 0 6)
# do
    # 
# 	echo java -cp $JAVA_HOME/lib/tools.jar:$myinstrumentation_jar \
#         ca.uoft.drsg.bminstrument.PseudoClient "delete $plan"
# done

echo 'done instrumenting'

# wait $RUN_PID

# cd -

# if grep -q "4 NullPointerException" $reportdir; then
# 	echo "NPE happened"
# else
# 	echo "NPE did not happen"
# fi

# sleep 10

# cat  $reportdir >> npe.log
# cat  $logfile >> npe.out

# awk '/\[BM\]/ {print} /Start Stack Trace/,/End Stack Trace/ {print}' $logfile > $resultdir/npe.log

# cd -
# echo 'Done'

# echo current_b$id.log


