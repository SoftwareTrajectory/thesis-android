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
# projects rating and summary
con <- dbConnect(MySQL(), user="omap", password="omap", dbname="android", host="localhost", port=3306)
#
# dates business
releases=read.table("../data/android_releases_major.csv",as.is=T,header=F)
names(releases)=c("index","date","release")
str(releases)
releases$date=as.POSIXct(releases$date)

get_series =function(id, start, end){
  dates=seq(start,end,by="day")
  res=rep(0,length(dates))
  for(i in c(1:(length(dates)))){
    ll <- dbGetQuery(con, 
           paste("select sum(c.added_lines) lines_added from android.android_change c ",
           "where c.author_id=",id, " and ",                 
           "c.project_id=18 and DATE_FORMAT(c.author_date, \"%Y-%m-%d\")=",
           shQuote(dates[i]),sep=""))
    if(!(is.na(ll))){
      res[i]=unlist(ll)
    }
  }
  res
}

monday_for_a_date = function(dt){
  wd=wday(dt)
  if(wd>2){
    dt - as.difftime(wd-2, unit="days")
  }else if(wd==1){
    dt - as.difftime(6, unit="days")
  }else{
    dt
  }  
}

pre_res=data.frame(release_id=7,tag="pre",id=7377,email="dimitrysh@google.com",start=as.Date("2013-02-03"),end=as.Date("2013-02-10"))
pre_res=cbind(pre_res,as.data.frame(t(get_series(7377,as.Date("2013-02-04"),as.Date("2013-02-10")))))

post_res=data.frame(release_id=7,tag="pre",id=7377,email="dimitrysh@google.com",start=as.Date("2013-02-03"),end=as.Date("2013-02-10"))
post_res=cbind(post_res,as.data.frame(t(get_series(7377,as.Date("2013-02-04"),as.Date("2013-02-10")))))

get_series(7377,as.Date("2013-02-04"),as.Date("2013-02-11"))

#
for(k in c(1:(length(releases$date)))){
  print(releases[k,])
  dt <- as.Date(releases[k,]$date)
  release_monday=monday_for_a_date(dt) 
  monday_before =release_monday-as.difftime(1, unit="weeks")
  monday_after  =release_monday+as.difftime(1, unit="weeks")
  print(paste("release monday", release_monday, "monday pre", monday_before, "monday post ", monday_after))
  for(i in c(1:4)){
   new.dt_pre_start  <- monday_before - as.difftime(i, unit="weeks")
   new.dt_pre_end    <- monday_before - as.difftime(i-1, unit="weeks") - as.difftime(1, unit="days")
   new.dt_post_start <- monday_after + as.difftime(i-1, unit="weeks")
   new.dt_post_end   <- monday_after + as.difftime(i, unit="weeks") - as.difftime(1, unit="days")
   print(paste(new.dt_post_start, new.dt_post_end))
   # PRE-RELEASE
   # users
   users=dbGetQuery(con, 
      paste("select distinct(c.author_id), cp.email from android.android_change c ",
      "join android.change_people cp on c.author_id=cp.id ",
      "where cp.email like \"%google.com\" and  c.added_lines>0 and ",                 
      "c.project_id=18 and DATE_FORMAT(c.author_date, \"%Y-%m-%d\") between ",
      shQuote(new.dt_pre_start), " and ", shQuote(new.dt_pre_end),
      sep=""))
   if(length(users)>0){
    for(j in c(1:(length(users$author_id)))){
      user=users[j,]$author_id
      email=users[j,]$email
      series=get_series(user, new.dt_pre_start, new.dt_pre_end)
      dd=data.frame(release_id=k,tag="pre",id=user,email=email,start=new.dt_pre_start,end=new.dt_pre_end)
      dd=cbind(dd,as.data.frame(t(series)))
      print(dd)
      pre_res=rbind(pre_res,dd)
    }   
   }
   # POST-RELEASE
   # users
   users=dbGetQuery(con, 
         paste("select distinct(c.author_id), cp.email from android.android_change c ",
         "join android.change_people cp on c.author_id=cp.id ",                 
         "where cp.email like \"%google.com\" and  c.added_lines>0 and ",                 
         "c.project_id=18 and DATE_FORMAT(c.author_date, \"%Y-%m-%d\") between ",
         shQuote(new.dt_post_start), " and ", shQuote(new.dt_post_end),
         sep=""))
   if(length(users)>0){
     for(j in c(1:(length(users$author_id)))){
       user=users[j,]$author_id
       email=users[j,]$email
       series=get_series(user, new.dt_post_start, new.dt_post_end)
       dd=data.frame(release_id=k,tag="post",id=user,email=email,start=new.dt_post_start,end=new.dt_post_end)
       dd=cbind(dd,as.data.frame(t(series)))
       print(dd)
       post_res=rbind(post_res,dd)
     }   
   }
  } 
}
post_res[1,]
post_res=post_res[-1,]
post_res[1,]


pre_res[1,]
pre_res=pre_res[-1,]
pre_res[1,]

dim(post_res)
dim(pre_res)

dm = rbind(pre_res[,c(2,7,8,9,10,11,12,13)], post_res[,c(2,7,8,9,10,11,12,13)])
dm = cbind(utag=paste(dm[,1],row.names(dm),sep=""),dm)
df <- melt(dm,id.vars=c("utag","tag"))

dd = cbind(data.frame(utag="pre_mean",tag="pre_mean"),t(colMeans(pre_res[,c(7,8,9,10,11,12,13)])))
dd = melt(dd,id.vars=c("utag","tag"))
df <- rbind(df,dd)

dd = cbind(data.frame(utag="post_mean",tag="post_mean"),t(colMeans(post_res[,c(7,8,9,10,11,12,13)])))
dd = melt(dd,id.vars=c("utag","tag"))
df <- rbind(df,dd)


# ggplot(df, aes(x=variable,y=value, group=utag)) + geom_path(aes(colour = tag))

p1=ggplot(df[grep("^pre",df$tag),], aes(x=variable,y=value, group=utag)) + scale_y_log10() + geom_line(aes(colour = tag))
p2=ggplot(df[grep("^post",df$tag),], aes(x=variable,y=value, group=utag)) + scale_y_log10() + geom_path(aes(colour = tag))

print(arrangeGrob(p1, p2, ncol=1))

write.table(pre_res,file="pre_portraits_weekly_google.txt", quote=F, col.names=T, row.names=F, sep="\t")
write.table(post_res,file="post_portraits_weekly_google.txt", quote=F, col.names=T, row.names=F, sep="\t")

#
# monthly
#
#
dt = as.Date("2013-02-04")
pre_res=data.frame(release_id=7,tag="pre",id=7377,email="dimitrysh@google.com",start=as.Date("2013-02-03"),end=as.Date("2013-02-10"))
pre_res=cbind(pre_res,as.data.frame(t(get_series(7377,dt,dt+as.difftime(4, unit="weeks")))))

post_res=pre_res

#
for(k in c(1:(length(releases$date)))){
  print(releases[k,])
  dt <- as.Date(releases[k,]$date)
  release_monday=monday_for_a_date(dt) 
  monday_before =release_monday-as.difftime(4, unit="weeks")
  monday_after  =release_monday+as.difftime(1, unit="weeks")
  print(paste("release monday", release_monday, "monday pre", monday_before, "monday post ", monday_after))
  # month starts
    new.dt_pre_start  <- monday_before
    new.dt_pre_end    <- monday_before + as.difftime(4, unit="weeks")
    new.dt_post_start <- monday_after
    new.dt_post_end   <- monday_after + as.difftime(4, unit="weeks")
    print(paste(new.dt_pre_start, new.dt_pre_end))
    # PRE-RELEASE
    # users
    users=dbGetQuery(con, 
        paste("select distinct(c.author_id), cp.email from android.android_change c ",
        "join android.change_people cp on c.author_id=cp.id ",
        "where cp.email like \"%google.com\" and c.added_lines>0 and ",                 
        "c.project_id=18 and DATE_FORMAT(c.author_date, \"%Y-%m-%d\") between ",
        shQuote(new.dt_pre_start), " and ", shQuote(new.dt_pre_end),
        sep=""))
    if(length(users)>0){
      for(j in c(1:(length(users$author_id)))){
        user=users[j,]$author_id
        email=users[j,]$email
        series=get_series(user, new.dt_pre_start, new.dt_pre_end)
        dd=data.frame(release_id=k,tag="pre",id=user,email=email,start=new.dt_pre_start,end=new.dt_pre_end)
        dd=cbind(dd,as.data.frame(t(series)))
        print(dd)
        pre_res=rbind(pre_res,dd)
      }   
    }
    # POST-RELEASE
    # users
    print(paste(new.dt_post_start, new.dt_post_end))
    users=dbGetQuery(con, 
        paste("select distinct(c.author_id), cp.email from android.android_change c ",
        "join android.change_people cp on c.author_id=cp.id ",                 
        "where cp.email like \"%google.com\" and  c.added_lines>0 and ",                 
        "c.project_id=18 and DATE_FORMAT(c.author_date, \"%Y-%m-%d\") between ",
        shQuote(new.dt_post_start), " and ", shQuote(new.dt_post_end),
        sep=""))
    if(length(users)>0){
      for(j in c(1:(length(users$author_id)))){
        user=users[j,]$author_id
        email=users[j,]$email
        series=get_series(user, new.dt_post_start, new.dt_post_end)
        dd=data.frame(release_id=k,tag="post",id=user,email=email,start=new.dt_post_start,end=new.dt_post_end)
        dd=cbind(dd,as.data.frame(t(series)))
        print(dd)
        post_res=rbind(post_res,dd)
      }   
    }
}
post_res[1,]
post_res=post_res[-1,]
post_res[1,]


pre_res[1,]
pre_res=pre_res[-1,]
pre_res[1,]

dim(post_res)
dim(pre_res)

dm = rbind(pre_res[,c(2,c(7:34))], post_res[,c(2,c(7:34))])
dm = cbind(utag=paste(dm[,1],row.names(dm),sep=""),dm)
df <- melt(dm,id.vars=c("utag","tag"))

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
