select cp.`name`, c.* from OMAP.`change` c 
join OMAP.`change_people` cp on c.author_id = cp.id 
where upper(c.`subject`) LIKE "AM %";

SELECT distinct(cp.id) FROM OMAP.change_people cp 
join OMAP.change c on c.author_id = cp.id 
where c.project_id=1 AND c.author_date 
BETWEEN "2009-05-04" AND "2009-06-01" AND c.added_files>0;

SELECT * FROM OMAP.change_people WHERE id = 7973;

SELECT sum(c.added_files) `value`, DATE_FORMAT(c.author_date, "%Y-%m-%d") `date` from OMAP.change c 
where c.author_id=7973 and c.project_id=1 AND c.`author_date` BETWEEN "2009-05-04" AND "2009-06-01"
and NOT(upper(c.subject) LIKE "AM %") 
group by `date` order by `date`;

SELECT c.* FROM OMAP.change_people cp 
join OMAP.change c on c.author_id = cp.id 
where c.project_id=1 AND c.author_date 
BETWEEN "2009-05-04" AND "2009-06-01" AND c.`author_id`=7973;