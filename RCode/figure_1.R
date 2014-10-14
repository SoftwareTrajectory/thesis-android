#
# generates figure 1 from section
#
require(RMySQL)
require(reshape2)
require(ggplot2)
require(scales)
require(gridExtra)
require(Cairo)
require(lubridate)
#

data=read.table("patternz.txt",as.is=T,sep=" ")
dm1=as.data.frame(data[data[,1]=="pre",-1])
dm1=dm1[1:4,]
dm1$samples=paste("sample",c(1:length(dm1$V2)),sep="")
df <- melt(dm1)
p1=ggplot(df, aes(x=variable,y=value, group=samples, color=samples)) + scale_y_log10() + geom_line()


dm2=as.data.frame(data[data[,1]=="post",-1])
dm2$samples=paste("sample",c(1:length(dm2$V2)),sep="")
df2 <- melt(dm2)
p2=ggplot(df2, aes(x=variable,y=value, group=samples, color=samples)) + scale_y_log10() + geom_line()

print(arrangeGrob(p1, p2, ncol=2))

write.table(pre_res,file="pre_portraits_monthly_google.txt", quote=F, col.names=T, row.names=F, sep="\t")
write.table(post_res,file="post_portraits_monthly_google.txt", quote=F, col.names=T, row.names=F, sep="\t")
