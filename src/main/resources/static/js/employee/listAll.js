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
            // 注意: 此處假設 DTO 中有名為 photoUrl 的欄位，若無則需調整
            const employeePhotoUrl = emp.photoUrl ? emp.photoUrl : '/images/no_image.png';

            row.innerHTML = `
                <td class="p-3 align-middle">${emp.employeeId}</td>
                <td class="p-3 align-middle">${emp.storeName || emp.storeId}</td>
                <td class="p-3 align-middle">
                    <div class="relative w-12 h-12 mx-auto">
                        <img src="${employeePhotoUrl}" alt="${emp.username}" class="w-full h-full rounded-full object-cover border-2 border-[var(--border-color)] cursor-pointer hover:border-[var(--primary-color)] transition-colors"
                             onclick='showEmployeeDetail(${JSON.stringify(emp).replace(/'/g, "&apos;")})'
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
        if (!confirm(`您確定要刪除員工 "${employeeName}" (ID: ${employeeId}) 嗎？此操作無法復原。`)) return;

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