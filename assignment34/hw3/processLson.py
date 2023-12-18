import json
import re
import os
import sys

def process_individual_json(line):
    json_data = json.loads(line)
    return json_data

def merge_json_data(json_data_list):
    matching_data = {name: values for data in json_data_list for name, values in data.items() if re.search(r'\s\d+', name)}

    cleaned_data = {}
    for name, values in matching_data.items():
        cleaned_name = re.sub(r'\s\d+', '', name)
        if cleaned_name not in cleaned_data:
            cleaned_data[cleaned_name] = values
        else:
            for key, value in values.items():
                if key in cleaned_data[cleaned_name]:
                    cleaned_data[cleaned_name][key] += value
                else:
                    cleaned_data[cleaned_name][key] = value

    merged_data = []
    for data in json_data_list:
        name = list(data.keys())[0]
        if name not in matching_data:
            merged_data.append(data)

    merged_data.extend([{cleaned_name: values} for cleaned_name, values in cleaned_data.items()])
    return merged_data

def process_json_file(file_path):
    with open(file_path, 'r') as file:
        json_data_list = [process_individual_json(line.strip()) for line in file]

    merged_data = merge_json_data(json_data_list)
    # print(json.dumps(merged_data, indent=2))

    base_path, file_name = os.path.split(file_path)
    file_name_without_extension, file_extension = os.path.splitext(file_name)
    new_file_name = f"{file_name_without_extension}_processed{file_extension}"
    new_file_path = os.path.join(base_path, new_file_name)

    with open(new_file_path, 'w') as file:
        for data in merged_data:
            file.write(json.dumps(data) + '\n')

if __name__ == "__main__":
    # Check if the correct number of command line arguments is provided
    if len(sys.argv) != 2:
        print("Usage: python mypy.py <json_file_path>")
        sys.exit(1)

    json_file_path = sys.argv[1]
    process_json_file(json_file_path)
