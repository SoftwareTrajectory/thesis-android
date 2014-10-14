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
dm1=as.data.frame(labels=paste()data[data[,1]=="pre",-1])
dm2=as.data.frame(data[data[,1]=="post",])

dm = rbind(pre_res[,c(2,c(7:34))], post_res[,c(2,c(7:34))])
dm = cbind(utag=paste(dm[,1],row.names(dm),sep=""),dm)
df <- melt(dm1,id.vars=c("V1"))
p1=ggplot(dm1, aes(x=variable,y=value, group=utag)) + scale_y_log10() + geom_line(aes(colour = tag))


dd = cbind(data.frame(utag="pre_mean",tag="pre_mean"),t(colMeans(pre_res[,c(7:34)])))
dd = melt(dd,id.vars=c("utag","tag"))
df <- rbind(df,dd)

dd = cbind(data.frame(utag="post_mean",tag="post_mean"),t(colMeans(post_res[,c(7:34)])))
dd = melt(dd,id.vars=c("utag","tag"))
df <- rbind(df,dd)


# ggplot(df, aes(x=variable,y=value, group=utag)) + geom_path(aes(colour = tag))

p1=ggplot(df[grep("^pre",df$tag),], aes(x=variable,y=value, group=utag)) + scale_y_log10() + geom_line(aes(colour = tag))
p2=ggplot(df[grep("^post",df$tag),], aes(x=variable,y=value, group=utag)) + scale_y_log10() + geom_path(aes(colour = tag))

print(arrangeGrob(p1, p2, ncol=1))

write.table(pre_res,file="pre_portraits_monthly_google.txt", quote=F, col.names=T, row.names=F, sep="\t")
write.table(post_res,file="post_portraits_monthly_google.txt", quote=F, col.names=T, row.names=F, sep="\t")
