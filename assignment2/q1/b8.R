# 定义数据
end_conditions <- c(rep("d1 > 0.9999 && d2 > 0.9999", 2), rep("d1 > 0.999 && d2 > 0.999", 2), rep("d1 > 0.99 && d2 > 0.99", 2))
platforms <- c("CentOs7", "Windows", "CentOs7", "Windows", "CentOs7", "Windows")
times <- c(33754, 2381, 185, 150, 16, 50)

# 创建数据框
data <- data.frame(EndCondition = end_conditions,
                   Platform = platforms,
                   Time = times)

# 载入 ggplot2 库
library(ggplot2)
library(scales)

squash_axis <- function(from, to, factor) { 
    # Args:
    #   from: left end of the axis
    #   to: right end of the axis
    #   factor: the compression factor of the range [from, to]
    
    trans <- function(x) {    
        # get indices for the relevant regions
        isq <- x > from & x < to
        ito <- x >= to
        
        # apply transformation
        x[isq] <- from + (x[isq] - from)/factor
        x[ito] <- from + (to - from)/factor + (x[ito] - to)
        
        return(x)
    }
    
    inv <- function(x) {
        # get indices for the relevant regions
        isq <- x > from & x < from + (to - from)/factor
        ito <- x >= from + (to - from)/factor
        
        # apply transformation
        x[isq] <- from + (x[isq] - from) * factor
        x[ito] <- to + (x[ito] - (from + (to - from)/factor))
        
        return(x)
    }
    
    # return the transformation
    return(trans_new("squash_axis", trans, inv))
}

# 绘制折线图，并标记每个数据点的时间
ggplot(data, aes(x = EndCondition, y = Time, group = Platform, color = Platform)) +
    geom_line() +
    geom_point(size = 3) +
    coord_trans(y = squash_axis(200, 40000, 60)) + 
    geom_text(aes(label = Time), vjust = 0.5, hjust = -0.6, size = 3) +  # 添加标签
    labs(title = "BUFFER_SIZE=8*1024",
         x = "End Condition",
         y = "Time (ms)",
         color = "Platform") +
    theme_minimal() +
    theme(axis.text.x = element_text(angle = 45, hjust = 1))  # 旋转 x 轴标签，以便更好地显示