#
# generates figure 1 from section
#
require(RMySQL)
require(ggplot2)
require(scales)
require(gridExtra)
require(Cairo)
#
# projects rating and summary
con <- dbConnect(MySQL(), user="omap", password="omap", dbname="OMAP", host="localhost", port=3306)
#daily
res_d <- dbGetQuery(con, 
    paste("select DATE_FORMAT(c.author_date, \"%Y-%m-%d\") a_date, sum(c.added_lines) lines_added from OMAP.change c ",
          "join OMAP.change_people cp on c.author_id=cp.id where cp.email like \"%google.com\" and ", 
          "c.project_id=1 and DATE_FORMAT(c.author_date, \"%Y-%m-%d\")>\"2004-01-01\" ",
          "group by YEAR(a_date),MONTH(a_date),DAY(a_date) ",
          "order by a_date;",sep=""))
#weekly
res_w <- dbGetQuery(con, 
          paste("select DATE_FORMAT(c.author_date, \"%Y-%m-%d\") a_date, sum(c.added_lines) lines_added from OMAP.change c ",
          "join OMAP.change_people cp on c.author_id=cp.id where cp.email like \"%google.com\" and ",                 
          "c.project_id=1 and DATE_FORMAT(c.author_date, \"%Y-%m-%d\")>\"2004-01-01\" ",
          "group by YEAR(a_date),MONTH(a_date),WEEK(a_date) ",
          "order by a_date;",sep=""))
# monthly
res_m <- dbGetQuery(con, 
          paste("select DATE_FORMAT(c.author_date, \"%Y-%m-%d\") a_date, sum(c.added_lines) lines_added from OMAP.change c ",
          "join OMAP.change_people cp on c.author_id=cp.id where cp.email like \"%google.com\" and ",                 
          "c.project_id=1 and DATE_FORMAT(c.author_date, \"%Y-%m-%d\")>\"2004-01-01\" ",
          "group by YEAR(a_date),MONTH(a_date) ",
          "order by a_date;",sep=""))
write.table(res_d, file="daily-google.csv",col.names=T)
write.table(res_w, file="weekly-google.csv",col.names=T)
write.table(res_m, file="monthly-google.csv",col.names=T)
res_d$a_date=as.POSIXct(res_d$a_date)
res_w$a_date=as.POSIXct(res_w$a_date)
res_m$a_date=as.POSIXct(res_m$a_date)
#
releases=read.table("../data/android_releases.csv",as.is=T,header=F)
names(releases)=c("date","release")
str(releases)
releases$date=as.POSIXct(releases$date)
#
p_d <- ggplot(data=res_d[res_d$a_date>as.POSIXct(as.Date("2008-07-01")),], aes(x=a_date,y=lines_added)) + geom_line() + 
  scale_y_log10("Added lines") + scale_x_datetime("Time") + ggtitle("Android OMAP, Added new code lines, DAILY")
p_d = p_d + geom_vline(xintercept=as.numeric(releases[releases$date<max(res$a_date),]$date), linetype=4) + theme_bw()
p_d
#
p_w <- ggplot(data=res_w[res_w$a_date>as.POSIXct(as.Date("2008-07-01")),], aes(x=a_date,y=lines_added)) + geom_line() + 
  scale_y_log10("Added lines") + scale_x_datetime("Time") + ggtitle("Android OMAP, Added new code lines, WEEKLY")
p_w = p_w + geom_vline(xintercept=as.numeric(releases[releases$date<max(res$a_date),]$date), linetype=4) + theme_bw()
p_w
#
p_m <- ggplot(data=res_m[res_m$a_date>as.POSIXct(as.Date("2008-07-01")),], aes(x=a_date,y=lines_added)) + geom_line() + 
  scale_y_log10("Added lines") + scale_x_datetime("Time") + ggtitle("Android OMAP, Added new code lines, MONTHLY")
p_m = p_m + geom_vline(xintercept=as.numeric(releases[releases$date<max(res$a_date),]$date), linetype=4) + theme_bw()
p_m
#
print(arrangeGrob(p_d, p_w, p_m, ncol=1))
Cairo(width = 800, height = 450, 
      file="figures/omap_added_lines_plot.png", 
      type="png", pointsize=9, 
      bg = "transparent", canvas = "white", units = "px", dpi = 82)
print(arrangeGrob(p_d, p_w, p_m, ncol=1))
dev.off()
ggsave(arrangeGrob(p_d, p_w, p_m, ncol=1), file="figures/omap_added_lines_plot.eps", width=10, height=6)
