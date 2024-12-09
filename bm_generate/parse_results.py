import re

def count_lines_between_markers(file_path):
    with open(file_path, 'r') as file:
        in_block = False
        count = 0

        for line in file:
            line = line.strip()

            if  "==START===" in line:
                in_block = True
                count = 0  # Reset the count for the new block
            
            elif "===END===" in line:
                res = re.search('===(\d+)', line)
                if in_block:
                    print(f"{count}, {res.groups()[0]}")
                    in_block = False
            
            elif in_block and "[BM]" in line:
                count += 1

# Replace 'your_file.txt' with the path to your file
path='/home/ubuntu/hadoop/hadoop-hdfs-project/hadoop-hdfs/target/surefire-reports/org.apache.hadoop.hdfs.server.blockmanagement.TestPendingReplication-output.txt'
count_lines_between_markers(path)
