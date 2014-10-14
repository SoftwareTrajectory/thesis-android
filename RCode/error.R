require(ggplot2)
require(Cairo)
require(reshape)
require(scales)
require(RColorBrewer)
require(grid)
library(gridExtra)
require(lattice)

##
##
##
##

data=read.csv("../results/step03_out_log.txt",head=F)
names(data)=c("strategy","window","paa","alphabet","error")

d=data[data$strategy=="EXACT",]
d=data[(data$window>=19.5 && data$window<21.5),]

p=wireframe(Error ~ paa * Alphabet, data = d, group=gr, scales = list(arrows = FALSE),
          drape = TRUE, colorkey = TRUE, screen = list(z = 40, x = -56, y=10),
            aspect = c(97/77, 0.8),
            xlim=range(d$paa), ylim=range(d$Alphabet), zlim=c(0.05, 0.2),
            main=paste("Gun/No Gun Classifier error rate, SLIDING_WINDOW=40"),
            col.regions = terrain.colors(100, alpha = 1) )
p


Cairo(width = 700, height = 750, file="gun/parameters.png", type="png", pointsize=12, 
      bg = "white", canvas = "white", units = "px", dpi = "auto")
print(p)
dev.off()


###
set.seed(3)
dat <- data.frame(Dates = rep(seq(Sys.Date(), Sys.Date() + 9, by = 1), 
                              each = 24),
                  Times = rep(0:23, times = 10),
                  Value = rep(c(0:12,11:1), times = 10) + rnorm(240))

new.dates <- with(dat, sort(unique(Dates)))
new.times <- with(dat, sort(unique(Times)))
new.values <- with(dat, matrix(Value, nrow = 10, ncol = 24, byrow = TRUE))

persp(new.dates, new.times, new.values, ticktype = "detailed", r = 10, 
      theta = 35, scale = FALSE)

require(lattice)
wireframe(Value ~ as.numeric(Dates) + Times, data = dat, drape = TRUE)

require(rgl)
open3d()
x <- sort(rnorm(1000))
y <- rnorm(1000)
z <- rnorm(1000) + atan2(x,y)
plot3d(data$paa, data$Alphabet, data$Error, type="l")