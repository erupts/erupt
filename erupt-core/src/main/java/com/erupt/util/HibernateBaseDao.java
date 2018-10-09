//package com.erupt.util;
//
//import org.hibernate.*;
//import org.hibernate.criterion.Criterion;
//import org.hibernate.criterion.Order;
//import org.hibernate.criterion.Restrictions;
//import org.hibernate.sql.JoinType;
//import org.hibernate.transform.Transformers;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Repository;
//
//import java.io.Serializable;
//import java.util.*;
//
//@Repository
//@SuppressWarnings("unchecked")
//public class HibernateBaseDao {
//
//	public static final int BATCH_SIZE = 30;
//
//	@Autowired
//	private SessionFactory sessionFactory;
//
//	public void deleteWithHql(String hql) {
//		this.getSession().createQuery(hql).executeUpdate();
//	}
//
//	public void save(Object entity) {
//		this.getSession().saveOrUpdate(entity);
//	}
//
//	public void flush() {
//		this.getSession().flush();
//	}
//
//	public void refresh(Object object) {
//		this.getSession().refresh(object);
//	}
//
//	public Object merge(Object object) {
//		return this.getSession().merge(object);
//	}
//
//	public void evict(Object obj) {
//		this.getSession().evict(obj);
//	}
//
//	public void update(Object entity) {
//		this.getSession().saveOrUpdate(entity);
//	}
//
//	public <T> T get(Class<T> clazz, Serializable id) {
//		return (T) this.getSession().get(clazz, id);
//	}
//
//	public <T> T get(Class<T> clazz, Serializable id,LockOptions lockOptions) {
//		return (T) this.getSession().get(clazz, id, lockOptions);
//	}
//
//	public void delete(Object entity) {
//		this.getSession().delete(entity);
//	}
//
//	public <T> void delete(Class<T> clazz, Serializable id) {
//		Session session = this.getSession();
//		session.delete(session.get(clazz, id));
//	}
//
//	public <T> List<T> findByHql(Class<T> clazz, String hql) {
//		return this.getSession().createQuery(hql).list();
//	}
//
//	public List<Object> find(Class<?> clazz, List<Criterion> criterions) {
//		return this.createCriteria(clazz, criterions, null).list();
//	}
//
//	public <T> List<T> findByGeneric(Class<T> clazz, List<Criterion> criterions) {
//		return this.createCriteria(clazz, criterions, null).list();
//	}
//
//	public Criteria getCriteria(Class<?> clazz) {
//		return this.getSession().createCriteria(clazz);
//	}
//
//	public Query getQuery(String hql) {
//		return this.getSession().createQuery(hql);
//	}
//
//	public Criteria getCriteria(Class<?> clazz, String alias) {
//		return this.getSession().createCriteria(clazz, alias);
//	}
//
//	public <T> List<T> find(Class<T> clazz, List<Criterion> criterions, List<Order> orders) {
//		return this.createCriteria(clazz, criterions, orders).list();
//	}
//
////	public Page findPage(Class<?> clazz, Page page, Criteria criteria) {
////		return this.findPage(clazz, page, criteria, null);
////	}
//
//	// public <T> List<T> findByProperties(Class<T> clazz,
//	// List<String> propertyList) {
//	// Criteria criteria = this.getSession().createCriteria(clazz);
//	//
//	// ProjectionList pList = null;
//	// if (null != propertyList && !propertyList.isEmpty()) {
//	// pList = Projections.projectionList();
//	// for (String pName : propertyList) {
//	// pList.add(Projections.property(pName).as(pName));
//	// }
//	// }
//	//
//	// criteria.setProjection(pList);
//	// criteria.setResultTransformer(Transformers.aliasToBean(clazz));
//	// return criteria.list();
//	// }
//
//	public <T> List<T> findMultipe(Class<T> clazz, Collection<Serializable> ids) {
//		if (ids.isEmpty()) {
//			return Collections.<T>emptyList();
//		}
//		Criterion[] criterions = new Criterion[ids.size()];
//
//		Iterator<Serializable> iterator = ids.iterator();
//		int i = 0;
//		while (iterator.hasNext()) {
//			criterions[i] = Restrictions.eq("id", iterator.next());
//			i++;
//		}
//		return this.getSession().createCriteria(clazz).add(Restrictions.or(criterions)).list();
//	}
//
////	public <T> List<T> findByProperties(Class<T> clazz, List<String> propertyList) {
////		return this.findByProperties(clazz, propertyList, null);
////	}
//
////	public <T> List<T> findByProperties(Class<T> clazz, String fields) {
////		return this.findByProperties(clazz, fields, null, null, null);
////	}
//
////	public <T> List<T> findByProperties(Class<T> clazz, String fields, String filter, ResultTransformer transformer,
////			List<Object> parameters) {
////		return this.findByProperties(clazz, null, fields, filter, null, null);
////	}
//
////	public <T> List<T> findByProperties(Class<T> clazz, String join, String fields, String filter,
////                                        ResultTransformer transformer, List<Object> parameters) {
////		StringBuffer hql = new StringBuffer("select ");
////		hql.append(fields);
////		hql.append(" from ");
////		hql.append(clazz.getName());
////		if (StringUtils.isNotEmpty(join)) {
////			hql.append(" as ").append(clazz.getSimpleName().toLowerCase());
////			hql.append(join).append(" ");
////		}
////		if (null != filter && !"".equals(filter)) {
////			hql.append(" where ");
////			hql.append(filter);
////		}
////		Domain domain = clazz.getAnnotation(Domain.class);
////		if (domain != null && StringUtils.isNotEmpty(domain.defaultSort())) {
////			String[] sorts = domain.defaultSort().split(",");
////			for (int i = 0; i < sorts.length; i++) {
////				String s = sorts[i];
////				if (s.startsWith("-")) {
////					sorts[i] = s.substring(1) + " desc";
////				}
////			}
////			hql.append(" order by  ");
////			hql.append(StringUtils.join(sorts, ","));
////		}
////		Query query = this.getSession().createQuery(hql.toString());
////
////		if (null != parameters && !parameters.isEmpty()) {
////			for (int i = 0; i < parameters.size(); i++) {
////				query.setParameter(i, parameters.get(i));
////			}
////		}
////
////		if (null != transformer) {
////			query.setResultTransformer(transformer);
////		}
////		return query.list();
////	}
//
////	public <T> List<T> findByProperties(Class<T> clazz, String fields, String filter) {
////		return this.findByProperties(clazz, fields, filter, null, null);
////	}
////
////	public <T> List<T> findByProperties(Class<T> clazz, String fields, String filter, List<Object> parameters) {
////		return this.findByProperties(clazz, fields, filter, null, parameters);
////	}
////
////	public <T> List<T> findByProperties(Class<T> clazz, List<String> propertyList, String filter) {
////		return this.findByProperties(clazz, StringUtils.join(propertyList, ","), filter);
////	}
//
//	public List<Object> findList(Class<?> clazz, int pageNumber, int pageSize, String sort, Criterion... criterions) {
//		Criteria criteria = this.getSession().createCriteria(clazz);
//		for (Criterion criterion : criterions) {
//			criteria.add(criterion);
//		}
//
//		if (null != sort && !"".equals(sort)) {
//			if (sort.startsWith("-")) {
//				criteria.addOrder(Order.desc(sort.substring(1, sort.length())));
//			} else {
//				criteria.addOrder(Order.asc(sort));
//			}
//		}
//
//		if (pageSize > 0) {
//			criteria.setFirstResult((pageNumber - 1) * pageSize);
//			criteria.setMaxResults(pageSize);
//		}
//
//		return criteria.list();
//	}
//
////	public Page findPage(Class<?> clazz, Page page, Criteria criteria, List<TableColumnDesc> columns) {
////		// String className = clazz.getSimpleName().toLowerCase();
////		// Criteria criteria = this.getSession().createCriteria(clazz,
////		// className);
////		//
////		// // if (null != page.getExample()) {
////		// // criteria.add(Example.create(page.getExample()));
////		// // }
////		//
////		// for (Criterion criterion : criterions) {
////		// criteria.add(criterion);
////		// }
////
////		criteria.setProjection(Projections.rowCount());
////		page.setTotal((Long) criteria.uniqueResult());
////
////		ProjectionList pList = null;
////		if (null != columns && !columns.isEmpty()) {
////			pList = Projections.projectionList();
////			List<TableColumnDesc> projectionColumns = new ArrayList<TableColumnDesc>();
////			for (TableColumnDesc columnDesc : columns) {
////				if (columnDesc.hasHandler()) {
////					continue;
////				}
////				String fieldPath = columnDesc.getName();
////				if (columnDesc.hasHandler() && isTransientField(clazz, fieldPath.split("_"))) {
////					continue;
////				}
////				projectionColumns.add(columnDesc);
////			}
////			this.fillProjectionListFromTableColumnDefine(pList, projectionColumns, criteria);
////		}
////
////		criteria.setProjection(pList);
////		String sort = page.getSort();
////		if (null != sort && !"".equals(sort)) {
////			String[] sorts = sort.split(",");
////			for (String s : sorts) {
////				boolean desc = false;
////				if (s.startsWith("-")) {
////					s = s.substring(1);
////					desc = true;
////				}
////				String[] array = s.split("\\.");
////				String sortProperty = array[array.length - 1];
////				if (array.length > 1) {
////					sortProperty = StringUtils.join(ArrayUtils.subarray(array, 0, array.length - 1), "_") + "."
////							+ sortProperty;
////				}
////				if (desc) {
////					criteria.addOrder(Order.desc(sortProperty));
////				} else {
////					criteria.addOrder(Order.asc(sortProperty));
////				}
////			}
////		}
////
////		if (page.getPageSize() > 0) {
////			criteria.setFirstResult((page.getPageNumber() - 1) * page.getPageSize());
////			criteria.setMaxResults(page.getPageSize());
////		}
////		criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
////		page.setList(criteria.list());
////
////		return page;
////	}
//
//
//
//	private void createCriteriasForPath(String criteriaPath, Set<String> criteriaPathSet, Criteria criteria) {
//		List<String> pathList = new ArrayList<String>();
//		for (String path : criteriaPath.split("_")) {
//			pathList.add(path);
//			String currPath = String.join("_",pathList);
//			if (!criteriaPathSet.contains(currPath)) {
//				criteria.createAlias(String.join(".",pathList), currPath, JoinType.LEFT_OUTER_JOIN);
//				criteriaPathSet.add(currPath);
//			}
//		}
//	}
//
//	public Long count(String hql) {
//		return (Long) this.getSession().createQuery(hql).uniqueResult();
//	}
//
//	public Long sqlCount(String sql, Map<String, Object> params) {
//		String csql = "select count(1) from (" + sql + ") as count_table";
//		SQLQuery query = this.getSession().createSQLQuery(csql);
//		if (null != params) {
//			for (String key : params.keySet()) {
//				query.setParameter(key, params.get(key));
//			}
//		}
//
//		return Long.parseLong(String.valueOf(query.uniqueResult()));
//	}
//
//	public Long count(String hql, List<Object> param) {
//		Query q = this.getSession().createQuery(hql);
//		if (param != null && param.size() > 0) {
//			for (int i = 0; i < param.size(); i++) {
//				q.setParameter(i, param.get(i));
//			}
//		}
//		return (Long) q.uniqueResult();
//	}
//
//	public Session getSession() {
//		return this.sessionFactory.getCurrentSession();
//	}
//
//	public Session openSession() {
//		return this.sessionFactory.openSession();
//	}
//
//	public StatelessSession getStatelessSession() {
//		return this.sessionFactory.openStatelessSession();
//	}
//
//	private <T> Criteria createCriteria(Class<T> clazz, List<Criterion> criterions, List<Order> orders) {
//		Criteria criteria = getSession().createCriteria(clazz);
//		if (null != criterions) {
//			for (Criterion criterion : criterions) {
//				criteria.add(criterion);
//			}
//		}
//
//		if (null != orders) {
//			for (Order order : orders) {
//				criteria.addOrder(order);
//			}
//		}
//		return criteria;
//	}
//
//	public Object queryUniqueById(String hql, Long id) {
//		Query query = this.getSession().createQuery(hql);
//		query.setParameter("id", id);
//		query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
//		return query.uniqueResult();
//	}
//
//	public void clear() {
//		// TODO Auto-generated method stub
//		this.getSession().clear();
//	}
//
//	public List<Object> findMapBySql(String sql, Map<String, Object> params, int firstResult, int pageSize) {
//		SQLQuery query = this.getSession().createSQLQuery(sql);
//		if (null != params) {
//			for (String key : params.keySet()) {
//				query.setParameter(key, params.get(key));
//			}
//		}
//		query.setFirstResult(firstResult);
//		query.setMaxResults(pageSize);
//		query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
//		return query.list();
//	}
//
//	// public List<Object> findListBySql(String sql) {
//	// SQLQuery query = this.getSession().createSQLQuery(sql);
//	// return query.list();
//	// }
//
//	public List<Object> findListBySql(String sql, Map<String, Object> params) {
//		SQLQuery query = this.getSession().createSQLQuery(sql);
//		if (null != params) {
//			for (String key : params.keySet()) {
//				query.setParameter(key, params.get(key));
//			}
//		}
//		return query.list();
//	}
//
//	public ScrollableResults getScrollResultSet(String sql, Map<String, Object> params) {
//		SQLQuery query = this.getSession().createSQLQuery(sql);
//		if (null != params) {
//			for (String key : params.keySet()) {
//				query.setParameter(key, params.get(key));
//			}
//		}
//		query.setReadOnly(true);
//		query.setCacheable(false);
//		return query.scroll(ScrollMode.FORWARD_ONLY);
//	}
//}
