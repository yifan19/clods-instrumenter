HADOOP_HOME=/home/ubuntu/hadoop
reportdir=hadoop-hdfs-project/hadoop-hdfs/target/surefire-reports
logfile=$reportdir/org.apache.hadoop.hdfs.server.blockmanagement.TestPendingReplication-output.txt
instrumentation_plan_dir=/home/ubuntu/plans2/

myinstrumentation_jar=/home/ubuntu/bm_instrument/target/uber-blameMasterInstrument-1.0.jar
echo java -cp $JAVA_HOME/lib/tools.jar:$myinstrumentation_jar \
    ca.uoft.drsg.bminstrument.Launcher $myinstrumentation_jar $testpid

# instrumentation_plans=$(ls $instrumentation_plan_dir/$1*)
# echo $instrumentation_plans
# exit 0

cd /home/ubuntu/bm_instrument/
mvn package -DskipTests
cd -

cd $HADOOP_HOME
rm $reportdir/*
mvn test '-Dtest=TestPendingReplication#testProcessPendingReplications' > /dev/null & 
mvn_pid=$!

while ! test -f "$logfile"
do
  sleep 1
  echo "Still waiting"
done

sleep 1

testpid=$(jps | grep surefire | awk '{print $1}')
echo $testpid detected from
jps
java -cp $JAVA_HOME/lib/tools.jar:$myinstrumentation_jar \
    ca.uoft.drsg.bminstrument.Launcher $myinstrumentation_jar $testpid

# final_plan=""

# for plan in $instrumentation_plans
# do
#     final_plan="add $plan\n$final_plan"
# done
#     echo -e "$final_plan" > plan.txt
# echo hello!
    java -cp $JAVA_HOME/lib/tools.jar:$myinstrumentation_jar \
    ca.uoft.drsg.bminstrument.PseudoClient file plan.txt

echo done
# done
# java -cp $JAVA_HOME/lib/tools.jar:$myinstrumentation_jar \
#     ca.uoft.drsg.bminstrument.PseudoClient "add $instrumentation_plan_dir/0_1_instrumentation.properties"

# java -cp $JAVA_HOME/lib/tools.jar:`$myinstrumentation_jar \
#     ca.uoft.drsg.bminstrument.PseudoClient "add $instrumentation_plan_dir/0_1_instrumentation.properties"



# echo wait for until

# result=1
# while [ $result -ne 0 ]:
# do
#   grep "Test ended, collect your data" $logfile
#   result=$?
#   echo $result
#   sleep 1
#   echo "Still waiting to collect result"
# done


# java -cp $JAVA_HOME/lib/tools.jar:$myinstrumentation_jar \
#     ca.uoft.drsg.bminstrument.PseudoClient "collect all"

wait $mvn_pid
