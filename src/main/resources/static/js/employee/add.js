/* =======================================================================================
 * 檔案: add.js (測試資料預填版)
 * 說明: 【核心修改】
 * 1. 移除自動填充測試數據功能，避免在生產環境中意外創建測試數據。
 * 2. 如需在開發環境中使用，請手動調用 populateWithTestData()
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
     * 【修正函式】隨機填入測試資料
     * 生成更多樣化的隨機測試資料，確保使用英文郵件格式
     */
    function populateRandomTestData() {
        const timestamp = Date.now().toString().slice(-6);
        const randomNum = Math.floor(Math.random() * 9000) + 1000;
        
        // 隨機姓名陣列
        const firstNames = ['王', '李', '張', '劉', '陳', '楊', '趙', '黃', '周', '吳', '徐', '孫', '胡', '朱', '高', '林', '何', '郭', '馬', '羅'];
        const middleNames = ['大', '小', '志', '雅', '美', '智', '宏', '文', '建', '明', '淑', '婷', '怡', '佳', '承', '俊', '嘉', '宜', '雨', '青'];
        const lastNames = ['明', '華', '強', '芳', '偉', '娟', '勇', '軍', '敏', '靜', '麗', '剛', '洋', '艷', '勤', '燕', '平', '東', '紅', '梅'];
        
        const randomFirstName = firstNames[Math.floor(Math.random() * firstNames.length)];
        const randomMiddleName = middleNames[Math.floor(Math.random() * middleNames.length)];
        const randomLastName = lastNames[Math.floor(Math.random() * lastNames.length)];
        const fullName = randomFirstName + randomMiddleName + randomLastName;
        
        // 【重要修正】完全獨立的英文郵件帳號生成系統
        const emailPrefixes = [
            'john', 'jane', 'mike', 'sarah', 'david', 'mary', 'robert', 'linda', 
            'james', 'susan', 'michael', 'karen', 'william', 'nancy', 'richard', 
            'lisa', 'joseph', 'betty', 'thomas', 'helen', 'daniel', 'sandra',
            'matthew', 'donna', 'anthony', 'carol', 'mark', 'ruth', 'donald',
            'sharon', 'steven', 'michelle', 'paul', 'laura', 'andrew', 'emily',
            'chris', 'jessica', 'brian', 'amanda', 'kevin', 'melissa', 'gary',
            'deborah', 'kenneth', 'stephanie', 'joshua', 'dorothy', 'jeffrey'
        ];
        
        // 生成純英文的郵件帳號
        const randomEmailPrefix = emailPrefixes[Math.floor(Math.random() * emailPrefixes.length)];
        const emailAccount = `${randomEmailPrefix}${randomNum}`;
        const emailAddress = `${emailAccount}@eatfast.com`;
        
        // 【驗證】確保郵件地址不含中文字符
        const hasChineseChars = /[\u4e00-\u9fff]/.test(emailAddress);
        if (hasChineseChars) {
            console.error('❌ 郵件地址包含中文字符，重新生成');
            populateRandomTestData(); // 遞歸重新生成
            return;
        }
        
        // 隨機性別
        const genders = ['M', 'F'];
        const randomGender = genders[Math.floor(Math.random() * genders.length)];
        
        // 根據性別生成相應的身分證字號
        const genderDigit = randomGender === 'M' ? '1' : '2';
        
        // 台灣身分證字母對照
        const idLetters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];
        const randomLetter = idLetters[Math.floor(Math.random() * idLetters.length)];
        const randomIdSuffix = Math.floor(Math.random() * 100000000).toString().padStart(8, '0');
        
        // 隨機電話號碼
        const phoneTypes = ['0912', '0913', '0918', '0919', '0920', '0921', '0922', '0928', '0932', '0933', '0934', '0937', '0938', '0939'];
        const randomPhoneType = phoneTypes[Math.floor(Math.random() * phoneTypes.length)];
        const randomPhoneNum = Math.floor(Math.random() * 1000000).toString().padStart(6, '0');
        
        // 隨機密碼
        const passwords = [
            `Pass${randomNum}!`,
            `Test${randomNum}@`,
            `Demo${randomNum}#`,
            `User${randomNum}$`,
            `Work${randomNum}%`
        ];
        const randomPassword = passwords[Math.floor(Math.random() * passwords.length)];
        
        try {
            // 填入隨機資料
            console.log('開始填入隨機測試資料...');
            console.log('使用的英文郵件前綴:', randomEmailPrefix);
            console.log('生成的完整郵件地址:', emailAddress);
            
            // 基本資料
            const usernameInput = document.getElementById('username');
            const accountInput = document.getElementById('account');
            const emailInput = document.getElementById('email');
            const passwordInput = document.getElementById('password');
            const phoneInput = document.getElementById('phone');
            const nationalIdInput = document.getElementById('nationalId');
            
            if (usernameInput) usernameInput.value = fullName;
            if (accountInput) accountInput.value = `emp${timestamp}${randomNum}`;
            
            // 【確保修正】使用預先驗證的純英文郵件地址
            if (emailInput) {
                emailInput.value = emailAddress;
                console.log('✅ 已設定郵件地址:', emailAddress);
                
                // 二次驗證：確認設定的值確實不含中文
                setTimeout(() => {
                    const currentValue = emailInput.value;
                    if (/[\u4e00-\u9fff]/.test(currentValue)) {
                        console.error('❌ 警告：郵件欄位仍包含中文字符:', currentValue);
                        emailInput.value = emailAddress; // 強制重設
                    }
                }, 100);
            }
            
            if (passwordInput) passwordInput.value = randomPassword;
            if (phoneInput) phoneInput.value = `${randomPhoneType}-${randomPhoneNum.substring(0,3)}-${randomPhoneNum.substring(3,6)}`;
            if (nationalIdInput) nationalIdInput.value = `${randomLetter}${genderDigit}${randomIdSuffix}`;
            
            // 設定性別
            const genderSelect = document.getElementById('gender');
            if (genderSelect && genderSelect.options.length > 1) {
                genderSelect.value = randomGender;
                console.log(`設定性別: ${randomGender}`);
            }
            
            // 隨機選擇角色（如果是總部管理員）
            const roleSelect = document.getElementById('role');
            if (roleSelect && roleSelect.options.length > 1) {
                // 檢查是否為hidden input（門市經理情況）
                if (roleSelect.type !== 'hidden') {
                    const availableRoles = Array.from(roleSelect.options).slice(1); // 跳過第一個空選項
                    if (availableRoles.length > 0) {
                        const randomRole = availableRoles[Math.floor(Math.random() * availableRoles.length)];
                        roleSelect.value = randomRole.value;
                        console.log(`設定角色: ${randomRole.value}`);
                    }
                } else {
                    console.log('角色已由系統自動設定（門市經理模式）');
                }
            }
            
            // 隨機選擇門市（如果是總部管理員）
            const storeSelect = document.getElementById('storeId');
            if (storeSelect && storeSelect.options.length > 1) {
                // 檢查是否為hidden input（門市經理情況）
                if (storeSelect.type !== 'hidden') {
                    const availableStores = Array.from(storeSelect.options).slice(1); // 跳過第一個空選項
                    if (availableStores.length > 0) {
                        const randomStore = availableStores[Math.floor(Math.random() * availableStores.length)];
                        storeSelect.value = randomStore.value;
                        console.log(`設定門市: ${randomStore.text}`);
                    }
                } else {
                    console.log('門市已由系統自動設定（門市經理模式）');
                }
            }
            
            // 顯示成功訊息
            showMessage(`✅ 已隨機填入測試資料：${fullName}`, 'success');
            
            console.log('✅ 隨機填入測試資料完成:', {
                姓名: fullName,
                帳號: `emp${timestamp}${randomNum}`,
                郵件: `${randomEmailPrefix}${randomNum}@eatfast.com`, // 修正後的郵件格式
                性別: randomGender,
                密碼: randomPassword,
                電話: `${randomPhoneType}-${randomPhoneNum.substring(0,3)}-${randomPhoneNum.substring(3,6)}`,
                身分證: `${randomLetter}${genderDigit}${randomIdSuffix}`
            });
            
        } catch (error) {
            console.error('❌ 填入測試資料時發生錯誤:', error);
            showMessage('❌ 填入測試資料失敗，請檢查控制台錯誤訊息', 'error');
        }
    }

    /**
     * 【相容性函數】為表單填入預設的測試資料
     * 直接調用新的隨機填入函數，確保使用正確的郵件格式
     */
    function populateWithTestData() {
        console.log('調用相容性函數，重導向至隨機填入函數');
        populateRandomTestData();
    }
    
    // 在全局作用域中暴露函數，供開發者手動調用
    window.populateWithTestData = populateWithTestData;
    window.populateRandomTestData = populateRandomTestData;

    // 【新增】隨機填入測試資料按鈕事件
    const randomFillBtn = document.getElementById('random-fill-btn');
    if (randomFillBtn) {
        console.log('✅ 找到隨機填入按鈕，正在綁定事件...');
        randomFillBtn.addEventListener('click', function(e) {
            e.preventDefault(); // 防止表單提交
            console.log('🎯 隨機填入按鈕被點擊了！');
            populateRandomTestData();
        });
        console.log('✅ 隨機填入按鈕事件綁定完成');
    } else {
        console.error('❌ 找不到隨機填入按鈕 (ID: random-fill-btn)');
    }

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