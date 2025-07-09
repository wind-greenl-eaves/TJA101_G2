/* =======================================================================================
 * 檔案: listAll.js (已修復完整版)
 * 路徑: resources/static/js/employee/listAll.js
 * 說明: 【本次修復核心】
 * 1. 提供完整的 JavaScript 程式碼，修復因先前版本省略函式內容而導致的查詢功能失效問題。
 * 2. 確保所有功能 (初始化、過濾、分頁、刪除、顯示詳情) 都完整實作。
 * 3. 維持先前對「角色權限」查詢的重構，即透過 API 動態獲取，而非前端硬編碼。
 * ======================================================================================= */
document.addEventListener('DOMContentLoaded', function() {
    const API_BASE_URL = "/api/v1/employees";
    // 【後端配對】: 指向 PermissionController 中新建的 API 端點
    const PERMISSION_API_URL = "/api/v1/permissions/roles";
    let originalEmployees = [];
    let filteredEmployees = [];
    let currentPage = 1;
    let rowsPerPage = parseInt(document.getElementById('rows-per-page').value) || 5;

    // DOM 元素獲取
    const tableBody = document.getElementById('employee-table-body');
    const paginationContainer = document.getElementById('pagination-container');
    const messageToast = document.getElementById('message-toast');
    const searchForm = document.getElementById('search-form');


    // ================================================================
    //                     批量上傳照片功能
    // ================================================================
    
    // 批量上傳相關元素
    const batchUploadBtn = document.getElementById('batch-upload-btn');
    const batchUploadModal = document.getElementById('batch-upload-modal');
    const closeBatchUpload = document.getElementById('close-batch-upload');
    const batchPhotoInput = document.getElementById('batch-photo-input');
    const batchDropZone = document.getElementById('batch-drop-zone');
    const batchPreviewContainer = document.getElementById('batch-preview-container');
    const batchPreviewGrid = document.getElementById('batch-preview-grid');
    const batchProgressContainer = document.getElementById('batch-progress-container');
    const batchProgressBar = document.getElementById('batch-progress-bar');
    const batchProgressText = document.getElementById('batch-progress-text');
    const batchCancelBtn = document.getElementById('batch-cancel-btn');
    const batchStartUpload = document.getElementById('batch-start-upload');
    
    let selectedFiles = [];
    const targetEmployeeIds = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
    
    // 批量上傳按鈕點擊事件
    if (batchUploadBtn) {
        batchUploadBtn.addEventListener('click', function() {
            // 前端權限檢查：確保只有總部管理員可以使用
            // 移除權限檢查，因為按鈕本身已經通過 Thymeleaf 進行權限控制
            // 如果按鈕可見，代表用戶已經有權限
            openBatchUploadModal();
        });
    }
    
    // 開啟批量上傳Modal
    function openBatchUploadModal() {
        batchUploadModal.classList.remove('hidden');
        resetBatchUploadForm();
    }
    
    // 關閉批量上傳Modal
    function closeBatchUploadModal() {
        batchUploadModal.classList.add('hidden');
        resetBatchUploadForm();
    }
    
    // 重置批量上傳表單
    function resetBatchUploadForm() {
        selectedFiles = [];
        batchPhotoInput.value = '';
        batchPreviewContainer.classList.add('hidden');
        batchProgressContainer.classList.add('hidden');
        batchPreviewGrid.innerHTML = '';
        batchProgressBar.style.width = '0%';
        batchProgressText.textContent = '0/12';
        batchStartUpload.disabled = true;
    }
    
    // Modal關閉事件
    if (closeBatchUpload) {
        closeBatchUpload.addEventListener('click', closeBatchUploadModal);
    }
    
    if (batchCancelBtn) {
        batchCancelBtn.addEventListener('click', closeBatchUploadModal);
    }
    
    // 拖放上傳功能
    if (batchDropZone) {
        batchDropZone.addEventListener('click', function() {
            batchPhotoInput.click();
        });
        
        // 拖放事件
        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            batchDropZone.addEventListener(eventName, preventDefaults, false);
        });
        
        ['dragenter', 'dragover'].forEach(eventName => {
            batchDropZone.addEventListener(eventName, highlight, false);
        });
        
        ['dragleave', 'drop'].forEach(eventName => {
            batchDropZone.addEventListener(eventName, unhighlight, false);
        });
        
        batchDropZone.addEventListener('drop', handleBatchDrop, false);
        
        function preventDefaults(e) {
            e.preventDefault();
            e.stopPropagation();
        }
        
        function highlight(e) {
            batchDropZone.classList.add('border-purple-400', 'bg-purple-50');
        }
        
        function unhighlight(e) {
            batchDropZone.classList.remove('border-purple-400', 'bg-purple-50');
        }
        
        function handleBatchDrop(e) {
            const dt = e.dataTransfer;
            const files = dt.files;
            handleBatchFileSelection(Array.from(files));
        }
    }
    
    // 檔案選擇事件
    if (batchPhotoInput) {
        batchPhotoInput.addEventListener('change', function(e) {
            handleBatchFileSelection(Array.from(e.target.files));
        });
    }
    
    // 處理批量檔案選擇
    function handleBatchFileSelection(files) {
        // 驗證檔案數量
        if (files.length !== 12) {
            showToast(`請選擇剛好 12 張照片，您選擇了 ${files.length} 張`, true);
            return;
        }
        
        // 驗證檔案格式和大小
        const validFiles = [];
        const maxSize = 5 * 1024 * 1024; // 5MB
        
        for (let file of files) {
            if (!['image/jpeg', 'image/png'].includes(file.type)) {
                showToast(`檔案 "${file.name}" 格式不支援，請選擇 JPG 或 PNG 格式`, true);
                return;
            }
            
            if (file.size > maxSize) {
                showToast(`檔案 "${file.name}" 大小超過 5MB 限制`, true);
                return;
            }
            
            validFiles.push(file);
        }
        
        selectedFiles = validFiles;
        generateBatchPreview();
        batchStartUpload.disabled = false;
    }
    
    // 生成批量預覽
    function generateBatchPreview() {
        batchPreviewGrid.innerHTML = '';
        batchPreviewContainer.classList.remove('hidden');
        
        selectedFiles.forEach((file, index) => {
            const previewItem = document.createElement('div');
            previewItem.className = 'relative bg-gray-100 border-2 border-gray-300 rounded-lg overflow-hidden cursor-move';
            previewItem.draggable = true;
            previewItem.dataset.index = index;
            
            const img = document.createElement('img');
            img.className = 'w-full h-20 object-cover';
            img.alt = `預覽 ${index + 1}`;
            
            const label = document.createElement('div');
            label.className = 'absolute bottom-0 left-0 right-0 bg-black bg-opacity-70 text-white text-xs text-center py-1';
            label.textContent = `員工 ${targetEmployeeIds[index]}`;
            
            const reader = new FileReader();
            reader.onload = function(e) {
                img.src = e.target.result;
            };
            reader.readAsDataURL(file);
            
            previewItem.appendChild(img);
            previewItem.appendChild(label);
            batchPreviewGrid.appendChild(previewItem);
            
            // 拖拽排序功能
            previewItem.addEventListener('dragstart', handleDragStart);
            previewItem.addEventListener('dragover', handleDragOver);
            previewItem.addEventListener('drop', handleDragDrop);
        });
    }
    
    let draggedIndex = null;
    
    function handleDragStart(e) {
        draggedIndex = parseInt(e.target.dataset.index);
    }
    
    function handleDragOver(e) {
        e.preventDefault();
    }
    
    function handleDragDrop(e) {
        e.preventDefault();
        const targetIndex = parseInt(e.target.closest('[data-index]').dataset.index);
        
        if (draggedIndex !== null && draggedIndex !== targetIndex) {
            // 交換檔案位置
            [selectedFiles[draggedIndex], selectedFiles[targetIndex]] = [selectedFiles[targetIndex], selectedFiles[draggedIndex]];
            generateBatchPreview();
        }
        
        draggedIndex = null;
    }
    
    // 開始批量上傳
    if (batchStartUpload) {
        batchStartUpload.addEventListener('click', async function() {
            await startBatchUpload();
        });
    }
    
    async function startBatchUpload() {
        batchProgressContainer.classList.remove('hidden');
        batchStartUpload.disabled = true;
        batchCancelBtn.disabled = true;
        
        let successCount = 0;
        let failureCount = 0;
        
        for (let i = 0; i < selectedFiles.length; i++) {
            const file = selectedFiles[i];
            const employeeId = targetEmployeeIds[i];
            
            try {
                await uploadSinglePhoto(employeeId, file);
                successCount++;
                showToast(`員工 ${employeeId} 照片上傳成功`);
            } catch (error) {
                failureCount++;
                showToast(`員工 ${employeeId} 照片上傳失敗: ${error.message}`, true);
            }
            
            // 更新進度
            const progress = ((i + 1) / selectedFiles.length) * 100;
            batchProgressBar.style.width = `${progress}%`;
            batchProgressText.textContent = `${i + 1}/12`;
            
            // 稍微延遲以避免服務器過載
            await new Promise(resolve => setTimeout(resolve, 500));
        }
        
        // 完成後的處理
        setTimeout(() => {
            if (successCount > 0) {
                showBatchUploadSuccess(successCount);
            }
            if (failureCount > 0) {
                showToast(`有 ${failureCount} 張照片上傳失敗，請檢查錯誤訊息`, true);
            }
            closeBatchUploadModal();
        }, 1000);
    }
    
    // 新增：批量上傳成功專用提示函數
    function showBatchUploadSuccess(count) {
        // 清除可能的舊提示
        messageToast.classList.add('hidden');
        
        // 設置專用的成功提示樣式和內容
        messageToast.textContent = `已成功上傳${count}張照片，請重新刷新頁面`;
        
        // 移除 transition-opacity 類別，避免與 JavaScript 動畫衝突
        messageToast.className = 'fixed top-5 right-5 bg-green-600 text-white py-4 px-8 rounded-lg shadow-xl z-50 opacity-100 text-lg font-semibold border-2 border-green-500';
        
        messageToast.classList.remove('hidden');
        
        // 使用更可靠的方式顯示提示 - 直接使用 setTimeout 不依賴 CSS 轉場
        if (window.batchUploadToastTimer) {
            clearTimeout(window.batchUploadToastTimer);
        }
        
        // 設置長時間顯示
        window.batchUploadToastTimer = setTimeout(() => {
            // 使用 JavaScript 控制淡出效果
            fadeOut(messageToast, 300, () => {
                messageToast.classList.add('hidden');
            });
        }, 10000); // 10秒後開始淡出
    }
    
    // 添加淡出動畫助手函數
    function fadeOut(element, duration, callback) {
        let opacity = 1;
        const interval = 10;
        const delta = interval / duration;
        
        const reduceOpacity = () => {
            opacity -= delta;
            element.style.opacity = opacity;
            
            if (opacity <= 0) {
                element.style.opacity = ''; // 清除內聯樣式
                if (callback) callback();
                return;
            }
            
            setTimeout(reduceOpacity, interval);
        };
        
        reduceOpacity();
    }

    // 上傳單張照片
    async function uploadSinglePhoto(employeeId, file) {
        const formData = new FormData();
        formData.append('photo', file);
        
        const response = await fetch(`${API_BASE_URL}/${employeeId}/photo`, {
            method: 'PUT',
            body: formData
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '上傳失敗');
        }
        
        return await response.json();
    }

    // ================================================================
    //                     輔助函式 (Helper Functions)
    // ================================================================

    /** 顯示短暫的提示訊息 (Toast) */
    function showToast(message, isError = false) {
        messageToast.textContent = message;
        messageToast.className = `fixed top-5 right-5 text-white py-3 px-6 rounded-lg shadow-lg transition-opacity duration-300 ${isError ? 'bg-red-500' : 'bg-green-500'}`;
        messageToast.classList.remove('hidden');
        setTimeout(() => {
            messageToast.classList.add('hidden');
        }, 3000);
    }

    /** 根據權限描述找到對應的 Font Awesome 圖示 */
    function findPermissionIcon(description) {
        const permissionIcons = {
            '管理': 'fa-solid fa-users-cog',
            '查看': 'fa-solid fa-eye',
            '發布': 'fa-solid fa-bullhorn',
            '更新': 'fa-solid fa-sync-alt',
            '處理': 'fa-solid fa-headset',
            '登入': 'fa-solid fa-right-to-bracket',
            '修改': 'fa-solid fa-key'
        };
        const defaultPermissionIcon = 'fa-solid fa-shield-halved';
        for (const key in permissionIcons) {
            if (description.startsWith(key)) {
                return permissionIcons[key];
            }
        }
        return defaultPermissionIcon;
    }

    // ================================================================
    //                 核心渲染與資料處理 (Core Rendering & Data)
    // ================================================================

    /** 渲染員工資料表格 */
    function renderTable() {
        tableBody.innerHTML = '';
        const displayEmployees = filteredEmployees;

        if (displayEmployees.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="10" class="p-8 text-center text-gray-400">沒有符合搜尋條件的員工資料。</td></tr>';
            renderPagination();
            return;
        }

        const paginatedEmployees = displayEmployees.slice(
            (currentPage - 1) * rowsPerPage,
            currentPage * rowsPerPage
        );

        const roleMap = {
            'STAFF': '一般員工',
            'MANAGER': '門市經理',
            'HEADQUARTERS_ADMIN': '總部管理員'
        };

        paginatedEmployees.forEach(emp => {
            const row = document.createElement('tr');
            row.className = 'border-b border-[var(--border-color)] last:border-b-0 hover:bg-gray-50 text-sm';
            const statusClass = emp.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800';
            const statusText = emp.status === 'ACTIVE' ? '啟用' : '停用';
            const editUrl = `/employee/edit/${emp.employeeId}`;
            const employeePhotoUrl = emp.photoUrl ? emp.photoUrl : '/images/no_image.png';

            // 創建一個安全的 JSON 字符串
            const empDataStr = JSON.stringify(emp).replace(/'/g, "\\'").replace(/"/g, '&quot;');

            row.innerHTML = `
                <td class="p-3 align-middle">${emp.employeeId}</td>
                <td class="p-3 align-middle">${emp.storeName || emp.storeId}</td>
                <td class="p-3 align-middle">
                    <div class="relative w-12 h-12 mx-auto">
                        <img src="${employeePhotoUrl}" 
                             alt="${emp.username}" 
                             class="w-full h-full rounded-full object-cover border-2 border-[var(--border-color)] cursor-pointer hover:border-[var(--primary-color)] transition-colors"
                             data-employee='${empDataStr}'
                             onerror="this.onerror=null; this.src='/images/no_image.png';">
                    </div>
                </td>
                <td class="p-3 font-medium align-middle">${emp.username}</td>
                <td class="p-3 align-middle">${emp.account}</td>
                <td class="p-3 align-middle">
                    <span class="role-badge clickable-role" data-role="${emp.role}" data-role-name="${roleMap[emp.role] || emp.role}">
                        <i class="fas fa-user-tag mr-1"></i>${roleMap[emp.role] || emp.role}
                    </span>
                </td>
                <td class="p-3 align-middle"><span class="px-2 py-1 text-xs font-semibold rounded-full ${statusClass}">${statusText}</span></td>
                <td class="p-3 align-middle">${emp.email}</td>
                <td class="p-3 align-middle"><a href="${editUrl}" class="font-semibold text-[var(--primary-color)] hover:text-[var(--primary-hover)]">修改</a></td>
                <td class="p-3 align-middle"><button class="delete-btn font-semibold text-red-500 hover:text-red-700" data-id="${emp.employeeId}" data-name="${emp.username}">刪除</button></td>
            `;
            
            // 為照片添加點擊事件監聽器
            const img = row.querySelector('img');
            img.addEventListener('click', function() {
                const employeeData = JSON.parse(this.dataset.employee.replace(/&quot;/g, '"'));
                showEmployeeDetail(employeeData);
            });

            tableBody.appendChild(row);
        });
    }

    /** 渲染分頁控制項 */
    function renderPagination() {
        paginationContainer.innerHTML = '';
        const totalItems = filteredEmployees.length;
        const totalPages = Math.ceil(totalItems / rowsPerPage);

        const startItem = totalItems === 0 ? 0 : (currentPage - 1) * rowsPerPage + 1;
        const endItem = Math.min(currentPage * rowsPerPage, totalItems);
        document.getElementById('display-range').textContent = `${startItem} - ${endItem}`;
        document.getElementById('total-count').textContent = totalItems;

        if (totalPages <= 1) return;

        const createPageButton = (text, page, isActive = false, isDisabled = false) => {
            const button = document.createElement('button');
            button.type = 'button';
            button.innerHTML = text;
            button.className = `px-3 py-1 rounded-md text-sm font-medium transition-colors ${isActive ? 'bg-[var(--primary-color)] text-white' : 'bg-white hover:bg-gray-100'} ${isDisabled ? 'opacity-50 cursor-not-allowed' : ''}`.trim();
            if (!isDisabled) {
                button.onclick = () => {
                    currentPage = page;
                    renderTable();
                    renderPagination();
                };
            }
            return button;
        };

        // 加入第一頁按鈕
        paginationContainer.appendChild(createPageButton('<i class="fas fa-angle-double-left"></i>', 1, false, currentPage === 1));
        // 上一頁按鈕
        paginationContainer.appendChild(createPageButton('<i class="fas fa-angle-left"></i>', currentPage - 1, false, currentPage === 1));

        // 計算要顯示的頁碼範圍
        let startPage = Math.max(1, currentPage - 2);
        let endPage = Math.min(totalPages, startPage + 4);
        startPage = Math.max(1, endPage - 4);

        // 顯示頁碼
        for (let i = startPage; i <= endPage; i++) {
            paginationContainer.appendChild(createPageButton(i.toString(), i, i === currentPage));
        }

        // 下一頁按鈕
        paginationContainer.appendChild(createPageButton('<i class="fas fa-angle-right"></i>', currentPage + 1, false, currentPage === totalPages));
        // 加入最末頁按鈕
        paginationContainer.appendChild(createPageButton('<i class="fas fa-angle-double-right"></i>', totalPages, false, currentPage === totalPages));
    }

    /** 根據左側表單條件過濾員工 */
    function filterEmployees() {
        const searchId = document.getElementById('search-id').value.trim();
        const searchName = document.getElementById('search-name').value.trim().toLowerCase();
        const searchStore = document.getElementById('search-store').value;
        const searchRole = document.getElementById('search-role').value;
        const searchStatus = document.getElementById('search-status').value;

        filteredEmployees = originalEmployees.filter(emp =>
            (!searchId || emp.employeeId.toString() === searchId) &&
            (!searchName || emp.username.toLowerCase().includes(searchName)) &&
            (!searchStore || emp.storeId.toString() === searchStore) &&
            (!searchRole || emp.role === searchRole) &&
            (!searchStatus || emp.status === searchStatus)
        );
        
        currentPage = 1;
        renderTable();
        renderPagination();
    }
    
    // ================================================================
    //                      API 互動 (API Interactions)
    // ================================================================

    /** 頁面初始化，獲取所有員工資料 */
    async function initializePage() {
        try {
            const response = await fetch(API_BASE_URL);
            if (!response.ok) throw new Error('無法從伺服器載入員工資料。');
            originalEmployees = await response.json();
            filteredEmployees = [...originalEmployees];
            renderTable();
            renderPagination();
        } catch (error) {
            tableBody.innerHTML = `<tr><td colspan="10" class="p-8 text-center text-red-500 font-semibold">${error.message}</td></tr>`;
        }
    }

    /** 處理刪除員工的請求 */
    async function handleDelete(employeeId, employeeName) {
        // 顯示視覺化的刪除確認Modal而不是簡單的confirm
        showDeleteConfirmModal(employeeId, employeeName);
    }

    /** 顯示視覺化的刪除確認Modal */
    function showDeleteConfirmModal(employeeId, employeeName) {
        // 獲取要刪除的員工資料
        const employee = originalEmployees.find(emp => emp.employeeId === employeeId);
        if (!employee) return;

        const modal = document.getElementById('delete-employee-modal');
        const confirmInput = document.getElementById('delete-confirmation-input');
        const confirmBtn = document.getElementById('confirm-delete-btn');
        
        // 設置員工資訊
        document.getElementById('delete-employee-name').textContent = employee.username;
        document.getElementById('delete-employee-id').textContent = `ID: ${employee.employeeId}`;
        document.getElementById('delete-employee-email').textContent = employee.email;
        
        // 設置角色顯示
        const roleMap = {
            'STAFF': '一般員工',
            'MANAGER': '門市經理', 
            'HEADQUARTERS_ADMIN': '總部管理員'
        };
        document.getElementById('delete-employee-role').textContent = roleMap[employee.role] || employee.role;
        
        // 設置員工照片
        const photo = document.getElementById('delete-employee-photo');
        photo.src = employee.photoUrl || '/images/no_image.png';
        
        // 清空確認輸入框
        confirmInput.value = '';
        confirmBtn.disabled = true;
        
        // 顯示Modal
        modal.classList.add('show');
        
        // 監聽確認輸入
        const inputHandler = function() {
            confirmBtn.disabled = confirmInput.value.trim().toUpperCase() !== 'DELETE';
        };
        
        // 移除舊的事件監聽器（如果存在）
        confirmInput.removeEventListener('input', inputHandler);
        confirmInput.addEventListener('input', inputHandler);
        
        // 設置確認刪除按鈕的點擊事件
        const confirmHandler = async function() {
            if (confirmInput.value.trim().toUpperCase() === 'DELETE') {
                modal.classList.remove('show');
                await performDelete(employeeId, employeeName);
            }
        };
        
        // 移除舊的事件監聽器（如果存在）
        confirmBtn.removeEventListener('click', confirmHandler);
        confirmBtn.addEventListener('click', confirmHandler);
    }

    /** 執行實際的刪除操作 */
    async function performDelete(employeeId, employeeName) {
        try {
            const response = await fetch(`${API_BASE_URL}/${employeeId}`, { method: 'DELETE' });
            if (response.ok) {
                showToast(`員工 "${employeeName}" 已成功刪除。`);
                originalEmployees = originalEmployees.filter(emp => emp.employeeId !== employeeId);
                filterEmployees();
            } else {
                const errorData = await response.json().catch(() => ({ message: `刪除失敗，狀態碼: ${response.status}` }));
                throw new Error(errorData.message);
            }
        } catch (error) {
            showToast(error.message, true);
        }
    }

    /** 顯示角色權限 Modal 的函式 (已重構為 API 呼叫) */
    async function showRolePermissions(role, roleName) {
        const modal = document.getElementById('permission-modal');
        const title = document.getElementById('permission-modal-title');
        const list = document.getElementById('permission-list');

        title.textContent = `${roleName} 的權限列表`;
        list.innerHTML = '<li class="permission-item"><i class="fas fa-spinner fa-spin mr-2"></i><span>正在從伺服器載入權限...</span></li>';
        modal.classList.add('show');

        try {
            const response = await fetch(`${PERMISSION_API_URL}/${role}`);
            if (!response.ok) throw new Error(`無法載入權限資料 (狀態碼: ${response.status})`);
            const permissions = await response.json();
            
            list.innerHTML = '';

            if (permissions.length === 0) {
                list.innerHTML = '<li class="text-gray-500 p-2">此角色目前沒有定義任何特定權限。</li>';
            } else {
                const sortedPermissions = [...permissions].sort((a, b) => a.permissionId - b.permissionId);
                sortedPermissions.forEach(perm => {
                    const iconClass = findPermissionIcon(perm.description);
                    const listItem = document.createElement('li');
                    listItem.className = 'permission-item';
                    listItem.innerHTML = `<i class="${iconClass} w-5 text-center mr-3"></i><span>${perm.description}</span>`;
                    list.appendChild(listItem);
                });
            }
        } catch (error) {
            console.error("權限載入失敗:", error);
            list.innerHTML = `<li class="permission-item text-red-500"><i class="fas fa-exclamation-triangle mr-2"></i><span>${error.message}</span></li>`;
        }
    }
    
    // ================================================================
    //           全域函式與事件監聽 (Global Functions & Listeners)
    // ================================================================
    
    /** 將函式掛載到 window，以便 HTML 中的 onclick 可以呼叫 */
    window.showEmployeeDetail = function(employee) {
        const modal = document.getElementById('employee-detail-modal');
        const modalPhoto = document.getElementById('modal-employee-photo');

        const genderDisplay = { 'M': '<i class="fas fa-mars text-blue-500 mr-1"></i>男', 'F': '<i class="fas fa-venus text-pink-500 mr-1"></i>女', 'O': '<i class="fas fa-genderless text-gray-500 mr-1"></i>其他' };
        const statusBadges = { 'ACTIVE': '<span class="status-badge status-active"><i class="fas fa-check-circle mr-1"></i>啟用</span>', 'INACTIVE': '<span class="status-badge status-inactive"><i class="fas fa-times-circle mr-1"></i>停用</span>' };
        const roleInfo = { 'STAFF': { icon: 'fas fa-user', text: '一般員工' }, 'MANAGER': { icon: 'fas fa-user-tie', text: '門市經理' }, 'HEADQUARTERS_ADMIN': { icon: 'fas fa-user-shield', text: '總部管理員' } };

        const formatDateTime = (dtStr) => dtStr ? new Date(dtStr).toLocaleString('zh-TW', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false }).replace(/\//g, '-') : 'N/A';

        document.getElementById('modal-employee-id').innerHTML = `<i class="fas fa-id-card-alt mr-2"></i>${employee.employeeId}`;
        document.getElementById('modal-username').innerHTML = `<i class="fas fa-user mr-2"></i>${employee.username}`;
        document.getElementById('modal-gender').innerHTML = genderDisplay[employee.gender] || `<i class="fas fa-question mr-1"></i>${employee.gender}`;
        document.getElementById('modal-national-id').innerHTML = `<i class="fas fa-passport mr-2"></i>${employee.nationalId}`;
        document.getElementById('modal-create-time').innerHTML = `<i class="far fa-calendar-alt mr-2"></i>${formatDateTime(employee.createTime)}`;
        document.getElementById('modal-email').innerHTML = `<i class="fas fa-envelope mr-2"></i>${employee.email}`;
        document.getElementById('modal-phone').innerHTML = `<i class="fas fa-phone mr-2"></i>${employee.phone}`;
        document.getElementById('modal-account').innerHTML = `<i class="fas fa-user-circle mr-2"></i>${employee.account}`;
        
        // 【修正】密碼欄位固定顯示 "Hide Display"，不顯示實際密碼
        document.getElementById('modal-password').innerHTML = `<i class="fas fa-key mr-2"></i><span class="text-red-600 font-semibold">Hide Display</span>`;
        
        const roleData = roleInfo[employee.role] || { icon: 'fas fa-user', text: employee.role };
        document.getElementById('modal-role').innerHTML = `<span class="role-badge"><i class="${roleData.icon} mr-1"></i>${roleData.text}</span>`;
        document.getElementById('modal-status').innerHTML = statusBadges[employee.status] || `<span class="status-badge">${employee.status}</span>`;
        document.getElementById('modal-store').innerHTML = `<i class="fas fa-store mr-2"></i>${employee.storeName} <span class="text-gray-500">(ID: ${employee.storeId})</span>`;

        modalPhoto.src = employee.photoUrl ? employee.photoUrl : '/images/no_image.png';
        modal.classList.add('show');
    };

    window.previewPhoto = function(photoUrl, username) {
        const modal = document.getElementById('photo-preview-modal');
        const previewImage = document.getElementById('preview-image');
        previewImage.src = photoUrl;
        previewImage.alt = `${username} 的照片`;
        modal.classList.add('show');
    };

    // 搜尋表單的事件監聽
    searchForm.addEventListener('submit', (e) => { e.preventDefault(); filterEmployees(); });
    searchForm.addEventListener('reset', () => { setTimeout(filterEmployees, 0); });

    // 表格的事件代理
    tableBody.addEventListener('click', e => {
        const deleteBtn = e.target.closest('.delete-btn');
        if (deleteBtn) {
            const id = parseInt(deleteBtn.dataset.id);
            const name = deleteBtn.dataset.name;
            handleDelete(id, name);
            return;
        }

        const roleBadge = e.target.closest('.clickable-role');
        if (roleBadge) {
            const role = roleBadge.dataset.role;
            const roleName = roleBadge.dataset.roleName;
            showRolePermissions(role, roleName);
            return;
        }
    });
    
    // 所有 Modal 的關閉事件
    document.querySelectorAll('.modal-backdrop').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.classList.remove('show');
            }
        });
        const closeButton = modal.querySelector('button[id^="close-"]');
        if (closeButton) {
            closeButton.addEventListener('click', () => modal.classList.remove('show'));
        }
    });

    // 刪除確認Modal的取消按鈕事件
    const cancelDeleteBtn = document.getElementById('cancel-delete-btn');
    if (cancelDeleteBtn) {
        cancelDeleteBtn.addEventListener('click', function() {
            const modal = document.getElementById('delete-employee-modal');
            modal.classList.remove('show');
            // 清空確認輸入框
            document.getElementById('delete-confirmation-input').value = '';
        });
    }
    
    document.getElementById('rows-per-page').addEventListener('change', function(e) {
        rowsPerPage = parseInt(e.target.value);
        currentPage = 1;
        renderTable();
        renderPagination();
    });

    // 添加 logo 點擊事件
    const headerLogo = document.querySelector('header img');
    if (headerLogo) {
        headerLogo.style.cursor = 'pointer';  // 添加手型游標
        headerLogo.addEventListener('click', () => {
            window.location.href = '/employee/select_page';
        });
    }

    // 頁面初始化
    initializePage();
});