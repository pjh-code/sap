let transactions = [];
let partsToMakers = {}; // 부품-제조사 목록을 동적으로 설정
let years = new Set(); // 고유한 연도 목록

// 페이지 로드 시 초기 설정
window.onload = function() {
    fetchTransactions();
};

// API로 거래 목록 가져오기
function fetchTransactions() {
    fetch('/api/transactions/list')
        .then(response => response.json())
        .then(data => {
            console.log('API 응답 데이터:', data); // 데이터 구조 확인
            transactions = Array.isArray(data) ? data : data.items;
            initializeYearAndMonth(transactions);
            initializePartsAndMakers(transactions);
            updateTable(transactions);
        })
        .catch(error => console.error('거래 목록을 가져오는 중 오류:', error));
}

// 연도 및 월 드롭다운 동적 생성
function initializeYearAndMonth(data) {
    const yearSelect = document.getElementById("year");

    data.forEach(transaction => {
        const transactionYear = new Date(transaction.transactionDate).getFullYear();
        years.add(transactionYear);
    });

    [...years].sort((a, b) => b - a).forEach(year => {
        const option = document.createElement("option");
        option.value = year;
        option.textContent = year;
        yearSelect.appendChild(option);
    });
}

// 선택한 연도에 따라 월을 유동적으로 업데이트
function updateMonths() {
    const monthSelect = document.getElementById("month");
    monthSelect.innerHTML = '<option value="">선택하세요</option>';

    const selectedYear = parseInt(document.getElementById("year").value);

    const monthsInYear = new Set(
        transactions
            .filter(transaction => new Date(transaction.transactionDate).getFullYear() === selectedYear)
            .map(transaction => new Date(transaction.transactionDate).getMonth() + 1)
    );

    [...monthsInYear].sort((a, b) => a - b).forEach(month => {
        const option = document.createElement("option");
        option.value = month;
        option.textContent = `${month}월`;
        monthSelect.appendChild(option);
    });
}

// 부품 및 제조사 목록 초기화 함수
function initializePartsAndMakers(data) {
    const partsSelect = document.getElementById("parts");

    data.forEach(transaction => {
        const part = transaction.item.parts;
        const maker = transaction.item.maker;

        if (!partsToMakers[part]) {
            partsToMakers[part] = new Set();
        }
        partsToMakers[part].add(maker);
    });

    Object.keys(partsToMakers).forEach(part => {
        const option = document.createElement("option");
        option.value = part;
        option.textContent = part.toUpperCase();
        partsSelect.appendChild(option);
    });
}

// 부품 선택 시 해당 부품의 제조사 목록을 동적으로 추가
function updateMakers() {
    const selectedPart = document.getElementById("parts").value;
    const makerSelect = document.getElementById("maker");

    makerSelect.innerHTML = '<option value="">선택하세요</option>';

    if (partsToMakers[selectedPart]) {
        partsToMakers[selectedPart].forEach(maker => {
            const option = document.createElement("option");
            option.value = maker;
            option.textContent = maker;
            makerSelect.appendChild(option);
        });
    }
}

// 필터 적용 함수
function applyFilter() {
    const year = document.getElementById("year").value;
    const month = document.getElementById("month").value;
    const parts = document.getElementById("parts").value;
    const maker = document.getElementById("maker").value;
    const profitPositive = document.getElementById("profitPositive").value;

    const filteredTransactions = transactions.filter(transaction => {
        const transactionYear = new Date(transaction.transactionDate).getFullYear().toString();
        const transactionMonth = (new Date(transaction.transactionDate).getMonth() + 1).toString();

        // 필터 조건 설정
        const isYearMatch = year ? transactionYear === year : true;
        const isMonthMatch = month ? transactionMonth === month : true;
        const isPartMatch = parts ? transaction.item.parts === parts : true;
        const isMakerMatch = maker ? transaction.item.maker === maker : true;

        // 수익 계산 (판매가 - 매입가)
        const profit = transaction.sellQuantity && transaction.sellPrice
            ? (transaction.sellPrice * transaction.sellQuantity) - (transaction.purchasePrice * transaction.purchaseQuantity || 0)
            : -((transaction.purchasePrice || 0) * (transaction.purchaseQuantity || 0));

        // 수익 여부 필터링 조건 설정
        const isProfitMatch = profitPositive
            ? (profitPositive === 'true' && profit >= 0) || (profitPositive === 'false' && profit < 0)
            : true;

        return isYearMatch && isMonthMatch && isPartMatch && isMakerMatch && isProfitMatch;
    });

    updateTable(filteredTransactions);
}


// 필터링된 결과로 테이블 업데이트
function updateTable(data) {
    const tableBody = document.getElementById("transaction-table-body");
    tableBody.innerHTML = "";

    data.forEach(transaction => {
        const row = document.createElement("tr");
        const date = new Date(transaction.transactionDate);
        
        // 수익 계산 (판매가 - 매입가)
        const profit = transaction.sellQuantity && transaction.sellPrice
            ? (transaction.sellPrice * transaction.sellQuantity) - (transaction.purchasePrice * transaction.purchaseQuantity || 0)
            : -((transaction.purchasePrice || 0) * (transaction.purchaseQuantity || 0));
        
        // 수익에 따른 스타일과 기호 적용
        const profitText = profit >= 0 ? `${profit.toLocaleString()}` : profit.toLocaleString();
        const profitColor = profit >= 0 ? "blue" : "red";

        row.innerHTML = `
            <td>${transaction.transactionId}</td>
			<td>${date.getFullYear()}</td>
            <td>${date.getMonth() + 1}</td>
            <td>${transaction.item.name}</td>
            <td>${transaction.item.parts}</td>
            <td>${transaction.item.maker}</td>
            <td>${(transaction.purchasePrice || 0).toLocaleString()}</td>
            <td>${(transaction.sellPrice || 0).toLocaleString()}</td>
            <td>${transaction.purchaseQuantity || ''}</td>
            <td>${transaction.sellQuantity || ''}</td>
            <td style="color: ${profitColor};">${profitText}</td>
        `;
        tableBody.appendChild(row);
    });
}



// 필터 초기화 함수
function resetFilters() {
    document.getElementById("year").value = "";
    document.getElementById("month").value = "";
    document.getElementById("parts").value = "";
    document.getElementById("maker").innerHTML = '<option value="">선택하세요</option>';
    document.getElementById("profitPositive").value = "";

    updateTable(transactions);
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
