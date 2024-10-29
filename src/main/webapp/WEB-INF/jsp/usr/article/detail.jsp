<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="pageTitle" value="DETAIL"></c:set>
<%@ include file="../common/head.jspf"%>
<%@ include file="../common/toastUiEditorLib.jspf"%>
<hr />

<!-- <iframe src="http://localhost:8080/usr/article/doIncreaseHitCount?id=757" frameborder="0"></iframe> -->
<!-- 변수 -->
<!-- JSTL 변수를 JavaScript에서 사용할 때, 문자열로 변환 -->
<script>
    const params = {};
    params.id = parseInt('${param.id}');
    params.memberId = parseInt('${loginedMemberId}');
    var isAlreadyAddGoodRp = ${isAlreadyAddGoodRp ? 'true' : 'false'};
    var isAlreadyAddBadRp = ${isAlreadyAddBadRp ? 'true' : 'false'};

    console.log(params);
    console.log(params.id);
    console.log(params.memberId);
</script>

<!-- 조회수 -->
<script>
function ArticleDetail__doIncreaseHitCount() {
		// 게시글의 ID를 기반으로 localStorage에 키 설정
		const localStorageKey = 'article__' + params.id + '__alreadyOnView';

		// 이미 조회한 경우 바로 리턴하지 않고 현재 조회수 표시
        if (localStorage.getItem(localStorageKey)) {
            // 이미 조회한 경우 조회수 표시만 갱신하고 조회수 증가 요청은 하지 않음
            // $.get('../article/getHitCount', {
            //     id: params.id,
            //     ajaxMode: 'Y'
            // }, function(data) {
            //     $('.article-detail__hit-count').empty().html(data.data1);
            // }, 'json');
            return; // 이미 조회한 경우 더 이상 진행하지 않음
        }

		// 처음 조회하는 경우, 조회수 증가 요청을 보내고 localStorage에 기록
		$.get('../article/doIncreaseHitCountRd', {
            id: params.id,
            ajaxMode: 'Y'
        }, function(data) {
            if(data.resultCode.startsWith('S-')) {
                $('.article-detail__hit-count').empty().html(data.data1);
				// 조회 완료 후 해당 게시글 ID로 localStorage에 기록
				localStorage.setItem(localStorageKey, true);
            }
        }, 'json');
	}
	$(function() {
		// 페이지 로드 즉시 조회수 증가 처리
		ArticleDetail__doIncreaseHitCount();
	})
</script>

<!-- 좋아요 싫어요 -->
<script>
<!-- 좋아요 싫어요 버튼	-->
	function checkRP() {
		if (isAlreadyAddGoodRp == true) {
			$('#likeButton').toggleClass('btn-outline');
		} else if (isAlreadyAddBadRp == true) {
			$('#DislikeButton').toggleClass('btn-outline');
		} else {
			return;
		}
	}
function doGoodReaction(articleId) {
			if(isNaN(params.memberId)) {
            	if(confirm('로그인 페이지로 이동하겠습니까?')) {
                	var currentUri = encodeURIComponent(window.location.href);
                	window.location.href = '../member/login?afterLoginUri=' + currentUri;
            	}
            return;

		}

		$.ajax({
			url: '/usr/reactionPoint/doGoodReaction',
			type: 'POST',
			data: {relTypeCode: 'article', relId: articleId},
			dataType: 'json',
			success: function(data){
				console.log(data);
				console.log('data.data1Name : ' + data.data1Name);
				console.log('data.data1 : ' + data.data1);
				console.log('data.data2Name : ' + data.data2Name);
				console.log('data.data2 : ' + data.data2);
				if(data.resultCode.startsWith('S-')){
					var likeButton = $('#likeButton');
					var likeCount = $('#likeCount');
					// var likeCountC = $('.likeCount');
					var likeCountBtn = likeButton.find('.likeCount');
					var DislikeButton = $('#DislikeButton');
					var DislikeCount = $('#DislikeCount');
					// var DislikeCountC = $('.DislikeCount');
					var DislikeCountBtn = DislikeButton.find('.DislikeCount');

					// 모든 좋아요 카운트 업데이트
                    likeCount.text(data.data1);
                    likeCountBtn.text(data.data1);

                    // 모든 싫어요 카운트 업데이트
                    DislikeCount.text(data.data2);
                    DislikeCountBtn.text(data.data2);

					if(data.resultCode == 'S-1') {
                        likeButton.toggleClass('btn-outline');
                    } else if(data.resultCode == 'S-2') {
                        DislikeButton.removeClass('btn-outline');
                        likeButton.toggleClass('btn-outline');
                    } else {
                        likeButton.toggleClass('btn-outline');
                    }

				}else {
					alert(data.msg);
				}

			},
			error: function(jqXHR,textStatus,errorThrown) {
				alert('좋아요 오류 발생 : ' + textStatus);
			}

		});
	}
function doBadReaction(articleId) {
	// if(isNaN(params.memberId) == true) {
	if(isNaN(params.memberId)) {
		if(confirm('로그인 페이지로 이동하겠습니까?')) {
			var currentUri = encodeURIComponent(window.location.href);
			window.location.href = '../member/login?afterLoginUri=' + currentUri;
		}
		return;
	}
	 $.ajax({
			url: '/usr/reactionPoint/doBadReaction',
			type: 'POST',
			data: {relTypeCode: 'article', relId: articleId},
			dataType: 'json',
			success: function(data){
				console.log(data);
				console.log('data.data1Name : ' + data.data1Name);
				console.log('data.data1 : ' + data.data1);
				console.log('data.data2Name : ' + data.data2Name);
				console.log('data.data2 : ' + data.data2);
				if(data.resultCode.startsWith('S-')){
					var likeButton = $('#likeButton');
					var likeCount = $('#likeCount');
					// var likeCountC = $('.likeCount');
					var likeCountBtn = likeButton.find('.likeCount');
					var DislikeButton = $('#DislikeButton');
					var DislikeCount = $('#DislikeCount');
					// var DislikeCountC = $('.DislikeCount');
					var DislikeCountBtn = DislikeButton.find('.DislikeCount');

					// 모든 좋아요 카운트 업데이트
                    likeCount.text(data.data1);
                    likeCountBtn.text(data.data1);

                    // 모든 싫어요 카운트 업데이트
                    DislikeCount.text(data.data2);
                    DislikeCountBtn.text(data.data2);

					if(data.resultCode == 'S-1') {
                        DislikeButton.toggleClass('btn-outline');
                    } else if(data.resultCode == 'S-2') {
                        likeButton.removeClass('btn-outline');
                        DislikeButton.toggleClass('btn-outline');
                    } else {
                        DislikeButton.toggleClass('btn-outline');
                    }

				}else {
					alert(data.msg);
				}
			},
			error: function(jqXHR,textStatus,errorThrown) {
				alert('싫어요 오류 발생 : ' + textStatus);
			}

		});
	}
	$(function() {
		checkRP();
	});
</script>

<!-- 댓글 수정 -->
<script>
function toggleModifybtn(replyId) {

	console.log(replyId);

	$('#modify-btn-'+replyId).hide();
	$('#save-btn-'+replyId).show();
	$('#reply-'+replyId).hide();
	$('#modify-form-'+replyId).show();
}
function doModifyReply(replyId) {
	 console.log(replyId); // 디버깅을 위해 replyId를 콘솔에 출력

	    // form 요소를 정확하게 선택
	    var form = $('#modify-form-' + replyId);
	    console.log(form); // 디버깅을 위해 form을 콘솔에 출력
	    // form 내의 input 요소의 값을 가져옵니다
	    var text = form.find('input[name="reply-text-' + replyId + '"]').val();
	    console.log(text); // 디버깅을 위해 text를 콘솔에 출력
	    // form의 action 속성 값을 가져옵니다
	    var action = form.attr('action');
	    console.log(action); // 디버깅을 위해 action을 콘솔에 출력

    $.post({
    	url: '/usr/reply/doModify', // 수정된 URL
        type: 'POST', // GET에서 POST로 변경
        data: { id: replyId, body: text }, // 서버에 전송할 데이터
        success: function(data) {
        	$('#modify-form-'+replyId).hide();
        	$('#reply-'+replyId).text(data);
        	$('#reply-'+replyId).show();
        	$('#save-btn-'+replyId).hide();
        	$('#modify-btn-'+replyId).show();
        },
        error: function(xhr, status, error) {
            alert('댓글 수정에 실패했습니다: ' + error);
        }
	})
}
</script>

<section class="mt-24 text-xl px-4">
	<div class="mx-auto">
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
					<th style="text-align: center;">BoardId</th>
					<td style="text-align: center;">${article.boardId}</td>
				</tr>
				<tr>
					<th style="text-align: center;">Writer</th>
					<td style="text-align: center;">${article.extra__writer}</td>
				</tr>
				<tr>
					<th class="reaction" style="text-align: center;">Like</th>
					<td id="likeCount" style="text-align: center;">${article.goodReactionPoint}</td>
				</tr>
				<tr>
					<th style="text-align: center;">Dislike</th>
					<td id="DislikeCount" style="text-align: center;">${article.badReactionPoint}</td>
				</tr>
				<tr>
					<th style="text-align: center;">LIKE / Dislike ${usersReaction }</th>
					<td style="text-align: center;">
						<button id="likeButton" class="btn btn-outline btn-success" onclick="doGoodReaction(${param.id})">
							👍 LIKE
							<span class="likeCount">${article.goodReactionPoint}</span>
						</button>
						<button id="DislikeButton" class="btn btn-outline btn-error" onclick="doBadReaction(${param.id})">
							👎 DISLIKE
							<span class="DislikeCount">${article.badReactionPoint}</span>
						</button>
					</td>
				</tr>
				<tr>
					<th style="text-align: center;">Views</th>

					<td style="text-align: center;">
						<span class="article-detail__hit-count">${article.hitCount}</span>
					</td>
				</tr>
				<tr>
					<th style="text-align: center;">Title</th>
					<td style="text-align: center;">${article.title}</td>
				</tr>
				<tr>
					<th style="text-align: center;">Attached Image</th>
					<td style="text-align: center;">
						<div style="text-align: center;">
							<c:forEach var="file" items="${attachedFiles}">
                				<img class="mx-auto rounded-xl" src="${file.fileUri}" alt="첨부 이미지" style="max-width: 100%; margin-bottom: 10px;" />
            				</c:forEach>
<%--							<img class="mx-auto rounded-xl" src="${rq.getImgUri(article.id)}" onerror="${rq.profileFallbackImgOnErrorHtml}" alt="" />--%>
						</div>
							<%--이미지 경로 표시--%>
<%--						<div>${rq.getImgUri(article.id)}</div>--%>
					</td>
				</tr>
				<tr>
					<th style="text-align: center;">Body</th>
					<td>
						<div class="toast-ui-viewer">
							<script type="text/x-template">${article.body}</script>
						</div>
					</td>
				</tr>

			</tbody>
		</table>
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

<script>
	function ReplyWrite__submit(form) {
		console.log(form.body.value);

		form.body.value = form.body.value.trim();

		if(form.body.value.length < 3){
			alert('3글자 이상 입력해');
			form.body.focus();
			return;
		}

		form.submit();
	}
</script>

<!-- 댓글 -->
<section class="mt-24 text-xl px-4">
	<c:if test="${rq.isLogined() }">
		<form action="../reply/doWrite" method="POST" onsubmit="ReplyWrite__submit(this); return false;" )>
			<table class="table" border="1" cellspacing="0" cellpadding="5" style="width: 100%; border-collapse: collapse;">
				<input type="hidden" name="relTypeCode" value="article" />
				<input type="hidden" name="relId" value="${article.id }" />
				<tbody>

					<tr>
						<th>댓글 내용 입력</th>
						<td style="text-align: center;">
							<textarea class="input input-bordered input-sm w-full max-w-xs" name="body" autocomplete="off" type="text"
								placeholder="내용을 입력해"></textarea>
						</td>

					</tr>
					<tr>
						<th></th>
						<td style="text-align: center;">
							<button class="btn btn-outline">작성</button>
						</td>

					</tr>
				</tbody>
			</table>
		</form>
	</c:if>

	<c:if test="${!rq.isLogined() }">
		댓글 작성을 위해 <a class="btn btn-outline btn-primary" href="${rq.loginUri }">로그인</a>이 필요합니다
	</c:if>

	<!-- 	댓글 리스트 -->
	<div class="mx-auto">
		<table class="table" border="1" cellspacing="0" cellpadding="5" style="width: 100%; border-collapse: collapse;">
			<thead>
				<tr>
					<th style="text-align: center;">Registration Date</th>
					<th style="text-align: center;">Writer</th>
					<th style="text-align: center;">Body</th>
					<th style="text-align: center;">Like</th>
					<th style="text-align: center;">Dislike</th>
					<th style="text-align: center;">Edit</th>
					<th style="text-align: center;">Delete</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="reply" items="${replies}">
					<tr class="hover">
						<td style="text-align: center;">${reply.regDate.substring(0,10)}</td>
						<td style="text-align: center;">${reply.extra__writer}</td>
						<td style="text-align: center;">
							<span id="reply-${reply.id }">${reply.body}</span>
							<form method="POST" id="modify-form-${reply.id }" style="display: none;" action="/usr/reply/doModify">
								<input type="text" value="${reply.body }" name="reply-text-${reply.id }" />
							</form>
						</td>
						<td style="text-align: center;">${reply.goodReactionPoint}</td>
						<td style="text-align: center;">${reply.badReactionPoint}</td>
						<td style="text-align: center;">
							<c:if test="${reply.userCanModify }">
								<%-- <a class="btn btn-outline btn-xs btn-success" href="../reply/modify?id=${reply.id }">수정</a> --%>
								<button onclick="toggleModifybtn('${reply.id}');" id="modify-btn-${reply.id }" style="white-space: nowrap;"
									class="btn btn-outline btn-xs btn-success">수정</button>
								<button onclick="doModifyReply('${reply.id}');" style="white-space: nowrap; display: none;"
									id="save-btn-${reply.id }" class="btn btn-outline btn-xs">저장</button>
							</c:if>
						</td>
						<td style="text-align: center;">
							<c:if test="${reply.userCanDelete }">
								<a class="btn btn-outline btn-xs btn-error" onclick="if(confirm('정말 삭제?') == false) return false;"
									href="../reply/doDelete?id=${reply.id }">삭제</a>
							</c:if>
						</td>
					</tr>
				</c:forEach>

				<c:if test="${empty replies}">
					<tr>
						<td colspan="4" style="text-align: center;">댓글이 없습니다</td>
					</tr>
				</c:if>
			</tbody>
		</table>

	</div>
</section>

<%@ include file="../common/foot.jspf"%>