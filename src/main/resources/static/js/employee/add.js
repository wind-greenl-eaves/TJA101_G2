/* =======================================================================================
 * 檔案: add.js (完全後端驗證版)
 * 說明: 【核心修改 - 2025/01/14】
 * 1. 完全移除所有前端驗證邏輯
 * 2. 只保留後端驗證錯誤顯示功能
 * 3. 保留必要的 UI 互動功能（密碼顯示切換、檔案上傳預覽、測試資料填入）
 * 4. 表單提交完全依賴後端 Spring Boot 驗證
 * ======================================================================================= */
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('add-form');
    const messageContainer = document.getElementById('message-container');
    const passwordInput = document.getElementById('password');
    const toggleIcon = document.getElementById('password-toggle');
    const successModal = document.getElementById('success-modal');
    const successModalMessage = document.getElementById('success-modal-message');
    const successModalConfirm = document.getElementById('success-modal-confirm');
    const photoInput = document.getElementById('photo');
    const previewImage = document.getElementById('preview-image');
    const uploadPlaceholder = document.getElementById('upload-placeholder');
    const dropZone = document.getElementById('drop-zone');

    /**
     * 隨機填入測試資料函數
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
        
        // 生成英文郵件帳號
        const emailPrefixes = [
            'john', 'jane', 'mike', 'sarah', 'david', 'mary', 'robert', 'linda', 
            'james', 'susan', 'michael', 'karen', 'william', 'nancy', 'richard', 
            'lisa', 'joseph', 'betty', 'thomas', 'helen', 'daniel', 'sandra',
            'matthew', 'donna', 'anthony', 'carol', 'mark', 'ruth', 'donald',
            'sharon', 'steven', 'michelle', 'paul', 'laura', 'andrew', 'emily'
        ];
        
        const randomEmailPrefix = emailPrefixes[Math.floor(Math.random() * emailPrefixes.length)];
        const emailAccount = `${randomEmailPrefix}${randomNum}`;
        const emailAddress = `${emailAccount}@eatfast.com`;
        
        // 隨機性別
        const genders = ['M', 'F'];
        const randomGender = genders[Math.floor(Math.random() * genders.length)];
        
        // 根據性別生成身分證字號
        const genderDigit = randomGender === 'M' ? '1' : '2';
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
            // 填入基本資料
            const usernameInput = document.getElementById('username');
            const accountInput = document.getElementById('account');
            const emailInput = document.getElementById('email');
            const passwordInput = document.getElementById('password');
            const phoneInput = document.getElementById('phone');
            const nationalIdInput = document.getElementById('nationalId');
            
            if (usernameInput) usernameInput.value = fullName;
            if (accountInput) accountInput.value = `emp${timestamp}${randomNum}`;
            if (emailInput) emailInput.value = emailAddress;
            if (passwordInput) passwordInput.value = randomPassword;
            if (phoneInput) phoneInput.value = `${randomPhoneType}-${randomPhoneNum.substring(0,3)}-${randomPhoneNum.substring(3,6)}`;
            if (nationalIdInput) nationalIdInput.value = `${randomLetter}${genderDigit}${randomIdSuffix}`;
            
            // 設定性別 - 修復性別值映射問題
            const genderSelect = document.getElementById('gender');
            if (genderSelect && genderSelect.tagName === 'SELECT') {
                try {
                    // 確保 options 存在且有內容
                    const options = genderSelect.options;
                    if (options && typeof options === 'object' && options.length > 0) {
                        // 將 'M', 'F' 映射到正確的枚舉值
                        const genderMapping = {
                            'M': 'MALE',
                            'F': 'FEMALE'
                        };
                        const targetGender = genderMapping[randomGender] || randomGender;
                        
                        // 安全地尋找對應的選項
                        let foundMatch = false;
                        for (let i = 0; i < options.length; i++) {
                            try {
                                const option = options[i];
                                if (option && option.value === targetGender) {
                                    genderSelect.selectedIndex = i;
                                    foundMatch = true;
                                    break;
                                }
                            } catch (innerError) {
                                console.warn('處理選項時發生錯誤:', innerError);
                                continue;
                            }
                        }
                        
                        if (!foundMatch) {
                            console.warn('未找到匹配的性別選項:', targetGender);
                        }
                    } else {
                        console.warn('性別選擇框的選項不可用或為空');
                    }
                } catch (error) {
                    console.warn('設定性別時發生錯誤:', error);
                }
            } else {
                console.warn('找不到性別選擇框或元素類型不正確');
            }
            
            // 隨機選擇角色（如果是總部管理員且有選擇框）
            const roleSelect = document.getElementById('role');
            if (roleSelect && roleSelect.tagName === 'SELECT') {
                try {
                    const roleOptions = roleSelect.options;
                    if (roleOptions && typeof roleOptions === 'object' && roleOptions.length > 0) {
                        const availableRoles = [];
                        for (let i = 0; i < roleOptions.length; i++) {
                            const option = roleOptions[i];
                            if (option && option.value && option.value !== '') {
                                availableRoles.push(option);
                            }
                        }
                        if (availableRoles.length > 0) {
                            const randomRole = availableRoles[Math.floor(Math.random() * availableRoles.length)];
                            roleSelect.value = randomRole.value;
                        }
                    }
                } catch (error) {
                    console.warn('設定角色時發生錯誤:', error);
                }
            }
            
            // 隨機選擇門市（如果是總部管理員且有選擇框）
            const storeSelect = document.getElementById('storeId');
            if (storeSelect && storeSelect.tagName === 'SELECT') {
                try {
                    const storeOptions = storeSelect.options;
                    if (storeOptions && typeof storeOptions === 'object' && storeOptions.length > 0) {
                        const availableStores = [];
                        for (let i = 0; i < storeOptions.length; i++) {
                            const option = storeOptions[i];
                            if (option && option.value && option.value !== '') {
                                availableStores.push(option);
                            }
                        }
                        if (availableStores.length > 0) {
                            const randomStore = availableStores[Math.floor(Math.random() * availableStores.length)];
                            storeSelect.value = randomStore.value;
                        }
                    }
                } catch (error) {
                    console.warn('設定門市時發生錯誤:', error);
                }
            }
            
            // 清除所有錯誤訊息
            clearAllErrors();
            showMessage(`✅ 已隨機填入測試資料：${fullName}`, 'success');
            
        } catch (error) {
            console.error('❌ 填入測試資料時發生錯誤:', error);
            showMessage('❌ 填入測試資料失敗: ' + error.message, 'error');
        }
    }

    // 隨機填入測試資料按鈕事件
    const randomFillBtn = document.getElementById('random-fill-btn');
    if (randomFillBtn) {
        randomFillBtn.addEventListener('click', function(e) {
            e.preventDefault();
            populateRandomTestData();
        });
    }

    // 密碼可見性切換
    if (toggleIcon && passwordInput) {
        toggleIcon.addEventListener('click', function () {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            this.classList.toggle('fa-eye');
            this.classList.toggle('fa-eye-slash');
        });
    }

    // 照片上傳預覽功能
    if (photoInput) {
        photoInput.addEventListener('change', function(event) {
            handleFilePreview(event.target.files[0]);
        });
    }

    // 拖拽上傳功能
    if (dropZone) {
        dropZone.addEventListener('dragover', function(e) {
            e.preventDefault();
            this.classList.add('border-[var(--primary-color)]');
        });

        dropZone.addEventListener('dragleave', function(e) {
            e.preventDefault();
            this.classList.remove('border-[var(--primary-color)]');
        });

        dropZone.addEventListener('drop', function(e) {
            e.preventDefault();
            this.classList.remove('border-[var(--primary-color)]');
            const files = e.dataTransfer.files;
            if (files.length > 0) {
                photoInput.files = files;
                handleFilePreview(files[0]);
            }
        });
    }

    // 檔案預覽處理
    function handleFilePreview(file) {
        if (file && file.type.startsWith('image/')) {
            const reader = new FileReader();
            reader.onload = function(e) {
                if (previewImage && uploadPlaceholder) {
                    previewImage.src = e.target.result;
                    previewImage.classList.remove('hidden');
                    uploadPlaceholder.classList.add('hidden');
                }
            };
            reader.readAsDataURL(file);
        }
    }

    // 表單提交事件監聽（完全依賴後端驗證）
    form.addEventListener('submit', async function(event) {
        event.preventDefault();
        clearAllErrors();
        messageContainer.classList.add('hidden');

        const formData = new FormData(form);
        
        try {
            const response = await fetch(form.getAttribute('action'), {
                method: 'POST',
                body: formData
            });

            let responseData;
            try {
                responseData = await response.json();
            } catch (e) {
                throw new Error('伺服器回應格式錯誤');
            }

            if (response.ok) {
                // 檢查回應類型：申請 or 直接創建員工
                if (responseData.type === 'application') {
                    // 門市經理提交申請的情況
                    showSuccessModal(
                        responseData.message + `\n申請編號：#${responseData.applicationId}`,
                        'application'
                    );
                } else {
                    // 總部管理員直接創建員工的情況
                    showSuccessModal(`員工 "${responseData.username}" 已成功新增！`, 'employee');
                }
            } else {
                if (response.status === 400 && responseData.errors) {
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

    // 顯示成功模態框
    function showSuccessModal(message, type) {
        if (successModalMessage && successModal && successModalConfirm) {
            successModalMessage.textContent = message;
            successModal.classList.remove('hidden');
            successModal.classList.add('flex');
            successModalConfirm.onclick = function() {
                // 根據類型決定重定向路徑
                const redirectTo = type === 'application' ? '/employee/select_page' : '/employee/listAll';
                window.location.href = redirectTo;
            };
        }
    }

    // 顯示一般訊息
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

    // 處理後端驗證錯誤（唯一的錯誤顯示機制）
    function handleValidationErrors(errors) {
        for (const field in errors) {
            const errorDiv = document.getElementById(`error-${field}`);
            if (errorDiv) {
                errorDiv.textContent = errors[field];
                
                // 為對應的輸入欄位添加錯誤樣式
                const inputElement = document.getElementById(field);
                if (inputElement) {
                    inputElement.classList.add('border-red-500');
                }
            }
        }
    }

    // 清除所有錯誤訊息和樣式
    function clearAllErrors() {
        // 清除錯誤訊息
        const errorDivs = document.querySelectorAll('.error-message');
        errorDivs.forEach(div => {
            div.textContent = '';
        });
        
        // 移除錯誤樣式
        const inputElements = document.querySelectorAll('.form-input');
        inputElements.forEach(input => {
            input.classList.remove('border-red-500');
        });
    }

    // 為全域使用暴露函數
    window.populateRandomTestData = populateRandomTestData;
    window.populateWithTestData = populateRandomTestData; // 相容性別名
});