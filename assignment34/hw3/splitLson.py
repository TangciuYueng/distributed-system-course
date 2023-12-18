import os
def split_file(file_name, chunk_count):
    try:
        # 读取原始文件
        with open(file_name, 'r', encoding='utf-8') as file:
            lines = file.readlines()

            # 计算每个块的行数
            lines_per_chunk = len(lines) // chunk_count
            remainder_lines = len(lines) % chunk_count

            # 分割文件
            start = 0
            for i in range(chunk_count):
                chunk_size = lines_per_chunk + (1 if i < remainder_lines else 0)
                chunk = lines[start:start + chunk_size]

                file_name_without_extension, file_extension = os.path.splitext(file_name)

                # 写入分割后的文件
                with open(f"{file_name_without_extension}_chunk_{i + 1}{file_extension}", 'w', encoding='utf-8') as chunk_file:
                    chunk_file.writelines(chunk)

                start += chunk_size

            print(f"成功将文件分割为 {chunk_count} 个块文件！")

    except FileNotFoundError:
        print("文件不存在！")
    except Exception as e:
        print(f"出现错误: {e}")


import sys

if len(sys.argv) != 3:
    sys.exit(1)

file_name = sys.argv[1]
chunk_count = int(sys.argv[2])

split_file(file_name, chunk_count)
