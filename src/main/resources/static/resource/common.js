$('select[data-value]').each(function(index, el) {
	const $el = $(el);

	defaultValue = $el.attr('data-value').trim();

	if (defaultValue.length > 0) {
		$el.val(defaultValue);
	}
});

function myScrollToTop() {
	window.scrollTo({ top: 0, behavior: 'smooth'});
}

// function setHeaderMargin() {
// 	const header = document.querySelector('.header');
// 	const headerMargin = document.querySelector('.make-header-margin');
// 	if (header && headerMargin) {
// 		const headerHeight = header.offsetHeight;
// 		headerMargin.style.marginTop = `${headerHeight}px`;
// 	}
// }
//
// // 페이지 로드 시 실행
// window.onload = setHeaderMargin;
//
// // 창 크기 조정 시에도 실행 (반응형 대응)
// window.onresize = setHeaderMargin;