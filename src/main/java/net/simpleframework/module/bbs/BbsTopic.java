package net.simpleframework.module.bbs;

import java.util.Date;

import net.simpleframework.ado.db.common.EntityInterceptor;
import net.simpleframework.common.ID;
import net.simpleframework.module.common.content.AbstractContentBean;
import net.simpleframework.module.common.content.EContentStatus;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@EntityInterceptor(listenerTypes = { "net.simpleframework.module.log.EntityUpdateLogAdapter",
		"net.simpleframework.module.log.EntityDeleteLogAdapter" }, columns = { "topic", "status",
		"bbsType", "best", "recommendation", "recommendationDuration" })
public class BbsTopic extends AbstractContentBean {
	/* 类目id */
	private ID categoryId;

	/* 类别 */
	private EBbsType bbsType;

	private EAskStatus askStatus;

	/* 精华贴 */
	private boolean best;

	/* 最后提交时间, 排序字段 */
	private Date lastPostDate;
	/* 最后提交人,统计数据 */
	private ID lastUserId;

	/* 跟贴数 */
	private int posts;
	/* 收藏,统计数据 */
	private int favorites;

	/* 推荐级别 >=0, 0表示取消推荐 */
	private int recommendation;

	/* 推荐日期 */
	private Date recommendationDate;

	/* 推荐时长, 单位s. 当超出推荐推荐时长后, recommendation=0 */
	private int recommendationDuration;

	public ID getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(final ID categoryId) {
		this.categoryId = categoryId;
	}

	public EBbsType getBbsType() {
		if (bbsType == null) {
			bbsType = EBbsType.normal;
		}
		return bbsType;
	}

	public void setBbsType(final EBbsType bbsType) {
		this.bbsType = bbsType;
	}

	public EAskStatus getAskStatus() {
		if (askStatus == null) {
			askStatus = EAskStatus.unresolved;
		}
		return askStatus;
	}

	public void setAskStatus(final EAskStatus askStatus) {
		this.askStatus = askStatus;
	}

	public int getPosts() {
		return posts;
	}

	public void setPosts(final int posts) {
		this.posts = posts;
	}

	public boolean isBest() {
		return best;
	}

	public void setBest(final boolean best) {
		this.best = best;
	}

	public Date getLastPostDate() {
		return lastPostDate != null ? lastPostDate : getCreateDate();
	}

	public void setLastPostDate(final Date lastPostDate) {
		this.lastPostDate = lastPostDate;
	}

	public ID getLastUserId() {
		return lastUserId;
	}

	public void setLastUserId(final ID lastUserId) {
		this.lastUserId = lastUserId;
	}

	public int getFavorites() {
		return favorites;
	}

	public void setFavorites(final int favorites) {
		this.favorites = favorites;
	}

	public int getRecommendation() {
		return recommendation;
	}

	public void setRecommendation(final int recommendation) {
		this.recommendation = recommendation;
	}

	public Date getRecommendationDate() {
		return recommendationDate;
	}

	public void setRecommendationDate(final Date recommendationDate) {
		this.recommendationDate = recommendationDate;
	}

	public int getRecommendationDuration() {
		return recommendationDuration;
	}

	public void setRecommendationDuration(final int recommendationDuration) {
		this.recommendationDuration = recommendationDuration;
	}

	{
		setStatus(EContentStatus.publish);
	}

	private static final long serialVersionUID = 4367483799292510748L;
}
