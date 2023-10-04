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
    - `wc -c`：这个命令告诉 `wc` 统计文件中的字节数
    - `wc -m`：这个命令告诉 `wc` 统计文件中的字符数
    - `wc -L`：这个命令告诉 `wc` 统计文件中的最长行的长度
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

- **ls -ld**:
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

### Symbolic links
创建“快捷方式”
`ln -s <origin file> <target dir>`

**还有更多选项**

|选项|描述|
|:--|:--|
|-f, --force |强制执行，如果存在同名链接，则先删除再创建。|
|-i, --interactive|交互式操作，如果存在同名链接，则提示用户是否覆盖。|
|-s, --symbolic|创建一个符号链接。|
|-v, --verbose|显示操作详细信息。|
|-r, --relative|创建一个相对路径的符号链接。|
|-n, --no-target-directory|当目标文件不存在时，不将链接解引用。|
|-t, --target-directory=DIRECTORY|将链接创建到指定目录中。|
|-T, --no-target-directory|强制将链接视为符号链接。|

在使用 `ln` 命令时，可以通过选项 `-s`或 `--symbolic` 创建一个符号链接，或者不使用该选项来创建一个硬链接。这两种链接的区别如下：

- 硬链接：硬链接是指将一个文件名链接到一个 **inode** 上，使得多个文件名指向同一个文件内容。创建硬链接时，目标文件必须是同一个文件系统中的一个已经存在的文件。当删除原始文件时，硬链接不会受到影响，因为它们都指向同一个 inode，只有当所有链接都被删除时，文件的内容才会被真正删除。
  - 文件副本形式，不占用实际空间
  - 只能在同一个文件系统

- 符号链接：符号链接是指将一个文件名链接到另一个文件名上，实际上是在文件系统中创建了一个新的文件，该文件包含了指向原始文件的指针。符号链接可以跨越文件系统边界，并且可以指向任何类型的文件（包括目录）。当删除原始文件时，符号链接会失效，但并不会影响到链接指向的文件。
   - 以路径形式存在
   - 可以跨越文件系统
### find
`find [path] [expression]`
`path` 是要查找的目录路径，可以是一个目录或文件名，也可以是多个路径，多个路径之间用空格分隔，如果未指定路径，则默认为当前目录。

`expression` 是可选参数，用于指定查找的条件，可以是文件名、文件类型、文件大小等等。

expression 中可使用的选项有二三十个之多，以下列出最常用的部份：

- `-name pattern`：按文件名查找，支持使用通配符 `*` 和 `?`。
- `-type type`：按文件类型查找，可以是 `f`（普通文件）、`d`（目录）、`l`（符号链接）等。
- `-size [+-]size[cwbkMG]`：按文件大小查找，支持使用 `+` 或 `-` 表示大于或小于指定大小，单位可以是 `c`（字节）、`w`（字数）、`b`（块数）、`k`（KB）、`M`（MB）或 `G`（GB）。
- `-mtime days`：按修改时间查找，支持使用 `+` 或 `-` 表示在指定天数前或后，`days` 是一个整数表示天数。
- `-user username`：按文件所有者查找。
- `-group groupname`：按文件所属组查找。
find 命令中用于时间的参数如下：

- `-amin n`：查找在 `n` 分钟内被访问过的文件。
- `-atime n`：查找在 `n*24` 小时内被访问过的文件。
- `-cmin n`：查找在 `n` 分钟内状态发生变化的文件（例如权限）。
- `-ctime n`：查找在 `n*24` 小时内状态发生变化的文件（例如权限）。
- `-mmin n`：查找在 `n` 分钟内被修改过的文件。
- `-mtime n`：查找在 `n*24` 小时内被修改过的文件。
- 
在这些参数中，`n` 可以是一个正数、负数或零。正数表示在指定的时间内修改或访问过的文件，负数表示在指定的时间之前修改或访问过的文件，零表示在当前时间点上修改或访问过的文件。

例如：`-mtime 0` 表示查找今天修改过的文件，`-mtime -7` 表示查找一周以前修改过的文件。

关于时间 `n` 参数的说明：

- `+n`：查找比 `n` 天前更早的文件或目录。

- `-n`：查找在 n 天内更改过属性的文件或目录。

- `n`：查找在 `n` 天前（指定那一天）更改过属性的文件或目录。

**Example**
```sh
# 查找当前目录下名为 file.txt 的文件
find . -name file.txt

# 将当前目录及其子目录下所有文件后缀为 .c 的文件列出来
find . -name "*.c"

# 查找 /home 目录下大于 1MB 的文件
find /home -size +1M

# 将当前目录及其子目录下所有 20 天前及更早更新过的文件列出
find . -ctime  +20
```

## 控制语句
**shell中0在判断的时候为真**

### if

```sh
if (($param < 2)); then
   echo hello
fi

# 当然也可以
if ((param < 2)); then
   echo hello
fi

# 但方括号就要这样
if [ $param -lt 2 ]; then
   echo hello
fi
```

### for
```sh
for ((i = 2; i < 10; i++));
do
   echo $i
done

# 遍历数组
my_array=("apple" "banana" "orange")

for item in "${my_array[@]}"
do
    echo "$item"
done

# 遍历列表，注意这里是以空格区分
my_list="apple banana orange"

for item in $my_list
do
    echo "$item"
done

# 数字范围，闭区间
for number in {1..5}
do
    echo "$number"
done
```


### while
在Shell中，`while`是一个用于创建循环的关键字，它的语法结构如下：

```bash
while [ condition ]
do
    # 执行的代码块
done
```

```bash
#!/bin/bash

counter=0

while [ $counter -lt 5 ]
do
    echo $counter
    counter=$((counter + 1))
done

echo "Done"


# 也可这样
while ((counter<5))
do
   echo $num
   ((num++))
done
```

### 括号
1. 单括号 `()`
单括号在Shell中通常用于以下几种情况：

   - 子shell：单括号可以创建一个子shell，在该子shell中执行命令。子shell中的变量更改不会影响到父shell。
   
     示例：
     ```bash
     (command)
     ```
   
   - 数组赋值：单括号可以用于将一系列的值赋给数组。
   
     示例：
     ```bash
     my_array=("apple" "banana" "orange")
     ```
   
   - 数学运算：在算术表达式中，单括号可以用于执行数学运算。
   
     示例：
     ```bash
     result=$((x + y))
     ```
   
   - 代码块分组：单括号可以将一系列的命令组合到一个代码块中。
   
     示例：
     ```bash
     {
         command1
         command2
         command3
     }
     ```

2. 方括号 `[]`
方括号在Shell中通常用于条件测试和判断操作，**一定要有两边空格**，例如:

   - 条件测试：在if语句和循环中，方括号用于执行条件测试和逻辑判断。
   
     示例：
     ```bash
     if [ $x -gt $y ]; then
         echo "x is greater than y"
     fi
     ```

   - 字符串比较：方括号可以用于比较字符串是否相等、不等、为空等。
   
     示例：
     ```bash
     if [ "$str1" = "$str2" ]; then
         echo "Strings are equal"
     fi
     ```
      使用双引号是为了确保在比较字符串时，即使字符串中包含空格或特殊字符，也能正确地进行比较。

      在Shell脚本中，使用`[` 或 `test` 来进行条件判断时，比较操作符`=`用于比较字符串是否相等。为了防止字符串中包含空格或特殊字符时导致意外结果，使用双引号将变量括起来是一个良好的习惯。

      假设`str1`和`str2`都是包含空格的字符串，如果不使用双引号括起来进行比较，Shell会将变量展开后执行比较操作。这可能会导致意外的结果或语法错误。而使用双引号括起来，Shell会将字符串作为一个整体进行比较，确保了比较的正确性。

   - 文件测试：方括号可以用于测试文件的各种属性，如存在性、类型、权限等。
   
     示例：
     ```bash
     if [ -f "$file" ]; then
         echo "File exists"
     fi
     ```


3. 在双括号`(( ))`中，变量使用时通常不需要再加上`$`符号。

   当使用双括号进行数学运算时，Shell会自动将变量解析为其对应的值。因此，不需要在变量前面使用`$`符号。

   示例：
   ```bash
   x=10
   ((result = x + 5))
   echo $result  # 输出: 15
   ```

   注意，如果你希望在双括号内引用变量的值，确保变量名与其他字符之间没有空格，以避免解析错误。

   示例：
   ```bash
   x=10
   ((result = x * 2))
   echo "Result: $result"  # 输出: Result: 20
   ```

   总结一下，在双括号`(( ))`中，变量通常不需要使用`$`符号，Shell会自动解析为变量的值。但是在其他情况下，如**字符串**拼接或命令替换等，仍然需要使用`$`符号来引用变量。
### 运算赋值
- let var=算术表达式,let后面就当成是算术运算--否则就是简单地字符串
- var=$[算术表达式]
- var=$((算术表达式))
- var=$(expr arg1 arg2 arg3 ...) 用expr  数字和符号之间必须用空格  而且*必须用/转义
- declare –i var = 数值
- echo ‘算术表达式’ | bc


**1. 使用`let`关键字进行算术运算：**
```bash
let "result = 2 + 3"
echo $result  # 输出：5
```

**2. 使用`$[ ]`进行算术运算：**
```bash
result=$[4 * 5]
echo $result  # 输出：20
```

**3. 使用`$(( ))`进行算术运算：**
```bash
result=$((6 / 2))
echo $result  # 输出：3
```

**4. 使用`expr`命令进行算术运算：**
```bash
result=$(expr 8 - 3)
echo $result  # 输出：5
```

**5. 使用`declare -i`声明变量为数值类型：**
```bash
declare -i result
result=10 / 2
echo $result  # 输出：5
```

**6. 使用`bc`命令进行高级算术运算：**
```bash
result=$(echo 'scale=2; 10 / 3' | bc)
echo $result  # 输出：3.33
```

## Users and Groups
允许多个用户同时使用同一台机器

-rwxrwxrwx （-）开头是一个文件

drwxrwxrwx (d) 可能是一个目录

### 三组rwx
1、第一组代表文件主的权限U

2、第二组代表同组用户权限G

3、第三组代表其他用户的权限O

### 数字表示
- **x=1 可执行**;

- **w=2 可写入**;

- xw=3 可写可执行；

- **r=4 可读取**;

- rx=5 可读可执行;

- rw=6 可写可读;

- rwx=7 可写可读可执行;

### chgrp
允许普通用户改变文件所属的组，只要该用户是该组的一员
**-c 或 --changes**：效果类似"-v"参数，但仅回报更改的部分。

**-f 或 --quiet 或 --silent**： 　不显示错误信息。

**-h 或 --no-dereference**： 　只对符号连接的文件作修改，而不改动其他任何相关文件。

**-R 或 --recursive**： 　递归处理，将指定目录下的所有文件及子目录一并处理。

**-v 或 --verbose**： 　显示指令执行过程。

**--help**： 　在线帮助。

**--reference=<参考文件或目录>**： 　把指定文件或目录的所属群组全部设成和参考文件或目录的所属群组相同。

**--version**： 　显示版本信息。

```sh
# 将log2012.log所属组改变为bin
chgrp -v bin log2012.log
```

### stat
以文字的格式来显示inode的内容
- 文件字节数
- 文件拥有者的User ID
- 文件的Group ID
- 文件的读写执行权限
- 文件的时间戳
  - ctime指inode上一次变动的时间
  - mtime指文件内容上一次变动的时间
  - atime指文件上一次打开的时间
- 链接数，即有多少文件名指向该inode
- 文件数据block的位置
```sh
root@LAPTOP-UER420HO:~# stat *sh
  File: get-docker.sh
  Size: 21927           Blocks: 48         IO Block: 4096   regular file
Device: 820h/2080d      Inode: 65994       Links: 1
Access: (0644/-rw-r--r--)  Uid: (    0/    root)   Gid: (    0/    root)
Access: 2023-09-29 11:53:30.824768717 +0800
Modify: 2023-09-28 09:11:30.455179993 +0800
Change: 2023-09-28 09:11:30.455179993 +0800
 Birth: -
```

## Process and JOb Control
ps用于显示当前进程状态

**参数**
- `-A`列出所有进程
- `-w`显示加宽可以显示更多资讯
- `-au`显示比较详细的资讯
- `aux`显示所有包含其他使用者的进程
- `-u <username>`指定用户的进程信息

**查找指定进程格式**
`ps -ef | grep <keyword>`

### 后台运行
#### 在命令后添加一个`&`
```bash

# 执行文件
./test.py &
 
# 查看是否在后台运行
ps -ef|grep test
 
# 后台的程序 需要关闭时，需要kill命令停止
killall 程序名/PID
```
但不推荐，查看运行日志不方便

#### nohup命令
远程连接关掉了但是还能让程序投胎运行
```bash
nohup python -u test.py > test.log 2>&1 &
```
- `test.py`要运行的程序
- `>`日志追加到的文件
- `1>&2` 意思是把标准输出重定向到标准错误.
- `2>&1` 意思是把标准错误输出重定向到标准输出。

### kill
```bash
root@LAPTOP-UER420HO:~/shellTest# bash temp.sh 60 &
[1] 1757
root@LAPTOP-UER420HO:~/shellTest# jobs
[1]+  Running                 bash temp.sh 60 &
root@LAPTOP-UER420HO:~/shellTest# kill %1
root@LAPTOP-UER420HO:~/shellTest# jobs
[1]+  Terminated              bash temp.sh 60
```

1. `bash temp.sh 60 &` - 这个命令在后台运行了一个名为 `temp.sh` 的脚本，并传递参数 `60` 给它。`&` 符号表示将命令放在后台运行，并且会显示一个作业号，这里是 `[1] 1757`。

2. `jobs` - 这个命令用来列出当前在**后台运行**的作业。在这种情况下，它显示了一个作业 `[1]`，它是通过 `bash temp.sh 60 &` 启动的，正在运行中。

3. `kill %1` - 这个命令用来终止指定作业。`%1` 表示作业号为1的作业，也就是上面启动的那个。因此，这个命令终止了该作业。

4. `jobs` - 再次运行 `jobs` 命令来列出当前的作业状态。现在它显示 `[1]+  Terminated`，表示作业1已经终止，`bash temp.sh 60` 不再在后台运行。

## 更多
[tutorialspoint](http://www.tutorialspoint.com/unix/)