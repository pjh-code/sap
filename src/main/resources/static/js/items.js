// 부품별 제조사 목록을 저장할 객체
let partsToMakers = {};
let allItems = [];

// 페이지 로드 시 API에서 아이템 목록을 가져와 테이블을 초기화하고 필터링에 사용할 데이터 준비
fetch('/api/items/list')
    .then(response => response.json())
    .then(items => {
        allItems = items;
        const tableBody = document.getElementById("item-table-body");

        // 부품별 제조사 목록 생성 및 테이블 초기 데이터 삽입
        items.forEach(item => {
            if (!partsToMakers[item.parts]) {
                partsToMakers[item.parts] = new Set();
            }
            partsToMakers[item.parts].add(item.maker);

            // 가격을 천 단위 구분 기호로 포맷팅 (화폐 기호 제거)
            const formattedPurchasePrice = new Intl.NumberFormat('ko-KR').format(item.purchasePrice);
            const formattedSellPrice = new Intl.NumberFormat('ko-KR').format(item.sellPrice);

            // 초기 테이블 표시
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${item.id}</td>
                <td>${item.name}</td>
                <td>${item.parts}</td>
                <td>${item.maker}</td>
                <td>${formattedPurchasePrice}</td>
                <td>${formattedSellPrice}</td>
                <td>${item.performance}</td>
            `;
            tableBody.appendChild(row);
        });

        // 부품 드롭다운 설정
        const partsSelect = document.getElementById("parts");
        Object.keys(partsToMakers).forEach(part => {
            const option = document.createElement("option");
            option.value = part;
            option.textContent = part.toUpperCase();
            partsSelect.appendChild(option);
        });
    })
    .catch(error => console.error('아이템 목록을 가져오는 중 오류:', error));

// 필터링된 결과로 테이블 업데이트
function updateTable(items) {
    const tableBody = document.getElementById("item-table-body");
    tableBody.innerHTML = ""; // 기존 테이블 초기화

    items.forEach(item => {
        const formattedPurchasePrice = new Intl.NumberFormat('ko-KR').format(item.purchasePrice);
        const formattedSellPrice = new Intl.NumberFormat('ko-KR').format(item.sellPrice);

        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${item.id}</td>
            <td>${item.name}</td>
            <td>${item.parts}</td>
            <td>${item.maker}</td>
            <td>${formattedPurchasePrice}</td>
            <td>${formattedSellPrice}</td>
            <td>${item.performance}</td>
        `;
        tableBody.appendChild(row);
    });
}

// 필터링 적용 함수
function applyFilter() {
    const name = document.getElementById("name").value.toLowerCase();
    const parts = document.getElementById("parts").value;
    const maker = document.getElementById("maker").value;
    const performance = document.getElementById("performance").value.toLowerCase();

    // 필터 조건에 맞는 아이템 필터링
    const filteredItems = allItems.filter(item => {
        return (!name || item.name.toLowerCase().includes(name)) &&
               (!parts || item.parts === parts) &&
               (!maker || item.maker === maker) &&
               (!performance || item.performance.toLowerCase().includes(performance));
    });

    updateTable(filteredItems); // 필터링된 결과로 테이블 업데이트
}

// 필터 초기화 함수
function resetFilters() {
    document.getElementById("name").value = "";
    document.getElementById("parts").value = "";
    document.getElementById("maker").value = "";
    document.getElementById("performance").value = "";
    updateTable(allItems); // 초기화 시 전체 목록 표시
}

// 부품 선택 시 해당 부품의 제조사 목록 업데이트
function updateMakers() {
    const parts = document.getElementById("parts").value;
    const makerSelect = document.getElementById("maker");

    // 제조사 목록 초기화
    makerSelect.innerHTML = '<option value="">선택하세요</option>';

    if (parts && partsToMakers[parts]) {
        partsToMakers[parts].forEach(maker => {
            const option = document.createElement("option");
            option.value = maker;
            option.textContent = maker;
            makerSelect.appendChild(option);
        });
    }
}
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

