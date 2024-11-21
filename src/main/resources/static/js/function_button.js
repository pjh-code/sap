// 페이지 스크롤 시 Top 버튼 표시 및 숨김 설정
window.onscroll = function() {
    const topBtn = document.getElementById("topBtn");
    if (document.body.scrollTop > 100 || document.documentElement.scrollTop > 100) {
        topBtn.style.display = "block";
    } else {
        topBtn.style.display = "none";
    }
};

// Top 버튼 클릭 시 맨 위로 이동
function scrollToTop() {
    window.scrollTo({ top: 0, behavior: "smooth" });
}
