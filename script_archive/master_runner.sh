
export HADOOP_VERSION=2.5.2
export INST_DIR=~/instrumentation_bug1
export REPO_NAME=hadoop
bash repro.sh baseline 1 1000000 1
bash repro.sh add 1 1000000 1
bash repro.sh add 2 1000000 1
bash repro.sh add 3 1000000 1
bash repro.sh add 4 1000000 1
bash repro.sh add 5 1000000 1
bash repro.sh add 6 1000000 1



export HADOOP_NAME=hadoop-2.7.4-SNAPSHOT
export HADOOP_VERSION=2.7.4-SNAPSHOT
export INST_DIR=~/instrumentation_bug3
export REPO_NAME=hadoop-bug3
# bash repro.sh x x 1000000
bash repro.sh baseline 1 1000000 3
bash repro.sh add 1 1000000 3
bash repro.sh add 2 1000000 3

export HADOOP_NAME=hadoop-2.8.2
export INST_DIR=~/instrumentation_bug2
export REPO_NAME=hadoop-bug2
# # bash repro.sh x x 1000000
bash repro.sh baseline 1 1000000 2
bash repro.sh add 1 1000000 2
bash repro.sh add 2 1000000 2
bash repro.sh add 3 1000000 2
