import re
import glob
import statistics
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
import pandas as pd

def parse_num_instrumentation(file_path):
    with open(file_path, 'r') as file:
        content = file.read()
    ok_line = re.findall(r'OK(.*)', content)
    # print(ok_line)
    if ok_line == []:
        return 0
    numbers_after_ok = re.findall(r'(\d+)\s*', ok_line[0])
    # Convert the extracted numbers to integers and calculate the sum
    sum_of_numbers = sum(map(int, numbers_after_ok))
    return sum_of_numbers
    
def parse_file(file_path, format='READ'):
    # timestamp = []
    duration_ms = []
    with open(file_path, 'r') as file:
        for line in file:
            content = re.search( format + r',(\d+),(\d+)', line)
            if content is None:
                continue
            # timestamp.append(int(content.group(1)))
            duration_ms.append(int(content.group(2)))
    return {
        # 'timestamp': timestamp,
        'results': duration_ms
    }

def compute_statistics(data):
    # Compute additional statistics based on the extracted information
    average_result = statistics.mean(data)
    max_result = max(data)
    min_result = min(data)
    stdv_result = statistics.stdev(data)
    total = len(data)

    return {
        'average_result': average_result,
        'max_result': max_result,
        'min_result': min_result,
        'stdv_result' : stdv_result,
        'total': total,
    }

def generate_boxplot(data, name, order):
    # plt.boxplot(data, labels=labels)
    
    # colors = ['lightblue', 'lightgreen', 'lightcoral', 'lightsalmon']
    # for patch, color in zip(box['boxes'], colors):
    #     patch.set_facecolor(color)

    # # Customize median line style
    # for median in box['medians']:
    #     median.set(color='black', linewidth=2)

    # # Customize whisker and cap style
    # for whisker, cap in zip(box['whiskers'], box['caps']):
    #     whisker.set(color='black', linestyle='-', linewidth=1.5)
    #     cap.set(color='black', linewidth=1.5)

    # # Customize flier style
    # for flier in box['fliers']:
    #     flier.set(marker='o', markersize=5, markerfacecolor='red', markeredgecolor='black')
    df = pd.DataFrame(data)
    df = df[order]

    # Set up the figure and axis
    plt.figure(figsize=(10, 6))

    # Create a box plot
    sns.boxplot(data=df)

    # Set labels and title
    plt.xlabel('Configurations')
    plt.ylabel('Time to Complete operation (ms)')
    plt.title('Time taken for 1M operation of size 4KB & replication factor=3')
    # plt.ylim(0,5)

    plt.savefig(name + '.jpeg')  # Save the plot as a JPEG file

def gen_detaset(db, op, patterns):
    dataset={}
    columns=[]
    for pattern in patterns:
        num_instr = db[pattern][op]['num_instr']
        data = db[pattern][op]['data'][1000:]
        key = f'{pattern}_{op}, \ni={num_instr}'
        columns.append(key)
        dataset[key] = data

    return dataset, columns

def main():
    
    patterns = ['baseline']
    patterns.extend([f'r{i}' for i in range (1,7)])
    # patterns.extend(['r2'])
    print("patterns:", patterns)
    annot = {
        'read': 'READ',
        'write': 'INSERT'
    }
    db = {}
    for pattern in patterns:
        round_number = pattern
        if pattern[0] == 'r':
            round_number = pattern[1:]
        logs = glob.glob( f'/home/ubuntu/ycsb-0.12.0/1_{round_number}/*{pattern}*.log')
        print(logs)
        db_per_round = {}
        for log in logs:
            op = re.search(rf'(.+){pattern}\.log', log.split('/')[-1]).group(1)
            log_result = log.replace('.log', '.result')
            data = parse_file(log, annot[op])
            stats = compute_statistics(data['results'][10000:])
            for key, value in stats.items():
                print(f"{key}: {value}")
            num_instrumentation_point = parse_num_instrumentation(log_result)
            output = {
                'data': data['results'],
                'num_instr': num_instrumentation_point,
            }
            db_per_round[op] = output
        db[pattern] = db_per_round
        db[pattern]['read']['num_instr'] -= db[pattern]['write']['num_instr']
            # print(num_instrumentation_point)
            
    # generate_boxplot(
    #     [ db['baseline']['write']['data'][1000:200000],
    #     db['r1']['write']['data'][1000:200000],
    #     db['r2']['write']['data'][1000:200000],
    #     db['r3']['write']['data'][1000:200000]],
    #     labels=patterns
    # )
    
    write_dataset, write_keys = gen_detaset(db, 'write', patterns)
    read_dataset, read_keys = gen_detaset(db, 'read', patterns)

    generate_boxplot(
        write_dataset, name='write', order=write_keys
    )
    generate_boxplot(
        read_dataset, name='read', order=read_keys
    )
#     generate_boxplot(
#         {
#      'baseline_read, i=0': db['baseline']['read']['data'][1000:],
#    #        'r1_read, i=' + str(db['r1']['read']['num_instr']) 
#    #            : db['r1']['read']['data'][1000:],
#             'r2_read, i=' + str(db['r2']['read']['num_instr'])
#                 : db['r2']['read']['data'][1000:],
#            'r4_read, i=' + str(db['r4']['read']['num_instr'])
#                : db['r4']['read']['data'][1000:],
#             'r6_read, i=' + str(db['r6']['read']['num_instr'])
#                : db['r6']['read']['data'][1000:],
           
#             }, name = 'read'
#     )
    
    # Displaying the results
    # print("File Information:")
    # for key, value in data.items():
    #     print(f"{key}: {value}")


if __name__ == "__main__":
    main()
    # parse_file('/home/ubuntu/ycsb-0.12.0/1_1/readr1.log')
