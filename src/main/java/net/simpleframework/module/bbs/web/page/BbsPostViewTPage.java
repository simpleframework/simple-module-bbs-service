package net.simpleframework.module.bbs.web.page;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.simpleframework.ado.bean.AbstractUserAwareBean;
import net.simpleframework.ado.lucene.ILuceneManager;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.DateUtils;
import net.simpleframework.common.ETimePeriod;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.TimePeriod;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.common.web.HttpUtils;
import net.simpleframework.common.web.JavascriptUtils;
import net.simpleframework.common.web.html.HtmlUtils;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.ctx.permission.PermissionConst;
import net.simpleframework.ctx.permission.PermissionUser;
import net.simpleframework.ctx.script.MVEL2Template;
import net.simpleframework.ctx.service.ado.ITreeBeanServiceAware;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.module.bbs.BbsAskVote;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.BbsPost;
import net.simpleframework.module.bbs.BbsTopic;
import net.simpleframework.module.bbs.BbsUserStat;
import net.simpleframework.module.bbs.EBbsType;
import net.simpleframework.module.bbs.IBbsCategoryService;
import net.simpleframework.module.bbs.IBbsContext;
import net.simpleframework.module.bbs.IBbsPostService;
import net.simpleframework.module.bbs.IBbsTopicService;
import net.simpleframework.module.bbs.web.BbsFavoriteRef;
import net.simpleframework.module.bbs.web.BbsPageletCreator;
import net.simpleframework.module.bbs.web.BbsPageletCreator.BbsListRowHandler;
import net.simpleframework.module.bbs.web.BbsUrlsFactory;
import net.simpleframework.module.bbs.web.BbsUtils;
import net.simpleframework.module.bbs.web.IBbsWebContext;
import net.simpleframework.module.bbs.web.IBbsWebContext.AttachmentDownloadHandler;
import net.simpleframework.module.bbs.web.page.t2.BbsCategoryPage;
import net.simpleframework.module.bbs.web.page.t2.BbsPostViewPage;
import net.simpleframework.module.bbs.web.page.t2.BbsTopicFormPage;
import net.simpleframework.module.bbs.web.page.t2.BbsTopicListPage;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.ContentException;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.module.common.web.content.ContentUtils;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.TextForward;
import net.simpleframework.mvc.common.DownloadUtils;
import net.simpleframework.mvc.common.element.EElementEvent;
import net.simpleframework.mvc.common.element.ImageElement;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.JS;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.PhotoImage;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.EWarnType;
import net.simpleframework.mvc.component.base.validation.ValidationBean;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.component.ext.ckeditor.HtmlEditorBean;
import net.simpleframework.mvc.component.ui.pager.AbstractPagerHandler;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.PagerBean;
import net.simpleframework.mvc.component.ui.tooltip.ETipElement;
import net.simpleframework.mvc.component.ui.tooltip.ETipPosition;
import net.simpleframework.mvc.component.ui.tooltip.TipBean;
import net.simpleframework.mvc.component.ui.tooltip.TipBean.HideOn;
import net.simpleframework.mvc.component.ui.tooltip.TipBean.Hook;
import net.simpleframework.mvc.component.ui.tooltip.TooltipBean;
import net.simpleframework.mvc.template.AbstractTemplatePage;
import net.simpleframework.mvc.template.struct.CategoryItem;
import net.simpleframework.mvc.template.struct.EImageDot;
import net.simpleframework.mvc.template.struct.NavigationButtons;
import net.simpleframework.mvc.template.struct.Pagelet;
import net.simpleframework.mvc.template.struct.Pagelets;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsPostViewTPage extends AbstractBbsTPage {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		pp.addImportJavascript(AbstractBbsTPage.class, "/js/bbs.js");

		final BbsTopic topic = getTopic(pp);
		ContentUtils.updateViews(pp, topic, bbsContext.getTopicService());

		// 记录到cookies
		ContentUtils.addViewsCookie(pp, "bbs_views", topic.getId());

		// 分页
		addComponentBean(pp, "BbsPostViewTPage_pager", PagerBean.class)
				.setPagerBarLayout(EPagerBarLayout.bottom).setNoResultDesc(null)
				.setContainerId("idBbsPostViewTPage_pager").setHandlerClass(PostViewTbl.class);

		// Html编辑器
		addHtmlEditorBean(pp).setTextarea("idBbsPostViewTPage_editor").setResizeEnabled(true)
				.setToolbarCanCollapse(false);
		// 验证
		addComponentBean(pp, "BbsPostViewTPage_validation", ValidationBean.class)
				.setWarnType(EWarnType.insertAfter).setTriggerSelector("#idBbsTopic_editor .simple_btn")
				.addValidators(new Validator(EValidatorMethod.required, "#idBbsPostViewTPage_editor"));

		// tooltip
		addTooltipComponent(pp);
		// 类目选择
		addCategoryDict(pp);

		// 下载
		addAjaxRequest_Download(pp);

		// PageletTab
		addPageletTabAjaxRequest(pp);

		// submit
		addAjaxRequest(pp, "BbsPostViewTPage_submit").setConfirmMessage($m("Confirm.Post"))
				.setHandlerMethod("doSubmit").setRole(PermissionConst.ROLE_ALL_ACCOUNT)
				.setSelector("#idBbsTopic_editor");

		final boolean manager = (Boolean) getVariables(pp).get("manager");
		if (isAsk(getTopic(pp))) {
			// vote
			addAjaxRequest(pp, "BbsPostViewTPage_ajaxVote").setHandlerMethod("doAjaxVote")
					.setRole(PermissionConst.ROLE_ALL_ACCOUNT);

			addAjaxRequest(pp, "BbsPostViewTPage_votePage", VoteSubmitPage.class);
			addWindowBean(pp, "BbsPostViewTPage_voteWin").setContentRef("BbsPostViewTPage_votePage")
					.setTitle($m("BbsPostViewTPage.17")).setPopup(true).setWidth(380).setHeight(159)
					.setXdelta(-200).setResizable(false);

			// unvote
			addAjaxRequest(pp, "BbsPostViewTPage_unvotePage", UnVoteSubmitPage.class);
			addWindowBean(pp, "BbsPostViewTPage_unvoteWin")
					.setContentRef("BbsPostViewTPage_unvotePage").setTitle($m("BbsPostViewTPage.16"))
					.setPopup(true).setWidth(380).setHeight(115).setXdelta(-200).setResizable(false);

			// remark
			addAjaxRequest(pp, "BbsPostViewTPage_remark_list").setHandlerMethod("doRemarkList");

			if (manager) {
				addAjaxRequest(pp, "BbsPostViewTPage_bestAnswer").setHandlerMethod("doBestAnswer")
						.setConfirmMessage($m("BbsPostViewTPage.20"));

				addAjaxRequest(pp, "BbsPostViewTPage_remark_delete")
						.setConfirmMessage($m("Confirm.Delete")).setHandlerMethod("doRemarkDelete");
			}
		} else {
			// replyFrom
			addAjaxRequest(pp, "BbsPostViewTPage_replyFrom").setHandlerMethod("doReplyFrom");
		}

		// edit
		addAjaxRequest(pp, "BbsPostViewTPage_edit").setHandlerMethod("doEdit");
		if (manager) {
			// delete
			addDeleteAjaxRequest(pp, "BbsPostViewTPage_delete");
		}
	}

	protected boolean isAsk(final BbsTopic topic) {
		return topic.getBbsType() == EBbsType.ask;
	}

	protected HtmlEditorBean addHtmlEditorBean(final PageParameter pp) {
		return (HtmlEditorBean) addHtmlEditorBean(pp, "BbsPostViewTPage_editor")
				.setStartupFocus(false).setElementsPath(false).setHeight("250");
	}

	protected AjaxRequestBean addAjaxRequest_Download(final PageParameter pp) {
		return addAjaxRequest(pp, "BbsPostViewTPage_download").setHandlerMethod("doDownload");
	}

	protected void addTooltipComponent(final PageParameter pp) {
		addAjaxRequest(pp, "BbsPostViewTPage_TipPage", BbsAttachmentTooltipPage.class);
		final TooltipBean tooltip = addComponentBean(pp, "BbsPostViewTPage_Tip", TooltipBean.class);
		final StringBuilder js = new StringBuilder();
		js.append("var s = element.readAttribute('onclick');");
		js.append("if (s.startsWith('$Actions')) {");
		js.append("  s = s.substring(s.indexOf('(\\'') + 2, s.indexOf('\\')'));");
		js.append("  element.setAttribute('params', s);");
		js.append("}");
		tooltip.addTip(new TipBean(tooltip)
				.setSelector(".BbsPostViewTPage a[onclick*='BbsPostViewTPage_download']")
				.setContentRef("BbsPostViewTPage_TipPage").setCache(true)
				.setTitle($m("BbsPostViewTPage.9")).setStem(ETipPosition.leftTop)
				.setHook(new Hook(ETipPosition.rightTop, ETipPosition.topLeft))
				.setHideOn(new HideOn(ETipElement.closeButton, EElementEvent.click)).setWidth(380)
				.setJsTipCreate(js.toString()));
	}

	@Override
	public KVMap createVariables(final PageParameter pp) {
		final BbsTopic topic = getTopic(pp);
		return ((KVMap) super.createVariables(pp)).add("bbsContext", bbsContext).add("topic", topic)
				.add("manager", BbsUtils.isManager(pp,
						bbsContext.getCategoryService().getBean(topic.getCategoryId())));
	}

	public IForward doDownload(final ComponentParameter cp) {
		final Attachment attachment = bbsContext.getAttachmentService()
				.getBean(cp.getParameter("id"));
		final JavascriptForward js = new JavascriptForward();
		if (attachment != null) {
			final IAttachmentService<Attachment> service = bbsContext.getAttachmentService();
			try {
				final AttachmentFile af = service.createAttachmentFile(attachment);
				js.append(
						JS.loc(DownloadUtils.getDownloadHref(af, AttachmentDownloadHandler.class), true));
			} catch (final IOException e) {
				throw ContentException.of(e);
			}
		} else {
			js.append("alert('").append($m("BbsPostViewTPage.8")).append("');");
		}
		return js;
	}

	@Transaction(context = IBbsContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final BbsPost post = getPost(cp, "postId");
		bbsContext.getPostService().delete(post.getId());
		return new JavascriptForward(
				JS.loc(getUrlsFactory().getUrl(cp, BbsPostViewPage.class, getTopic(cp))));
	}

	public IForward doAjaxVote(final ComponentParameter cp) {
		final BbsPost post = getPost(cp, "postId");
		final JavascriptForward js = new JavascriptForward();
		if (post.getUserId().equals(cp.getLoginId())) {
			js.append("alert('").append($m("VoteSubmitPage.0")).append("');");
		} else {
			final BbsAskVote askVote = bbsContext.getAskVoteService().getAskVote(post,
					cp.getLogin().getId());
			if (askVote == null) {
				js.append("var act = $Actions['BbsPostViewTPage_voteWin'];");
			} else {
				js.append("var act = $Actions['BbsPostViewTPage_unvoteWin'];");
			}
			js.append("act.trigger = $Actions['BbsPostViewTPage_ajaxVote'].trigger;");
			js.append("act('postId=").append(post.getId()).append("');");
		}
		return js;
	}

	public IForward doReplyFrom(final ComponentParameter cp) {
		final BbsPost reply = getPost(cp, "replyId");
		final TextForward tf = new TextForward();
		tf.append("<div class='cc'>").append(reply.getCcomment()).append("</div>");
		tf.append("<div class='bb'>")
				.append(DateUtils.getRelativeDate(reply.getCreateDate(), DATE_NUMBERCONVERT))
				.append("</div>");
		return tf;
	}

	@Transaction(context = IBbsContext.class)
	public IForward doBestAnswer(final ComponentParameter cp) {
		final BbsPost post = getPost(cp, "postId");
		bbsContext.getPostService().doBestAnswer(post);
		return new JavascriptForward(
				JS.loc(getUrlsFactory().getUrl(cp, BbsPostViewPage.class, getTopic(cp))));
	}

	public IForward doRemarkList(final ComponentParameter cp) {
		return new TextForward(getRemarkList(cp, getPost(cp, "parentId")));
	}

	@Transaction(context = IBbsContext.class)
	public IForward doRemarkDelete(final ComponentParameter cp) {
		final BbsPost post = getPost(cp, "remarkId");
		bbsContext.getPostService().delete(post.getId());
		return null;
	}

	public IForward doEdit(final ComponentParameter cp) {
		final BbsPost remark = getPost(cp, "remarkId");
		if (remark != null) {
			return new JavascriptForward("_BBS.edit('").append("remarkId:").append(remark.getId())
					.append("', '").append(JavascriptUtils.escape(remark.getCcomment())).append("', '")
					.append($m("BbsPostViewTPage.11", cp.getUser(remark.getUserId()))).append("');");
		} else {
			final BbsPost post = getPost(cp, "postId");
			if (post != null) {
				return new JavascriptForward("_BBS.edit('").append("postId:").append(post.getId())
						.append("', '").append(JavascriptUtils.escape(post.getCcomment())).append("', '")
						.append($m("BbsPostViewTPage.11", cp.getUser(post.getUserId()))).append("');");
			} else {
				final BbsTopic topic = getTopic(cp);
				if (topic != null) {
					return new JavascriptForward(
							JS.loc(getUrlsFactory().getUrl(cp, BbsTopicFormPage.class, topic)));
				}
			}
		}
		return null;
	}

	@Transaction(context = IBbsContext.class)
	public IForward doSubmit(final ComponentParameter cp) {
		final Document doc = HtmlUtils
				.createHtmlDocument(cp.getParameter("idBbsPostViewTPage_editor"));
		if (doc.text().length() < 10) {
			throw ContentException.of($m("BbsPostViewTPage.25"));
		}

		final IBbsPostService service = bbsContext.getPostService();
		BbsPost remark = getPost(cp, "remarkId");
		BbsPost parent = null;
		if (remark != null) {
			parent = service.getBean(remark.getParentId());
		}
		if (parent == null) {
			parent = getPost(cp, "parentId");
		}

		if (parent != null) {
			final boolean insert = remark == null;
			if (insert) {
				remark = service.createBean();
				remark.setParentId(parent.getId());
				remark.setContentId(parent.getContentId());
				remark.setCreateDate(new Date());
				remark.setUserId(cp.getLoginId());
			}
			HtmlUtils.doDocument(doc, HtmlUtils.REPLACE_TAG_VISITOR("p", "div"));
			remark.setCcomment(doPostContent(cp, remark, doc));
			if (insert) {
				service.insert(remark);
			} else {
				service.update(remark);
			}
			return new JavascriptForward("_BBS.doRemark_callback('").append(parent.getId())
					.append("');");
		}

		final BbsTopic topic = getTopic(cp);
		BbsPost post = getPost(cp, "postId");
		final boolean insert = post == null;
		if (insert) {
			post = service.createBean();
			post.setContentId(topic.getId());
			final BbsPost reply = getPost(cp, "replyId");
			if (reply != null) {
				post.setReplyId(reply.getId());
			}
			post.setCreateDate(new Date());
			post.setUserId(cp.getLoginId());
		}
		post.setCcomment(doPostContent(cp, post, doc));
		if (insert) {
			service.insert(post);
		} else {
			service.update(post);
		}
		return new JavascriptForward(
				JS.loc(getUrlsFactory().getUrl(cp, BbsPostViewPage.class, topic)));
	}

	protected String doPostContent(final PageParameter pp, final Object bean, final Document doc) {
		return singleton(BbsTopicForm.class).doTopicContent(pp, bean, doc);
	}

	public boolean isTopicEditable(final PageParameter pp, final BbsTopic topic) {
		// 没有回帖 - 1小时内
		if (topic.getPosts() == 0 && topic.getUserId().equals(pp.getLoginId())) {
			final Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR_OF_DAY, -1);
			return topic.getCreateDate().after(cal.getTime());
		}
		return false;
	}

	public boolean isPostEditable(final PageParameter pp, final BbsPost post) {
		if (post.getUserId().equals(pp.getLoginId())) {
			final Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR_OF_DAY, -1);
			return post.getCreateDate().after(cal.getTime());
		}
		return false;
	}

	public String getTopicContent(final PageParameter pp, final BbsTopic topic) {
		// final Document doc = HtmlUtils.createHtmlDocument(content, false);
		// for (final Element img : doc.select("img")) {
		// }
		// return doc.html();
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='topb'>");
		String url = getUrlsFactory().getUrl(pp, BbsPostViewPage.class, topic);
		if (pp.getBoolParameter("order")) {
			sb.append(new LinkElement("#(BbsPostViewTPage.14)").setHref(url));
		} else {
			url = HttpUtils.addParameters(url, "order=true");
			sb.append(new LinkElement("#(BbsPostViewTPage.13)").setHref(url));
		}
		sb.append("</div>");
		if (isAsk(topic)) {
			sb.append("<div class='ask_icon'>").append("</div>");
		}
		final IModuleRef ref = ((IBbsWebContext) bbsContext).getFavoriteRef();
		if (ref != null) {
			sb.append(((BbsFavoriteRef) ref).toFavoriteElement(pp, topic.getId()));
		}
		sb.append(ContentUtils.getContent(pp, bbsContext.getAttachmentService(), topic));
		return sb.toString();
	}

	public String getPostContent(final PageParameter pp, final BbsPost post) {
		// ContentUtils.getContent(pp, bbsContext.getAttachmentService(),
		// post.getContent())
		final BbsTopic topic = getTopic(pp);
		final StringBuilder sb = new StringBuilder();

		final IBbsPostService pService = bbsContext.getPostService();
		final boolean ask = isAsk(topic);
		boolean gap = ask;
		BbsPost reply;
		if (!ask && (reply = pService.getBean(post.getReplyId())) != null) {
			sb.append("<div class='ReplyFrom' onclick=\"_BBS.replyFrom(this, 'replyId=")
					.append(reply.getId()).append("');\">");
			sb.append("#(BbsPostViewTPage.2)");
			final PermissionUser user = pp.getUser(reply.getUserId());
			sb.append(PhotoImage.icon16(pp.getPhotoUrl(user.getId()))).append(user);
			sb.append("</div>");
			gap = true;
		}
		if (ask) {
			if (post.isBestAnswer()) {
				sb.append("<div class='BestAnswer'>");
				sb.append($m("BbsPostViewTPage.19")).append("</div>");
			}
			sb.append(
					"<span class='ask_post' onclick=\"$Actions['BbsPostViewTPage_ajaxVote']('postId=")
					.append(post.getId()).append("');\">").append("<span class='votes'>")
					.append(post.getVotes()).append("</span>#(BbsPostViewTPage.16)").append("</span>");
		}
		if (gap) {
			sb.append("<div class='ReplyFrom_gap'></div>");
		}
		sb.append(post.getCcomment());
		if (ask) {
			sb.append("<div id='remark_").append(post.getId())
					.append("' class='BbsContent_Remark_List'>");
			sb.append(getRemarkList(pp, post));
			sb.append("</div>");
		}
		return sb.toString();
	}

	protected String getRemarkList(final PageParameter pp, final BbsPost post) {
		final StringBuilder sb = new StringBuilder();
		final boolean manager = (Boolean) getVariables(pp).get("manager");
		final IDataQuery<BbsPost> children = ((ITreeBeanServiceAware<BbsPost>) bbsContext
				.getPostService()).queryChildren(post);
		BbsPost _remark;
		int i = 0;
		if (children.getCount() > 0) {
			sb.append("<div class='rlist'>");
			while ((_remark = children.next()) != null) {
				sb.append("<div class='ritem'>");
				final PermissionUser user = pp.getUser(_remark.getUserId());
				sb.append(PhotoImage.icon16(pp.getPhotoUrl(user)).setTitle(user.toString()));
				sb.append(_remark.getCcomment());
				sb.append(" <div class='rbar'>");
				sb.append(Convert.toDateString(_remark.getCreateDate()));
				if (manager || isPostEditable(pp, _remark)) {
					sb.append(SpanElement.SEP()).append(new LinkElement("#(Edit)").setOnclick(
							"$Actions['BbsPostViewTPage_edit']('remarkId=" + _remark.getId() + "');"));
				}
				if (manager) {
					sb.append(SpanElement.SEP()).append(new LinkElement($m("Delete"))
							.setOnclick("_BBS.doRemark_delete(this, '" + _remark.getId() + "');"));
				}
				sb.append(" </div>");
				sb.append("</div>");
				// 一页显示的个数
				if (++i >= Convert.toInt(pp.getParameter("_count"), 8)) {
					break;
				}
			}
			sb.append(InputElement.hidden("_count").setText(Math.max(i, 8)));
			if (i < children.getCount()) {
				sb.append("<div class='mbar'><a onclick=\"_BBS.doRemark_list(this);\">")
						.append($m("BbsPostViewTPage.26")).append("</a></div>");
			}
			sb.append("</div>");
		}
		sb.append(InputElement.hidden("parentId").setText(post.getId()));
		sb.append(InputElement.hidden("topicId").setText(getTopic(pp).getId()));
		return sb.toString();
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='BbsPostViewTPage'>");
		sb.append(toTopicHTML(pp));
		sb.append("<div id='idBbsPostViewTPage_pager'></div>"); // toPagerHTML
		sb.append(toEditorHTML(pp));
		sb.append("</div>");
		return sb.toString();
	}

	protected String toTopicHTML(final PageParameter pp) {
		final KVMap variables = new KVMap().addAll(getVariables(pp));
		return MVEL2Template.replace(variables, AbstractBbsTPage.class,
				"BbsPostViewTPage_topic.html");
	}

	protected String toEditorHTML(final PageParameter pp) {
		return MVEL2Template.replace(getVariables(pp), AbstractBbsTPage.class,
				"BbsPostViewTPage_editor.html");
	}

	protected String toPagerHTML(final PageParameter pp, final List<?> data) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='BbsPostItems'>");
		for (final Object item : data) {
			final BbsPost post = (BbsPost) item;
			sb.append("<table id='post_").append(post.getId())
					.append("' class='fixed_table' cellpadding='0'><tr>");
			sb.append(" <td class='BbsUser'>").append(toUserHTML(pp, post)).append("</td>");
			sb.append(" <td valign='top'>");
			sb.append("  <div class='ReplyFrom_c' style='display: none;'></div>");
			sb.append("  <div class='BbsContent BbsPostContent'>").append(getPostContent(pp, post))
					.append("</div>");
			sb.append("  <div class='BbsContent_Bar'>").append(toPostBarHTML(pp, post));
			sb.append("  </div>");
			sb.append(" </td>");
			sb.append("</tr></table>");
		}
		sb.append("</div>");
		return sb.toString();
	}

	public String toTopicStatHTML(final PageParameter pp, final BbsTopic topic) {
		final StringBuilder sb = new StringBuilder();
		final StringBuilder v = new StringBuilder();
		final PermissionUser user = pp.getUser(topic.getUserId());
		v.append(new ImageElement(pp.getPhotoUrl(user.getId())).setClassName("photo_icon icon16")
				.setTitle(user.getText()));
		v.append(
				new SpanElement(DateUtils.getRelativeDate(topic.getCreateDate(), DATE_NUMBERCONVERT)));
		sb.append(_toTopicStatItem("#(BbsPostViewTPage.3)", v));
		Date lastPostDate;
		if ((lastPostDate = topic.getLastPostDate()) != null) {
			v.setLength(0);
			final PermissionUser lastUser = pp.getUser(topic.getLastUserId());
			v.append(new ImageElement(pp.getPhotoUrl(lastUser.getId()))
					.setClassName("photo_icon icon16").setTitle(lastUser.getText()));
			v.append(new SpanElement(DateUtils.getRelativeDate(lastPostDate, DATE_NUMBERCONVERT)));
			sb.append(_toTopicStatItem("#(BbsPostViewTPage.4)", v));
		}
		sb.append(_toTopicStatItem("#(BbsPostViewTPage.5)", SpanElement.num(topic.getPosts())));
		sb.append(_toTopicStatItem("#(BbsPostViewTPage.6)", SpanElement.num(topic.getViews())));
		return sb.toString();
	}

	private String _toTopicStatItem(final Object k, final Object v) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='s1'>");
		sb.append(" <div class='lbl'>").append(k).append("</div>");
		sb.append(" <div class='val'>").append(v).append("</div>");
		sb.append("</div>");
		return sb.toString();
	}

	public String toTopicBarHTML(final PageParameter pp, final BbsTopic topic) {
		final StringBuilder sb = new StringBuilder();
		sb.append(new SpanElement("#(BbsPostViewTPage.0)")
				.setClassName("span_btn_right btn_reply_from").setOnclick("_BBS.reply();"));
		final boolean manager = (Boolean) getVariables(pp).get("manager");
		if (manager || isTopicEditable(pp, topic)) {
			sb.append(new SpanElement("#(Edit)").setClassName("span_btn_right")
					.setOnclick("$Actions['BbsPostViewTPage_edit']('topicId=" + topic.getId() + "');"));
		}
		return sb.toString();
	}

	public String toPostBarHTML(final PageParameter pp, final BbsPost post) {
		final StringBuilder sb = new StringBuilder();
		final BbsTopic topic = getTopic(pp);
		if (getPost(pp, "replyId") != null || pp.getUser(pp.getParameter("userId")).getId() != null) {
			sb.append("<span class='span_btn_left' onclick=\"");
			sb.append(JS.loc(getUrlsFactory().getUrl(pp, BbsPostViewPage.class, topic))).append("\">");
			sb.append("#(BbsPostViewTPage.12)");
			sb.append("</span>");
		} else {
			final String url = getUrlsFactory().getUrl(pp, BbsPostViewPage.class, topic);
			sb.append("<span class='span_btn_left' onclick=\"")
					.append(JS.loc(HttpUtils.addParameters(url, "userId=" + post.getUserId())))
					.append("\">");
			sb.append("#(BbsPostViewTPage.15)");
			sb.append("</span>");
			int replies;
			if ((replies = post.getReplies()) > 0) {
				sb.append("<span class='span_btn_left' onclick=\"")
						.append(JS.loc(HttpUtils.addParameters(url, "replyId=" + post.getId())))
						.append("\">");
				sb.append(SpanElement.num(replies)).append("#(BbsPostViewTPage.0)");
				sb.append("</span>");
			}
		}

		final boolean manager = (Boolean) getVariables(pp).get("manager");
		final Object id = post.getId();
		if (manager) {
			sb.append(new SpanElement().setClassName("span_btn_right btn_delete")
					.setOnclick("$Actions['BbsPostViewTPage_delete']('postId=" + id + "');"));
		}

		if (isAsk(topic)) {
			if (manager) {
				sb.append(new SpanElement("#(BbsPostViewTPage.19)").setClassName("span_btn_right")
						.setOnclick("$Actions['BbsPostViewTPage_bestAnswer']('postId=" + id + "');"));
			}
			// 评论
			sb.append(new SpanElement("#(BbsPostViewTPage.21)").setClassName("span_btn_right")
					.setOnclick("_BBS.reply('parentId:" + id + "', '"
							+ $m("BbsPostViewTPage.22", pp.getUser(post.getUserId())) + "');"));
		} else {
			// 回复
			sb.append(new SpanElement("#(BbsPostViewTPage.0)").setClassName("span_btn_right")
					.setOnclick("_BBS.reply('replyId:" + id + "', '"
							+ $m("BbsPostViewTPage.23", pp.getUser(post.getUserId())) + "');"));
		}

		if (manager || isPostEditable(pp, post)) {
			sb.append(new SpanElement("#(Edit)").setClassName("span_btn_right")
					.setOnclick("$Actions['BbsPostViewTPage_edit']('postId=" + id + "');"));
		}
		return sb.toString();
	}

	public String toUserHTML(final PageParameter pp, final AbstractUserAwareBean bean) {
		final StringBuilder sb = new StringBuilder();
		final ID userId = bean != null ? bean.getUserId() : pp.getLoginId();
		if (userId == null) {
			return "";
		}
		final BbsUserStat stat = bbsContext.getUserStatService().getUserStat(userId);
		sb.append("<div>");
		sb.append("<img class='photo_icon icon48' src='").append(pp.getPhotoUrl(userId))
				.append("' />");
		sb.append("</div>");
		sb.append("<div class='stat' align='center'><table><tr>");
		sb.append(" <td class='fc'>");
		sb.append("  <div class='l1'>").append(stat.getTopics()).append("</div>");
		sb.append("  <div class='l2'>#(BbsPostViewTPage.10)</div>");
		sb.append(" </td>");
		sb.append(" <td>");
		sb.append("  <div class='l1'>").append(stat.getPosts()).append("</div>");
		sb.append("  <div class='l2'>#(BbsPostViewTPage.5)</div>");
		sb.append(" </td>");
		sb.append("</tr></table></div>");
		sb.append("<div class='name'>").append(createTopicsLink(pp, pp.getUser(userId)))
				.append("</div>");
		if (bean != null) {
			final String createDate = Convert.toDateString(bean.getCreateDate());
			sb.append("<div title='").append(createDate).append("'>");
			sb.append(createDate.substring(5));
			sb.append("</div>");
		}
		sb.append("<div></div>");
		return sb.toString();
	}

	@Override
	public NavigationButtons getNavigationBar(final PageParameter pp) {
		final BbsTopic topic = getTopic(pp);
		final BbsUrlsFactory urls = getUrlsFactory();
		final BbsCategory category = bbsContext.getCategoryService().getBean(topic.getCategoryId());
		return NavigationButtons.of(
				new LinkElement(bbsContext.getModule()).setHref(urls.getUrl(pp, BbsCategoryPage.class)),
				new SpanElement().addElements(
						new LinkElement(category.getText())
								.setHref(urls.getUrl(pp, BbsTopicListPage.class, category)),
						createCategoryDictMenu(pp)));
	}

	public IForward doPageletTab(final ComponentParameter cp) {
		final IBbsTopicService service = bbsContext.getTopicService();
		final BbsPageletCreator creator = ((IBbsWebContext) bbsContext).getPageletCreator();

		final ETimePeriod tp = Convert.toEnum(ETimePeriod.class, cp.getParameter("time"));
		final IBbsCategoryService cService = bbsContext.getCategoryService();
		final IDataQuery<?> dq = service.queryRecommendationBeans(
				cService.getBean(cp.getParameter("categoryId")), new TimePeriod(tp));

		return new TextForward(
				cp.wrapHTMLContextPath(creator.create(cp, dq).setDotIcon(EImageDot.numDot).toString()));
	}

	@Override
	protected Pagelets getPagelets(final PageParameter pp) {
		final IBbsTopicService service = bbsContext.getTopicService();
		final BbsTopic topic = getTopic(pp);
		final BbsPageletCreator creator = ((IBbsWebContext) bbsContext).getPageletCreator();

		final Pagelets lets = Pagelets.of();

		// 按相关度
		final ILuceneManager lService = service.getLuceneService();
		lets.add(new Pagelet(new CategoryItem($m("BbsPostViewTPage.7")),
				creator.create(pp,
						lService.query(StringUtils.join(lService.getQueryTokens(topic.getTopic()), " "),
								BbsTopic.class),
						new BbsListRowHandler() {
							@Override
							protected BbsTopic toBean(final Object o) {
								final BbsTopic topic2 = super.toBean(o);
								return topic2 != null && !topic2.equals(topic) ? topic2 : null;
							}
						}).setDotIcon(EImageDot.imgDot3)));

		// 按推荐
		final IBbsCategoryService cService = bbsContext.getCategoryService();
		final ID categoryId = topic.getCategoryId();
		final IDataQuery<?> dq = service.queryRecommendationBeans(cService.getBean(categoryId),
				TimePeriod.week);
		lets.add(new Pagelet(new CategoryItem($m("BbsPostViewTPage.18")),
				creator.create(pp, dq).setDotIcon(EImageDot.numDot))
						.setTabs(creator.createTimePeriodTabs("categoryId=" + categoryId)));

		// 历史记录
		lets.add(creator.getHistoryPagelet(pp));
		return lets;
	}

	public static BbsTopic getTopic(final PageParameter pp) {
		return getCacheBean(pp, bbsContext.getTopicService(), "topicId");
	}

	public static BbsPost getPost(final PageParameter pp, final String key) {
		return getCacheBean(pp, bbsContext.getPostService(), key);
	}

	public static BbsCategory getCategory(final PageParameter pp) {
		BbsCategory category = getCacheBean(pp, bbsContext.getCategoryService(), "categoryId");
		if (category == null) {
			final BbsTopic topic = BbsTopicForm.getTopic(pp);
			if (topic != null && (category = bbsContext.getCategoryService()
					.getBean(topic.getCategoryId())) != null) {
				pp.setRequestAttr("categoryId", category);
			}
		}
		return category;
	}

	public static class PostViewTbl extends AbstractPagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final BbsTopic topic = getTopic(cp);
			cp.addFormParameter("topicId", topic.getId());

			final IBbsPostService service = bbsContext.getPostService();

			final BbsPost post = getPost(cp, "replyId");
			if (post != null) {
				final Object replyId = post.getId();
				cp.addFormParameter("replyId", replyId);
				return service.queryByReplies(topic, replyId);
			}

			final PermissionUser user = cp.getUser(cp.getParameter("userId"));
			Object userId;
			if ((userId = user.getId()) != null) {
				cp.addFormParameter("userId", userId);
				return service.queryByUser(topic, userId);
			}

			if (cp.getBoolParameter("order")) {
				return service.queryWithASC(topic);
			}
			return service.query(topic);
		}

		@Override
		public String toPagerHTML(final ComponentParameter cp, final List<?> data) {
			return ((BbsPostViewTPage) get(cp)).toPagerHTML(cp, data);
		}
	}

	public static class VoteSubmitPage extends _VoteSubmitPage {
		@Override
		protected void onForward(final PageParameter pp) throws Exception {
			super.onForward(pp);

			addAjaxRequest(pp, "VoteSubmitPage_vote").setHandlerMethod("doVote")
					.setRole(PermissionConst.ROLE_ALL_ACCOUNT).setSelector(".VoteSubmitPage");

			addComponentBean(pp, "VoteSubmitPage_validation", ValidationBean.class)
					.setWarnType(EWarnType.insertAfter).setTriggerSelector("#idVoteSubmitPage_vote")
					.addValidators(new Validator(EValidatorMethod.required, "#vs_description"));
		}

		@Transaction(context = IBbsContext.class)
		public IForward doVote(final ComponentParameter cp) {
			final BbsPost post = getPost(cp, "postId");
			bbsContext.getAskVoteService().insertVote(post, cp.getLoginId(),
					cp.getParameter("vs_description"));
			return updateVotes(post).append("$Actions['BbsPostViewTPage_voteWin'].close();");
		}

		@Override
		protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
				final String currentVariable) throws IOException {
			final BbsPost post = getPost(pp, "postId");
			final StringBuilder sb = new StringBuilder();
			sb.append("<div class='VoteSubmitPage'>");
			sb.append(" <div class='tt'>").append(
					InputElement.textarea("vs_description").setRows(4).setText($m("VoteSubmitPage.1")))
					.append("</div>");
			sb.append(" <div class='bb'>");
			sb.append(LinkButton.saveBtn().setId("idVoteSubmitPage_vote")
					.setOnclick("$Actions['VoteSubmitPage_vote']('postId=" + post.getId() + "')"));
			sb.append(SpanElement.SPACE).append(LinkButton.closeBtn());
			sb.append(" </div>");
			sb.append("</div>");
			sb.append(JavascriptUtils.wrapScriptTag("$('vs_description').select();"));
			return sb.toString();
		}
	}

	public static class UnVoteSubmitPage extends _VoteSubmitPage {
		@Override
		protected void onForward(final PageParameter pp) throws Exception {
			super.onForward(pp);

			addAjaxRequest(pp, "UnVoteSubmitPage_unvote").setHandlerMethod("doUnVote")
					.setRole(PermissionConst.ROLE_ALL_ACCOUNT);
		}

		@Transaction(context = IBbsContext.class)
		public IForward doUnVote(final ComponentParameter cp) {
			final BbsPost post = getPost(cp, "postId");
			bbsContext.getAskVoteService().deleteVote(post, cp.getLoginId());
			return updateVotes(post).append("$Actions['BbsPostViewTPage_unvoteWin'].close();");
		}

		@Override
		protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
				final String currentVariable) throws IOException {
			final BbsPost post = getPost(pp, "postId");
			final StringBuilder sb = new StringBuilder();
			sb.append("<div class='UnVoteSubmitPage'>");
			sb.append(" <div class='tt'>#(UnVoteSubmitPage.0)</div>");
			sb.append(" <div class='bb'>");
			sb.append(new LinkButton($m("UnVoteSubmitPage.1"))
					.setOnclick("$Actions['UnVoteSubmitPage_unvote']('postId=" + post.getId() + "')"));
			sb.append(SpanElement.SPACE).append(LinkButton.closeBtn());
			sb.append(" </div>");
			sb.append("</div>");
			return sb.toString();
		}
	}

	static abstract class _VoteSubmitPage extends AbstractTemplatePage {
		protected JavascriptForward updateVotes(final BbsPost post) {
			final JavascriptForward js = new JavascriptForward();
			js.append("var votes = $('#post_").append(post.getId()).append(" .ask_post .votes');");
			js.append("if (votes) votes.innerHTML = '").append(post.getVotes()).append("';");
			return js;
		}
	}
}