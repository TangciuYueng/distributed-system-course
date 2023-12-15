# 读取文件并建立姓名到记录起始位置的映射关系
name_pointers = {}

import sys
import json

if len(sys.argv) != 2:
    sys.exit(1)

file_path = sys.argv[1]

with open(file_path, 'r') as file:
    current_pos = 0
    line = file.readline()

    while line:
        # 记录当前姓名对应的起始位置
        name = list(eval(line).keys())[0]
        name_pointers[name] = current_pos

        # 更新当前位置，准备读取下一行
        current_pos = file.tell()
        line = file.readline()

# 打印姓名到记录起始位置的映射关系
for name, pointer in name_pointers.items():
    print(f'{name}: {pointer}')

# 验证
given_name = 'mvv'

if given_name in name_pointers:
    with open(file_path, 'r') as file:
        file.seek(name_pointers[given_name])
        json_data = json.loads(file.readline())
        print(json.dumps(json_data, indent=2))
else:
    print("no this name")
