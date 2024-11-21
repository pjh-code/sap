// 업로드 폼 전송 이벤트 처리
document.getElementById("uploadForm").onsubmit = async function(event) {
    event.preventDefault();
    const formData = new FormData(this);
    const messageElement = document.getElementById("message");

    try {
        // 파일 업로드 요청 전송
        const response = await fetch("/api/items/upload", {
            method: "POST",
            body: formData
        });

        // 응답 처리
        const result = await response.text();
        messageElement.innerText = response.ok ? "파일이 성공적으로 업로드되었습니다!" : "오류 발생: " + result;
        messageElement.style.color = response.ok ? "#4CAF50" : "#e74c3c"; // 성공 시 녹색, 오류 시 빨간색
    } catch (error) {
        messageElement.innerText = "파일 업로드 중 문제가 발생했습니다.";
        messageElement.style.color = "#e74c3c";
    }
};
// 사이드바 토글 함수
						function toggleSidebar() {
						    const sidebar = document.getElementById('sidebar');
						    const toggleIcon = document.getElementById('toggle-icon');
						    const toggleBtn = document.getElementById('toggle-btn');
						    sidebar.classList.toggle('open');

						    // 사이드바 상태에 따라 버튼 위치 조정
						    if (sidebar.classList.contains('open')) {
						        toggleIcon.innerHTML = '&lt;'; // 닫기 아이콘 '<'
						        toggleBtn.style.left = '182px'; // 사이드바가 열린 상태에서의 위치
						    } else {
						        toggleIcon.innerHTML = '&gt;'; // 열기 아이콘 '>'
						        toggleBtn.style.left = '42px'; // 닫힌 상태에서의 위치
						    }
						}

		// 탭 클릭 이벤트
					$(document).ready(function(){
					    $('ul.tabs li').click(function(){
					        var tab_id = $(this).attr('data-tab');

					        $('ul.tabs li').removeClass('current');
					        $('.tab-content').removeClass('current');

					        $(this).addClass('current');
					        $("#" + tab_id).addClass('current');
					    });
					});
