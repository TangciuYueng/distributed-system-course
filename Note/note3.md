# Linux
## Connection
### SSH(Secure Shell)
Remote Connection
`ssh [opts] <username>@remote.host`
- *username* is the username on the *remote* host
- *remote.host* is the url of the server you want to log into(IP address usually)
- `-p <port>`connect to a specific port
- `-l`to specify username (no need for @ anymore).
- `-X`这是一个SSH选项，表示启用X11转发。X11是用于图形用户界面的协议，这个选项允许你在远程服务器上运行图形化的应用程序，并将它们的窗口显示在本地计算机上
- `-f`后台运行，此时推荐配合`-n`参数
- `-n`将标准输入重定向到`/dev/null`防止读取标准输入
- `-N`不执行远程命令，只做端口转发
- `-q`安静模式，忽略一切对话和错误提示
- `-T`禁用伪终端配置，不会创建交互式终端环境

**Example**
- 连接进入远程电脑终端`ssh sjm324@128.253.141.42`

- 在远程电脑上执行命令`ssh -l www-online 192.168.110.34 "cat /proc/cpuinfo"`
  - `"cat /proc/cpuinfo"`: 这是要在远程计算机上执行的命令。在这种情况下，它执行了 `cat /proc/cpuinfo` 命令，该命令用于显示有关计算机CPU（中央处理器）的信息。这将返回有关CPU型号、核数、速度等的详细信息。

- 在远程电脑上执行脚本`ssh -l www-online 192.168.110.34 "/home/www-online/uptimelog.sh"`

- `ssh -l www-online 192.168.110.34 "/home/www-online/uptimelog.sh > /dev/null 2>&1 &`
  -  `&` 符号表示将该命令放在后台运行，这意味着它将在后台执行，而不会阻塞当前SSH连接
  - `> /dev/null`：这部分将脚本的**标准输出**重定向到 `/dev/null`，这意味着脚本的输出将被丢弃，不会显示在终端上。
  - `2>&1`：这部分将脚本的**标准错误**（stderr）也重定向到标准输出，这样脚本的错误消息也将被重定向到 `/dev/null`。
  - 示例代码
    ```sh
    #!/bin/bash
    while :
    do
        uptime >> 'uptime.log'
        sleep 1
    done
    exit 0
    ```
    ```sh
    #!/bin/bash
    ssh -f -n -l www-online 192.168.110.34 "/home/www-online/uptimelog.sh &" # 后台运行ssh
    pid=$(ps aux | grep "ssh -f -n -l www-online 192.168.110.34 /home/www-online/uptimelog.sh" | awk '{print $2}' | sort -n | head -n 1) #获取进程号
    echo "ssh command is running, pid:${pid}"
    sleep 3 && kill ${pid} && echo "ssh command is complete" # 延迟3秒后执行kill命令，关闭ssh进程，延迟时间可以根据调用的命令不同调整
    exit 0
    ```
    - `grep "ssh -f -n -l www-online 192.168.110.34 /home/www-online/uptimelog.sh"`：这部分使用 `grep` 过滤出包含特定SSH命令的行。
    - `awk '{print $2}'`：这个命令使用 `awk` 提取输出行的第二列，即进程ID（PID）。
    - `sort -n`：这个命令将PID按数字顺序进行排序。
    - `head -n 1`：这个命令选择排序后的第一个PID，即最小的PID，一般来说Linux上最小的PID通常会找到最近启动的进程。

### SCP
Secure Copy（scp）是一种用于在本地计算机和远程计算机之间进行安全文件传输的命令。以下是有关scp的一些重要信息：

- `scp`命令的基本语法是：`scp [选项] <源文件或目录> <目标文件或目录>`。

- 它的功能类似于`cp`命令，但它是通过**网络**进行文件传输的工具。

- 你可以使用`scp`将文件从客户端传输到远程主机，也可以将文件从远程主机传输到客户端。

- 若要复制目录，你需要使用`-r`选项，就像在`cp`命令中一样。

- 在远程主机上，你需要指定用户名和主机名来表示目标位置，语法为：`用户名@主机名:/目标路径`。请注意，冒号（:）是路径的一部分，用于指示远程路径的起始。

- 如果没有足够的权限，你将无法复制或访问文件。在访问远程主机时，请确保你有适当的权限。

- 较新的系统通常支持在远程目录上使用TAB键进行自动补全，以提高操作的便捷性。

**Example**

1. 从本地计算机上传文件到远程主机：
   
   ```
   scp local_file.txt remote_user@remote_host:/path/to/destination/
   ```

   这个命令将 `local_file.txt` 从本地计算机复制到远程主机的 `/path/to/destination/` 目录中。

2. 从远程主机下载文件到本地计算机：

   ```
   scp remote_user@remote_host:/path/to/remote_file.txt  ~/Desktop/
   ```

   这个命令将远程主机上的 `remote_file.txt` 复制到本地计算机的 ` ~/Desktop/` 桌面目录中。

3. 使用 `-r` 标志来复制整个目录：

   ```
   scp -r local_directory/ remote_user@remote_host:/path/to/destination/
   ```

   这个命令将本地目录 `local_directory/` **递归地复制**到远程主机的 `/path/to/destination/` 目录中。


- `local_file.txt`：本地计算机上的文件名或路径。
- `remote_user`：远程主机上的用户名。
- `remote_host`：远程主机的主机名或IP地址。
- `/path/to/destination/`：目标路径，可以是本地或远程计算机上的路径。


### X/X-Win(X-Windowing)
Remote Graphics
### SFTP(Secure File Transfer Protocol)
Data Transfer

## Shell命令
- `whoami`显示当前登录用户的用户名
- `hostname`显示当前主机的主机名
- `echo $HOME`显示当前用户的家目录，环境变量
- `pwd`显示当前工作目录（当前所在的文件夹）的路径
- `date`显示当前系统的日期和时间
- `cal`显示当前月份的日历
  - `cal -j 3 1999`显示指定月份的日历
```sh
root@LAPTOP-UER420HO:~# whoami
root
root@LAPTOP-UER420HO:~# hostname
LAPTOP-UER420HO
root@LAPTOP-UER420HO:~# echo $HOME
/root
root@LAPTOP-UER420HO:~# pwd
/root
root@LAPTOP-UER420HO:~# date
Fri Sep 29 11:43:14 CST 2023
root@LAPTOP-UER420HO:~# cal
   September 2023
Su Mo Tu We Th Fr Sa
                1  2
 3  4  5  6  7  8  9
10 11 12 13 14 15 16
17 18 19 20 21 22 23
24 25 26 27 28 29 30

root@LAPTOP-UER420HO:~# cal -j 3 1999
        March 1999
 Su  Mo  Tu  We  Th  Fr  Sa
     60  61  62  63  64  65
 66  67  68  69  70  71  72
 73  74  75  76  77  78  79
 80  81  82  83  84  85  86
 87  88  89  90

root@LAPTOP-UER420HO:~# shazam
shazam: command not found
root@LAPTOP-UER420HO:~# echo my login is $(whoami)
my login is root
```

**更多命令**

1. **awk**:
   - 用于文本处理和报告生成的强大工具。
   - 示例：`awk '{print $1}' file.txt` 将打印文件 `file.txt` 中每行的第一个字段。

2. **cat**:
   - 用于将文件内容连接并输出到终端。
   - 示例：`cat file1.txt file2.txt` 将将 `file1.txt` 和 `file2.txt` 的内容连接并输出。

3. **cut**:
   - 用于从文本行中剪切出指定的字段。
   - 示例：`cut -d',' -f1 file.csv` 将剪切以逗号分隔的CSV文件的第一个字段。

4. **diff**:
   - 用于比较两个文件的差异。
   - 示例：`diff file1.txt file2.txt` 将比较两个文件 `file1.txt` 和 `file2.txt` 的差异。

5. **grep**:
   - 用于在文本中搜索指定的字符串或模式。
   - 示例：`grep "pattern" file.txt` 将在 `file.txt` 中查找包含 "pattern" 的行。

6. **head**:
   - 用于显示文件的前几行，默认为前10行。
   - 示例：`head -n 5 file.txt` 将显示 `file.txt` 的前5行。

7. **less**:
   - 用于逐页查看文件内容。
   - 示例：`less file.txt` 可以逐页查看 `file.txt` 的内容。
     - **空格键 (Space)**:
        - 按下空格键将向前滚动一页，浏览文件的下一页内容。

     - **b键**:
        - 按下 `b` 键将向后滚动一页，浏览文件的前一页内容。

     - **< 键**:
        - 按下 `<` 键将光标移动到文件的第一行。

     - **> 键**:
        - 按下 `>` 键将光标移动到文件的最后一行。

     - **/ 键**:
        - 按下 `/` 键后，可以输入搜索字符串，然后按回车键，`less` 将在文件中向前搜索匹配的字符串。按下 `n` 键可以查找下一个匹配项。

     - **? 键**:
        - 按下 `?` 键后，可以输入搜索字符串，然后按回车键，`less` 将在文件中向后搜索匹配的字符串。按下 `N` 键可以查找上一个匹配项。

     - **h 键**:
        - 按下 `h` 键将显示 `less` 帮助页面，其中包含更多可用命令的信息。

     - **q 键**:
        - 按下 `q` 键将退出 `less`，返回到终端。

8. **sed**:
   - 用于对文本进行流编辑，进行文本替换和处理。
   - 示例：`sed 's/old/new/g' file.txt` 将替换 `file.txt` 中的所有 "old" 为 "new"。

9. **sort**:
   - 用于对文本行进行排序。
   - 示例：`sort file.txt` 将对 `file.txt` 的内容进行排序。

10. **split**:
    - 用于将大文件拆分成较小的文件。
    - 示例：`split -l 1000 file.txt` 将 `file.txt` 拆分成每个包含1000行的文件。

11. **tail**:
    - 用于显示文件的末尾几行，默认为最后10行。
    - 示例：`tail -n 5 file.txt` 将显示 `file.txt` 的末尾5行。

12. **tr**:
    - 用于字符替换或删除。
    - 示例：`echo "Hello" | tr 'a-z' 'A-Z'` 将输出 "HELLO"。

13. **uniq**:
    - 用于删除或报告文本文件中的重复行。
    - 示例：`sort file.txt | uniq` 将删除 `file.txt` 中的重复行。

14. **wc**:
    - 用于统计文件的行数、字数和字符数。
    - 示例：`wc -l file.txt` 将显示 `file.txt` 中的行数。
    - `wc -l`：这个命令告诉 `wc` 统计文件中的行数（lines）。
    - `wc -w`：这个命令告诉 `wc` 统计文件中的字数（words）。
    - `wc -c`：这个命令告诉 `wc` 统计文件中的字符数
**Command Line**
以下是与你提供的提示相关的命令行操作和示例：

1. **history**:
   - `history` 命令用于查看命令历史记录，它会列出之前在终端中执行的命令。
   - 示例：运行 `history` 命令将显示以前执行的命令列表。

2. **上箭头 ↑ 和下箭头 ↓**:
   - 使用上箭头和下箭头键可以在命令历史中上下浏览，以选择之前执行过的命令。

3. **!!**:
   - `!!` 命令用于重新执行上一条命令，即使它已经不在屏幕上可见。
   - 示例：运行 `!!` 将重新执行上一条命令。

4. **!数字**:
   - 使用 `!` 后跟一个数字，可以重新执行命令历史中特定位置的命令，该数字对应于 `history` 命令的输出中的行号。
   - 示例：`!132` 将重新执行历史中第 132 行的命令。

5. **!命令**:
   - 使用 `!` 后跟一个命令关键字，可以重新执行最近执行的匹配该关键字的命令。
   - 示例：`!ls` 将重新执行最近执行的 "ls" 命令。

6. **左箭头 ← 和右箭头 →**:
   - 在命令行中，左箭头和右箭头键可用于在当前输入行中移动光标。左箭头将光标向左移动，右箭头将光标向右移动。

7. **<Del> 和 <Backspace>**:
   - `<Del>` 键用于删除光标后的字符，`<Backspace>` 键用于删除光标前的字符。这些键可用于编辑命令行输入。

## I/O redirection
### 管道`|`
- **w**:
   - `w` 命令用于显示当前登录到系统的用户列表以及一些关于他们的信息，如用户名、终端、登录时间等。

- **w | less**:
   - 使用管道 `|` 将 `w` 的输出发送到 `less` 分页程序，允许你按页浏览用户列表。这对于查看大量用户信息很有用，因为可以逐页查看。

- **w | grep 'tuta'**:
   - 使用管道 `|` 将 `w` 的输出发送到 `grep` 命令，用于过滤包含指定字符串（例如 'tuta'）的行，然后显示匹配的行。

- **w | grep -v 'tuta'**:
   - 使用管道 `|` 将 `w` 的输出发送到 `grep` 命令，并使用 `-v` 选项来反转匹配，从而显示不包含指定字符串（例如 'tuta'）的行。

- **w | grep 'tuta' | sed s/tuta/scholar/g**:
   - 使用管道 `|` 将 `w` 的输出发送到 `grep` 命令，以查找包含 'tuta' 的行，然后将这些行的输出发送到 `sed` 命令，使用 `s/tuta/scholar/g` 替换 'tuta' 为 'scholar'，最后显示替换后的结果。

**Example**
记录当前登录的不同用户数量
```sh
who | awk '{print $1}' | sort -u | wc -l
```
- `who` 命令用于列出当前登录到系统的用户列表。
- `awk '{print $1}'` 用于提取用户列表中的用户名（第一个字段）。
- `sort -u` 用于对用户名进行排序并去重，以获得唯一的用户名列表。
- `wc -l` 用于统计唯一用户名的行数，从而得到不同用户的数量

### 重定向文件`>`/`>>`
`>>` 和 `>` 是用于重定向输出的两个不同的操作符，它们之间有重要的区别：

- `>`（大于号）：
   - `>` 用于将命令的输出重定向到文件，并且如果目标文件已经存在，则会覆盖文件中的内容。
   - 例如，`command > file.txt` 将命令的输出写入 `file.txt`，如果 `file.txt` 已经存在，它会被覆盖。

- `>>`（双大于号）：
   - `>>` 也用于将命令的输出重定向到文件，但与 `>` 不同，它会将输出追加到文件的末尾，而不是覆盖文件。
   - 例如，`command >> file.txt` 将命令的输出追加到 `file.txt` 的末尾，如果 `file.txt` 存在，它将不会被覆盖，而是添加到文件末尾。

总结：
- `>` 用于**创建或覆盖**文件并写入输出。
- `>>` 用于将输出**追加到已存在的文件末尾**，或者**创建新文件并写入输出**。

## 文件操作
### ls
以下是你提供的`ls`命令选项的中文解释：

- **ls -a**:
  - 列出目录中的所有文件，包括以`.`开头的隐藏文件。

- **ls -ld***:
  - 列出有关目录本身的详细信息（而非它的内容）。选项`-d`用于显示目录的详细信息。

- **ls -F**:
  - 在每个文件或目录名称的末尾添加指示字符，以指示它们的类型。例如，目录名会加上`/`字符。

- **ls -l**:
  - 提供简单的长格式文件和目录列表，显示权限、所有者、组、文件大小、修改日期和名称等详细信息。

- **ls -lR**:
  - 执行递归的长格式列表，显示当前目录及其子目录中文件和目录的详细信息。

- **ls -lh**:
  - 以人类可读的格式显示文件大小，使得更容易理解文件的大小（例如：1K、2M、3G）。

- **ls -lS**:
  - 根据文件大小对文件进行排序，最大的文件将首先显示。用于查找目录中的大文件。

- **ls -lt**:
  - 根据修改时间对文件进行排序，最近修改的文件将首先显示。对于检查目录中的最新更改很有用。

### 其他命令

- **cp [file1] [file2]**:
   - 复制文件。将 `file1` 复制到 `file2`，创建 `file2` 的副本。

- **mkdir [name]**:
   - 创建目录。使用指定的名称创建一个新的目录。

- **rmdir [name]**:
   - 移除（空）目录。删除指定的空目录。

- **mv [file] [destination]**:
   - 移动/重命名文件。将文件 `file` 移动到指定的目的地，或者用新名称重命名文件。

- **rm [file]**:
   - 移除文件。删除指定的文件。使用 `-r` 选项可以递归删除目录及其内容。

- **file [file]**:
   - 识别文件类型。确定指定文件的类型，例如文本文件、二进制文件等。

- **less [file]**:
   - 分页查看文件。使用 `less` 命令可以逐页查看文件内容。

- **head -n N [file]**:
   - 显示文件的前 N 行。显示文件的开头部分，可以指定要显示的行数。

- **tail -n N [file]**:
   - 显示文件的后 N 行。显示文件的末尾部分，可以指定要显示的行数。

- **ln –s [file] [new]**:
   - 创建符号链接。创建一个指向 `file` 的符号链接，并指定一个新的名称 `new`。

- **cat [file] [file2…]**:
   - 显示文件。将一个或多个文件的内容连接在一起并显示。

- **tac [file] [file2…]**:
   - 以逆序显示文件。与 `cat` 类似，但是以相反的顺序显示文件内容。

- **touch [file]**:
   - 更新修改时间。更新指定文件的修改时间，如果文件不存在则创建一个空文件。

- **od [file]**:
   - 显示文件内容，特别是二进制内容。以八进制或其他格式显示文件的内容，主要用于查看二进制文件。

## nohap &
通过ssh远程连接，启动一个后台程序，即使断开连接也可以继续运行