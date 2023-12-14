import json
import os

def split_json(input_file, output_folder, output_file_count):
    with open(input_file, 'r', encoding='utf-8') as file:
        data = json.load(file)

    total_objects = len(data)
    objects_per_file = total_objects // output_file_count
    remaining_objects = total_objects % output_file_count

    # Ensure the output folder exists
    os.makedirs(output_folder, exist_ok=True)

    start_index = 0
    for i in range(output_file_count):
        end_index = start_index + objects_per_file + (1 if i < remaining_objects else 0)
        output_file = os.path.join(output_folder, f'output_{i + 1}.json')

        with open(output_file, 'w', encoding='utf-8') as file:
            json.dump(data[start_index:end_index], file, ensure_ascii=False, indent=2)

        start_index = end_index

if __name__ == "__main__":
    input_file = "path/to/your/input.json"
    output_folder = "path/to/your/output_folder"
    output_file_count = 4

    split_json(input_file, output_folder, output_file_count)
