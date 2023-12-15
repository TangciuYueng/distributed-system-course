import sys

if len(sys.argv) != 2:
    sys.exit(1)

file_path = sys.argv[1]

try:
    with open(file_path, 'r') as file:
        position = 0
        while True:
            char = file.read(1)
            if not char:
                break
            print(f'Position: {position}, Character: {char}')
            position += 1
except FileNotFoundError:
    print("文件未找到，请检查文件路径是否正确。")
except Exception as e:
    print(f"发生错误: {str(e)}")
