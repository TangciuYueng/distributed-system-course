import json
import bson
import sys
import os

def bson_to_json(bson_file_path):
    # 提取文件名和文件路径
    base_path, file_name = os.path.split(bson_file_path)
    
    # 构造输出JSON文件路径
    json_file_name = os.path.splitext(file_name)[0] + '.json'
    json_file_path = os.path.join(base_path, json_file_name)

    with open(bson_file_path, 'rb') as bson_file:
        bson_data = bson_file.read()

    json_data = bson.decode(bson_data)

    # print(json.dumps(json_data, indent=2))

    将JSON数据写入文件
    with open(json_file_path, 'wb') as json_file:
        json_file.write(json_data)

    print(f"BSON文件 '{bson_file_path}' 已成功转换为JSON文件 '{json_file_path}'。")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python script.py <bson_file_path>")
        sys.exit(1)

    bson_file_path = sys.argv[1]
    bson_to_json(bson_file_path)
