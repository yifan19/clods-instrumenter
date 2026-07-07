
# export HADOOP_NAME=hadoop-2.5.2
# export INST_DIR=~/instrumentation_bug1
# export REPO_NAME=hadoop
# bash benchmark_ycsb.sh add 4 10000000 1

# export HADOOP_NAME=hadoop-2.7.4-SNAPSHOT
# export INST_DIR=~/instrumentation_bug3
# export REPO_NAME=hadoop-bug3
# # bash repro.sh x x 10
# bash benchmark_ycsb.sh baseline 1 10000000 3
# bash benchmark_ycsb.sh add 1 10000000 3
# bash benchmark_ycsb.sh add 2 10000000 3
# 
export HADOOP_NAME=hadoop-2.8.2
export INST_DIR=~/instrumentation_bug2
export REPO_NAME=hadoop-bug2
# # # bash repro.sh x x 10
# bash benchmark_ycsb.sh baseline 1 10000000 2
bash benchmark_ycsb.sh add 1 10000000 2
# bash benchmark_ycsb.sh add 2 10000000 2
# bash benchmark_ycsb.sh add 3 10000000 2
