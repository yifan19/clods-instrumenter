workpem="~/work.pem"
option=''
keys="/home/ubuntu/public_keys.txt"
slaves="/home/ubuntu/etc/hadoop/slaves"
master="/home/ubuntu/etc/hadoop/masters"
# VERSION=2.5.2
HADOOP_REPO_NAME=${REPO_NAME:=hadoop}
HBASE_NAME=hbase-1.2.5
ZOOKEEPER_NAME=zookeeper-3.4.6
HADOOP_NAME=${HADOOP_NAME:=hadoop-2.5.2}
if [ $# -gt 0 ]
then
	option=$1
fi
echo $option
if [ "$option" = "gen" ]
then

if [ -f $keys ]
then
	rm $keys
    touch $keys
fi

	for slave in $(cat $slaves $master)
	do
		echo $slave
		ssh -i $workpem $slave 'ssh-keygen -t rsa -P "" -f ~/.ssh/id_rsa'
		ssh -i $workpem $slave 'cat ~/.ssh/id_rsa.pub' >> $keys
	done
	grep work ~/.ssh/authorized_keys >> $keys
	exit 0
fi

if [ "$option" = "clean" ]
then
echo hello
	# i=1
	for slave in $(cat $slaves)
	do
		echo cleaning $slave
		ssh -i $workpem $slave 'rm -rf /tmp/hadoop-ubuntu*'
		ssh -i $workpem $slave 'killall java'
		ssh -i $workpem $slave 'rm -rf /home/ubuntu/zk_storage/zookeeper/data'
		# ssh -i $workpem $slave "mkdir -p /home/ubuntu/zk_storage/zookeeper/data; echo $i > /home/ubuntu/zk_storage/zookeeper/data/myid"
		# i=$(echo "$i + 1" | bc)
	done
	killall java
	rm -rf /tmp/hadoop-ubuntu*
	echo 'y' | ~/$HADOOP_NAME/bin/hadoop namenode -format
	exit 0
fi


if [ "$option" = "start" ]
then
	cd ~/$HADOOP_NAME/; ./sbin/start-dfs.sh
	for slave in $(cat $slaves)
	do
		ssh -i $workpem $slave "cd ~/$ZOOKEEPER_NAME/; ./bin/zkServer.sh start"
	done
	cd ~/$HBASE_NAME/; ./bin/start-hbase.sh

	exit 0
fi
if [ "$option" = "stop" ]
then
	cd ~/$HADOOP_NAME/; ./sbin/stop-dfs.sh
	for slave in $(cat $slaves)
	do
		ssh -i $workpem $slave "cd ~/$ZOOKEEPER_NAME/; ./bin/zkServer.sh stop"
		ssh -i $workpem $slave "killall -s 9 java"

	done
	killall -s 9 java
	cd ~/$HBASE_NAME/; ./bin/stop-hbase.sh
	exit 0
fi



for slave in $(cat $slaves $master)
do
	echo sending keys to $slave
	scp -i $workpem $keys $slave:~/.ssh/authorized_keys
done

for slave in $(cat $slaves)
do
	echo updating openjdk $slave
	ssh -i $workpem $slave 'sudo apt update'
	ssh -i $workpem $slave 'sudo apt install openjdk-8-jdk'
done

for slave in $(cat $slaves $master)
do
	echo sending server $slave 
	# ssh -i $workpem $slave mkdir
	ssh -i $workpem $slave rm -rf ~/$HADOOP_NAME/
	scp -i $workpem  ~/$HADOOP_REPO_NAME/hadoop-dist/target/$HADOOP_NAME.tar.gz $slave:~/
	ssh -i $workpem $slave "tar -xzf /home/ubuntu/$HADOOP_NAME.tar.gz"
	scp -r -i $workpem ~/etc $slave:~/$HADOOP_NAME/
done
i=1
for slave in $(cat $slaves)
do
	echo sending server $slave
	ssh -i $workpem $slave "rm -rf ~/$ZOOKEEPER_NAME"

	scp -i $workpem  ~/$ZOOKEEPER_NAME.tar.gz $slave:~/

	ssh -i $workpem $slave "tar -xzf /home/ubuntu/$ZOOKEEPER_NAME.tar.gz"
	scp -r -i $workpem ~/conf $slave:~/$ZOOKEEPER_NAME/
	ssh -i $workpem $slave "mkdir -p /home/ubuntu/zk_storage/zookeeper/data; echo $i > /home/ubuntu/zk_storage/zookeeper/data/myid"
	i=$(echo "$i + 1" | bc)

done

for slave in $(cat $slaves $master)
do
	# ssh -i $workpem $slave mkdir
	ssh -i $workpem $slave rm -rf ~/$HBASE_NAME/
	scp -i $workpem  ~/$HBASE_NAME-bin.tar.gz $slave:~/
	ssh -i $workpem $slave "tar -xzf /home/ubuntu/$HBASE_NAME-bin.tar.gz"
	scp -r -i $workpem ~/hbase-config/* $slave:~/$HBASE_NAME/conf/
done


