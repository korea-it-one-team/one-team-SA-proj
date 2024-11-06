<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="pageTitle" value="MODIFY"></c:set>
<%@ include file="../common/head.jspf"%>
<%@ include file="../common/toastUiEditorLib.jspf"%>
<hr />

<script type="text/javascript">
	function ArticleModify__submit(form) {
		form.title.value = form.title.value.trim();
		if (form.title.value.length == 0) {
			alert('제목을 입력해주세요');
			return;
		}
		const editor = $(form).find('.toast-ui-editor').data(
				'data-toast-editor');
		const markdown = editor.getMarkdown().trim();
		if (markdown.length == 0) {
			alert('내용 써');
			editor.focus();
			return;
		}
		form.body.value = markdown;
		form.submit();
	}
</script>


<section class="mt-24 text-xl px-4">
	<div class="mx-auto">
		<form onsubmit="ArticleModify__submit(this); return false;" action=" ../article/doModify" method="POST">
			<input type="hidden" name="id" value="${article.id}" />
			<input type="hidden" name="body">
			<table class="table" border="1" cellspacing="0" cellpadding="5" style="width: 100%; border-collapse: collapse;">
				<tbody>
					<tr>
						<th style="text-align: center;">ID</th>
						<td style="text-align: center;">${article.id}</td>
					</tr>
					<tr>
						<th style="text-align: center;">Registration Date</th>
						<td style="text-align: center;">${article.regDate.substring(0,10)}</td>
					</tr>
					<tr>
						<th style="text-align: center;">Modified date</th>
						<td style="text-align: center;">${article.updateDate}</td>
					</tr>
					<tr>
						<th style="text-align: center;">Writer</th>
						<td style="text-align: center;">${article.extra__writer}</td>
					</tr>
					<tr>
						<th style="text-align: center;">Title</th>
						<td style="text-align: center;">
							<input name="title" value="${article.title}" type="text" autocomplete="off" placeholder="새 제목을 입력해"
								class="input input-bordered input-primary w-full max-w-xs input-sm " />
						</td>
					</tr>
					<tr>
						<th style="text-align: center;">Body</th>
						<td style="text-align: center;">
							<%-- 							<input name="body" value="${article.body}" type="text" autocomplete="off" placeholder="새 내용을 입력해" --%>
							<!-- 								class="input input-bordered input-primary w-full max-w-xs input-sm " /> -->
							<div class="toast-ui-editor">
								<script type="text/x-template">${article.body }
      </script>
							</div>
						</td>
					</tr>
					<tr>
						<th></th>
						<td style="text-align: center;">
							<button class="btn btn-primary">수정</button>
						</td>
					</tr>

				</tbody>
			</table>
		</form>
		<div class="btns">
			<button class="btn" type="button" onclick="history.back()">뒤로가기</button>
			<c:if test="${article.userCanModify }">
				<a class="btn" href="../article/modify?id=${article.id }">수정</a>
			</c:if>
			<c:if test="${article.userCanDelete }">
				<a class="btn" href="../article/doDelete?id=${article.id }">삭제</a>
			</c:if>

		</div>
	</div>
</section>

<%@ include file="../common/foot.jspf"%>