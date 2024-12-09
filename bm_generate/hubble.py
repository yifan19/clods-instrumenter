import argparse
import subprocess
import os
import zipfile
import re
import constants
from pathlib import Path

def extract_class_files(jar_file, output_dir):
    """Extract all class files from the jar."""
    with zipfile.ZipFile(jar_file, 'r') as jar:
        jar.extractall(output_dir)

def parse_bytecode(inst_id, class_file):
    """Parse bytecode using javap and extract basic blocks."""
    try:
        # Use javap to disassemble the class file
        javap_output = subprocess.check_output(
            ["javap", "-v", "-p", class_file], stderr=subprocess.STDOUT, text=True
        )
    except subprocess.CalledProcessError as e:
        print(f"Error running javap on {class_file}: {e}")
        return []

    basic_blocks = []
    class_name = None
    method_name = None
    param = ""
    lines = javap_output.splitlines()
    length = len(lines)
    for i, line in enumerate(lines):
        line = line.strip()

        # Capture class name
        if result := re.search(r"^\s*((final )|(public )|(private )|(abstract ))*\s*class ([A-Za-z$0-9_.]+)", line):
            class_name = result.group().split()[-1]
            print(class_name)
        # Detect method declarations
        if (i + 3) < length and lines[i+1].strip().startswith('descriptor') \
            and lines[i+2].strip().startswith('flags') \
            and lines[i+3].strip().startswith('Code'):
            if result := re.search(
                # r"^((final)|(public)|(private)|(protected))? ([A-Za-z$0-9_.]+) ([A-Za-z$0-9_]+) \(([A-Za-z$0-9_.<>]+)\);$", line):
                r"([\w_.$]+)\((.*)\).*;$", line):
                method_name = result[1]
                # print(method_name)
                parsed = result[2]
                param = ','.join([re.sub(r'<.*>', '', p).strip() for p in parsed.split(',')])
                if (method_name == class_name):
                    #constructor
                    method_name = method_name.split('.')[-1]
                print(method_name)
                inst_id += 1
                basic_blocks.append((inst_id, class_name, method_name, param, "-1", "ENTRY", "logCutting"))
                inst_id +=1
            else:
                method_name = None
        if line.startswith('LineNumberTable'):
            method_name = None
        # Detect bytecode jump instructions
        if method_name and any(op in line for op in constants.RET_LIST):
            if class_name is None:
                print(javap_output)
                print(f'method_name = {method_name}')
                exit(1)
            if part :=  re.search(r'^(\d+):.*', line):
                bci = int(part.groups()[0])
                basic_blocks.append((inst_id, class_name, method_name, param, bci, '100', "logCutting"))
    return inst_id,basic_blocks

def main(jar_file, filter_kw, output):
    temp_dir = "tmp"
    os.makedirs(temp_dir, exist_ok=True)
    print(filter_kw)
    # Step 1: Extract class files
    print(f"Extracting {jar_file}...")
    extract_class_files(jar_file, temp_dir)
    inst_id = 0
    # Step 2: Process each class file
    all_blocks = []
    class_count = 0
    for root, _, files in os.walk(temp_dir):
        for file in files:
            if file.endswith(".class"):
                class_file_path = os.path.join(root, file)
                if filter_kw and any(re.search(f, class_file_path) for f in filter_kw):
                    class_count +=1
                    print(f"Processing {class_file_path}...")
                    inst_id, basic_blocks = parse_bytecode(inst_id, class_file_path)
                    all_blocks.extend(basic_blocks)
    
    for i, b in enumerate(all_blocks):
        r = {
            'id': b[0],
            'class_name': b[1],
            'method_name': b[2],
            'params': b[3],
            'bci': b[4],
            'line_number': b[5],
            'strategy': b[6],
        }
        plan = constants.HUBBLE_TEMPLATE.format(**r)
        # print(plan)
        with open(f'/home/ubuntu/plans_hubble/{i}.properties', 'w') as f:
            f.write(plan)
    
    print(f'stats {class_count}, {len(all_blocks)}')
    # Step 3: Cleanup temporary files
    # print(f"Cleaning up temporary files...")
    # for root, dirs, files in os.walk(temp_dir):
    #     for file in files:
    #         os.remove(os.path.join(root, file))
    #     for dir in dirs:
    #         os.rmdir(os.path.join(root, dir))
    # os.rmdir(temp_dir)

    # Step 4: Output the results
    print("Basic Blocks Found:")
    for block in all_blocks:
        print(f"Class: {block[0]}, Method: {block[1]}, Index: {block[2]}, Instruction: {block[3]}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Extract and display basic blocks from Java class files in a JAR."
    )
    parser.add_argument(
        "-jar", type=str, required=True, help="Path to the JAR file to process."
    )
    parser.add_argument(
        "-f", "--filter", nargs='+' , default=[], 
        help="Optional keyword to filter class files to process."
    )
    parser.add_argument(
        "-o", "--output", type=str, default=None, 
        help="Optional file to save the output."
    )

    args = parser.parse_args()

    main(args.jar, args.filter, args.output)
