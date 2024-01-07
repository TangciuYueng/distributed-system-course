# 服务器地址和目标目录
$serverAddress = "root@8.130.173.131"
$targetDirectory = "~"

# 遍历 i 和 j 的所有可能取值
for ($i=4; $i -le 7; $i++) {
    for ($j=0; $j -le 3; $j++) {
        # 构造文件名
        $fileName = "dblp_line_processed_chunk_${i}_bucket_${j}.lson"
        # $fileName = "dblp_line_processed_chunk_${i}_bucket_${j}_index_tree.ser"
        
        # 使用 scp 命令传输文件到服务器
        scp "$fileName" "$($serverAddress):$($targetDirectory)"
        
        # 如果你的文件在其他目录，可能需要提供完整路径，例如：
        # scp "/path/to/files/$fileName" "$serverAddress:$targetDirectory"
        
        # 输出传输信息
        Write-Host "File $fileName transferred to $($serverAddress):$($targetDirectory)"
    }
}
