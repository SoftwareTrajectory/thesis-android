#
#
#
select cp.name name, count(distinct(ac.commit_hash)) commits, count(distinct(ac.author_id)) authors, 
count(distinct(ac.committer_id)) committers, count(distinct(target)) targets
from change_project cp
join android_change ac on ac.project_id=cp.id
join change_target ct on ct.change_id=ac.id
group by name order by commits desc limit 10;

