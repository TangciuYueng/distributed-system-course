import json
import bson
import sys
import os

def json_to_bson(json_file_path):
    # 提取文件名和文件路径
    base_path, file_name = os.path.split(json_file_path)
    
    # 构造输出BSON文件路径
    bson_file_name = os.path.splitext(file_name)[0] + '.bson'
    bson_file_path = os.path.join(base_path, bson_file_name)

    with open(json_file_path, 'r') as json_file:
        json_data = json.load(json_file)

    # 使用bson.dumps将json_data转换为BSON格式
    bson_data = bson.encode(json_data)

    # 将BSON数据写入文件
    with open(bson_file_path, 'wb') as bson_file:
        bson_file.write(bson_data)

    print(f"JSON文件 '{json_file_path}' 已成功转换为BSON文件 '{bson_file_path}'。")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python script.py <json_file_path>")
        sys.exit(1)

    json_file_path = sys.argv[1]
    json_to_bson(json_file_path)
