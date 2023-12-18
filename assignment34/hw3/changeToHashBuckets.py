import sys
import os
import json

def custom_hash_function(name):
    # 初始化哈希值
    hash_value = 0

    # 遍历姓名中的每个字符
    for char in name:
        # 将字符的ASCII码值加到哈希值中
        hash_value += ord(char)

    return hash_value

class HashBuffer:
    def __init__(self, filename):
        self.filename = filename
        self.buffer = []
        self.buffer_size = 500  # 设置缓冲区大小，根据实际需求调整

    def add_to_buffer(self, data):
        self.buffer.append(data)
        if len(self.buffer) == self.buffer_size:
            self.flush_buffer()

    def flush_buffer(self):
        with open(self.filename, 'a') as f:
            for item in self.buffer:
                f.write(json.dumps(item)+"\n")
            self.buffer = []

def main():
    if len(sys.argv) != 3:
        print("error")
        exit(1)
    
    file_path = sys.argv[1]
    num = int(sys.argv[2])

    file_name_without_extension, file_extension = os.path.splitext(file_path)

    # 创建四个缓冲区
    buffers = [HashBuffer(f'{file_name_without_extension}_bucket_{i}.lson') for i in range(num)]

    # 读取原始数据文件
    with open(file_path, 'r') as f:
        for line in f:
            # 解析原始数据
            data = json.loads(line)
            name = list(data.keys())[0]
            values = data[name]

            # 计算哈希值
            hash_value = custom_hash_function(name)

            # print(name, hash_value)

            # 对哈希值取模，映射到缓冲区
            buffer_index = hash_value % num

            # print(name, buffer_index)

            # 将元数据添加到对应的缓冲区中
            buffers[buffer_index].add_to_buffer(data)

    # 将缓冲区中剩余的数据写入数据文件
    for buffer in buffers:
        buffer.flush_buffer()

if __name__ == "__main__":
    main()
