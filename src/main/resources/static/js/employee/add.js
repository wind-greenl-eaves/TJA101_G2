/* =======================================================================================
 * 檔案: add.js (測試資料預填版)
 * 說明: 【核心修改】
 * 1. 新增 populateWithTestData() 函式，用於在頁面載入時，自動為表單填入一組隨機的、
 * 符合基本格式的測試資料。
 * 2. 在 DOMContentLoaded 事件的起始處呼叫此函式，以實現自動填寫。
 * 3. 此修改旨在加速開發與測試流程，讓開發者無需手動輸入即可快速提交表單。
 * ======================================================================================= */
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('add-form');
    const messageContainer = document.getElementById('message-container');
    const passwordInput = document.getElementById('password');
    const toggleIcon = document.getElementById('password-toggle');
    const successModal = document.getElementById('success-modal');
    const successModalMessage = document.getElementById('success-modal-message');
    const successModalConfirm = document.getElementById('success-modal-confirm');
    
    /**
     * 【新增函式】: 為表單填入預設的測試資料
     * 說明: 此函式會產生隨機的帳號、信箱等，並填入表單中，方便快速測試。
     */
    function populateWithTestData() {
        const timestamp = Date.now().toString().slice(-6); // 取得時間戳記後6位
        const randomSuffix = Math.floor(Math.random() * 9000) + 1000; // 產生 1000-9999 的隨機數
        
        // 生成一個符合格式的隨機身分證字號
        const randomIdSuffix = Math.floor(Math.random() * 100000000).toString().padStart(8, '0');
        const genderDigit = '1'; // 假設為男性
        const alphabet = "ABCDEFGHJKLMNPQRSTUVXYWZIO";
        const randomLetter = alphabet[Math.floor(Math.random() * alphabet.length)];

        // 填入資料到各個欄位，使用時間戳記來確保帳號唯一
        document.getElementById('username').value = `測試人員${randomSuffix}`;
        document.getElementById('account').value = `tester${timestamp}${randomSuffix}`; // 加入時間戳記確保唯一性
        document.getElementById('email').value = `tester${timestamp}${randomSuffix}@example.com`;
        document.getElementById('password').value = `Test${randomSuffix}!`; // 更強的密碼格式
        document.getElementById('phone').value = `0912${randomSuffix.toString().padStart(6, '0')}`; // 確保是合法的手機號碼
        document.getElementById('nationalId').value = `${randomLetter}${genderDigit}${randomIdSuffix}`;
        
        // 設定必填的下拉選單
        document.getElementById('role').value = 'STAFF';
        document.getElementById('gender').value = 'M';
        document.getElementById('storeId').value = '1';
    }
    
    // 【核心修改】: 在頁面載入完成後，立即呼叫函式以填入測試資料。
    populateWithTestData();


    // 表單提交事件監聽
    form.addEventListener('submit', async function(event) {
        event.preventDefault();
        clearAllErrors();
        messageContainer.classList.add('hidden');

        // 【修正】確保使用 FormData 正確提交，支援檔案上傳
        const formData = new FormData(form);
        
        // 檢查送出的資料
        console.log('送出的表單資料:');
        for (let [key, value] of formData.entries()) {
            console.log(`${key}: ${value}`);
        }
        
        try {
            // 【確認】使用 FormData 時不要設置 Content-Type header
            const response = await fetch(form.getAttribute('action'), {
                method: 'POST',
                body: formData // 不設置 Content-Type，讓瀏覽器自動設置 multipart/form-data
            });

            let responseData;
            try {
                responseData = await response.json();
                console.log('後端回應:', responseData); // 印出後端回應
            } catch (e) {
                throw new Error('伺服器回應格式錯誤');
            }

            if (response.ok) {
                showSuccessModal(`員工 "${responseData.username}" 已成功新增！`);
            } else {
                if (response.status === 400 && responseData.errors) {
                    console.log('驗證錯誤:', responseData.errors); // 印出驗證錯誤
                    handleValidationErrors(responseData.errors);
                    showMessage('資料驗證失敗，請檢查下方欄位。', 'error');
                } else {
                    showMessage(responseData.message || '發生未知錯誤，請稍後再試。', 'error');
                }
            }
        } catch (error) {
            console.error('Fetch Error:', error);
            showMessage('無法連接到伺服器，請檢查您的網路連線。', 'error');
        }
    });

    // 密碼可見性切換 (維持不變)
    if(toggleIcon) {
        toggleIcon.addEventListener('click', function () {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            this.classList.toggle('fa-eye');
            this.classList.toggle('fa-eye-slash');
        });
    }

    // 顯示成功後的 Modal 提示框 (維持不變)
    function showSuccessModal(message) {
        successModalMessage.textContent = message;
        successModal.classList.remove('hidden');
        successModalConfirm.onclick = function() {
            window.location.href = '/employee/listAll';
        };
    }

    // 在頁面上方顯示一般訊息 (維持不變)
    function showMessage(message, type) {
        messageContainer.textContent = message;
        messageContainer.className = `p-4 mx-8 mt-6 text-sm rounded-lg ${
            type === 'error' 
                ? 'bg-red-100 text-red-700 border border-red-400'
                : 'bg-green-100 text-green-700 border border-green-400'
        }`;
        messageContainer.classList.remove('hidden');
        messageContainer.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    /**
     * 【重要】: 此函式現在是顯示錯誤的唯一入口。
     * 它會解析後端傳來的錯誤物件，並將錯誤訊息顯示在對應欄位下方。
     * @param {Object} errors - 後端傳來的錯誤物件，格式為 { fieldName: errorMessage }
     */
    function handleValidationErrors(errors) {
        for (const field in errors) {
            const errorDiv = document.getElementById(`error-${field}`);
            if (errorDiv) {
                errorDiv.textContent = errors[field];
            }
        }
    }

    // 清除所有欄位下方的錯誤訊息 (維持不變)
    function clearAllErrors() {
        const errorDivs = document.querySelectorAll('.error-message');
        errorDivs.forEach(div => div.textContent = '');
    }

    // 新增即時驗證功能
    const formInputs = form.querySelectorAll('input, select');
    
    /**
     * 【修正】即時驗證函式 - 統一使用主控制器的驗證端點
     * 修正 API 路徑，使用 POST /api/v1/employees/validate-field
     */
    async function validateFieldRealtime(name, value) {
        if (!value || value.trim() === '') return;
        
        try {
            // 【修正】使用正確的 API 路徑和格式
            const response = await fetch('/api/v1/employees/validate-field', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    field: name,
                    value: value.trim()
                })
            });

            const data = await response.json();
            const errorDiv = document.getElementById(`error-${name}`);
            
            if (response.ok && data.isAvailable) {
                errorDiv.textContent = '';
                return true;
            } else {
                errorDiv.textContent = data.message || `此${name}已被使用`;
                return false;
            }
        } catch (error) {
            console.error('驗證請求失敗:', error);
            return true; // 驗證失敗時不阻止提交
        }
    }

    // 單一欄位驗證函數
    async function validateField(field) {
        const name = field.name;
        const value = field.value;
        
        // 跳過照片欄位的後端驗證
        if (name === 'photo') {
            return;
        }

        try {
            const response = await fetch(`/api/v1/employees/validate/${name}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ [name]: value })
            });

            const data = await response.json();
            
            const errorDiv = document.getElementById(`error-${name}`);
            if (errorDiv) {
                if (response.status === 400 && data.errors && data.errors[name]) {
                    errorDiv.textContent = data.errors[name];
                    field.classList.add('border-red-500');
                } else {
                    errorDiv.textContent = '';
                    field.classList.remove('border-red-500');
                }
            }
        } catch (error) {
            console.error('驗證過程發生錯誤:', error);
        }
    }

    // 【統一驗證邏輯】- 移除舊的驗證函數，統一使用新的驗證邏輯
    // 為每個輸入欄位添加即時驗證
    formInputs.forEach(input => {
        // 使用統一的驗證函數
        input.addEventListener('blur', () => {
            if (input.name !== 'photo') {
                validateFieldRealtime(input.name, input.value);
            }
        });
        
        // 針對select元素，在change時也進行驗證
        if (input.tagName.toLowerCase() === 'select') {
            input.addEventListener('change', () => {
                validateFieldRealtime(input.name, input.value);
            });
        }
        
        // 為文字輸入框添加輸入延遲驗證
        if (input.type === 'text' || input.type === 'email' || input.type === 'tel' || input.type === 'password') {
            let debounceTimer;
            input.addEventListener('input', () => {
                clearTimeout(debounceTimer);
                debounceTimer = setTimeout(() => {
                    validateFieldRealtime(input.name, input.value);
                }, 500);
            });
        }
    });

    const photoInput = document.getElementById('photo');
    const previewImage = document.getElementById('preview-image');
    const uploadPlaceholder = document.getElementById('upload-placeholder');
    const dropZone = document.getElementById('drop-zone');
    const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    // 處理照片選擇
    photoInput.addEventListener('change', handlePhotoSelect);

    // 拖放功能
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, preventDefault, false);
    });

    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, highlight, false);
    });

    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, unhighlight, false);
    });

    dropZone.addEventListener('drop', handleDrop, false);

    function preventDefault(e) {
        e.preventDefault();
        e.stopPropagation();
    }

    function highlight(e) {
        dropZone.classList.add('border-[var(--primary-color)]');
    }

    function unhighlight(e) {
        dropZone.classList.remove('border-[var(--primary-color)]');
    }

    function handleDrop(e) {
        const dt = e.dataTransfer;
        const file = dt.files[0];
        
        if (file) {
            photoInput.files = dt.files;
            handlePhotoSelect({ target: photoInput });
        }
    }

    function handlePhotoSelect(e) {
        const file = e.target.files[0];
        if (file) {
            // 檢查檔案大小
            if (file.size > MAX_FILE_SIZE) {
                showError('photo', '檔案大小不能超過 5MB');
                e.target.value = '';
                return;
            }

            // 檢查檔案類型
            if (!file.type.match('image.*')) {
                showError('photo', '請上傳圖片檔案 (PNG, JPG, JPEG)');
                e.target.value = '';
                return;
            }

            // 預覽圖片
            const reader = new FileReader();
            reader.onload = function(e) {
                previewImage.src = e.target.result;
                previewImage.classList.remove('hidden');
                uploadPlaceholder.classList.add('hidden');
            };
            reader.readAsDataURL(file);
            clearError('photo');
        }
    }

    // 錯誤處理函數
    function showError(fieldName, message) {
        const errorDiv = document.getElementById(`error-${fieldName}`);
        if (errorDiv) {
            errorDiv.textContent = message;
        }
    }

    function clearError(fieldName) {
        const errorDiv = document.getElementById(`error-${fieldName}`);
        if (errorDiv) {
            errorDiv.textContent = '';
        }
    }
});