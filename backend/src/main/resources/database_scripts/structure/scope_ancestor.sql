create or replace view scope_ancestor
as with recursive recursive_scope_ancestor (scope_fk, ancestor_fk, start_date, end_date, direct, `virtual`, default_relation, ancestor_deleted) as (
	select
		scope_fk,
		parent_fk,
		sr.start_date,
		sr.end_date,
		1 as direct,
		s.`virtual`,
		sr.`default`,
		deleted as ancestor_deleted
	from scope_relation sr inner join scope s on sr.parent_fk = s.pk
	union all
	select
		rsa.scope_fk,
		sr.parent_fk,
		case
			when sr.start_date is null then rsa.start_date
			when rsa.start_date is null then sr.start_date
			else greatest(sr.start_date, rsa.start_date)
		end as start_date,
		case
			when sr.end_date is null then rsa.end_date
			when rsa.end_date is null then sr.end_date
			else least(sr.end_date, rsa.end_date)
		end as end_date,
		0 as direct,
		s.virtual or rsa.virtual,
		sr.`default` && rsa.`default_relation`,
		s.deleted or rsa.ancestor_deleted AS ancestor_deleted
	from recursive_scope_ancestor rsa
	inner join scope_relation sr on sr.scope_fk = rsa.ancestor_fk
	inner join scope s on sr.parent_fk = s.pk
	)
select
	scope_fk,
	ancestor_fk,
	min(start_date) as start_date,
-- If all end_dates are null, then null is returned implicitly.
-- Otherwise we return a max of end_date, knowing that max function ignores the null values
	CASE WHEN MAX(CASE WHEN end_date IS NULL THEN 1 ELSE 0 END) = 0
		THEN MAX(end_date)
	END as end_date,
	bit_or(direct) as direct,
	bit_and(`virtual`) as `virtual`,
	bit_or(default_relation) as `default`,
	bit_and(ancestor_deleted) as ancestor_deleted
from recursive_scope_ancestor
group by scope_fk, ancestor_fk
order by scope_fk;
