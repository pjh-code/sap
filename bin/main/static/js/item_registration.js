document.addEventListener("DOMContentLoaded", function() {
    fetch('/api/items/list')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            const partsToMakersMap = new Map();

            // 아이템 목록에서 부품명(parts)과 제조사(maker)를 맵에 추가
            data.forEach(item => {
                if (item.parts && item.maker) {
                    if (!partsToMakersMap.has(item.parts)) {
                        partsToMakersMap.set(item.parts, new Set());
                    }
                    partsToMakersMap.get(item.parts).add(item.maker);
                }
            });

            const partsSelect = document.getElementById("itemParts");
            const makerSelect = document.getElementById("itemMaker");

            // 부품명 옵션 추가
            partsToMakersMap.forEach((makers, part) => {
                const option = document.createElement("option");
                option.value = part;
                option.textContent = part;
                partsSelect.appendChild(option);
            });

            // 부품명 선택 시 해당 부품에 맞는 제조사 목록으로 필터링
            partsSelect.addEventListener("change", function() {
                updateMakerOptions(partsToMakersMap, partsSelect.value);
            });

            // 초기 제조사 목록 업데이트
            updateMakerOptions(partsToMakersMap, partsSelect.value);
        })
        .catch(error => {
            console.error("부품명과 제조사 목록을 가져오는 중 오류:", error);
        });
});

// 부품명에 따라 제조사 목록 업데이트 함수
function updateMakerOptions(partsToMakersMap, selectedPart) {
    const makerSelect = document.getElementById("itemMaker");
    makerSelect.innerHTML = '<option value="">제조사를 선택하세요</option><option value="custom">직접 입력</option>';

    if (partsToMakersMap.has(selectedPart)) {
        partsToMakersMap.get(selectedPart).forEach(maker => {
            const option = document.createElement("option");
            option.value = maker;
            option.textContent = maker;
            makerSelect.appendChild(option);
        });
    }
}

// "직접 입력" 옵션 선택 시 추가 입력 필드를 표시하는 함수
function toggleCustomInput(type) {
    if (type === "parts") {
        const partsSelect = document.getElementById("itemParts");
        const customParts = document.getElementById("customParts");
        customParts.style.display = partsSelect.value === "custom" ? "block" : "none";
    } else if (type === "maker") {
        const makerSelect = document.getElementById("itemMaker");
        const customMaker = document.getElementById("customMaker");
        customMaker.style.display = makerSelect.value === "custom" ? "block" : "none";
    }
}

function registerItem(event) {
    event.preventDefault(); // 폼 제출 기본 동작 막기

    const itemData = {
        id: document.getElementById("itemId").value,
        name: document.getElementById("itemName").value,
        category: document.getElementById("itemCategory") ? document.getElementById("itemCategory").value : null,
        description: document.getElementById("itemDescription") ? document.getElementById("itemDescription").value : null,
        price: document.getElementById("itemPrice") ? parseFloat(document.getElementById("itemPrice").value) : null,
        stockQuantity: document.getElementById("itemStockQuantity") ? parseInt(document.getElementById("itemStockQuantity").value) : null,
        maker: document.getElementById("itemMaker").value === "custom" ? document.getElementById("customMaker").value : document.getElementById("itemMaker").value,
        parts: document.getElementById("itemParts").value === "custom" ? document.getElementById("customParts").value : document.getElementById("itemParts").value,
        performance: document.getElementById("itemPerformance").value,
        purchasePrice: parseFloat(document.getElementById("itemPurchasePrice").value),
        sellPrice: parseFloat(document.getElementById("itemSellPrice").value)
    };

    fetch('/api/items/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(itemData)
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to register item');
        return response.json();
    })
    .then(data => {
        document.getElementById("message").textContent = "아이템이 성공적으로 등록되었습니다.";
        document.getElementById("itemForm").reset(); // 폼 초기화
    })
    .catch(error => {
        console.error("아이템 등록 중 오류:", error);
        document.getElementById("message").textContent = "아이템 등록 중 오류가 발생했습니다.";
    });
}





// 입력 내용을 지우는 함수
function clearForm() {
    document.getElementById("itemForm").reset();
    document.getElementById("message").textContent = ""; // 메시지 텍스트 초기화
}

