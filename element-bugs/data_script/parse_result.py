import re
import numpy as np
import sys
import glob
import os
import argparse

def process(f, data='send'):
    if data == 'send':
        start_pattern = re.compile(r"START sendTextMessage = (\d+)")
        end_pattern = re.compile(r"END RoomSyncHandler = (\d+)")
        r = process_notif(f, start_pattern, end_pattern)
    elif data == 'notif':
        push_data_pattern = re.compile(r'SYNC/Push: .* pushData (\d+)')
        room_sync_pattern = re.compile(r'END RoomSyncHandler = (\d+)')
        r = process_notif(f, push_data_pattern, room_sync_pattern)

    dirname = os.path.dirname(f)
    num_round = os.path.basename(f).split('.')[0]
    instr_plan_path = os.path.join(dirname, '..', num_round)
    print(instr_plan_path)
    instr_plan = len(glob.glob(instr_plan_path + '/*.properties'))
    return process_data(*r, instr_plan)

def process_notif(f, s1,s2):
    timestamps = []
    start = 0
    num_inst = 0
    clods_stacktrace = re.compile(r'CLODS/-.*:\s*java.lang.Exception')
    clods_pattern = re.compile(r'CLODS/\d+')
    with open(f, 'r') as fd:
        for line in fd:
            line = line.replace(' ' * 126, '')
            push = s1.search(line)
            sync = s2.search(line)
            stack = clods_stacktrace.search(line)
            pattern = clods_pattern.search(line)
            if push:
                start = int(push[1])
            elif sync:
                # print(sync)
                if start > 0:
                    timestamps.append((int(sync[1]) - start)/1e6)
                    start = 0
            elif stack or pattern:
                # print('instr detected' + line)
                num_inst += 1

    print(len(timestamps))
    return timestamps, num_inst
            
def process_send(f):
    with open(f, 'r') as fd:
        log_data = fd.read()
    # Extract timestamps using regex


    start_times = list(map(int, re.findall(start_pattern, log_data)))
    end_times = list(map(int, re.findall(end_pattern, log_data)))

    # Combine start and end times into pairs
    timestamps = list(zip(start_times, end_times))

    # Calculate latencies in milliseconds
    results = [(end - start)/1e6 for start, end in timestamps]
    return results

def process_data(results, num_inst, instr_plan):
    latencies = results[len(results)-90:]
    # Compute average and p99 latencies
    average_latency = np.mean(latencies)
    stdev_latency = np.std(latencies)
    p99_latency = np.percentile(latencies, 99)

    r = {
        'avg(ms)': average_latency,
        'p99': p99_latency,
        'stdev': stdev_latency,
        '# instr points' : num_inst,
        '# instrumentation plan' : instr_plan,
    }
    return r

def main():

    parser = argparse.ArgumentParser("parse_result")
    parser.add_argument(
        '-t', '--types', 
        choices=['notif', 'send'], 
        required=True, 
        help="Specify the types to filter (e.g., 'notif', 'send')."
    )
    parser.add_argument(
        'files', 
        nargs='+', 
        help="List of input files to process."
    )
    args = parser.parse_args()

    results = []
    for f in args.files:
        print(os.path.basename(f), end=",")
        results.append(process(f, data=args.types))
    print()

    for metric in results[0]:
        print(metric, end=',')
        for r in results:
            print(r[metric], end=',')
        print()

    
main()