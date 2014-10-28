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
#
znorm <- function(ts){
  ts.mean <- mean(ts[1,])
  ts.dev <- sd(ts[1,])
  (ts - ts.mean)/ts.dev
}
#
data=read.table("patternz-best.txt",as.is=T,sep=" ")
dm1=as.data.frame(data[data[,1]=="pre",-1])
dm1=dm1[c(1,2,13,4),]
dm1$Samples=paste("trajectory-",c(1:length(dm1$V2)),sep="")
df <- melt(dm1)
df$variable=rep(c(1:28),each=4)
p1=ggplot(df, aes(x=variable,y=value, group=Samples, color=Samples)) + scale_y_log10() + ylab("Deleted LOC") + 
  scale_x_discrete(breaks=seq(0,28,by=7),labels=paste(seq(0,28,by=7))) + xlab("Days") +
  geom_line(size=2) +  theme_bw() + ggtitle("Pre-release trajectories with best class-characteristic pattern, ebbbebbbbbbb") +
  theme(legend.position="bottom")
p1


dm2=as.data.frame(data[data[,1]=="post",-1])
dm2=dm2[1:4,]
dm2$Samples=paste("trajectory-",c(1:length(dm2$V2)),sep="")
df2 <- melt(dm2)
df2$variable=rep(c(1:28),each=4)
p2=ggplot(df2, aes(x=variable,y=value, group=Samples, color=Samples)) + scale_y_log10() + ylab("Deleted LOC") + 
  scale_x_discrete(breaks=seq(0,28,by=7),labels=paste(seq(0,28,by=7))) + xlab("Days") +
  geom_line(size=2) +  theme_bw() + ggtitle("Post-release trajectories with best class-characteristic pattern edbbbbbbbbbb") +
  theme(legend.position="bottom")
p2
#
#
data=read.table("patterns-second-best.txt",as.is=T,sep=" ")
dm1=as.data.frame(data[data[,1]=="pre",-1])
dm1=dm1[c(1,2,5,6),]
dm1$Samples=paste("trajectory-",c(1:length(dm1$V2)),sep="")
df <- melt(dm1)
df$variable=rep(c(1:28),each=4)
p3=ggplot(df, aes(x=variable,y=value, color=Samples)) + scale_y_log10() + ylab("Deleted LOC") + 
  scale_x_discrete(breaks=seq(0,28,by=7),labels=paste(seq(0,28,by=7))) + xlab("Days") +
  geom_line(size=2) +  theme_bw() + ggtitle("Pre-release trajectories with second best class-characteristic pattern bbbbbcbbbebb") +
  theme(legend.position="bottom")
p3


dm2=as.data.frame(data[data[,1]=="post",-1])
dm2=dm2[c(3,4,5,13),]
dm2$Samples=paste("trajectory-",c(1:length(dm2$V2)),sep="")
df2 <- melt(dm2)
df2$variable=rep(c(1:28),each=4)
p4=ggplot(df2, aes(x=variable,y=value, group=Samples, color=Samples)) + scale_y_log10() + ylab("Deleted LOC") + 
  scale_x_discrete(breaks=seq(0,28,by=7),labels=paste(seq(0,28,by=7))) + xlab("Days") +
  geom_line(size=2) +  theme_bw() + ggtitle("Post-release trajectories with second best class-characteristic pattern bbbbbebbcbbb") +
  theme(legend.position="bottom")
p4

print(arrangeGrob(p1, p2, p3, p4, ncol=2))

Cairo(width = 1600, height = 1000, 
      file="figures/omap_deleted_lines_patterns_plot.png", 
      type="png", pointsize=9, 
      bg = "transparent", canvas = "white", units = "px", dpi = 82)
print(arrangeGrob(p1, p2, p3, p4, ncol=2))
dev.off()
ggsave(arrangeGrob(p1, p2, p3, p4, ncol=2), file="figures/omap_deleted_lines_patterns_plot.eps", width=14, height=8)
