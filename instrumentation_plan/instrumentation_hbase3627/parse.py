import re

# Input protobuf-like text
proto_text = """\
instrumentationRules {
  id: 39
  className: "org.apache.hadoop.hbase.zookeeper.ZKUtil"
  methodName: "getDataNoWatch"
  parameterTypes: "org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher"
  parameterTypes: "java.lang.String"
  parameterTypes: "org.apache.zookeeper.data.Stat"
  lineNumber: 600
  byteCodeIndex: 138
}
"""

# Function to parse protobuf-like text and convert it to .properties format
def protobuf_to_properties(proto_text):
    properties_lines = []
    parameter_count = 0

    # Regex patterns to extract key-value pairs
    key_value_pattern = re.compile(r"(\w+):\s*(\d+|\"[^\"]+\")")
    repeated_field_pattern = re.compile(r"(\w+):\s*\"([^\"]+)\"")

    for line in proto_text.splitlines():
        line = line.strip()
        if key_value_match := key_value_pattern.match(line):
            key, value = key_value_match.groups()
            value = value.strip('"')  # Remove quotes from string values
            properties_lines.append(f"{key}={value}")
        elif repeated_field_match := repeated_field_pattern.match(line):
            key, value = repeated_field_match.groups()
            parameter_count += 1
            properties_lines.append(f"{key}[{parameter_count}]={value}")

    return "\n".join(properties_lines)

# Write the properties file
def write_properties_file(filename, content):
    with open(filename, "w") as file:
        file.write(content)

# Convert protobuf to properties
properties_content = protobuf_to_properties(proto_text)
file_name = "instrumentationRules.properties"
write_properties_file(file_name, properties_content)

print(f"Properties file '{file_name}' generated successfully!")
